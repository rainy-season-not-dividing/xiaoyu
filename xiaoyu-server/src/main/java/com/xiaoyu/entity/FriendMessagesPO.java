package com.xiaoyu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 好友私信表
 */
@Data
@TableName("friend_messages")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendMessagesPO {

    /** 私信 ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 发送者 UID */
    private Long fromId;

    /** 接收者 UID */
    private Long toId;

    /** 消息内容 */
    private String content;

    /** 发送时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}