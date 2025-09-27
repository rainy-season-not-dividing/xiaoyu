package com.xiaoyua.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaoyua.context.BaseContext;
import com.xiaoyua.dto.comment.CommentCreateDTO;
import com.xiaoyua.entity.CommentPO;
import com.xiaoyua.entity.NotificationPO;
import com.xiaoyua.entity.PostPO;
import com.xiaoyua.entity.UserPO;
import com.xiaoyua.mapper.jCommentMapper;
import com.xiaoyua.mapper.jPostMapper;
import com.xiaoyua.mapper.jPostStatMapper;
import com.xiaoyua.mapper.jUserMapper;
import com.xiaoyua.service.jCommentService;
import com.xiaoyua.service.jPushService;
import com.xiaoyua.vo.comment.CommentVO;
import com.xiaoyua.vo.user.UserSimpleVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class jCommentServiceImpl implements jCommentService {
    @Autowired
    jCommentMapper jCommentMapper;
    @Autowired
    jUserMapper jUserMapper;
    @Autowired
    private jPostMapper jPostMapper;
    @Autowired
    private jPushService jPushService;
    @Autowired
    private jPostStatMapper jPostStatMapper;

    public final class JacksonUtil {
        private static final ObjectMapper MAPPER = new ObjectMapper();
        public static String toJson(Object obj) {
            try {
                return obj == null ? null : MAPPER.writeValueAsString(obj);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("序列化失败", e);
            }
        }
    }
    @Override
    public CommentVO addComment(CommentCreateDTO comment) {
        CommentPO commentPO=new CommentPO();
        commentPO.setUserId(BaseContext.getCurrentId());
        commentPO.setContent(comment.getContent());
        commentPO.setStatus(CommentPO.Status.VISIBLE);
        commentPO.setItemType(CommentPO.ItemType.POST);
        commentPO.setAtUsers(
                comment.getAtUsers() == null || comment.getAtUsers().isEmpty()
                        ? null                                  // 数据库接受 NULL
                        : JacksonUtil.toJson(comment.getAtUsers()) // 得到 "[{"id":1,"nickname":"xxx"},...]"
        );
        commentPO.setItemId(comment.getPostId());
        commentPO.setParentId(comment.getParentId());
        commentPO.setCreatedAt(LocalDateTime.now());
        jCommentMapper.insert(commentPO);
        // 更新统计：评论+1（仅对动态）
        try { if (commentPO.getItemType() == CommentPO.ItemType.POST) jPostStatMapper.incComment(commentPO.getItemId()); } catch (Exception ignored) {}

        // 创建评论通知
        createCommentNotification(commentPO);

        // 创建@用户通知
        createAtUserNotifications(commentPO, comment.getAtUsers());

        // 构建并返回CommentVO
        return buildCommentVO(commentPO, comment.getAtUsers());
    }

    public void deleteComment(Long commentId) {
        // 先查询获取 itemId 和 itemType
        CommentPO existed = jCommentMapper.selectById(commentId);
        if (existed == null) {
            return;
        }
        jCommentMapper.deleteById(commentId);
        // 更新统计：评论-1（仅对动态）
        try { if (existed.getItemType() == CommentPO.ItemType.POST) jPostStatMapper.decComment(existed.getItemId()); } catch (Exception ignored) {}

    }

    /**
     * 查询一篇文章的全部评论（含二级回复）
     */
    public IPage<CommentVO> getComments(Long postId, int page, int size, String sort) {
        log.info("获取评论列表: postId={}, page={}, size={}, sort={}", postId, page, size, sort);

        // 获取当前用户ID
        Long currentUserId = null;
        try {
            currentUserId = BaseContext.getCurrentId();
        } catch (Exception ignored) {}

        // 使用联表查询，一次SQL获取一级评论的所有数据
        Page<CommentVO> pageParam = new Page<>(page, size);
        // 先用原始查询验证是否有数据
        long totalComments = jCommentMapper.selectCount(
                new QueryWrapper<CommentPO>()
                        .eq("item_id", postId)
                        .eq("item_type", CommentPO.ItemType.POST)
                        .eq("parent_id", 0)
        );
        log.info("数据库中该动态的一级评论总数: {}", totalComments);

        // 再检查状态为VISIBLE的评论数量
        long visibleComments = jCommentMapper.selectCount(
                new QueryWrapper<CommentPO>()
                        .eq("item_id", postId)
                        .eq("item_type", CommentPO.ItemType.POST)
                        .eq("parent_id", 0)
                        .eq("status", CommentPO.Status.VISIBLE)
        );
        log.info("数据库中该动态状态为VISIBLE的一级评论数: {}", visibleComments);

        log.info("开始查询评论: postId={}, currentUserId={}", postId, currentUserId);
        IPage<CommentVO> rootCommentsPage = jCommentMapper.selectCommentsWithDetails(pageParam, postId, sort, currentUserId);
        log.info("查询到一级评论数量: {}", rootCommentsPage.getRecords().size());

        if (rootCommentsPage.getRecords().isEmpty()) {
            log.warn("未查询到任何评论数据: postId={}", postId);
            return rootCommentsPage;
        }

        // 获取一级评论ID列表
        List<Long> rootIds = rootCommentsPage.getRecords().stream()
                .map(CommentVO::getId)
                .collect(Collectors.toList());

        // 批量查询二级评论
        List<CommentVO> subComments = jCommentMapper.selectSubCommentsWithDetails(rootIds, currentUserId);

        // 按父评论ID分组二级评论
        Map<Long, List<CommentVO>> subCommentsMap = subComments.stream()
                .collect(Collectors.groupingBy(CommentVO::getParentId));

        // 处理@用户信息并设置二级评论
        for (CommentVO rootComment : rootCommentsPage.getRecords()) {
            // 解析@用户信息
            rootComment.setAtUsers(parseAtUsers(rootComment.getAtUsersJson()));

            // 设置二级评论
            List<CommentVO> replies = subCommentsMap.getOrDefault(rootComment.getId(), Collections.emptyList());
            // 为二级评论也解析@用户信息
            for (CommentVO reply : replies) {
                reply.setAtUsers(parseAtUsers(reply.getAtUsersJson()));
            }
            rootComment.setReplies(replies);
            rootComment.setReplyCount(replies.size());
        }

        log.info("获取评论列表完成: postId={}, total={}, pages={}", postId, rootCommentsPage.getTotal(), rootCommentsPage.getPages());
        return rootCommentsPage;
    }

    /*工具方*/

    private List<UserSimpleVO> parseAtUsers(String json) {
        if (StrUtil.isBlank(json)) return Collections.emptyList();
        try {
            // 兼容两种格式：
            // 1) JSON 数组（[1,2,3] 或 [{...UserSimpleVO}...]）
            if (StrUtil.startWith(json.trim(), "[")) {
                try {
                    // 优先解析为 Long 列表（ID 列表）
                    List<Long> ids = JSONUtil.toList(JSONUtil.parseArray(json), Long.class);
                    return buildUsersByIds(ids);
                } catch (Exception ignore) {
                    // 如果不是 ID 列表，则尝试直接解析为 UserSimpleVO 列表
                    return JSONUtil.toList(JSONUtil.parseArray(json), UserSimpleVO.class);
                }
            }
            // 2) 逗号分隔的 ID 字符串："1,2,3"
            List<Long> ids = Arrays.stream(json.split(","))
                    .map(String::trim)
                    .filter(StrUtil::isNotBlank)
                    .map(Long::valueOf)
                    .collect(Collectors.toList());
            return buildUsersByIds(ids);
        } catch (Exception e) {
            log.warn("解析@用户列表失败，原始值: {}，错误: {}", json, e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 根据用户ID集合构建 UserSimpleVO 列表
     */
    private List<UserSimpleVO> buildUsersByIds(List<Long> ids) {
        if (CollUtil.isEmpty(ids)) return Collections.emptyList();
        try {
            List<UserPO> users = jUserMapper.selectBatchIds(ids);
            if (CollUtil.isEmpty(users)) return Collections.emptyList();
            return users.stream().map(u -> new UserSimpleVO(
                    u.getId(), u.getNickname(), u.getAvatarUrl(), u.getGender(), u.getCampusId(),
                    u.getIsRealName(), u.getCreatedAt()
            )).collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("批量查询@用户信息失败, ids={}, error={}", ids, e.getMessage());
            return Collections.emptyList();
        }
    }


    /**
     * 创建评论通知
     */
    private void createCommentNotification(CommentPO commentPO) {
        try {
            Long fromUserId = commentPO.getUserId();
            Long postId = commentPO.getItemId();

            // 获取动态作者ID
            PostPO post = jPostMapper.selectById(postId);
            if (post == null) {
                return;
            }

            Long toUserId = post.getUserId();

            // 不给自己发通知
            if (toUserId.equals(fromUserId)) {
                return;
            }

            // 获取评论用户信息
            UserPO fromUser = jUserMapper.selectById(fromUserId);
            if (fromUser == null) {
                return;
            }

            // 如果是回复评论，需要特殊处理
            if (commentPO.getParentId() != null && commentPO.getParentId() > 0) {
                // 获取被回复的评论
                CommentPO parentComment = jCommentMapper.selectById(commentPO.getParentId());
                if (parentComment != null) {
                    Long parentUserId = parentComment.getUserId();
                    // 如果回复的不是动态作者，则通知被回复的用户
                    if (!parentUserId.equals(toUserId) && !parentUserId.equals(fromUserId)) {
                        createReplyNotification(commentPO, parentComment, fromUser);
                    }
                }
            }

            // 使用PushService发送通知
            jPushService.pushCommentNotification(
                    toUserId,
                    fromUserId,
                    postId,
                    NotificationPO.RefType.POST.name(),
                    commentPO.getContent()
            );

        } catch (Exception e) {
            log.error("创建评论通知失败: commentId={}, error={}",
                    commentPO.getId(), e.getMessage(), e);
        }
    }

    /**
     * 创建回复通知
     */
    private void createReplyNotification(CommentPO replyComment, CommentPO parentComment, UserPO fromUser) {
        try {
            Long toUserId = parentComment.getUserId();

            // pushCommentNotification会自动构建通知内容

            // 使用PushService发送通知
            jPushService.pushCommentNotification(
                    toUserId,
                    fromUser.getId(),
                    replyComment.getItemId(),
                    NotificationPO.RefType.POST.name(),
                    replyComment.getContent()
            );

        } catch (Exception e) {
            log.error("创建回复通知失败: replyCommentId={}, parentCommentId={}, error={}",
                    replyComment.getId(), parentComment.getId(), e.getMessage(), e);
        }
    }

    /**
     * 创建@用户通知
     */
    private void createAtUserNotifications(CommentPO commentPO, List<Long> atUserIds) {
        if (atUserIds == null || atUserIds.isEmpty()) {
            return;
        }

        try {
            Long fromUserId = commentPO.getUserId();

            for (Long toUserId : atUserIds) {
                // 不给自己发通知
                if (toUserId.equals(fromUserId)) {
                    continue;
                }

                // 使用PushService发送@用户通知
                jPushService.pushAtUserNotification(
                        toUserId,
                        fromUserId,
                        commentPO.getItemId(),
                        NotificationPO.RefType.POST.name(),
                        commentPO.getContent()
                );
            }

        } catch (Exception e) {
            log.error("创建@用户通知失败: commentId={}, atUserIds={}, error={}",
                    commentPO.getId(), atUserIds, e.getMessage(), e);
        }
    }

    /**
     * 构建CommentVO对象
     */
    private CommentVO buildCommentVO(CommentPO commentPO, List<Long> atUserIds) {
        try {
            CommentVO commentVO = new CommentVO();
            commentVO.setId(commentPO.getId());
            commentVO.setContent(commentPO.getContent());
            commentVO.setParentId(commentPO.getParentId());
            commentVO.setStatus(commentPO.getStatus().name());
            commentVO.setCreatedAt(commentPO.getCreatedAt());

            // 获取用户信息
            UserPO user = jUserMapper.selectById(commentPO.getUserId());
            if (user != null) {
                UserSimpleVO userVO = new UserSimpleVO(
                        user.getId(),
                        user.getNickname(),
                        user.getAvatarUrl(),
                        user.getGender(),
                        user.getCampusId(),
                        user.getIsRealName(),
                        user.getCreatedAt()
                );
                commentVO.setUser(userVO);
            }

            // 设置@用户信息
            if (atUserIds != null && !atUserIds.isEmpty()) {
                List<UserSimpleVO> atUsers = buildUsersByIds(atUserIds);
                commentVO.setAtUsers(atUsers);
            } else {
                commentVO.setAtUsers(Collections.emptyList());
            }

            // 设置默认值
            commentVO.setLikeCnt(0);
            commentVO.setIsLiked(false);
            commentVO.setReplyCount(0);
            commentVO.setReplies(Collections.emptyList());

            return commentVO;
        } catch (Exception e) {
            log.error("构建CommentVO失败: commentId={}, error={}", commentPO.getId(), e.getMessage(), e);
            throw new RuntimeException("构建评论数据失败", e);
        }
    }

}
