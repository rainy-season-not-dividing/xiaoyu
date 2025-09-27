package com.xiaoyua.dto.post;

import lombok.Data;

/**
 * 动态文件批量查询DTO
 */
@Data
public class PostFileDTO {
    /**
     * 动态ID
     */
    private Long postId;

    /**
     * 文件ID
     */
    private Long id;

    /**
     * 文件URL
     */
    private String fileUrl;

    /**
     * 缩略图URL
     */
    private String thumbUrl;

    /**
     * 文件大小
     */
    private Long size;

    /**
     * 业务类型
     */
    private String bizType;
}
