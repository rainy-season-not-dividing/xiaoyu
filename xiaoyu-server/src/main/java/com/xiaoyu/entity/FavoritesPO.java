package com.xiaoyu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 收藏表
 */
@Data
@TableName("favorites")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FavoritesPO {

    /** ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 收藏者 UID */
    private Long userId;

    /** 业务对象 ID */
    private Long itemId;

    /** 业务类型 */
    @EnumValue
    private ItemType itemType;

    /** 收藏时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    public enum ItemType {
        POST, TASK
    }
}