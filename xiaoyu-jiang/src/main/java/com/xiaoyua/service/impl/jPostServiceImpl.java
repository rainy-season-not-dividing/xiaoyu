package com.xiaoyua.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaoyua.common.constant.PostConstant;
import com.xiaoyua.context.BaseContext;
import com.xiaoyua.dto.post.PostCreateDTO;
import com.xiaoyua.dto.post.PostFileDTO;
import com.xiaoyua.dto.post.PostQueryDTO;
import com.xiaoyua.dto.post.PostUpdateDTO;
import com.xiaoyua.dto.post.TopicSimpleDTO;
import com.xiaoyua.entity.*;
import com.xiaoyua.mapper.*;
import com.xiaoyua.es.yujiPostSearchRepository;
import com.xiaoyua.service.*;
import com.xiaoyua.vo.file.FileSimpleVO;
import com.xiaoyua.vo.post.PostVO;
import com.xiaoyua.vo.common.PageResult;
import com.xiaoyua.vo.search.PostSearchVO;
import com.xiaoyua.vo.topic.TopicSimpleVO;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class jPostServiceImpl implements jPostService {

    @Autowired
    private jPostMapper postMapper;

    @Autowired
    private jPostFileMapper postFileMapper;

    @Resource
    private yujiPostSearchRepository repository;

    // 依赖接口而非实现，降低耦合
    private final jLikeService likeService;
    private final jFavService favService;
    private final jMessageService messageService;
    private final jUserMapper userMapper;
    private final jPostStatMapper postStatMapper;
    private final jTopicPostMapper topicPostMapper;
    private final jTopicMapper topicMapper;
    private final jPostFileServiceImpl postFileService;
    private final jFileService fileService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public PostVO createPost(PostCreateDTO postCreateDTO, Long userId) {
        // 基础校验
        System.out.println(postCreateDTO.toString());
        if (postCreateDTO == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        if (postCreateDTO.getContent() == null || postCreateDTO.getContent().isBlank()) {
            throw new IllegalArgumentException("动态内容不能为空");
        }

        // 构建并保存 PostPO
        PostPO post = new PostPO();
        post.setUserId(userId);
        post.setTitle(postCreateDTO.getTitle());
        post.setContent(postCreateDTO.getContent());
        post.setCampusId(postCreateDTO.getCampusId());
        if (postCreateDTO.getVisibility() != null) {
            System.out.println(postCreateDTO.getVisibility());
            post.setVisibility(PostPO.Visibility.valueOf(postCreateDTO.getVisibility()));
        } else {
            post.setVisibility(PostPO.Visibility.PUBLIC);
        }
        post.setPoiLat(postCreateDTO.getPoiLat());
        post.setPoiLng(postCreateDTO.getPoiLng());
        post.setPoiName(postCreateDTO.getPoiName());
        post.setIsTop(0);
        post.setStatus(PostPO.Status.PUBLISHED);
        post.setCreatedAt(LocalDateTime.now());
        post.setUpdatedAt(LocalDateTime.now());

        postMapper.insert(post);

        // 将新增的post写进es
        PostSearchVO searchVO = new PostSearchVO();
        searchVO.setId(post.getId());
        searchVO.setTitle(post.getTitle());
        searchVO.setContent(post.getContent());
        searchVO.setCreatedAt(post.getCreatedAt());
        UserPO user = userMapper.selectById(post.getUserId());
        searchVO.setUser(new PostSearchVO.UserVO(user.getId(), user.getNickname(), user.getAvatarUrl()));
        // 写入es
        repository.save(searchVO);

        // 初始化统计记录
        postStatMapper.initIfAbsent(post.getId());

        // 关联文件（如果有）
        if (postCreateDTO.getFileIds() != null && !postCreateDTO.getFileIds().isEmpty()) {
            List<PostFilePO> postFileList = new ArrayList<>();
            int sort = 0;
            for (Long fileId : postCreateDTO.getFileIds()) {
                PostFilePO rel = new PostFilePO();
                rel.setPostId(post.getId());
                rel.setFileId(fileId);
                rel.setSort(sort++);
                postFileList.add(rel);
            }
            postFileService.saveBatch(postFileList);
        }
        if (postCreateDTO.getTopicIds() != null && !postCreateDTO.getTopicIds().isEmpty()) {
            List<TopicPostPO> topicPostList = postCreateDTO.getTopicIds().stream()
                    .map(topicId -> TopicPostPO.builder()
                            .postId(post.getId())
                            .topicId(topicId)
                            .build())
                    .collect(Collectors.toList());
            topicPostMapper.saveBatch(topicPostList);

            // 增加话题的动态数量
            try {
                topicMapper.batchUpdatePostCount(postCreateDTO.getTopicIds(), 1);
            } catch (Exception e) {
                log.error("更新话题动态数量失败, postId: {}, topicIds: {}", post.getId(), postCreateDTO.getTopicIds(), e);
            }
        }
        // 返回详情或概要，这里复用现有转换
        return getPostDetail(post.getId());
    }

    @Override
    public PageResult<PostVO> listAll(Integer pageNum, Integer pageSize, String sort) {
        if (pageNum == null || pageNum < 1)
            pageNum = 1;
        if (pageSize == null || pageSize < 1)
            pageSize = 20;

        Long currentUserId = BaseContext.getCurrentId();

        // 使用优化的联表查询替代原有的N+1查询
        Page<PostVO> page = new Page<>(pageNum, pageSize);
        IPage<PostVO> postVOPage = postMapper.selectPostsWithDetails(page, currentUserId);

        // 批量处理文件和话题等关联数据，提升性能
        List<PostVO> vos = postVOPage.getRecords();
        batchFillPostRelatedData(vos);

//        List<PostSearchVO> searchVOs = vos.stream().map(
//                postVO -> {
//                    PostSearchVO searchVO = new PostSearchVO();
//                    searchVO.setId(postVO.getId());
//                    searchVO.setTitle(postVO.getTitle());
//                    searchVO.setContent(postVO.getContent());
//                    searchVO.setCreatedAt(postVO.getCreatedAt());
//                    searchVO.setUser(new PostSearchVO.UserVO(postVO.getUser().getId(),
//                            postVO.getUser().getNickname(), postVO.getUser().getAvatarUrl()));
//                    return searchVO;
//                }
//        ).toList();
//        repository.saveAll(searchVOs);

        return PageResult.of(vos, pageNum, pageSize, postVOPage.getTotal());
    }

    @Override
    public PageResult<PostVO> listByUser(Long userId, Integer pageNum, Integer pageSize, String sort) {
        if (userId == null || userId <= 0)
            throw new IllegalArgumentException("userId 无效");
        if (pageNum == null || pageNum < 1)
            pageNum = 1;
        if (pageSize == null || pageSize < 1)
            pageSize = 20;

        QueryWrapper<PostPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", "PUBLISHED");
        queryWrapper.eq("user_id", userId);

        Long viewerId = BaseContext.getCurrentId();
        if (viewerId == null) {
            queryWrapper.eq("visibility", "PUBLIC");
        } else if (!userId.equals(viewerId)) {
            boolean isFriend = false;
            boolean sameCampus = false;
            try {
                isFriend = messageService.isFriend(userId, viewerId);
            } catch (Exception ignored) {
            }
            try {
                var owner = userMapper.selectById(userId);
                var viewer = userMapper.selectById(viewerId);
                sameCampus = owner != null && viewer != null && owner.getCampusId() != null
                        && owner.getCampusId().equals(viewer.getCampusId());
            } catch (Exception ignored) {
            }

            // 仅允许可见范围
            if (isFriend && sameCampus) {
                queryWrapper.and(w -> w.eq("visibility", "PUBLIC").or().eq("visibility", "FRIEND").or().eq("visibility",
                        "CAMPUS"));
            } else if (isFriend) {
                queryWrapper.and(w -> w.eq("visibility", "PUBLIC").or().eq("visibility", "FRIEND"));
            } else if (sameCampus) {
                queryWrapper.and(w -> w.eq("visibility", "PUBLIC").or().eq("visibility", "CAMPUS"));
            } else {
                queryWrapper.eq("visibility", "PUBLIC");
            }
        }

        if ("hot".equalsIgnoreCase(sort)) {
            queryWrapper.orderByDesc("like_cnt", "comment_cnt", "created_at");
        } else {
            queryWrapper.orderByDesc("is_top", "created_at");
        }

        // 使用优化的联表查询替代N+1查询
        Page<PostVO> page = new Page<>(pageNum, pageSize);
        IPage<PostVO> postVOPage = postMapper.selectPostsWithDetails(page, viewerId);

        // 批量处理文件和话题等关联数据
        List<PostVO> vos = postVOPage.getRecords();
        batchFillPostRelatedData(vos);
        return PageResult.of(vos, pageNum, pageSize, postVOPage.getTotal());
    }

    @Override
    public List<PostVO> listHot(Integer limit) {
        int topN = (limit == null || limit <= 0) ? 10 : limit;

        // 获取当前用户ID
        Long currentUserId = null;
        try {
            currentUserId = BaseContext.getCurrentId();
        } catch (Exception ignored) {
        }

        // 先查询热门动态ID列表
        java.util.List<PostPO> list = postMapper.selectHotPosts(topN);
        List<Long> postIds = list.stream().map(PostPO::getId).collect(Collectors.toList());

        if (postIds.isEmpty()) {
            return new ArrayList<>();
        }

        // 使用联表查询获取完整数据
        List<PostVO> vos = postMapper.selectPostsWithDetailsByIds(postIds, currentUserId);

        // 批量处理文件和话题等关联数据
        batchFillPostRelatedData(vos);

        return vos;
    }

    @Override
    public PageResult<PostVO> getPosts(PostQueryDTO postQueryDTO) {
        // 获取当前用户ID
        Long currentUserId = null;
        try {
            currentUserId = BaseContext.getCurrentId();
        } catch (Exception ignored) {
        }

        // 使用优化的联表查询替代N+1查询
        Page<PostVO> voPage = new Page<>(postQueryDTO.getPage(), postQueryDTO.getSize());
        IPage<PostVO> postVOPage = postMapper.selectPostsWithDetails(voPage, currentUserId);

        // 批量处理文件和话题等关联数据
        List<PostVO> postVOList = postVOPage.getRecords();
        batchFillPostRelatedData(postVOList);

        // 返回分页结果
        return PageResult.of(postVOList, postQueryDTO.getPage(), postQueryDTO.getSize(), postVOPage.getTotal());
    }

    @Override
    public PostVO getPostDetail(Long postId) {
        // 根据ID查询动态
        PostPO postPO = postMapper.selectById(postId);

        // 检查动态是否存在
        if (postPO == null) {
            throw new RuntimeException("动态不存在");
        }

        // 检查动态状态
        if (!"PUBLISHED".equals(postPO.getStatus().name())) {
            throw new RuntimeException("动态不可访问");
        }

        // 权限检查
        Long currentUserId = BaseContext.getCurrentId();
        if (!hasViewPermission(postPO, currentUserId)) {
            throw new RuntimeException("无权限查看该动态");
        }

        // 增加浏览量
        try {
            postStatMapper.incView(postId);
        } catch (Exception ignored) {
        }

        // 使用联表查询获取完整数据（包含最新统计）
        List<PostVO> vos = postMapper.selectPostsWithDetailsByIds(Arrays.asList(postId), currentUserId);
        if (vos.isEmpty()) {
            throw new RuntimeException("动态数据获取失败");
        }

        PostVO postVO = vos.get(0);
        // 批量处理文件和话题等关联数据（单个动态也使用批量方法）
        batchFillPostRelatedData(Arrays.asList(postVO));

        return postVO;
    }

    @Override
    public void deletePost(Long postId) {
        PostPO postPO = postMapper.selectById(postId);
        if (postPO == null) {
            throw new RuntimeException("动态不存在");
        }

        // 查询关联的话题ID列表，用于减少话题动态计数
        List<TopicPostPO> topicPosts = topicPostMapper.selectList(
                new LambdaQueryWrapper<TopicPostPO>().eq(TopicPostPO::getPostId, postId));
        List<Long> topicIds = topicPosts.stream()
                .map(TopicPostPO::getTopicId)
                .collect(Collectors.toList());

        // if (!"PUBLISHED".equals(postPO.getStatus().name())) {
        // throw new RuntimeException("动态不可删除");
        // }
        postMapper.deleteById(postId);
        topicPostMapper.delete(new QueryWrapper<TopicPostPO>().eq("post_id", postId));
        postFileMapper.delete(new QueryWrapper<PostFilePO>().eq("post_id", postId));

        // 减少话题的动态数量
        if (!topicIds.isEmpty()) {
            try {
                topicMapper.batchUpdatePostCount(topicIds, -1);
            } catch (Exception e) {
                log.error("减少话题动态数量失败, postId: {}, topicIds: {}", postId, topicIds, e);
            }
        }

        Long userId = BaseContext.getCurrentId();
        likeService.deleteLike(postId, userId, "POST");
        favService.deleteFavorite(postId, userId);

        // 删除redis缓存
        redisTemplate.delete(PostConstant.POST_DETAIL_KEY_PREFIX + postId);
        // 删除es中对应的记录
        repository.deleteById(postId);
        return;
    }

    /**
     * 检查用户是否有权限查看动态
     */
    private boolean hasViewPermission(PostPO postPO, Long currentUserId) {
        String visibility = postPO.getVisibility().name();

        // 公开动态，所有人可见
        if ("PUBLIC".equals(visibility)) {
            return true;
        }
        // 未登录用户只能看公开动态
        if (currentUserId == null) {
            return false;
        }
        // 自己的动态总是可见
        if (postPO.getUserId().equals(currentUserId)) {
            return true;
        }
        // 好友可见：需要检查好友关系
        if ("FRIEND".equals(visibility)) {
            try {
                return messageService.isFriend(postPO.getUserId(), currentUserId);
            } catch (Exception e) {
                return false;
            }
        }
        // 校园可见：需要检查是否同校园
        if ("CAMPUS".equals(visibility)) {
            try {
                UserPO owner = userMapper.selectById(postPO.getUserId());
                UserPO viewer = userMapper.selectById(currentUserId);
                if (owner == null || viewer == null)
                    return false;
                return owner.getCampusId() != null && owner.getCampusId().equals(viewer.getCampusId());
            } catch (Exception e) {
                return false;
            }
        }
        return false;
    }

    /**
     * 更新动态
     *
     * @param postUpdateDTO
     * @param postId
     */
    public void updatePost(PostUpdateDTO postUpdateDTO, Long postId) {
        PostPO postPO = postMapper.selectById(postId);
        if (postPO == null) {
            return;
        }

        // 查询原有的话题ID列表，用于后续更新话题计数
        List<TopicPostPO> oldTopicPosts = topicPostMapper.selectList(
                new LambdaQueryWrapper<TopicPostPO>().eq(TopicPostPO::getPostId, postId));
        List<Long> oldTopicIds = oldTopicPosts.stream()
                .map(TopicPostPO::getTopicId)
                .collect(Collectors.toList());

        if (postUpdateDTO.getTitle() != null) {
            postPO.setTitle(postUpdateDTO.getTitle());
        }
        if (postUpdateDTO.getContent() != null) {
            postPO.setContent(postUpdateDTO.getContent());
        }
        if (postUpdateDTO.getCampusId() != null) {
            postPO.setCampusId(postUpdateDTO.getCampusId());
        }
        if (postUpdateDTO.getVisibility() != null) {
            postPO.setVisibility(PostPO.Visibility.valueOf(postUpdateDTO.getVisibility()));
        }
        if (postUpdateDTO.getIsTop() != null) {
            postPO.setIsTop(postUpdateDTO.getIsTop());
        }
        postMapper.updateById(postPO);

        // 删除文件
        // postFileMapper根据taskId批量删除
        postFileMapper.delete(new LambdaQueryWrapper<PostFilePO>().eq(PostFilePO::getPostId, postId));
        topicPostMapper.delete(new LambdaQueryWrapper<TopicPostPO>().eq(TopicPostPO::getPostId, postId));

        // 减少原有话题的动态数量
        if (!oldTopicIds.isEmpty()) {
            try {
                topicMapper.batchUpdatePostCount(oldTopicIds, -1);
            } catch (Exception e) {
                log.error("减少原有话题动态数量失败, postId: {}, oldTopicIds: {}", postId, oldTopicIds, e);
            }
        }

        // 更新文件

        // filemapper 批量添加
        List<FilePO> fileList = postUpdateDTO.getFiles().stream().map(
                fileUrl -> FilePO.builder().userId(postId).fileUrl(fileUrl).build()).toList();
        // 插入文件表
        fileService.saveBatch(fileList);
        List<Long> fileIds = fileList.stream().map(FilePO::getId).toList();
        List<PostFilePO> taskFilesPOList = fileIds.stream()
                .map(fileId -> PostFilePO.builder().postId(postId).fileId(fileId).build())
                .toList();
        // 插入文件-动态表
        postFileService.saveBatch(taskFilesPOList);

        // 更新话题关联
        if (postUpdateDTO.getTopicIds() != null && !postUpdateDTO.getTopicIds().isEmpty()) {
            List<TopicPostPO> topicPostPOList = postUpdateDTO.getTopicIds().stream()
                    .map(topicId -> TopicPostPO.builder()
                            .postId(postId)
                            .topicId(topicId)
                            .build())
                    .collect(Collectors.toList());
            topicPostMapper.saveBatch(topicPostPOList);

            // 增加新话题的动态数量
            try {
                topicMapper.batchUpdatePostCount(postUpdateDTO.getTopicIds(), 1);
            } catch (Exception e) {
                log.error("增加新话题动态数量失败, postId: {}, newTopicIds: {}", postId, postUpdateDTO.getTopicIds(), e);
            }
        }

        // 更新后写入es
        PostSearchVO postSearchVO = new PostSearchVO();
        postSearchVO.setId(postPO.getId());
        postSearchVO.setTitle(postPO.getTitle());
        postSearchVO.setContent(postPO.getContent());
        postSearchVO.setCreatedAt(postPO.getCreatedAt());
        UserPO userPO = userMapper.selectById(postPO.getUserId());
        postSearchVO.setUser(new PostSearchVO.UserVO(userPO.getId(), userPO.getNickname(), userPO.getAvatarUrl()));
        repository.save(postSearchVO);

        // todo: 删除缓存
        redisTemplate.delete(PostConstant.POST_DETAIL_KEY_PREFIX + postId);
    }

    /** 搜索：只要 title 或 content 完全等于 keyword */
    public PageResult<PostSearchVO> searchExact(String keyword, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page - 1, size,
                Sort.by(Sort.Order.desc("createdAt"))); // 也可以按时间倒序
        org.springframework.data.domain.Page<PostSearchVO> pageRes = repository
                .findByTitleContainingOrContentContaining(keyword, keyword, pageable);
        return new PageResult<>(pageRes.getContent(), page, size, pageRes.getTotalElements());
    }

    @Override
    public PageResult<PostVO> listLike(Integer pageNum, Integer pageSize, String sort) {
        if (pageNum == null || pageNum < 1)
            pageNum = 1;
        if (pageSize == null || pageSize < 1)
            pageSize = 20;

        Long currentUserId = BaseContext.getCurrentId();
        if (currentUserId == null) {
            throw new RuntimeException("用户未登录");
        }

        int offset = (pageNum - 1) * pageSize;

        // 查询用户点赞的动态列表
        List<PostPO> posts = postMapper.selectLikedPostsByUser(currentUserId, offset, pageSize, sort);
        List<Long> postIds = posts.stream().map(PostPO::getId).collect(Collectors.toList());

        List<PostVO> vos = new ArrayList<>();
        if (!postIds.isEmpty()) {
            // 使用联表查询获取完整数据
            vos = postMapper.selectPostsWithDetailsByIds(postIds, currentUserId);

            // 批量处理文件和话题等关联数据
            batchFillPostRelatedData(vos);
        }

        // 查询总数
        long total = postMapper.countLikedPostsByUser(currentUserId);

        return PageResult.of(vos, pageNum, pageSize, total);
    }

    @Override
    public PageResult<PostVO> listFavorite(Integer pageNum, Integer pageSize, String sort) {
        if (pageNum == null || pageNum < 1)
            pageNum = 1;
        if (pageSize == null || pageSize < 1)
            pageSize = 20;

        Long currentUserId = BaseContext.getCurrentId();
        if (currentUserId == null) {
            throw new RuntimeException("用户未登录");
        }

        int offset = (pageNum - 1) * pageSize;

        // 查询用户收藏的动态列表
        List<PostPO> posts = postMapper.selectFavoritedPostsByUser(currentUserId, offset, pageSize, sort);
        List<Long> postIds = posts.stream().map(PostPO::getId).collect(Collectors.toList());

        List<PostVO> vos = new ArrayList<>();
        if (!postIds.isEmpty()) {
            // 使用联表查询获取完整数据
            vos = postMapper.selectPostsWithDetailsByIds(postIds, currentUserId);

            // 批量处理文件和话题等关联数据
            batchFillPostRelatedData(vos);
        }

        // 查询总数
        long total = postMapper.countFavoritedPostsByUser(currentUserId);

        return PageResult.of(vos, pageNum, pageSize, total);
    }

    /**
     * 填充动态的关联数据（文件和话题）
     * 这些数据相对较少，保持原有的查询逻辑
     */
    private void fillPostRelatedData(PostVO postVO) {
//        try {
//            // 填充文件列表
//            QueryWrapper<PostFilePO> pfw = new QueryWrapper<>();
//            pfw.eq("post_id", postVO.getId()).orderByAsc("sort");
//            List<PostFilePO> relations = postFileMapper.selectList(pfw);
//            if (relations != null && !relations.isEmpty()) {
//                List<Long> fileIds = relations.stream().map(PostFilePO::getFileId).collect(Collectors.toList());
//                if (!fileIds.isEmpty()) {
//                    List<FilePO> files = fileMapper.selectBatchIds(fileIds);
//                    // 保持与关系表的顺序一致
//                    Map<Long, FilePO> fileMap = files.stream().filter(Objects::nonNull)
//                            .collect(Collectors.toMap(FilePO::getId, f -> f));
//                    List<FileSimpleVO> fileVOs = relations.stream()
//                            .map(rel -> fileMap.get(rel.getFileId()))
//                            .filter(Objects::nonNull)
//                            .map(this::convertToFileSimpleVO)
//                            .collect(Collectors.toList());
//                    postVO.setFiles(fileVOs);
//                } else {
//                    postVO.setFiles(new ArrayList<>());
//                }
//            } else {
//                postVO.setFiles(new ArrayList<>());
//            }
//        } catch (Exception ignored) {
//        }
//
//        try {
//            // 填充话题列表
//            QueryWrapper<TopicPostPO> tpw = new QueryWrapper<>();
//            tpw.eq("post_id", postVO.getId());
//            List<TopicPostPO> topicRels = topicPostMapper.selectList(tpw);
//            if (topicRels != null && !topicRels.isEmpty()) {
//                List<Long> topicIds = topicRels.stream().map(TopicPostPO::getTopicId).collect(Collectors.toList());
//                if (!topicIds.isEmpty()) {
//                    List<TopicPO> topics = topicMapper.selectBatchIds(topicIds);
//                    List<TopicSimpleVO> topicVOs = topics.stream()
//                            .filter(Objects::nonNull)
//                            .map(this::convertToTopicSimpleVO)
//                            .collect(Collectors.toList());
//                    postVO.setTopics(topicVOs);
//                } else {
//                    postVO.setTopics(new ArrayList<>());
//                }
//            } else {
//                postVO.setTopics(new ArrayList<>());
//            }
//        } catch (Exception ignored) {
//        }
    }

    /**
     * 批量获取动态文件映射
     * @param postIds 动态ID列表
     * @return postId -> 有序文件列表的映射
     */
    private Map<Long, List<FileSimpleVO>> batchGetFileMap(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Map.of();
        }

        // 1. 批量查询文件信息（已按 sort 排序）
        List<PostFileDTO> dtoList = postFileMapper.selectFileByPostIds(postIds);

        // 2. 按 postId 分组并保持顺序
        return dtoList.stream()
                .collect(Collectors.groupingBy(PostFileDTO::getPostId,
                        LinkedHashMap::new,
                        Collectors.mapping(this::convertToFileSimpleVO, Collectors.toList())));
    }

    /**
     * 批量获取动态话题映射
     * @param postIds 动态ID列表
     * @return postId -> 话题列表的映射
     */
    private Map<Long, List<TopicSimpleVO>> batchGetTopicMap(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Map.of();
        }

        // 批量查询话题信息
        List<TopicSimpleDTO> dtoList = topicPostMapper.selectTopicByPostIds(postIds);

        // 按 postId 分组
        return dtoList.stream()
                .collect(Collectors.groupingBy(TopicSimpleDTO::getPostId,
                        Collectors.mapping(this::convertToTopicSimpleVO, Collectors.toList())));
    }

    /**
     * PostFileDTO -> FileSimpleVO 转换
     */
    private FileSimpleVO convertToFileSimpleVO(PostFileDTO dto) {
        if (dto == null) return null;

        FileSimpleVO vo = new FileSimpleVO();
        vo.setId(dto.getId());
        vo.setFileUrl(dto.getFileUrl());
        vo.setThumbnailUrl(dto.getThumbUrl());
        vo.setFileSize(dto.getSize() == null ? 0L : dto.getSize());

        // 基于bizType推断文件类型
        String type = "DOCUMENT";
        if (dto.getBizType() != null) {
            switch (dto.getBizType()) {
                case "AVATAR":
                case "BG":
                case "POST":
                    type = "IMAGE";
                    break;
                case "TASK":
                case "COMMENT":
                    type = "DOCUMENT";
                    break;
            }
        }
        vo.setFileType(type);
        return vo;
    }

    /**
     * TopicSimpleDTO -> TopicSimpleVO 转换
     */
    private TopicSimpleVO convertToTopicSimpleVO(TopicSimpleDTO dto) {
        if (dto == null) return null;

        TopicSimpleVO vo = new TopicSimpleVO();
        vo.setId(dto.getId());
        vo.setName(dto.getName());
        vo.setDescription(dto.getDescription());
        vo.setPostCount(dto.getPostCnt());
        return vo;
    }

    /**
     * 批量填充动态的关联数据（文件和话题）
     * 替代原有的单个查询方式，提升性能
     */
    private void batchFillPostRelatedData(List<PostVO> vos) {
        if (vos == null || vos.isEmpty()) {
            return;
        }

        try {
            // 1. 拿到当前页所有 postId
            List<Long> postIds = vos.stream().map(PostVO::getId).collect(Collectors.toList());

            // 2. 一次性查文件、话题
            Map<Long, List<FileSimpleVO>> fileMap = batchGetFileMap(postIds);
            Map<Long, List<TopicSimpleVO>> topicMap = batchGetTopicMap(postIds);

            // 3. 内存组装
            vos.forEach(vo -> {
                vo.setFiles(fileMap.getOrDefault(vo.getId(), List.of()));
                vo.setTopics(topicMap.getOrDefault(vo.getId(), List.of()));
            });
        } catch (Exception e) {
            log.error("批量填充动态关联数据失败", e);
            vos.forEach(this::fillPostRelatedData);
        }
    }
}
