package com.xiaoyu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 标签字典表
 */
@Data
@TableName("tags")
public class TagsPO {

    /** 标签 ID */
    @TableId(type = IdType.AUTO)
    private Integer id;

    /** 标签名称 */
    private String name;


    /** 后台排序权重 */
    private Integer weight;

    /** 是否热门：0否 1是 */
    private Integer isHot;


}