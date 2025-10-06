package com.xiaoyu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaoyu.common.utils.RedisIdUtil;
import com.xiaoyu.constant.TaskConstant;
import com.xiaoyu.context.BaseContext;
import com.xiaoyu.entity.TaskOrdersPO;
import com.xiaoyu.entity.TaskReviewsPO;
import com.xiaoyu.entity.TasksPO;
import com.xiaoyu.exception.BadRequestException;
import com.xiaoyu.exception.NotExistsException;
import com.xiaoyu.mapper.yujiTaskOrdersMapper;
import com.xiaoyu.result.PageResult;
import com.xiaoyu.service.yujiTaskOrdersService;
import com.xiaoyu.service.yujiTaskReviewsService;
import com.xiaoyu.service.yujiTaskStatsService;
import com.xiaoyu.service.yujiTasksService;
import com.xiaoyu.vo.task.ListTaskOrdersVO;
import com.xiaoyu.vo.task.TaskOrdersVO;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;


@Service
@RequiredArgsConstructor
public class yujiTaskOrdersServiceImpl extends ServiceImpl<yujiTaskOrdersMapper, TaskOrdersPO> implements yujiTaskOrdersService {

    @Resource
    private yujiTasksService yujiTasksService;

    @Resource
    private yujiTaskOrdersMapper yujiTaskOrdersMapper;

    @Resource
    private yujiTaskStatsService yujiTaskStatsService;

    @Resource
    private yujiTaskReviewsService yujiTaskReviewsService;

    @Resource
    private RedisIdUtil redisIdUtil;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

//    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;
//    static{
//        SECKILL_SCRIPT = new DefaultRedisScript<>();
//        SECKILL_SCRIPT.setLocation(new ClassPathResource("seckill.lua"));
//        SECKILL_SCRIPT.setResultType(Long.class);
//    }


    private final TransactionTemplate transactionTemplate;



