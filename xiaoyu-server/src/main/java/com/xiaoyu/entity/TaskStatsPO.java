package com.xiaoyu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 任务统计表
 */
@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor
@TableName("task_stats")
public class TaskStatsPO {

    /** 任务 ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long taskId;

    /** 浏览量 */
    private Integer viewCnt;

    /** 接单数 */
    private Integer orderCnt;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}