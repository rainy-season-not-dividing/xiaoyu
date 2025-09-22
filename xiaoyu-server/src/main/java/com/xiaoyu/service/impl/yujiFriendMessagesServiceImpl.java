package com.xiaoyu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaoyu.context.BaseContext;
import com.xiaoyu.dto.friend.SendMessageDTO;
import com.xiaoyu.entity.FriendMessagesPO;
import com.xiaoyu.mapper.yujiFriendMessagesMapper;
import com.xiaoyu.result.PageResult;
import com.xiaoyu.service.yujiFriendMessagesService;
import com.xiaoyu.vo.message.SendMessageVO;
import org.springframework.stereotype.Service;


@Service
public class yujiFriendMessagesServiceImpl extends ServiceImpl<yujiFriendMessagesMapper, FriendMessagesPO> implements yujiFriendMessagesService {
    @Override
    public SendMessageVO sendMessage(SendMessageDTO message) {
        // 获取当前用户id
        Long currentId = BaseContext.getId();
        // 写表
        FriendMessagesPO friendMessagesPO = FriendMessagesPO.builder()
                .fromId(currentId)
                .toId(message.getToId())
                .content(message.getContent()).build();
        save(friendMessagesPO);
        return SendMessageVO.builder()
                .id(friendMessagesPO.getId())
                .fromId(friendMessagesPO.getFromId())
                .toId(friendMessagesPO.getToId())
                .content(friendMessagesPO.getContent())
                .createdAt(friendMessagesPO.getCreatedAt()).build();
    }

    @Override
    public PageResult<FriendMessagesPO> getMessages(Integer page, Integer size, Long friendId) {
        // 获取当前用户
        Long userId = BaseContext.getId();
        // 设置page信息
        Page<FriendMessagesPO> pageSet = new Page<>(page,size);
        // 获取消息列表
        Page<FriendMessagesPO> pageInfo = this.page(
                pageSet,
                new LambdaQueryWrapper<FriendMessagesPO>()
                        .eq(FriendMessagesPO::getFromId, friendId)
                        .eq(FriendMessagesPO::getToId, userId)
                        .or(qw -> qw.eq(FriendMessagesPO::getFromId, userId)
                                .eq(FriendMessagesPO::getToId, friendId))
                        .orderByDesc(FriendMessagesPO::getCreatedAt)
        );
        // 封装返回
        return new PageResult<>(pageInfo.getRecords(),page,size,pageInfo.getTotal());
    }
}