    @Override
    public TaskOrdersVO createTaskOrder(Long taskId) {
        /**
         * 雇佣者请求接单（创建订单）
         * 1、任务存在且 处于RECRUIT 招聘状态
         * 2、任务接收者不是任务发布者
         * 3、把 4 个判断 + 后续下单的 SQL 全部放进同一事务，利用 MySQL 的行锁（SELECT … FOR UPDATE）把并发请求串行化；
         * 4、任务状态修改为 RUNNING 进行中的状态
         *
         *
         * 1、创建任务订单表，并将状态设置为WAIT_ACCEPT等待接受状态
         * 2、todo: 通知雇主 tasksPO.getPublisherId() 有接单请求
         */

        /*
        初代方法，直接写库，无法保证原子性，导致多下单、重复下单

        // 先判断这个单是否存在
        TasksPO tasksPO = yujiTasksService.getOne(new LambdaQueryWrapper<TasksPO>().eq(TasksPO::getId, taskId));
        if(tasksPO==null) throw new NotExistsException("任务不存在");
        TaskOrdersPO taskOrdersPO = getOne(new LambdaQueryWrapper<>(TaskOrdersPO.class).eq(TaskOrdersPO::getTaskId,taskId));
        if(taskOrdersPO!=null) throw new BadRequestException("该任务已存在订单");

        if(tasksPO.getStatus() != TasksPO.Status.RECRUIT){
            throw new BadRequestException("任务状态异常，不处于招聘状态");
        }
        // 1、获取用户id
        Long currentId = BaseContext.getId();
        if(currentId.equals(tasksPO.getPublisherId())){
            throw new BadRequestException("不能自己接自己发布的任务");
        }

        //4、修改task_stats表
        yujiTaskStatsService.update(new LambdaUpdateWrapper<TaskStatsPO>()
                .eq(TaskStatsPO::getTaskId,taskId)
                .setSql("order_cnt = order_cnt + 1"));

        // 2、封装实体类 --- 把这部分加入分布式锁功能，用Redisson锁，避免任务重复被接受
        TaskOrdersPO orderPO = TaskOrdersPO.builder()
                .taskId(taskId)
                .receiverId(currentId)
                .status(TaskOrdersPO.Status.WAIT_ACCEPT).build();
        // 3、保存
//        rabbitTemplate.convertAndSend(RabbitConstant.EXCHANGE_ORDER,RabbitConstant.ROUTING_KEY_ORDER,orderPO);
        save(orderPO);

        // 4、 返回结果
        // 企业上，一般VO只有一个字段时才会用map替代，其它情况下不能
        return new TaskOrdersVO(orderPO.getId(), orderPO.getStatus());
        */

        /*
        升级方法，使用Redis分布式锁，解决多下单问题，并通过lua脚本，解决原子性问题
         */
        //  把 4 个判断 + 后续下单的 SQL 全部放进同一事务，利用 MySQL 的行锁（SELECT … FOR UPDATE）把并发请求串行化；
        return transactionTemplate.execute(status -> {
            // 1. 行锁任务，同时把后面要用的字段一次性拿出来
            TasksPO task = yujiTasksService.getOne(
                    new LambdaQueryWrapper<TasksPO>()
                            .eq(TasksPO::getId, taskId)
                            .last("LIMIT 1 FOR UPDATE"));   // 锁住整行
            if (task == null) {
                throw new NotExistsException("任务不存在");
            }
            if (task.getStatus() != TasksPO.Status.RECRUIT) {
                throw new BadRequestException("任务状态异常，不处于招聘状态");
            }
            // 2. 行锁订单（如果订单表有唯一索引可以直接插，利用唯一索引冲突抛异常）
            TaskOrdersPO exist = getOne(
                    new LambdaQueryWrapper<TaskOrdersPO>()
                            .eq(TaskOrdersPO::getTaskId, taskId)
                            .last("LIMIT 1 FOR UPDATE"));
            if (exist != null) {
                throw new BadRequestException("该任务已存在订单");
            }

            // 3. 不能接自己发布的任务
            Long currentId = BaseContext.getId();
            if (currentId.equals(task.getPublisherId())) {
                throw new BadRequestException("不能自己接自己发布的任务");
            }

            // 4. 全部通过，生成订单
            TaskOrdersPO orderPO = TaskOrdersPO.builder()
                    .taskId(taskId)
                    .receiverId(currentId)
                    .status(TaskOrdersPO.Status.ACCEPTED).build();
            save(orderPO);
            // 修改任务状态
            task.setStatus(TasksPO.Status.RUNNING);
            yujiTasksService.updateById(task);
            // 删除缓存
            stringRedisTemplate.delete(TaskConstant.TASK_DETAIL_PREFIX + taskId);

            return new TaskOrdersVO(orderPO.getId(), orderPO.getStatus());
        });
    }

    @Override
    public TaskOrdersVO acceptTaskOrder(Long taskOrderId) {
        /**
         * 这个不要了！！！！！！！！！！！
         */
        /**
         * 雇主同意接单请求
         * 1、同意任务接收者是任务发布者
         * 2、任务存在且 处于WAIT_ACCEPT待接单状态
         *
         *
         * 1、修改任务订单表对应记录状态为 ACCEPTED已接受
         * 2、修改任务表对应记录状态为 RUNNING进行中
         * 3、todo: 通知雇佣者taskOrdersPO.getReceiverId() 请求被雇主同意了
         */

        // 先判断这个单是否存在
        TaskOrdersPO taskOrdersPO = getOne(new LambdaQueryWrapper<TaskOrdersPO>().eq(TaskOrdersPO::getId, taskOrderId));
        if(taskOrdersPO==null) throw new NotExistsException("该任务订单不存在");
        if(taskOrdersPO.getStatus() != TaskOrdersPO.Status.WAIT_ACCEPT){
            throw new BadRequestException("任务状态异常,请稍后再试");
        }
        // 1、获取用户id
        Long currentId = BaseContext.getId();
        // 判断这个任务是不是当前用户发布的
        TasksPO taskPO = yujiTasksService.getOne(new LambdaQueryWrapper<TasksPO>().eq(TasksPO::getId, taskOrdersPO.getTaskId()));
        if(!currentId.equals(taskPO.getPublisherId())){
            throw new BadRequestException("任务状态异常，当前用户不是任务发布者");
        }
        // 修改任务订单表的状态，接收订单
        taskOrdersPO.setStatus(TaskOrdersPO.Status.ACCEPTED);
        updateById(taskOrdersPO);
        taskPO.setStatus(TasksPO.Status.RUNNING);
        yujiTasksService.updateById(taskPO);
        // 删除缓存
        stringRedisTemplate.delete(TaskConstant.TASK_DETAIL_PREFIX + taskPO.getId());
        return new TaskOrdersVO(taskOrderId, taskOrdersPO.getStatus());
    }

