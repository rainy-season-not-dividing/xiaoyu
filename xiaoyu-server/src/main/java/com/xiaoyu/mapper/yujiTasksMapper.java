package com.xiaoyu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaoyu.entity.TasksPO;
import com.xiaoyu.vo.task.GetTasksVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface yujiTasksMapper extends BaseMapper<TasksPO> {
    Page<GetTasksVO> getTasks(Page<GetTasksVO> pageSet, TasksPO.Status status, String keyword, Integer tagId, Long userId);

    Page<GetTasksVO> getReceivedTasks(Page<GetTasksVO> pageSet, Long userId);

    GetTasksVO getTask(Long currentId);
}
