package com.xiaoyua.service.impl;

import com.xiaoyua.entity.UserPO;
import com.xiaoyua.mapper.jUserMapper;
import com.xiaoyua.mq.message.NotificationMessage;
import com.xiaoyua.mq.message.PrivateMessage;
import com.xiaoyua.mq.producer.MessageProducer;
import com.xiaoyua.mq.producer.NotificationProducer;
import com.xiaoyua.service.jPushService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 独立推送服务实现类
 *
 * @author xiaoyu
 */
@Service
@Slf4j
public class jPushServiceImpl implements jPushService {

    @Autowired
    private NotificationProducer notificationProducer;

    @Autowired
    private MessageProducer messageProducer;

    @Autowired
    private jUserMapper jUserMapper;

    @Override
    public void pushNotification(Long userId, String type, String title, String content,
                                 Long refId, String refType, Long fromUserId) {
        try {
            // 不给自己发通知
            if (userId.equals(fromUserId)) {
                return;
            }

            NotificationMessage message = new NotificationMessage(
                    userId, type, title, content, refId, refType, fromUserId);

            // 如果有发送者信息，填充发送者详情
            if (fromUserId != null) {
                UserPO fromUser = jUserMapper.selectById(fromUserId);
                if (fromUser != null) {
                    message.setFromUserNickname(fromUser.getNickname());
                    message.setFromUserAvatar(fromUser.getAvatarUrl());
                }
            }

            notificationProducer.sendNotificationPush(message);

            log.info("推送通知消息成功: userId={}, type={}, fromUserId={}",
                    userId, type, fromUserId);

        } catch (Exception e) {
            log.error("推送通知消息失败: userId={}, type={}, error={}",
                    userId, type, e.getMessage(), e);
        }
    }


    @Override
    public void pushPrivateMessage(Long originalMessageId, Long fromUserId, Long toUserId,
                                   String content, String refType) {
        // 委托给带转发信息的重载方法，转发信息为null
        pushPrivateMessage(originalMessageId, fromUserId, toUserId, content, refType, null);
    }

    @Override
    public void pushPrivateMessage(Long originalMessageId, Long fromUserId, Long toUserId,
                                   String content, String refType, Long forwardItemId) {
        try {
            PrivateMessage message = new PrivateMessage(
                    originalMessageId, fromUserId, toUserId, content);
            // 设置消息类型
            message.setMessageType(refType == null ? "TEXT" : refType);

            // 设置转发信息（仅当messageType为POST或TASK时）
            if ("POST".equals(refType) || "TASK".equals(refType)) {
                message.setForwardItemId(forwardItemId);
            }

            // 填充发送者信息
            UserPO fromUser = jUserMapper.selectById(fromUserId);
            if (fromUser != null) {
                message.setFromUserNickname(fromUser.getNickname());
                message.setFromUserAvatar(fromUser.getAvatarUrl());
            }

            messageProducer.sendMessagePush(message);

            log.info(
                    "推送私信消息成功: originalMessageId={}, fromUserId={}, toUserId={}, messageType={}, forwardItemId={}",
                    originalMessageId, fromUserId, toUserId, message.getMessageType(), forwardItemId);

        } catch (Exception e) {
            log.error(
                    "推送私信消息失败: originalMessageId={}, fromUserId={}, toUserId={}, refType={}, forwardItemId={}, error={}",
                    originalMessageId, fromUserId, toUserId, refType, forwardItemId, e.getMessage(), e);
        }
    }

    @Override
    public void pushLikeNotification(Long toUserId, Long fromUserId, Long itemId, String itemType) {
        UserPO fromUser = jUserMapper.selectById(fromUserId);
        String fromUserNickname = fromUser != null ? fromUser.getNickname() : "某用户";

        String title = "收到新的点赞";
        String content = String.format("%s 点赞了你的%s", fromUserNickname, getContentTypeName(itemType));

        pushNotification(toUserId, "INTERACTION", title, content, itemId, itemType, fromUserId);
    }

