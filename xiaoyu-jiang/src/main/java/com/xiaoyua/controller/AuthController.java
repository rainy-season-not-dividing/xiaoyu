package com.xiaoyua.controller;

import com.xiaoyua.context.BaseContext;
import com.xiaoyua.dto.auth.PasswordLoginDTO;
import com.xiaoyua.result.Result;
import com.xiaoyua.service.jAuthService;
import com.xiaoyua.vo.auth.LoginVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final jAuthService jAuthService;

    /**
     * 用户名密码登录
     * @param loginDTO 登录请求参数
     * @return 登录结果
     */
    @PostMapping("/password/login")
    public Result<LoginVO> passwordLogin(@RequestBody PasswordLoginDTO loginDTO) {
        try {
            // 基本参数验证
            if (loginDTO.getAccount() == null || loginDTO.getAccount().trim().isEmpty()) {
                return Result.error("用户名不能为空");
            }
            if (loginDTO.getPassword() == null || loginDTO.getPassword().trim().isEmpty()) {
                return Result.error("密码不能为空");
            }
            if (loginDTO.getAccount().length() < 3 || loginDTO.getAccount().length() > 50) {
                return Result.error("用户名长度必须在3-50个字符之间");
            }
            if (loginDTO.getPassword().length() < 6 || loginDTO.getPassword().length() > 20) {
                return Result.error("密码长度必须在6-20个字符之间");
            }

            LoginVO loginVO = jAuthService.passwordLogin(loginDTO);


            log.info("用户登录成功: {}", loginDTO.getAccount());
            return Result.success(loginVO);
        } catch (Exception e) {
            log.error("用户登录失败: {}, 错误: {}", loginDTO.getAccount(), e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 用户注册
     * @param params 密码
     * @return 注册结果
     */
    @PostMapping("/register")
    public Result<LoginVO> register(@RequestBody Map<String,Object> params) {
        String account = (String) params.get("account");
        String password = (String) params.get("password");
        try {
            // 基本参数验证
            if (account == null || account.trim().isEmpty()) {
                return Result.error("用户名不能为空");
            }
            if (password == null || password.trim().isEmpty()) {
                return Result.error("密码不能为空");
            }
            if (account.length() < 3 || account.length() > 50) {
                return Result.error("用户名长度必须在3-50个字符之间");
            }
            if (password.length() < 6 || password.length() > 20) {
                return Result.error("密码长度必须在6-20个字符之间");
            }

            LoginVO loginVO = jAuthService.register(account, password);
            log.info("用户注册成功: {}", account);
            return Result.success(loginVO);
        } catch (Exception e) {
            log.error("用户注册失败: {}, 错误: {}", account, e.getMessage());
            return Result.error(e.getMessage());
        }
    }

    /**
     * 退出登录
     */
    @PostMapping("/logout")
    public Result<String> logout() {
        return Result.success("退出登录成功");
    }
}