    @Override
    public TaskOrdersVO refuseTaskOrder(Long taskOrderId) {
        /**
         * 这个不要了！！！！！！！！！！！  如果要加这个，要修改状态
         */
        /**
         * 雇主拒绝接单请求
         * 1、拒绝任务接收者是任务发布者
         * 2、任务存在且 处于WAIT_ACCEPT待接单状态
         *
         * 1、修改任务表对应记录状态为 RECRUIT 招聘中
         * 2、删除任务订单表中对应的记录
         * 3、todo: 通知雇佣者 taskOrdersPO.getReceiverId() 被拒绝了
         */

        // 先判断这个单是否存在
        TaskOrdersPO taskOrdersPO = getOne(new LambdaQueryWrapper<TaskOrdersPO>().eq(TaskOrdersPO::getId, taskOrderId));
        if(taskOrdersPO==null) throw new NotExistsException("该任务订单不存在");
        if(taskOrdersPO.getStatus() != TaskOrdersPO.Status.WAIT_ACCEPT){
            throw new BadRequestException("任务状态异常");
        }
        // 1、获取用户id
        Long currentId = BaseContext.getId();
        // 判断这个任务是不是当前用户发布的
        TasksPO taskPO = yujiTasksService.getOne(new LambdaQueryWrapper<TasksPO>().eq(TasksPO::getId, taskOrdersPO.getTaskId()));
        if(!currentId.equals(taskPO.getPublisherId())){
            throw new BadRequestException("任务状态异常，当前用户不是任务发布者");
        }

        // 删除任务订单表中对应的记录
        removeById(taskOrderId);
        // 修改任务表状态为 RECRUIT 招聘中
        taskPO.setStatus(TasksPO.Status.RECRUIT);
        yujiTasksService.updateById(taskPO);
        // 删除缓存
        stringRedisTemplate.delete(TaskConstant.TASK_DETAIL_PREFIX + taskPO.getId());
        return new TaskOrdersVO(taskOrderId, taskOrdersPO.getStatus());
    }

    @Override
    public TaskOrdersVO cancelTaskOrder(Long taskOrderId) {
        /**
         * 雇佣者取消订单
         * 1、订单取消者必须是任务接收者
         * 2、任务订单存在 且只能是等待接收WAIT_ACCEPT 或者 已接受ACCEPTED这两种状态
         *
         * 1、修改任务表对应记录状态为 RECRUIT 招聘中
         * 2、删除任务订单表中对应的记录
         * 3、todo: 通知雇主taskPO.getPublisherId  任务已被雇佣者 taskOrdersPO.getReceiverId() 取消
         */

        // 先判断这个单是否存在
        TaskOrdersPO taskOrdersPO = getOne(new LambdaQueryWrapper<TaskOrdersPO>().eq(TaskOrdersPO::getId, taskOrderId));
        if(taskOrdersPO==null) throw new NotExistsException("该任务订单不存在");
        if(taskOrdersPO.getStatus() != TaskOrdersPO.Status.WAIT_ACCEPT && taskOrdersPO.getStatus() != TaskOrdersPO.Status.ACCEPTED){
            throw new BadRequestException("任务状态异常");
        }
        // 1、获取用户id
        Long currentId = BaseContext.getId();
        // 判断这个任务是不是当前用户接收的
        if(!currentId.equals(taskOrdersPO.getReceiverId())){
            throw new BadRequestException("任务状态异常，当前用户不是任务接收者");
        }
        TasksPO taskPO = yujiTasksService.getOne(new LambdaQueryWrapper<TasksPO>().eq(TasksPO::getId, taskOrdersPO.getTaskId()));

        // 修改任务订单表的状态，改为 招募中
        taskPO.setStatus(TasksPO.Status.RECRUIT);
        // 修改任务表中任务的状态
        yujiTasksService.updateById(taskPO);
        // 删除缓存
        stringRedisTemplate.delete(TaskConstant.TASK_DETAIL_PREFIX + taskPO.getId());
        // 删除对应订单表记录
        removeById(taskOrderId);
        return new TaskOrdersVO(taskOrderId, taskOrdersPO.getStatus());
    }

