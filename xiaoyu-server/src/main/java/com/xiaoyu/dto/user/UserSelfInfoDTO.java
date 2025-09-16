package com.xiaoyu.dto.user;

import lombok.Data;
import java.time.LocalDate;

/**
 * 用户资料修改 DTO
 */
@Data
public class UserSelfInfoDTO {

    /** 昵称（最多30字符） */
    private String nickname;

    /** 头像URL */
    private String avatarUrl;

    /** 生日（YYYY-MM-DD） */
    private LocalDate birthday;

    /** 性别：0未知 1男 2女 */
    private Integer gender;

    /** 校区ID */
    private Long campusId;

    /** 手机号可见范围：0公开 1好友 2仅自己 */
    private Integer privacyMobile;

    /** 生日可见范围 */
    private Integer privacyBirthday;

    /** 粉丝列表可见范围 */
    private Integer privacyFans;
}