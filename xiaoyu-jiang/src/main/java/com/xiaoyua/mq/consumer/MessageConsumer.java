package com.xiaoyua.mq.consumer;

import com.xiaoyua.config.MessageQueueConstants;
import com.xiaoyua.dto.message.ForwardMessageContent;
import com.xiaoyua.dto.message.MessageContent;
import com.xiaoyua.dto.message.TextMessageContent;
import com.xiaoyua.entity.MessagePO;
import com.xiaoyua.mapper.jMessageMapper;
import com.xiaoyua.mq.message.PrivateMessage;
import com.xiaoyua.mq.producer.MessageProducer;
import com.xiaoyua.service.ForwardMessageService;
import com.xiaoyua.service.jOfflineMessageService;
import com.xiaoyua.utils.MessageContentUtil;
import com.xiaoyua.websocket.UnifiedWebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.HashMap;

/**
 * 私信消息消费者
 *
 * @author xiaoyu
 */
@Component
@Slf4j
public class MessageConsumer {

    @Autowired
    private UnifiedWebSocketHandler webSocketHandler;

    @Autowired
    private MessageProducer messageProducer;

    @Autowired
    private jOfflineMessageService jOfflineMessageService;

    @Autowired
    private jMessageMapper jMessageMapper;

    @Autowired
    private ForwardMessageService forwardMessageService;

    /**
     * 处理私信推送消息
     */
    @RabbitListener(queues = MessageQueueConstants.MESSAGE_PUSH_QUEUE)
    public void handleMessagePush(PrivateMessage message) {
        try {
            log.info("处理私信推送消息: messageId={}, fromUserId={}, toUserId={}, messageType={}",
                    message.getMessageId(), message.getFromUserId(), message.getToUserId(), message.getMessageType());

            // 首先将消息存储到数据库
            saveMessageToDatabase(message);

            // 检查接收者是否在线
            if (webSocketHandler.isUserOnline(message.getToUserId())) {
                // 用户在线，直接推送
                pushToWebSocket(message);
                log.info("私信实时推送成功: messageId={}, toUserId={}, messageType={}",
                        message.getMessageId(), message.getToUserId(), message.getMessageType());
            } else {
                // 用户离线，记录日志
                log.info("用户离线，消息已存储: userId={}, messageId={}, messageType={}",
                        message.getToUserId(), message.getMessageId(), message.getMessageType());
            }

        } catch (Exception e) {
            log.error("处理私信推送消息失败: messageId={}, fromUserId={}, toUserId={}, error={}",
                    message.getMessageId(), message.getFromUserId(), message.getToUserId(), e.getMessage(), e);

            // 不再抛出异常，避免重复重试
            // 发送失败就失败，记录错误日志即可
            log.warn("私信推送失败，消息将被丢弃: messageId={}, toUserId={}",
                    message.getMessageId(), message.getToUserId());
        }
    }

//    /**
//     * 处理离线消息存储
//     */
//    @RabbitListener(queues = MessageQueueConstants.OFFLINE_MESSAGE_QUEUE)
//    public void handleOfflineMessageStorage(PrivateMessage message) {
//        try {
//            log.info("处理离线消息存储: messageId={}, toUserId={}, messageType={}",
//                    message.getMessageId(), message.getToUserId(), message.getMessageType());
//
//            // TODO: 实现离线消息存储逻辑
//            // 这里暂时只记录日志，后续会实现OfflineMessageService
//            log.info("离线消息已存储: messageId={}, toUserId={}, messageType={}, content={}",
//                    message.getMessageId(), message.getToUserId(), message.getMessageType(),
//                    truncateContent(message.getContent(), 50));
//
//        } catch (Exception e) {
//            log.error("处理离线消息存储失败: messageId={}, toUserId={}, error={}",
//                    message.getMessageId(), message.getToUserId(), e.getMessage(), e);
//
//            // 不再抛出异常，避免重复重试
//            // 离线消息存储失败就失败，记录错误日志即可
//            log.warn("离线消息存储失败，消息将被丢弃: messageId={}, toUserId={}",
//                    message.getMessageId(), message.getToUserId());
//        }
//    }

