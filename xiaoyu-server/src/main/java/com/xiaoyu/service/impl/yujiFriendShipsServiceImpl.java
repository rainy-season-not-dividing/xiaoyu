package com.xiaoyu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaoyu.context.BaseContext;
import com.xiaoyu.dto.friend.SendFriendRequestDTO;
import com.xiaoyu.entity.BlacklistsPO;
import com.xiaoyu.entity.FriendMessagesPO;
import com.xiaoyu.entity.FriendshipsPO;
import com.xiaoyu.exception.AlreadyBeFriendException;
import com.xiaoyu.exception.AlreadySendFriendShipRequestException;
import com.xiaoyu.mapper.yujiFriendShipsMapper;
import com.xiaoyu.result.PageResult;
import com.xiaoyu.service.yujiBlacklistsService;
import com.xiaoyu.service.yujiFriendMessagesService;
import com.xiaoyu.service.yujiFriendShipsService;
import com.xiaoyu.vo.friend.FriendlistVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Slf4j
public class yujiFriendShipsServiceImpl extends ServiceImpl<yujiFriendShipsMapper, FriendshipsPO> implements yujiFriendShipsService {

    @Resource
    private yujiFriendMessagesService yujiFriendMessagesService;

    @Resource
    private yujiBlacklistsService yujiBlacklistsService;

    @Resource
    private yujiFriendShipsMapper yujiFriendShipsMapper;


    @Override
    @Transactional
    public void  sendFriendRequest(SendFriendRequestDTO sendFriendRequestDTO) {
        // 获取当前用户id
        Long currentId = BaseContext.getId();
        // 获取目标用户id
        Long friendId =  sendFriendRequestDTO.getFriendId();
        FriendMessagesPO friendMessagesPO = new FriendMessagesPO();
        friendMessagesPO.setFromId(currentId);
        friendMessagesPO.setToId(friendId);
        friendMessagesPO.setContent(sendFriendRequestDTO.getMessage());
        yujiFriendMessagesService.save(friendMessagesPO);
        // todo:发送好友申请通知
        // 创建好友关系
        // 判断是否已经是好友关系
        FriendshipsPO friendshipPO = getOne(new LambdaQueryWrapper<>(FriendshipsPO.class)
                .eq(FriendshipsPO::getUserId,Math.min(currentId,friendId))
                .eq(FriendshipsPO::getFriendId,Math.max(currentId,friendId)));

        // 是否被拉黑了
        if(yujiBlacklistsService.getOne(new LambdaQueryWrapper<>(BlacklistsPO.class).eq(BlacklistsPO::getTargetId,currentId).eq(BlacklistsPO::getOwnerId,friendId))!=null){
            throw new AlreadyBeFriendException("对方不接受你的消息");
        }

        // 是否已经是好友或者申请好友了
        if(friendshipPO != null){
            // 处理已经是好友过了的逻辑
            if(friendshipPO.getStatus() == FriendshipsPO.Status.ACCEPTED) throw new AlreadyBeFriendException("已经是好友啦");
            if(friendshipPO.getStatus() == FriendshipsPO.Status.PENDING) throw new AlreadySendFriendShipRequestException("已经申请好友了");
        }

        // 被拒绝或是被删除过
        FriendshipsPO friendshipsPO = FriendshipsPO.builder()
                .id(friendshipPO.getId())
                .userId(Math.min(currentId,friendId))
                .requesterId(currentId)
                .status(FriendshipsPO.Status.PENDING)
                .friendId(Math.max(currentId,friendId)).build();
        save(friendshipsPO);
    }

    @Override
    public void acceptFriendRequest(Long friendId) {
        // 获取当前用户id
        Long currentId = BaseContext.getId();
        // 构建wrapper
        LambdaUpdateWrapper<FriendshipsPO> updateWrapper = new LambdaUpdateWrapper<>(FriendshipsPO.class)
                .eq(FriendshipsPO::getUserId,Math.min(currentId,friendId))
                .eq(FriendshipsPO::getFriendId,Math.max(currentId,friendId))
                .eq(FriendshipsPO::getStatus,FriendshipsPO.Status.PENDING)
                .set(FriendshipsPO::getStatus,FriendshipsPO.Status.ACCEPTED);
        // update
        update(updateWrapper);
    }

    @Override
    public void refuseFriendRequest(Long friendId) {
        // 获取当前用户id
        Long currentId = BaseContext.getId();
        // 构建wrapper
        LambdaUpdateWrapper<FriendshipsPO> updateWrapper = new LambdaUpdateWrapper<>(FriendshipsPO.class)
                .eq(FriendshipsPO::getUserId,Math.min(currentId,friendId))
                .eq(FriendshipsPO::getFriendId,Math.max(currentId,friendId))
                .eq(FriendshipsPO::getStatus,FriendshipsPO.Status.PENDING)
                .set(FriendshipsPO::getStatus,FriendshipsPO.Status.REFUSED);
        // update
        update(updateWrapper);
    }

    @Override
    public void deleteFriendships(Long friendId) {
        // 获取当前用户id
        Long currentId = BaseContext.getId();
        // 构建wrapper
        LambdaUpdateWrapper<FriendshipsPO> updateWrapper = new LambdaUpdateWrapper<>(FriendshipsPO.class)
                .eq(FriendshipsPO::getUserId,Math.min(currentId,friendId))
                .eq(FriendshipsPO::getFriendId,Math.max(currentId,friendId))
                .eq(FriendshipsPO::getStatus,FriendshipsPO.Status.PENDING)
                .set(FriendshipsPO::getStatus,FriendshipsPO.Status.DELETED);
        // update
        update(updateWrapper);
    }

    @Override
    public PageResult<FriendlistVO> getFriendlist(Integer page, Integer pageSize, FriendshipsPO.Status status) {
        // 前端协调 "，accepted：不要分页查询   pending： 分页查询"
        // userID
        Long userId = BaseContext.getId();
        log.info("当前的userId:{}",userId);
        PageResult<FriendlistVO> pageResult = null;
        if(status == FriendshipsPO.Status.ACCEPTED){
            List<FriendlistVO> pageInfo = yujiFriendShipsMapper.getFriendlist(userId,status);
            pageResult = new PageResult<>(pageInfo,1,1, (long) pageInfo.size());
        }
        else{
            // 设置page
            Page<FriendlistVO> pageSet = new Page<>(page,pageSize);
            // 自定义sql语句，得到结果的page
            Page<FriendlistVO> pageInfo = yujiFriendShipsMapper.getFriendlist(pageSet,userId,status);
            // 封装成PageResult类型并返回
            pageResult = new PageResult<>(pageInfo.getRecords(),page,pageSize,pageInfo.getTotal());
        }
        return pageResult;
    }
}
