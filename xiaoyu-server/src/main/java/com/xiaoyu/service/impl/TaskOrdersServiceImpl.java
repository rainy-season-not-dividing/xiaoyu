package com.xiaoyu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaoyu.context.BaseContext;
import com.xiaoyu.entity.TaskOrdersPO;
import com.xiaoyu.entity.TaskReviewsPO;
import com.xiaoyu.entity.TasksPO;
import com.xiaoyu.exception.BadRequestException;
import com.xiaoyu.exception.NotExistsException;
import com.xiaoyu.mapper.TaskOrdersMapper;
import com.xiaoyu.result.PageResult;
import com.xiaoyu.service.TaskOrdersService;
import com.xiaoyu.service.TaskReviewsService;
import com.xiaoyu.service.TasksService;
import com.xiaoyu.vo.ListTaskOrdersVO;
import com.xiaoyu.vo.TaskOrdersVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class TaskOrdersServiceImpl extends ServiceImpl<TaskOrdersMapper, TaskOrdersPO> implements TaskOrdersService {

    @Resource
    private TasksService tasksService;

    @Resource
    private TaskOrdersMapper taskOrdersMapper;




    @Override
    public TaskOrdersVO createTaskOrder(Long taskId) {
        // 先判断这个单是否存在
        TasksPO tasksPO = tasksService.getOne(new LambdaQueryWrapper<TasksPO>().eq(TasksPO::getId, taskId));
        if(tasksPO==null) throw new NotExistsException("任务不存在");
        if(tasksPO.getStatus() != TasksPO.Status.RECRUIT){
            throw new BadRequestException("任务状态异常");
        }
        // 1、获取用户id
        Long currentId = BaseContext.getId();
        // 2、封装实体类
        TaskOrdersPO orderPO = TaskOrdersPO.builder()
                .taskId(taskId)
                .receiverId(currentId)
                .status(TaskOrdersPO.Status.WAIT_ACCEPT).build();
        // 3、保存
        save(orderPO);
        // todo：是否需要添加到task_stas数据统计表中
        // 4、 返回结果
        /** 企业上，一般VO只有一个字段时才会用map替代，其它情况下不能    */
        return new TaskOrdersVO(orderPO.getId(), orderPO.getStatus());
    }

    @Override
    public TaskOrdersVO acceptTaskOrder(Long taskOrderId) {
        // 先判断这个单是否存在
        TaskOrdersPO taskOrdersPO = getOne(new LambdaQueryWrapper<TaskOrdersPO>().eq(TaskOrdersPO::getId, taskOrderId));
        if(taskOrdersPO==null) throw new NotExistsException("该任务订单不存在");
        if(taskOrdersPO.getStatus() != TaskOrdersPO.Status.WAIT_ACCEPT){
            throw new BadRequestException("任务状态异常");
        }
        // 1、获取用户id
        Long currentId = BaseContext.getId();
        // 判断这个任务是不是当前用户发布的
        TasksPO taskPO = tasksService.getOne(new LambdaQueryWrapper<TasksPO>().eq(TasksPO::getId, taskOrdersPO.getTaskId()));
        if(!currentId.equals(taskPO.getPublisherId())){
            throw new BadRequestException("任务状态异常，当前用户不是任务发布者");
        }
        if(currentId.equals(taskOrdersPO.getReceiverId())){
            throw new BadRequestException("不能自己接自己发布的任务");
        }
        // 修改任务订单表的状态，接收订单
        taskOrdersPO.setStatus(TaskOrdersPO.Status.ACCEPTED);
        updateById(taskOrdersPO);
        // todo：是否需要添加到task_stas数据统计表中
        return new TaskOrdersVO(taskOrderId, taskOrdersPO.getStatus());
    }

    @Override
    public TaskOrdersVO refuseTaskOrder(Long taskOrderId) {
        // 先判断这个单是否存在
        TaskOrdersPO taskOrdersPO = getOne(new LambdaQueryWrapper<TaskOrdersPO>().eq(TaskOrdersPO::getId, taskOrderId));
        if(taskOrdersPO==null) throw new NotExistsException("该任务订单不存在");
        if(taskOrdersPO.getStatus() != TaskOrdersPO.Status.WAIT_ACCEPT){
            throw new BadRequestException("任务状态异常");
        }
        // 1、获取用户id
        Long currentId = BaseContext.getId();
        // 判断这个任务是不是当前用户发布的
        TasksPO taskPO = tasksService.getOne(new LambdaQueryWrapper<TasksPO>().eq(TasksPO::getId, taskOrdersPO.getTaskId()));
        if(!currentId.equals(taskPO.getPublisherId())){
            throw new BadRequestException("任务状态异常，当前用户不是任务发布者");
        }
        if(currentId.equals(taskOrdersPO.getReceiverId())){
            throw new BadRequestException("不能自己拒绝自己发布的任务");
        }
        // 修改任务订单表的状态，订单状态不变，保持待接单
//        taskOrdersPO.setStatus(TaskOrdersPO.Status.WAIT_ACCEPT);
//        updateById(taskOrdersPO);
        return new TaskOrdersVO(taskOrderId, taskOrdersPO.getStatus());
    }

    @Override
    public TaskOrdersVO cancelTaskOrder(Long taskOrderId) {
        // 先判断这个单是否存在
        TaskOrdersPO taskOrdersPO = getOne(new LambdaQueryWrapper<TaskOrdersPO>().eq(TaskOrdersPO::getId, taskOrderId));
        if(taskOrdersPO==null) throw new NotExistsException("该任务订单不存在");
        if(taskOrdersPO.getStatus() != TaskOrdersPO.Status.WAIT_ACCEPT){
            throw new BadRequestException("任务状态异常");
        }
        // 1、获取用户id
        Long currentId = BaseContext.getId();
        // 判断这个任务是不是当前用户发布的
        TasksPO taskPO = tasksService.getOne(new LambdaQueryWrapper<TasksPO>().eq(TasksPO::getId, taskOrdersPO.getTaskId()));
        if(!currentId.equals(taskPO.getPublisherId())){
            throw new BadRequestException("任务状态异常，当前用户不是任务发布者");
        }
        if(currentId.equals(taskOrdersPO.getReceiverId())){
            throw new BadRequestException("不能自己拒绝自己发布的任务");
        }
        // 修改任务订单表的状态，改为 取消
        // todo:是否需要删除表中对应的记录呢， 取消订单的前提是什么，这里弄的是订单只处于待接单状态才行
        taskOrdersPO.setStatus(TaskOrdersPO.Status.CANCELLED);
        updateById(taskOrdersPO);
        return new TaskOrdersVO(taskOrderId, taskOrdersPO.getStatus());
    }

    @Override
    public TaskOrdersVO finishTaskOrder(Long taskOrderId) {
        // 先判断这个单是否存在
        TaskOrdersPO taskOrdersPO = getOne(new LambdaQueryWrapper<TaskOrdersPO>().eq(TaskOrdersPO::getId, taskOrderId));
        if(taskOrdersPO==null) throw new NotExistsException("该任务订单不存在");
        if(taskOrdersPO.getStatus() != TaskOrdersPO.Status.ACCEPTED){
            throw new BadRequestException("任务状态异常");
        }
        // 1、获取用户id
        Long currentId = BaseContext.getId();
        // 判断这个任务是不是当前用户接收的
        TasksPO taskPO = tasksService.getOne(new LambdaQueryWrapper<TasksPO>().eq(TasksPO::getId, taskOrdersPO.getTaskId()));
        if(!currentId.equals(taskOrdersPO.getReceiverId())){
            throw new BadRequestException("不能标记完成非自己接收的订单");
        }
        // 修改任务订单表的状态，改为 完成(接单者)
        taskOrdersPO.setStatus(TaskOrdersPO.Status.DELIVERY);
        updateById(taskOrdersPO);
        // todo：是否需要添加到task_stas数据统计表中
        return new TaskOrdersVO(taskOrderId, taskOrdersPO.getStatus());
    }

    @Override
    public TaskOrdersVO confirmFinishTaskOrder(Long taskOrderId) {
        // 先判断这个单是否存在
        TaskOrdersPO taskOrdersPO = getOne(new LambdaQueryWrapper<TaskOrdersPO>().eq(TaskOrdersPO::getId, taskOrderId));
        if(taskOrdersPO==null) throw new NotExistsException("该任务订单不存在");
        if(taskOrdersPO.getStatus() != TaskOrdersPO.Status.ACCEPTED){
            throw new BadRequestException("任务状态异常");
        }
        // 1、获取用户id
        Long currentId = BaseContext.getId();
        // 判断这个任务是不是当前用户发布的
        TasksPO taskPO = tasksService.getOne(new LambdaQueryWrapper<TasksPO>().eq(TasksPO::getId, taskOrdersPO.getTaskId()));
        if(!currentId.equals(taskPO.getPublisherId())){
            throw new BadRequestException("不能标记完成非自己发布的任务");
        }
        // 修改任务订单表的状态，改为 完成(接单者)
        taskOrdersPO.setStatus(TaskOrdersPO.Status.FINISH);
        updateById(taskOrdersPO);
        // todo：是否需要添加到task_stas数据统计表中
        return new TaskOrdersVO(taskOrderId, taskOrdersPO.getStatus());
    }

    @Override
    @Transactional
    public PageResult<ListTaskOrdersVO> getListOrders(Integer page, Integer size, TaskOrdersPO.Status status, TaskReviewsPO.RoleType roleType) {
        // 构造Page类
        Long currentId = BaseContext.getId();
        Page<ListTaskOrdersVO> pageSet = new Page<>(page,size);
        // 自定义sql进行查询
        Page<ListTaskOrdersVO> pageInfo = taskOrdersMapper.getListOrders(pageSet,status,roleType,currentId);
        // 返回结果
        return  new PageResult(pageInfo.getRecords(),page,size,pageInfo.getTotal());
    }


}
