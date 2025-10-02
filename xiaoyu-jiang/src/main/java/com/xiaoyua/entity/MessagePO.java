package com.xiaoyua.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 好友私信表
 */
@Data
@TableName("friend_messages")
public class MessagePO {

    /** 私信 ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 发送者 UID */
    private Long fromId;

    /** 接收者 UID */
    private Long toId;

    /** 消息类型 */
    @EnumValue
    private MessageType type;

    /** 消息内容（JSON格式） */
    private String content;

    /** 发送时间 */
    private LocalDateTime createdAt;

    /**
     * 消息类型枚举
     */
    public enum MessageType {
        /** 普通文本消息 */
        TEXT,
        /** 动态转发 */
        POST,
        /** 任务转发 */
        TASK
    }
}