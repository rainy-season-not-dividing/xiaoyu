package com.xiaoyu.dto.user;


import lombok.Data;

@Data
public class BindMobileDTO {
    /**
     * 手机号
     */
    private String mobile;
    /**
     * 验证码
     */
    private String code;
}
