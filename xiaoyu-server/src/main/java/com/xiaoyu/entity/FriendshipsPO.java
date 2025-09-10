package com.xiaoyu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 好友关系表
 */
@Data
@TableName("friendships")
@Builder
public class FriendshipsPO {

    /** 好友关系 ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户 UID */
    private Long userId;

    /** 好友 UID */
    private Long friendId;

    /** 好友申请人的id */
    private Long requesterId;

    /** 好友状态 */
    @EnumValue
    private Status status;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(fill = FieldFill.UPDATE)
    private LocalDateTime updatedAt;

    public enum Status {
        PENDING, ACCEPTED, REFUSED, DELETED
    }
}