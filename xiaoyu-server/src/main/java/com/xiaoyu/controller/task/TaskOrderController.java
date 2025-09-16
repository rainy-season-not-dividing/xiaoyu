package com.xiaoyu.controller.task;


import com.xiaoyu.entity.TaskOrdersPO;
import com.xiaoyu.entity.TaskReviewsPO;
import com.xiaoyu.result.PageResult;
import com.xiaoyu.result.Result;
import com.xiaoyu.service.TaskOrdersService;
import com.xiaoyu.vo.task.ListTaskOrdersVO;
import com.xiaoyu.vo.task.TaskOrdersVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/task_orders")
@Slf4j
public class TaskOrderController {

    @Resource
    private TaskOrdersService taskOrdersService;

    @PostMapping("/{task_id}")
    public Result<TaskOrdersVO> createTaskOrder(@PathVariable Long taskId){
        log.info("创建任务订单：{}",taskId);
        return Result.success("接单请求已发送",taskOrdersService.createTaskOrder(taskId));
    }

    @PutMapping("/{order_id}/accept")
    public Result<TaskOrdersVO> acceptTaskOrder(@PathVariable Long taskOrderId){
        log.info("接受任务订单：{}",taskOrderId);
        return Result.success("已接收接单请求",taskOrdersService.acceptTaskOrder(taskOrderId));
    }

    @PutMapping("/{order_id}/refuse")
    public Result<TaskOrdersVO> refuseTaskOrder(@PathVariable Long orderId){
        log.info("拒绝任务订单：{}",orderId);
        return Result.success("已拒绝接单请求",taskOrdersService.refuseTaskOrder(orderId));
    }

    @PutMapping("/{order_id}/cancel")
    public Result<TaskOrdersVO> cancelTaskOrder(@PathVariable Long orderId){
        log.info("取消任务订单：{}",orderId);
        return Result.success("已取消任务订单",taskOrdersService.cancelTaskOrder(orderId));
    }

    @PutMapping("/{order_id}/finish")
    public Result<TaskOrdersVO> finishTaskOrder(@PathVariable Long orderId){
        log.info("完成任务订单：{}",orderId);
        return Result.success("已完成任务订单",taskOrdersService.finishTaskOrder(orderId));
    }

    @PutMapping("/{order_id}/confirm-finish")
    public Result<TaskOrdersVO> confirmFinishTaskOrder(@PathVariable Long orderId){
        log.info("确认完成任务订单：{}",orderId);
        return Result.success("已确认完成任务订单",taskOrdersService.confirmFinishTaskOrder(orderId));
    }

    @GetMapping
    public Result<PageResult<ListTaskOrdersVO>> listTaskOrders(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) TaskOrdersPO.Status status,
            @RequestParam(required = false) TaskReviewsPO.RoleType roleType
    ){
        log.info("获取所有任务订单");
        return Result.success("获取所有任务订单成功",taskOrdersService.getListOrders(page,size,status,roleType));
    }


}
