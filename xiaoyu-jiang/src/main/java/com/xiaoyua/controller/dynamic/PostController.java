package com.xiaoyua.controller.dynamic;

import com.xiaoyu.common.utils.RedisUtil;
import com.xiaoyua.common.constant.PostConstant;
import com.xiaoyua.context.BaseContext;
import com.xiaoyua.dto.post.PostCreateDTO;
import com.xiaoyua.dto.post.PostForm;
import com.xiaoyua.dto.post.PostUpdateDTO;
import com.xiaoyua.entity.FilePO;
import com.xiaoyua.service.jPostService;
import com.xiaoyua.service.jLikeService;
import com.xiaoyua.service.jFavService;
import com.xiaoyua.service.jShareService;
import com.xiaoyua.service.jFileService;
import com.xiaoyua.vo.post.PostVO;
import com.xiaoyua.result.Result;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;

import java.util.*;
import java.util.concurrent.TimeUnit;

import com.xiaoyua.common.enums.TargetType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;

@RestController
@RequestMapping("/posts")
@Slf4j
@Validated
@Tag(name = "动态管理", description = "动态相关接口")
public class PostController {
    @Autowired
    private jPostService jPostService;
    
    @Autowired
    private jLikeService jLikeService;
    
    @Autowired
    private jFavService jFavService;
    
    @Autowired
    private jShareService jShareService;


