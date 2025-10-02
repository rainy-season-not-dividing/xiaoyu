package com.xiaoyu.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 校区字典表
 */
@Data
@TableName("campuses")
public class CampusPO {

    /** 校区 ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 校区名称 */
    private CampuseName name;

    /** 省 */
    private String province;

    /** 市 */
    private String city;

    /** 详细地址 */
    private String address;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;


    public enum CampuseName{
        NANHU,       // 南湖校区
        YUJIATOU,    // 余家头校区
        MAFANGSHAN;  // 马房山校区
    }
}