package com.xiaoyua.dto.post;

import lombok.Data;

/**
 * 话题批量查询DTO
 */
@Data
public class TopicSimpleDTO {
    /**
     * 动态ID
     */
    private Long postId;

    /**
     * 话题ID
     */
    private Long id;

    /**
     * 话题名称
     */
    private String name;

    /**
     * 话题描述
     */
    private String description;

    /**
     * 动态数量
     */
    private Integer postCnt;
}
