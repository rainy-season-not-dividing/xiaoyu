package com.xiaoyu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaoyu.dto.task.PublishTaskDTO;
import com.xiaoyu.entity.TasksPO;
import com.xiaoyu.result.PageResult;
import com.xiaoyu.vo.task.GetTasksVO;
import com.xiaoyu.vo.task.PublishTaskVO;

import java.util.Map;

public interface yujiTasksService extends IService<TasksPO> {
    PublishTaskVO publishTask(PublishTaskDTO publishTaskDTO);

    PageResult<GetTasksVO> getTasks(Integer page, Integer size, TasksPO.Status status, String keyword, Integer tagId);

    PageResult<GetTasksVO> getMyPublishedTasks(Integer page, Integer size,  TasksPO.Status status);

    PageResult<GetTasksVO> getMyReceivedTasks(Integer page, Integer size);

    GetTasksVO getTask(Long taskId) throws InterruptedException;

    Map<String, Object> updateTask(Long taskId, PublishTaskDTO newTaskDTO);

    void removeTask(Long taskId);

    void favoriteTask(Long taskId);

    void removeFavoriteTask(Long taskId);
}