    @Override
    public TaskOrdersVO finishTaskOrder(Long taskOrderId) {
        /**
         * 雇佣者完成订单
         * 1、完成订单者必须是任务接收者
         * 2、任务订单存在 且只能是 ACCEPTED已接受
         *
         * 1、修改任务表对应记录状态为 DELIVERY已完成
         * 2、修改任务订单表对应记录状态为 完成
         * 3、创建任务评价表对应的记录
         * 4、todo: 订单完成，通知雇主 taskPO.getPublisherId  任务已被完成
         */


        // 先判断这个单是否存在
        TaskOrdersPO taskOrdersPO = getOne(new LambdaQueryWrapper<TaskOrdersPO>().eq(TaskOrdersPO::getId, taskOrderId));
        if(taskOrdersPO==null) throw new NotExistsException("该任务订单不存在");
        if(taskOrdersPO.getStatus() != TaskOrdersPO.Status.ACCEPTED){
            throw new BadRequestException("任务状态异常");
        }
        // 1、获取用户id
        Long currentId = BaseContext.getId();
        // 判断这个任务是不是当前用户接收的
        if(!currentId.equals(taskOrdersPO.getReceiverId())){
            throw new BadRequestException("不能标记完成非自己接收的订单");
        }
        // 修改任务订单表的状态，改为 DELIVERY完成
        taskOrdersPO.setStatus(TaskOrdersPO.Status.DELIVERY);
        updateById(taskOrdersPO);


        return new TaskOrdersVO(taskOrderId, taskOrdersPO.getStatus());
    }

    @Override
    public TaskOrdersVO confirmFinishTaskOrder(Long taskOrderId) {
        /**
         * 雇主确认完成任务订单
         * 1、确认完成者必须是任务发布者
         * 2、任务订单存在 且只能是 DELIVERY已交付
         *
         * 1、修改任务表对应记录状态为 FINISH完成
         * 2、修改任务订单表对应记录状态为 FINISH完成
         * 3、todo: 订单完成，通知接单者 taskOrdersPO.getReceiverId() 雇主已确认任务完成
         */

        // 先判断这个单是否存在
        TaskOrdersPO taskOrdersPO = getOne(new LambdaQueryWrapper<TaskOrdersPO>().eq(TaskOrdersPO::getId, taskOrderId));
        if(taskOrdersPO==null) throw new NotExistsException("该任务订单不存在");
        if(taskOrdersPO.getStatus() != TaskOrdersPO.Status.DELIVERY){
            throw new BadRequestException("任务状态异常");
        }
        // 1、获取用户id
        Long currentId = BaseContext.getId();
        // 判断这个任务是不是当前用户发布的
        TasksPO taskPO = yujiTasksService.getOne(new LambdaQueryWrapper<TasksPO>().eq(TasksPO::getId, taskOrdersPO.getTaskId()));
        if(!currentId.equals(taskPO.getPublisherId())){
            throw new BadRequestException("不能标记完成非自己发布的任务");
        }
        // 修改任务订单表的状态，改为 完成(接单者)
        taskOrdersPO.setStatus(TaskOrdersPO.Status.FINISH);
        updateById(taskOrdersPO);
        // 修改任务表中记录的状态
        taskPO.setStatus(TasksPO.Status.FINISH);
        yujiTasksService.updateById(taskPO);
        // 删除缓存
        stringRedisTemplate.delete(TaskConstant.TASK_DETAIL_PREFIX + taskPO.getId());
        return new TaskOrdersVO(taskOrderId, taskOrdersPO.getStatus());
    }

    @Override
    @Transactional
    public PageResult<ListTaskOrdersVO> getListOrders(Integer page, Integer size, TaskOrdersPO.Status status, TaskReviewsPO.RoleType roleType) {
        // 构造Page类
        Long currentId = BaseContext.getId();
        Page<ListTaskOrdersVO> pageSet = new Page<>(page,size);
        // 自定义sql进行查询
        Page<ListTaskOrdersVO> pageInfo = yujiTaskOrdersMapper.getListOrders(pageSet,status,roleType,currentId);
        // 返回结果
        return  new PageResult(pageInfo.getRecords(),page,size,pageInfo.getTotal());
    }


}
