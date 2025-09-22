package com.xiaoyua.entity;

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
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TaskFilePO {

    /** 主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 任务 ID */
    private Long taskId;

    /** 文件 ID */
    private Long fileId;
}