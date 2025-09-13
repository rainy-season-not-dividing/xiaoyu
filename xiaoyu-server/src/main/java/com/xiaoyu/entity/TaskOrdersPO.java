package com.xiaoyu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 任务订单表
 */
@Data
@TableName("task_orders")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskOrdersPO {

    /** 订单主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 任务 ID */
    private Long taskId;

    /** 接单者 UID */
    private Long receiverId;

    /** 订单状态 */
    @EnumValue
    private Status status;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    public enum Status {
        WAIT_ACCEPT, ACCEPTED, REFUSED, CANCELLED,DELIVERY, FINISH
    }
}