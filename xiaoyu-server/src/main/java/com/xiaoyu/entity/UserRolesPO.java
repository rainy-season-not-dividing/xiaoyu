package com.xiaoyu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

/**
 * 用户角色关联表
 */
@Data
@TableName("user_roles")
public class UserRolesPO {

    /** 用户 ID */
    @TableId
    private Long userId;

    /** 角色 ID */
    @TableId
    private Integer roleId;
}