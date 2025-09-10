package com.xiaoyu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 任务-文件关联表
 */
@Data
@TableName("task_files")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TaskFilesPO {

    /** 主键 ID*/
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 任务 ID */
    private Long taskId;

    /** 文件 ID */
    private Long fileId;
}