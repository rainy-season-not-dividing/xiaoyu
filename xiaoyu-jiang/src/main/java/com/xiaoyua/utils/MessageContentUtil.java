package com.xiaoyua.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaoyua.dto.message.MessageContent;
import com.xiaoyua.dto.message.TextMessageContent;
import com.xiaoyua.dto.message.ForwardMessageContent;
import com.xiaoyua.dto.message.PostForwardMessageContent;
import com.xiaoyua.dto.message.TaskForwardMessageContent;
import com.xiaoyua.entity.MessagePO;
import lombok.extern.slf4j.Slf4j;

/**
 * 消息内容工具类
 *
 * @author xiaoyu
 */
@Slf4j
public class MessageContentUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 将MessageContent转换为JSON字符串
     */
    public static String toJsonString(MessageContent content) {
        try {
            return objectMapper.writeValueAsString(content);
        } catch (JsonProcessingException e) {
            log.error("MessageContent转JSON失败", e);
            throw new RuntimeException("消息内容序列化失败", e);
        }
    }

    /**
     * 从JSON字符串解析MessageContent
     */
    public static MessageContent fromJsonString(String json, MessagePO.MessageType type) {
        try {
            switch (type) {
                case TEXT:
                    return objectMapper.readValue(json, TextMessageContent.class);
                case POST:
                    return objectMapper.readValue(json, PostForwardMessageContent.class);
                case TASK:
                    return objectMapper.readValue(json, TaskForwardMessageContent.class);
                default:
                    throw new IllegalArgumentException("不支持的消息类型: " + type);
            }
        } catch (JsonProcessingException e) {
            log.error("JSON转MessageContent失败: json={}, type={}", json, type, e);
            throw new RuntimeException("消息内容反序列化失败", e);
        }
    }

    /**
     * 从JSON字符串解析MessageContent（根据字符串类型）
     */
    public static MessageContent fromJsonString(String json, String typeStr) {
        try {
            MessagePO.MessageType type = MessagePO.MessageType.valueOf(typeStr);
            return fromJsonString(json, type);
        } catch (IllegalArgumentException e) {
            log.error("不支持的消息类型字符串: {}", typeStr, e);
            throw new RuntimeException("不支持的消息类型: " + typeStr, e);
        }
    }

    /**
     * 创建文本消息内容
     */
    public static TextMessageContent createTextContent(String text) {
        return new TextMessageContent(text);
    }

    /**
     * 获取消息内容的文本表示（用于显示和长度检查）
     */
    public static String getDisplayText(MessageContent content) {
        if (content instanceof TextMessageContent) {
            return ((TextMessageContent) content).getText();
        } else if (content instanceof PostForwardMessageContent) {
            PostForwardMessageContent forward = (PostForwardMessageContent) content;
            return "[转发动态] " + forward.getTitle();
        } else if (content instanceof TaskForwardMessageContent) {
            TaskForwardMessageContent forward = (TaskForwardMessageContent) content;
            return "[转发任务] " + forward.getTitle();
        } else if (content instanceof ForwardMessageContent) {
            // 兼容旧的ForwardMessageContent
            ForwardMessageContent forward = (ForwardMessageContent) content;
            return "[转发] " + forward.getTitle();
        }
        return "[未知消息类型]";
    }

    /**
     * 获取消息内容的长度（用于验证）
     */
    public static int getContentLength(MessageContent content) {
        return getDisplayText(content).length();
    }
}
