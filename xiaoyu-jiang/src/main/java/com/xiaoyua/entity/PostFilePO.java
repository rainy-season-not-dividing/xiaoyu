package com.xiaoyua.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 动态-文件关联表
 */
@Data
@TableName("post_files")
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PostFilePO {

    /** 动态 ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long postId;

    /** 文件 ID */
    private Long fileId;

    /** 顺序号 */
    private Integer sort;
}