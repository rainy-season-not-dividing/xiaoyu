package com.xiaoyu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaoyu.dto.PublishTaskDTO;
import com.xiaoyu.entity.TasksPO;
import com.xiaoyu.result.PageResult;
import com.xiaoyu.vo.GetTasksVO;
import com.xiaoyu.vo.PublishTaskVO;

import java.util.List;

public interface TasksService extends IService<TasksPO> {
    PublishTaskVO publishTask(PublishTaskDTO publishTaskDTO);

    PageResult<GetTasksVO> getTasks(Integer page, Integer size, String status, String keyword, Integer tagId);

    PageResult<GetTasksVO> getMyPublishedTasks(Integer page, Integer size);

    PageResult<GetTasksVO> getMyReceivedTasks(Integer page, Integer size);

    GetTasksVO getTask(Long taskId);
}
