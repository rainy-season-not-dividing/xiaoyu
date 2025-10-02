package com.xiaoyua.vo.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.xiaoyua.entity.CampusPO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户搜索结果VO
 * 用于返回用户搜索结果的基本信息
 *
 * @author xiaoyu
 */
@Data
@Schema(description = "用户搜索结果")
public class UserSearchVO {

    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1")
    private Long id;

    /**
     * 用户账号
     */
    @Schema(description = "用户账号", example = "1234567890")
    private String account;

    /**
     * 用户昵称
     */
    @Schema(description = "用户昵称", example = "张三")
    private String nickname;

    /**
     * 头像URL
     */
    @JsonProperty("avatar_url")
    @Schema(description = "头像URL", example = "https://example.com/avatar.jpg")
    private String avatarUrl;

    /**
     * 性别：0-未知，1-男，2-女
     */
    @Schema(description = "性别：0-未知，1-男，2-女", example = "1")
    private Integer gender;

    /**
     * 校区ID
     */
    @JsonProperty("campus_id")
    @Schema(description = "校区ID", example = "1")
    private Long campusId;

    /**
     * 校区名称
     */
    @JsonProperty("campus_name")
    @Schema(description = "校区名称", example = "北京校区")
    private CampusPO.CampuseName campusName;

    /**
     * 是否实名认证：0-未认证，1-已认证
     */
    @JsonProperty("is_real_name")
    @Schema(description = "是否实名认证：0-未认证，1-已认证", example = "1")
    private Integer isRealName;

    /**
     * 注册时间
     */
    @JsonProperty("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "注册时间", example = "2024-01-01 12:00:00")
    private LocalDateTime createdAt;

    /**
     * 搜索匹配类型：account-账号匹配，nickname-昵称匹配
     */
    @JsonProperty("match_type")
    @Schema(description = "搜索匹配类型", example = "nickname")
    private String matchType;

    /**
     * 是否为好友
     */
    @JsonProperty("is_friend")
    @Schema(description = "是否为好友", example = "false")
    private Boolean isFriend = false;
}
