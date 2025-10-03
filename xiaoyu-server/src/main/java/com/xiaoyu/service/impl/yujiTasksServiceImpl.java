package com.xiaoyu.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.xiaoyu.constant.TaskConstant;
import com.xiaoyu.context.BaseContext;
import com.xiaoyu.dto.friend.ShareFriendDTO;
import com.xiaoyu.dto.task.PublishTaskDTO;
import com.xiaoyu.entity.*;
import com.xiaoyu.mapper.yujiTasksMapper;
import com.xiaoyu.result.PageResult;
import com.xiaoyu.service.*;
import com.xiaoyu.vo.task.GetTasksVO;
import com.xiaoyu.vo.task.PublishTaskVO;
import com.xiaoyu.service.yujiFilesService;
import com.xiaoyu.common.utils.RedisUtil;
import com.xiaoyua.service.jPushService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xiaoyu.exception.NotExistsException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class yujiTasksServiceImpl extends ServiceImpl<yujiTasksMapper, TasksPO> implements yujiTasksService {

    @Resource
    private yujiTaskFilesService yujiTaskFilesService;

    @Resource
    private yujiTagItemsService yujiTagItemsService;

    @Resource
    private yujiTasksMapper yujiTasksMapper;

    @Resource
    private yujiFavoritesService yujiFavoritesService;

    @Resource
    private yujiFilesService yujiFilesService;

    @Resource
    private yujiTaskStatsService yujiTaskStatsService;

    @Resource
    private yujiTaskReviewsService yujiTaskReviewsService;

    @Resource
    private RedisUtil redisUtil;

    @Resource
    private RedisTemplate<String ,Object> redisTemplate;

    @Resource
    private jPushService jPushService;


    @Override
    @Transactional
    public PublishTaskVO publishTask(PublishTaskDTO publishTaskDTO) {
         // 获取当前用户id
        Long currentId = BaseContext.getId();

        // 封装tasks实体类
        TasksPO tasksPO = BeanUtil.copyProperties(publishTaskDTO,TasksPO.class);
        // 任务状态默认为 auditing 审核中
        // todo： 异步上传至后台审核平台中
        tasksPO.setStatus(TasksPO.Status.AUDITING);
        tasksPO.setPublisherId(currentId);
        if(publishTaskDTO.getVisibility() != null){
            tasksPO.setVisibility(TasksPO.Visibility.valueOf(publishTaskDTO.getVisibility()));
        }
        save(tasksPO);
        // 封装task_files实体类
        Long taskId = tasksPO.getId();

        List<FilesPO> fileList = publishTaskDTO.getFileUrls().stream().map(
                fileUrl -> FilesPO.builder().userId(currentId).fileUrl(fileUrl).bizType(FilesPO.BizType.TASK).build()
        ).toList();
        yujiFilesService.saveBatch(fileList);
        List<Long> fileIds = fileList.stream().map(FilesPO::getId).toList();
        if(CollUtil.isNotEmpty(fileIds)){
            List<TaskFilesPO> taskFilesPOList = fileIds.stream()
                    .map(fileId->TaskFilesPO.builder().taskId(taskId).fileId(fileId).build())
                    .toList();
            yujiTaskFilesService.saveBatch(taskFilesPOList);
        }
        // 封装tag_items实体类
        List<Integer> tagIds = publishTaskDTO.getTagIds();
        if(CollUtil.isNotEmpty(tagIds)){
            List<TagItemsPO> tagItemsPOList = tagIds.stream()
                    .map(tagId->TagItemsPO.builder().tagId(tagId).itemId(taskId).build())
                    .toList();
            yujiTagItemsService.saveBatch(tagItemsPOList);
        }
        // 封装DTO类
        PublishTaskVO taskVO = BeanUtil.copyProperties(tasksPO,PublishTaskVO.class);

        // 添加任务统计
        TaskStatsPO taskStatsPO = TaskStatsPO.builder().taskId(taskId).build();
        yujiTaskStatsService.save(taskStatsPO);


        // 返回
        return taskVO;
    }

    @Override
    public PageResult<GetTasksVO> getTasks(Integer page, Integer size, TasksPO.Status status, String keyword, Integer tagId) {
        // 封装Page
        Page<GetTasksVO> pageSet = Page.of(page,size);
        // mysql查询，自定义sql语句
        // todo: redis优化，减轻数据库压力
        Page<GetTasksVO> pageInfo = yujiTasksMapper.getTasks(pageSet,status,keyword,tagId,null);
        // 返回结果
        return new PageResult<>(pageInfo.getRecords(),page,size,pageInfo.getTotal());
    }

    @Override
    public PageResult<GetTasksVO> getMyPublishedTasks(Integer page, Integer size, TasksPO.Status status) {
        // 封装Page
        Page<GetTasksVO> pageSet = Page.of(page,size);
        Long currentId = BaseContext.getId();
        // mysql查询，自定义sql语句
        Page<GetTasksVO> pageInfo = yujiTasksMapper.getTasks(pageSet,status,null,null, currentId);
        // 返回结果
        return new PageResult<>(pageInfo.getRecords(),page,size,pageInfo.getTotal());

    }

    @Override
    public PageResult<GetTasksVO> getMyReceivedTasks(Integer page, Integer size) {
        // 封装Page
        Page<GetTasksVO> pageSet = Page.of(page,size);
        Long currentId = BaseContext.getId();
        // mysql查询，自定义sql语句
        Page<GetTasksVO> pageInfo = yujiTasksMapper.getReceivedTasks(pageSet, currentId);
        // 返回结果
        return new PageResult<>(pageInfo.getRecords(),page,size,pageInfo.getTotal());

    }

    @Override
    public GetTasksVO getTask(Long taskId) throws InterruptedException{
        // mysql查询，自定义sql语句
//        GetTasksVO taskInfo = yujiTasksMapper.getTask(taskId);
        List<GetTasksVO> list = redisUtil.<GetTasksVO, Long>queryWithLogicExpire(
                TaskConstant.TASK_DETAIL_PREFIX,
                taskId,
//                GetTasksVO.class,
                new TypeReference<GetTasksVO>() {},
                id -> {
                    GetTasksVO taskInfo = yujiTasksMapper.getTask(id);
                    List<GetTasksVO> resultList = new ArrayList<>();
                    if (taskInfo != null) {
                        resultList.add(taskInfo);
                        return resultList;
                    }
                    return null;
                },
                TaskConstant.TASK_DETAIL_EXPIRE, TimeUnit.SECONDS
        );
        // 修改task_stats
        yujiTaskStatsService.lambdaUpdate().setSql("view_cnt = view_cnt + 1")
                .eq(TaskStatsPO::getTaskId, taskId)
                .update();
        // 返回结果
        return list!=null?list.getFirst():null;
    }

    @Override
    @Transactional
    public Map<String, Object> updateTask(Long taskId, PublishTaskDTO newTaskDTO) {
        // 1、根据id返回任务详细
            TasksPO tasksPO = getById(taskId);
            if(tasksPO == null) throw new NotExistsException("该任务不存在");
        // 2、任务状态修改为auditing
            BeanUtils.copyProperties(newTaskDTO,tasksPO);
            tasksPO.setStatus(TasksPO.Status.AUDITING);
        // 3、获取用户id， 设置为publisher_id
            tasksPO.setPublisherId(BaseContext.getId());
        // 4、封装tasks实体类
        // 5、更新任务
            updateById(tasksPO);
        // 6、更新关联表  file_ids 和 tag_ids
        // 先删后增
        yujiTaskFilesService.remove(new LambdaQueryWrapper<TaskFilesPO>().eq(TaskFilesPO::getTaskId,taskId));
        // 处理文件
        Long currentId = BaseContext.getId();
//        List<MultipartFile> files = newTaskDTO.getFiles();
//        List<Long> fileIds = new ArrayList<>();
//        if (files != null && !files.isEmpty()) {
//            for (MultipartFile file : files) {
//                fileIds.add((Long) yujiFilesService.uploadFile(file, "POST", currentId).get("fileId"));
//            }
//        }
        List<FilesPO> fileList = newTaskDTO.getFileUrls().stream().map(
                fileUrl -> FilesPO.builder().userId(currentId).fileUrl(fileUrl).build()
        ).toList();
        yujiFilesService.saveBatch(fileList);
        List<Long> fileIds = fileList.stream().map(FilesPO::getId).toList();
        List<TaskFilesPO> taskFilesPOList = fileIds.stream()
                .map(fileId -> TaskFilesPO.builder().taskId(taskId).fileId(fileId).build())
                .toList();
        yujiTaskFilesService.saveBatch(taskFilesPOList);
        yujiTagItemsService.remove(new LambdaQueryWrapper<TagItemsPO>().eq(TagItemsPO::getItemId,taskId));
        List<TagItemsPO> tagItemsPOList = newTaskDTO.getTagIds().stream()
                .map(tagId -> TagItemsPO.builder().itemId(taskId).tagId(tagId).build())
                .toList();
        yujiTagItemsService.saveBatch(tagItemsPOList);

        // 删除缓存
        redisTemplate.delete(TaskConstant.TASK_DETAIL_PREFIX+taskId);

        return Collections.singletonMap("updated_at", tasksPO.getUpdatedAt());
    }

    @Override
    @Transactional
    public void removeTask(Long taskId) {
        // 1、是否存在
        TasksPO tasksPO = getById(taskId);
        if(tasksPO == null) throw new NotExistsException("任务不存在");
        // 2、删除关联文件表和关联标签表中对应的记录
        yujiTagItemsService.remove(new LambdaQueryWrapper<TagItemsPO>().eq(TagItemsPO::getItemId, taskId));
        yujiTaskFilesService.remove(new LambdaQueryWrapper<TaskFilesPO>().eq(TaskFilesPO::getTaskId, taskId));
        // 3、删除任务
        removeById(taskId);
        // 删除相关联的表, 任务订单表保留
        yujiTaskReviewsService.remove(new LambdaQueryWrapper<TaskReviewsPO>().eq(TaskReviewsPO::getTaskId, taskId));
        yujiTaskStatsService.remove(new LambdaQueryWrapper<TaskStatsPO>().eq(TaskStatsPO::getTaskId, taskId));


        // 删除缓存：
        redisTemplate.delete(TaskConstant.TASK_DETAIL_PREFIX+taskId);
    }

    @Override
    @Transactional
    public void favoriteTask(Long taskId) {
        if(yujiTasksMapper.selectById(taskId) == null){
            throw new NotExistsException("任务不存在");
        }
        // 1、获取当前用户
        Long currentId = BaseContext.getId();
        // 2、构造实体
        FavoritesPO favoritesPO = FavoritesPO.builder()
                .userId(currentId)
                .itemId(taskId)
                .itemType(FavoritesPO.ItemType.TASK).build();
        // 3、保存到收藏表中
        yujiFavoritesService.save(favoritesPO);
    }

    @Override
    @Transactional
    public void removeFavoriteTask(Long taskId) {
        // 删除收藏表中对应的记录
        yujiFavoritesService.remove(new LambdaQueryWrapper<FavoritesPO>().eq(FavoritesPO::getUserId, BaseContext.getId()).eq(FavoritesPO::getItemId, taskId).eq(FavoritesPO::getItemType, FavoritesPO.ItemType.TASK));
    }

    @Override
    public void shareTask(Long taskId, ShareFriendDTO shareFriendDTO) {
        Long currentId = BaseContext.getId();
        for(Long friendId:shareFriendDTO.getShareUserIds()){
            jPushService.pushShareNotification(friendId,currentId,taskId, "TASK");
        }
    }
}