    @Autowired
    private jFileService jFileService;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private RedisTemplate<String , Object> redisTemplate;



    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "发布动态（@ModelAttribute 混合表单：表单字段 + files）")
    public Result<PostVO> createPost(@RequestBody PostForm form) {
        log.info("createPost,{form}",form);
        Long userId = BaseContext.getCurrentId();
        // 将表单映射到 DTO
        PostCreateDTO dto = new PostCreateDTO();
        dto.setTitle(form.getTitle());
        dto.setContent(form.getContent());
        dto.setCampusId(form.getCampusId());
        dto.setVisibility(form.getVisibility());
        dto.setPoiLat(form.getPoiLat());
        dto.setPoiLng(form.getPoiLng());
        dto.setPoiName(form.getPoiName());

        // 处理文件
//        List<MultipartFile> files = form.getFiles();
//        if (files != null && !files.isEmpty()) {
//            List<Long> fileIds = new ArrayList<>();
//            for (MultipartFile file : files) {
//                log.info("这里的userId: {}",userId);
//                var fileVO = jFileService.uploadFile(file, "POST", userId);
//                if (fileVO != null && fileVO.getId() != null) {
//                    fileIds.add(fileVO.getId());
//                }
//            }
//            dto.setFileIds(fileIds);
//        }
        Long currentId = BaseContext.getCurrentId();
        List<FilePO> fileList = form.getFiles().stream().map(
                fileUrl -> FilePO.builder().userId(currentId).fileUrl(fileUrl).build()
        ).toList();
        jFileService.saveBatch(fileList);
        List<Long> fileIds = fileList.stream().map(FilePO::getId).toList();
        dto.setFileIds(fileIds);


        PostVO postVO = jPostService.createPost(dto, userId);
        return Result.success("发布成功", postVO);
    }

    @PostMapping("/{post_id}/like")
    @Operation(summary = "点赞动态")
    public Result addLike(@PathVariable("post_id") @Min(1) Long postId) {
        log.info("addLike postId={}", postId);
        Long userId = BaseContext.getCurrentId();
        jLikeService.addLike(postId, userId, TargetType.POST);
        // 返回最新的点赞数量
        long likeCount = jLikeService.getLikeCount(postId, TargetType.POST);
        return Result.success("点赞成功", Map.of("like_cnt", likeCount));
    }

    @DeleteMapping("/{post_id}/like")
    @Operation(summary = "取消点赞动态")
    public Result deleteLike(@PathVariable("post_id") @Min(1) Long postId) {
        log.info("deleteLike postId={}", postId);
        Long userId = BaseContext.getCurrentId();
        jLikeService.deleteLike(postId, userId, TargetType.POST);
        
        // 返回最新的点赞数量
        long likeCount = jLikeService.getLikeCount(postId, TargetType.POST);
        return Result.success("取消点赞成功", Map.of("like_cnt", likeCount));
    }

    @PostMapping("/{post_id}/favorite")
    @Operation(summary = "收藏动态")
    public Result addFav(@PathVariable("post_id") @Min(1) Long postId) {
        log.info("addFav postId={}", postId);
        Long userId = BaseContext.getCurrentId();
        jFavService.addFavorite(postId, userId, TargetType.POST);
        long favCount = jFavService.getFavoriteCount(postId, TargetType.POST);
        return Result.success("收藏成功", Map.of("fav_cnt", favCount));
    }
    
    @DeleteMapping("/{post_id}/favorite")
    @Operation(summary = "取消收藏动态")
    public Result deleteFav(@PathVariable("post_id") @Min(1) Long postId) {
        log.info("deleteFav postId={}", postId);
        Long userId = BaseContext.getCurrentId();
        jFavService.deleteFavorite(postId, userId, TargetType.POST);
        long favCount = jFavService.getFavoriteCount(postId, TargetType.POST);
        return Result.success("取消收藏成功", Map.of("fav_cnt", favCount));
    }

    @PostMapping("/{post_id}/share")
    @Operation(summary = "转发动态")
    public Result addShare(@PathVariable("post_id") @Min(1) Long postId){
        log.info("addShare postId={}", postId);
        jShareService.addShare(postId, BaseContext.getCurrentId());
        return Result.success("转发成功");
    }

    @GetMapping("/hot")
    @Operation(summary = "获取热门动态（按浏览数倒序，默认前10条）")
    public Result<List<PostVO>> getHotPosts(@RequestParam(value = "limit", required = false) Integer limit) throws InterruptedException{
        log.info("listHot limit={}", limit);
//        var list = jPostService.listHot(limit);

        List<PostVO> list = redisUtil.<PostVO, Integer>queryWithLogicExpire(
                PostConstant.POST_HOT_KEY_PREFIX,
                limit, PostVO.class,
                id->jPostService.listHot(id),
                PostConstant.POST_HOT_TIMEOUT, TimeUnit.SECONDS
        );
        return Result.success("success", list);
    }

    @GetMapping
    @Operation(summary = "获取全部动态列表")
    public Result getAllPosts(@RequestParam(value = "page", required = false) Integer page,
                              @RequestParam(value = "size", required = false) Integer size,
                              @RequestParam(value = "sort", required = false) String sort) {
        log.info("listAll page={}, size={}, sort={}", page, size, sort);
        var pageResult = jPostService.listAll(page, size, sort);
        return Result.success("success", pageResult);
    }

    @GetMapping("/user/{user_id}")
    @Operation(summary = "获取指定用户的动态列表")
    public Result getPostsByUser(@PathVariable("user_id") @Min(1) Long userId,
                                 @RequestParam(value = "page", required = false) Integer page,
                                 @RequestParam(value = "size", required = false) Integer size,
                                 @RequestParam(value = "sort", required = false) String sort) {
        log.info("listByUser userId={}, page={}, size={}, sort={}", userId, page, size, sort);
        var pageResult = jPostService.listByUser(userId, page, size, sort);
        return Result.success("success", pageResult);
    }

    @GetMapping("/{post_id}")
    @Operation(summary = "获取动态详情")
    public Result getPostDetail(@PathVariable("post_id") @Min(1) Long postId) {
        log.info("getPostDetail postId={}", postId);
        // 参数验证
        if (postId == null || postId <= 0) {
            return Result.error("动态ID不能为空");
        }
        
        try {
            // 调用服务层获取动态详情
//            PostVO postVO = jPostService.getPostDetail(postId);
            List<PostVO> list = redisUtil.<PostVO, Long>queryWithLogicExpire(
                    PostConstant.POST_DETAIL_KEY_PREFIX,
                    postId, PostVO.class,
                    id-> {
                        PostVO postVo =  jPostService.getPostDetail(id);
                        List<PostVO> postList = new ArrayList<>();
                        if(postVo != null){
                            postList.add(postVo);
                            return postList;
                        }
                        return null;
                    },
                    PostConstant.POST_DETAIL_TIMEOUT, TimeUnit.SECONDS
            );
            return Result.success("获取成功", list!=null?list.getFirst():null);
        } catch (RuntimeException e) {
            log.warn("获取动态详情失败: postId={}, error={}", postId, e.getMessage());
            return Result.error(e.getMessage());
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @PutMapping("/{post_id}")
    @Operation(summary = "更新动态")
    public Result updatePost(@RequestBody @Valid PostUpdateDTO postUpdateDTO, @PathVariable("post_id") @Min(1) Long postId) {
        log.info("updatePost postUpdateDTO={}, postId={}", postUpdateDTO, postId);
        jPostService.updatePost(postUpdateDTO,postId);
        return Result.success("更新成功");
    }

    @DeleteMapping("/{post_id}")
    @Operation(summary = "删除动态")
    public Result deletePost(@PathVariable("post_id") Long postId) {
        log.info("deletePost postId={}", postId);
        jPostService.deletePost(postId);
        return Result.success("删除成功");
    }


    void clearCache(String pattern){
        log.info("清理缓存：{}",pattern);
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
        log.info("清理完成");
    }

    @Scheduled(cron = "0 0 * * * ?")
    void clearCache(){
        log.info("定时任务调度, 更新热门动态");
        clearCache(PostConstant.POST_HOT_KEY_PREFIX+"::*");
    }


}
