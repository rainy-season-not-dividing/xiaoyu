package com.xiaoyua.service;

import com.xiaoyua.dto.auth.PasswordLoginDTO;
import com.xiaoyua.vo.auth.LoginVO;

/**
 * 认证服务接口
 */
public interface jAuthService {

    /**
     * 用户名密码登录
     * @param loginDTO 登录请求参数
     * @return 登录结果
     */
    LoginVO passwordLogin(PasswordLoginDTO loginDTO);

    /**
     * 用户注册
     * @param password 密码
     * @return 注册结果
     */
    LoginVO register(String account, String password);
}
