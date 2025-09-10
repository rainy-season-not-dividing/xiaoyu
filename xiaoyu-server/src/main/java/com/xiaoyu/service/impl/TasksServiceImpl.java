package com.xiaoyu.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaoyu.context.BaseContext;
import com.xiaoyu.dto.PublishTaskDTO;
import com.xiaoyu.entity.TagItemsPO;
import com.xiaoyu.entity.TaskFilesPO;
import com.xiaoyu.entity.TasksPO;
import com.xiaoyu.mapper.TasksMapper;
import com.xiaoyu.result.PageResult;
import com.xiaoyu.service.TagItemsService;
import com.xiaoyu.service.TaskFilesService;
import com.xiaoyu.service.TasksService;
import com.xiaoyu.vo.GetTasksVO;
import com.xiaoyu.vo.PublishTaskVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TasksServiceImpl extends ServiceImpl<TasksMapper, TasksPO> implements TasksService {

    @Resource
    private TaskFilesService taskFilesService;

    @Resource
    private TagItemsService tagItemsService;

    @Resource
    private TasksMapper tasksMapper;

    @Override
    @Transactional
    public PublishTaskVO publishTask(PublishTaskDTO publishTaskDTO) {
         // 获取当前用户id
        Long currentId = BaseContext.getId();
        // 封装tasks实体类
        TasksPO tasksPO = BeanUtil.copyProperties(publishTaskDTO,TasksPO.class);
            // 任务状态默认为 auditing 审核中
        tasksPO.setStatus(TasksPO.Status.AUDITING);
        tasksPO.setPublisherId(currentId);
        if(publishTaskDTO.getVisibility() != null){
            tasksPO.setVisibility(TasksPO.Visibility.valueOf(publishTaskDTO.getVisibility()));
        }
        save(tasksPO);
        // 封装task_files实体类
        Long taskId = tasksPO.getId();
        List<Long> fileIds = publishTaskDTO.getFileIds();
        if(CollUtil.isNotEmpty(fileIds)){
            List<TaskFilesPO> taskFilesPOList = fileIds.stream()
                    .map(fileId->TaskFilesPO.builder().taskId(taskId).fileId(fileId).build())
                    .toList();
            taskFilesService.saveBatch(taskFilesPOList);
        }
        // 封装tag_items实体类
        List<Integer> tagIds = publishTaskDTO.getTagIds();
        if(CollUtil.isNotEmpty(tagIds)){
            List<TagItemsPO> tagItemsPOList = tagIds.stream()
                    .map(tagId->TagItemsPO.builder().tagId(tagId).itemId(taskId).build())
                    .toList();
            tagItemsService.saveBatch(tagItemsPOList);
        }
        // 封装DTO类
        PublishTaskVO taskVO = BeanUtil.copyProperties(tasksPO,PublishTaskVO.class);
        // todo: 任务评价和任务统计这里   是否   需要加上
        // 返回
        return taskVO;
    }

    @Override
    public PageResult<GetTasksVO> getTasks(Integer page, Integer size, String status, String keyword, Integer tagId) {
        // 封装Page
        Page<GetTasksVO> pageSet = Page.of(page,size);
        // mysql查询，自定义sql语句
        // todo: redis优化，减轻数据库压力
        Page<GetTasksVO> pageInfo = tasksMapper.getTasks(pageSet,status,keyword,tagId,null);
        // 返回结果
        return new PageResult<>(pageInfo.getRecords(),page,size,pageInfo.getTotal());
    }

    @Override
    public PageResult<GetTasksVO> getMyPublishedTasks(Integer page, Integer size) {
        // 封装Page
        Page<GetTasksVO> pageSet = Page.of(page,size);
        Long currentId = BaseContext.getId();
        // mysql查询，自定义sql语句
        // todo: redis优化，减轻数据库压力
        Page<GetTasksVO> pageInfo = tasksMapper.getTasks(pageSet,null,null,null, currentId);
        // 返回结果
        return new PageResult<>(pageInfo.getRecords(),page,size,pageInfo.getTotal());

    }

    @Override
    public PageResult<GetTasksVO> getMyReceivedTasks(Integer page, Integer size) {
        // 封装Page
        Page<GetTasksVO> pageSet = Page.of(page,size);
        Long currentId = BaseContext.getId();
        // mysql查询，自定义sql语句
        // todo: redis优化，减轻数据库压力
        Page<GetTasksVO> pageInfo = tasksMapper.getReceivedTasks(pageSet, currentId);
        // 返回结果
        return new PageResult<>(pageInfo.getRecords(),page,size,pageInfo.getTotal());

    }

    @Override
    public GetTasksVO getTask(Long taskId) {
        // 封装Page
        Long currentId = BaseContext.getId();
        // mysql查询，自定义sql语句
        // todo: redis优化，减轻数据库压力
        GetTasksVO taskInfo = tasksMapper.getTask(currentId);
        // 返回结果
        return taskInfo;

    }
}