    @Override
    public void pushFavoriteNotification(Long toUserId, Long fromUserId, Long itemId, String itemType) {
        UserPO fromUser = jUserMapper.selectById(fromUserId);
        String fromUserNickname = fromUser != null ? fromUser.getNickname() : "某用户";

        String title = "收到新的收藏";
        String content = String.format("%s 收藏了你的%s", fromUserNickname, getContentTypeName(itemType));

        pushNotification(toUserId, "INTERACTION", title, content, itemId, itemType, fromUserId);
    }

    @Override
    public void pushCommentNotification(Long toUserId, Long fromUserId, Long itemId,
                                        String itemType, String commentContent) {
        UserPO fromUser = jUserMapper.selectById(fromUserId);
        String fromUserNickname = fromUser != null ? fromUser.getNickname() : "某用户";

        String title = "收到新的评论";
        String content = String.format("%s 评论了你的%s: %s",
                fromUserNickname, getContentTypeName(itemType),
                truncateContent(commentContent, 50));

        pushNotification(toUserId, "INTERACTION", title, content, itemId, itemType, fromUserId);
    }

    @Override
    public void pushShareNotification(Long toUserId, Long fromUserId, Long itemId, String itemType) {
        log.info("开始推送转发通知: toUserId={}, fromUserId={}, itemId={}, itemType={}",
                toUserId, fromUserId, itemId, itemType);

        UserPO fromUser = jUserMapper.selectById(fromUserId);
        String fromUserNickname = fromUser != null ? fromUser.getNickname() : "某用户";

        String content = String.format("%s 转发了一个%s给你", fromUserNickname, getContentTypeName(itemType));

        log.info("转发通知内容: toUserId={}, content={}", toUserId, content);

        pushPrivateMessage(itemId, fromUserId, toUserId, content, itemType, itemId);

        log.info("转发通知推送完成: toUserId={}, fromUserId={}", toUserId, fromUserId);
    }

    @Override
    public void pushSystemNotification(Long userId, String title, String content) {
        pushNotification(userId, "SYSTEM", title, content,null,null,null);
    }

    @Override
    public void pushAtUserNotification(Long toUserId, Long fromUserId, Long itemId,
                                       String itemType, String commentContent) {
        UserPO fromUser = jUserMapper.selectById(fromUserId);
        String fromUserNickname = fromUser != null ? fromUser.getNickname() : "某用户";

        String title = "有人@了你";
        String content = String.format("%s 在%s中@了你: %s",
                fromUserNickname, getContentTypeName(itemType),
                truncateContent(commentContent, 50));

        pushNotification(toUserId, "INTERACTION", title, content, itemId, itemType, fromUserId);
    }

    @Override
    public void pushFriendRequestNotification(Long toUserId, Long fromUserId, String requestMessage) {
        UserPO fromUser = jUserMapper.selectById(fromUserId);
        String fromUserNickname = fromUser != null ? fromUser.getNickname() : "某用户";

        String title = "收到好友申请";
        String content = String.format("%s 向你发送了好友申请: %s",
                fromUserNickname,
                truncateContent(requestMessage, 50));

        // 使用INTERACTION类型，refType为FRIEND_REQUEST
        pushNotification(toUserId, "INTERACTION", title, content, fromUserId, "CHAT", fromUserId);

        log.info("推送好友申请通知成功: toUserId={}, fromUserId={}", toUserId, fromUserId);
    }

    /**
     * 获取内容类型中文名称
     */
    private String getContentTypeName(String itemType) {
        switch (itemType.toUpperCase()) {
            case "POST":
                return "动态";
            case "COMMENT":
                return "评论";
            case "TASK":
                return "任务";
            default:
                return "内容";
        }
    }

    /**
     * 截断内容
     */
    private String truncateContent(String content, int maxLength) {
        if (content == null || content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength) + "...";
    }
}
