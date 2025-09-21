package com.xiaoyu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 话题-动态关联表
 */
@Data
@TableName("topic_posts")
public class TopicPostsPO {

    /** 主键 ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 话题 ID */

    private Long topicId;

    /** 动态 ID */

    private Long postId;
}