    /**
     * 推送到WebSocket
     */
    private void pushToWebSocket(PrivateMessage message) {
        try {
            // 构建基本消息对象
            Map<String, Object> wsMessage = new java.util.HashMap<>();
            wsMessage.put("type", "private_message");
            wsMessage.put("message_id", message.getOriginalMessageId());
            wsMessage.put("from_user_id", message.getFromUserId());
            wsMessage.put("from_user_nickname",
                    message.getFromUserNickname() != null ? message.getFromUserNickname() : "");
            wsMessage.put("from_user_avatar", message.getFromUserAvatar() != null ? message.getFromUserAvatar() : "");
            wsMessage.put("content", message.getContent()); // 直接使用普通文本内容
            wsMessage.put("ref_type", message.getMessageType() != null ? message.getMessageType() : "TEXT");
            wsMessage.put("created_at",
                    message.getCreatedAt().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            wsMessage.put("timestamp", System.currentTimeMillis());

            // 如果是转发消息，添加转发项目的基本信息
            if ("POST".equals(message.getMessageType()) || "TASK".equals(message.getMessageType())) {
//                wsMessage.put("forward_item_type", message.getMessageType());
                wsMessage.put("forward_item_id", message.getForwardItemId() != null ? message.getForwardItemId() : 0);

                // 获取转发项目的基本信息（不是完整JSON）
                try {
                    Map<String, Object> itemInfo = getForwardItemBasicInfo(message.getMessageType(), message.getForwardItemId());
                    wsMessage.put("forward_item_info", itemInfo);
                } catch (Exception e) {
                    log.warn("获取转发项目基本信息失败: messageId={}, itemType={}, itemId={}, error={}",
                            message.getMessageId(), message.getMessageType(), message.getForwardItemId(), e.getMessage());
                    wsMessage.put("forward_item_info", null);
                }
            }

            WebSocketSession session = webSocketHandler.getUserSession(message.getToUserId());
            boolean sent = false;
            if (session != null && session.isOpen()) {
                try {
                    String messageJson = new com.fasterxml.jackson.databind.ObjectMapper()
                            .writeValueAsString(wsMessage);
                    session.sendMessage(new org.springframework.web.socket.TextMessage(messageJson));
                    sent = true;
                } catch (Exception e) {
                    log.error("WebSocket发送失败: {}", e.getMessage());
                }
            }

            if (!sent) {
                log.warn("WebSocket推送失败，用户可能已离线: toUserId={}, messageId={}",
                        message.getToUserId(), message.getMessageId());
                // 推送失败，转为离线存储
                messageProducer.sendOfflineMessageStorage(message);
            }

        } catch (Exception e) {
            log.error("WebSocket推送异常: toUserId={}, messageId={}, error={}",
                    message.getToUserId(), message.getMessageId(), e.getMessage(), e);
            // 推送异常，转为离线存储
            messageProducer.sendOfflineMessageStorage(message);
        }
    }


    /**
     * 将消息存储到数据库
     */
    private void saveMessageToDatabase(PrivateMessage message) {
        try {
            log.info("保存消息到数据库: messageId={}, fromUserId={}, toUserId={}, messageType={}",
                    message.getOriginalMessageId(), message.getFromUserId(), message.getToUserId(), message.getMessageType());

            // 创建MessagePO实体
            MessagePO messagePO = new MessagePO();
            // 不设置ID，让数据库自动生成
            messagePO.setFromId(message.getFromUserId());
            messagePO.setToId(message.getToUserId());
            messagePO.setType(MessagePO.MessageType.valueOf(message.getMessageType()));

            // 根据消息类型处理content
            String dbContent;
            if ("POST".equals(message.getMessageType()) || "TASK".equals(message.getMessageType())) {
                // 转发消息：需要重新构建完整的JSON格式
                try {
                    MessageContent forwardContent;
                    if ("POST".equals(message.getMessageType())) {
                        forwardContent = forwardMessageService.createPostForwardContent(message.getForwardItemId());
                    } else {
                        forwardContent = forwardMessageService.createTaskForwardContent(message.getForwardItemId());
                    }
                    dbContent = MessageContentUtil.toJsonString(forwardContent);
                    log.info("转发消息JSON内容已重新构建: messageId={}, itemId={}",
                            message.getOriginalMessageId(), message.getForwardItemId());
                } catch (Exception e) {
                    log.error("构建转发消息JSON失败，使用原始内容: messageId={}, error={}",
                            message.getOriginalMessageId(), e.getMessage());
                    // 如果构建失败，包装原始内容为文本消息
                    TextMessageContent textContent = new TextMessageContent(message.getContent());
                    dbContent = MessageContentUtil.toJsonString(textContent);
                }
            } else {
                // 文本消息：包装成TextMessageContent的JSON格式
                TextMessageContent textContent = new TextMessageContent(message.getContent());
                dbContent = MessageContentUtil.toJsonString(textContent);
            }

            messagePO.setContent(dbContent);
            messagePO.setCreatedAt(message.getCreatedAt());

            // 插入数据库
            int inserted = jMessageMapper.insert(messagePO);

            if (inserted > 0) {
                log.info("消息保存成功: 数据库ID={}, 原始messageId={}", messagePO.getId(), message.getOriginalMessageId());
            } else {
                log.error("消息保存失败: 原始messageId={}", message.getOriginalMessageId());
            }

        } catch (Exception e) {
            log.error("保存消息到数据库失败: messageId={}, error={}",
                    message.getOriginalMessageId(), e.getMessage(), e);
            throw new RuntimeException("保存消息失败", e);
        }
    }

    /**
     * 获取转发项目的基本信息（用于WebSocket推送）
     */
    private Map<String, Object> getForwardItemBasicInfo(String itemType, Long itemId) {
        Map<String, Object> itemInfo = new HashMap<>();

        try {
            if ("POST".equals(itemType)) {
                // 获取动态基本信息
                var forwardContent = forwardMessageService.createPostForwardContent(itemId);
                if (forwardContent != null) {
                    itemInfo.put("title", forwardContent.getTitle() != null ? forwardContent.getTitle() : "");
                    itemInfo.put("author_nickname", forwardContent.getAuthor() != null ? forwardContent.getAuthor().getNickname() : "");
                    itemInfo.put("content_preview", truncateContent(forwardContent.getContent(), 50));
                    itemInfo.put("author_avatar_url",forwardContent.getAuthor().getAvatarUrl());

                    // 获取第一个文件的路径用于预览
                    if (forwardContent.getFiles() != null && !forwardContent.getFiles().isEmpty()) {
                        ForwardMessageContent.ForwardFile firstFile = forwardContent.getFiles().get(0);
                        itemInfo.put("preview_file_url", firstFile.getUrl());
                        itemInfo.put("preview_thumbnail_url", firstFile.getThumbnailUrl());
                        itemInfo.put("preview_file_type", firstFile.getFileType());
                    } else {
                        itemInfo.put("preview_file_url", null);
                        itemInfo.put("preview_thumbnail_url", null);
                        itemInfo.put("preview_file_type", null);
                    }
                }
            } else if ("TASK".equals(itemType)) {
                // 获取任务基本信息
                var forwardContent = forwardMessageService.createTaskForwardContent(itemId);
                if (forwardContent != null) {
                    itemInfo.put("title", forwardContent.getTitle() != null ? forwardContent.getTitle() : "");
                    itemInfo.put("author_nickname", forwardContent.getAuthor() != null ? forwardContent.getAuthor().getNickname() : "");
                    itemInfo.put("content_preview", truncateContent(forwardContent.getContent(), 50));
                    itemInfo.put("author_avatar_url",forwardContent.getAuthor().getAvatarUrl());

                    // 获取第一个文件的路径用于预览
                    if (forwardContent.getFiles() != null && !forwardContent.getFiles().isEmpty()) {
                        ForwardMessageContent.ForwardFile firstFile = forwardContent.getFiles().get(0);
                        itemInfo.put("preview_file_url", firstFile.getUrl());
                        itemInfo.put("preview_thumbnail_url", firstFile.getThumbnailUrl());
                        itemInfo.put("preview_file_type", firstFile.getFileType());
                    } else {
                        itemInfo.put("preview_file_url", null);
                        itemInfo.put("preview_thumbnail_url", null);
                        itemInfo.put("preview_file_type", null);
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取转发项目基本信息失败: itemType={}, itemId={}, error={}",
                    itemType, itemId, e.getMessage());
        }

        return itemInfo;
    }

    /**
     * 截断内容用于预览
     */
    private String truncateContent(String content, int maxLength) {
        if (content == null || content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength) + "...";
    }
}
