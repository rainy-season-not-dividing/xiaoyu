package com.xiaoyu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaoyu.entity.TaskOrdersPO;
import com.xiaoyu.entity.TaskReviewsPO;
import com.xiaoyu.result.PageResult;
import com.xiaoyu.vo.ListTaskOrdersVO;
import com.xiaoyu.vo.TaskOrdersVO;

public interface TaskOrdersService extends IService<TaskOrdersPO> {
    TaskOrdersVO createTaskOrder(Long taskId);

    TaskOrdersVO acceptTaskOrder(Long taskOrderId);

    TaskOrdersVO refuseTaskOrder(Long taskOrderId);

    TaskOrdersVO cancelTaskOrder(Long taskOrderId);

    TaskOrdersVO finishTaskOrder(Long taskOrderId);

    TaskOrdersVO confirmFinishTaskOrder(Long taskOrderId);

    PageResult<ListTaskOrdersVO> getListOrders(Integer page, Integer size, TaskOrdersPO.Status status, TaskReviewsPO.RoleType roleType);
}
