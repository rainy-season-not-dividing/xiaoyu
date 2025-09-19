package com.xiaoyua.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xiaoyua.context.BaseContext;
import com.xiaoyua.dto.auth.PasswordLoginDTO;
import com.xiaoyua.entity.UserAuthPO;
import com.xiaoyua.entity.UserPO;
import com.xiaoyua.mapper.jUserAuthMapper;
import com.xiaoyua.mapper.jUserMapper;
import com.xiaoyua.service.jAuthService;
import com.xiaoyua.utils.JwtUtil;
import com.xiaoyua.utils.PasswordUtil;
import com.xiaoyua.vo.auth.LoginVO;
import com.xiaoyua.vo.user.UserSimpleVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 认证服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class jAuthServiceImpl implements jAuthService {

    private final jUserMapper jUserMapper;
    private final jUserAuthMapper jUserAuthMapper;
    private final JwtUtil jwtUtil;

    @Override
    public LoginVO passwordLogin(PasswordLoginDTO loginDTO) {
        // 1. 根据用户名查找用户认证信息
        LambdaQueryWrapper<UserAuthPO> authQuery = new LambdaQueryWrapper<>();
        authQuery.eq(UserAuthPO::getIdentityType, UserAuthPO.IdentityType.PASSWORD)
                .eq(UserAuthPO::getAccount, loginDTO.getAccount());
        
        UserAuthPO userAuth = jUserAuthMapper.selectOne(authQuery);

        if (userAuth == null) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 2. 验证密码
        if (!PasswordUtil.matches(loginDTO.getPassword(), userAuth.getCredential())) {
            throw new RuntimeException("用户名或密码错误");
        }

        // 3. 查询用户信息
        UserPO user = jUserMapper.selectById(userAuth.getUserId());
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        // 4. 检查用户状态
        if (user.getStatus() != 0) {
            throw new RuntimeException("账号已被封禁");
        }

        // 5. 更新验证时间
        userAuth.setVerifiedAt(LocalDateTime.now());
        jUserAuthMapper.updateById(userAuth);

        // 6. 生成JWT令牌
        String accessToken = jwtUtil.generateToken(user.getId());
        String refreshToken = jwtUtil.generateToken(user.getId()); // 使用相同方法生成刷新令牌
        Long expiresIn = 7200L; // 2小时过期时间

        // 7. 构建用户简单信息
        UserSimpleVO userSimpleVO = new UserSimpleVO();
        userSimpleVO.setId(user.getId());
        userSimpleVO.setNickname(user.getNickname());
        userSimpleVO.setAvatarUrl(user.getAvatarUrl());
        userSimpleVO.setGender(user.getGender());

        // 7.1 设置用户id
        BaseContext.setCurrentId(user.getId());
        log.info("set user id: {}", BaseContext.getCurrentId());

        // 8. 返回登录结果
        return new LoginVO(accessToken, refreshToken, expiresIn, userSimpleVO);
    }

    @Override
    @Transactional
    public LoginVO register(String account, String password) {
        // 1. 检查用户名是否已存在
        LambdaQueryWrapper<UserAuthPO> authQuery = new LambdaQueryWrapper<>();
        authQuery.eq(UserAuthPO::getIdentityType, UserAuthPO.IdentityType.PASSWORD)
                .eq(UserAuthPO::getIdentifier, account);
        
        UserAuthPO existingAuth = jUserAuthMapper.selectOne(authQuery);
        if (existingAuth != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 2. 创建用户
        UserPO user = new UserPO();
        user.setNickname(account);
        user.setGender(0);
        user.setStatus(0);
        user.setIsRealName(0);
        user.setPrivacyMobile(0);
        user.setPrivacyBirthday(0);
        user.setPrivacyFans(0);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        
        jUserMapper.insert(user);

        // 3. 创建用户认证信息
        UserAuthPO userAuth = new UserAuthPO();
        userAuth.setAccount(account);
        userAuth.setUserId(user.getId());
        userAuth.setIdentityType(UserAuthPO.IdentityType.PASSWORD);
        userAuth.setIdentifier(account);
        userAuth.setCredential(PasswordUtil.encode(password));
        userAuth.setVerifiedAt(LocalDateTime.now());
        
        jUserAuthMapper.insert(userAuth);

        // 4. 生成JWT令牌
        String accessToken = jwtUtil.generateToken(user.getId());
        String refreshToken = jwtUtil.generateToken(user.getId()); // 使用相同方法生成刷新令牌
        Long expiresIn = 7200L; // 2小时过期时间

        // 5. 构建用户简单信息
        UserSimpleVO userSimpleVO = new UserSimpleVO();
        userSimpleVO.setId(user.getId());
        userSimpleVO.setNickname(user.getNickname());
        userSimpleVO.setAvatarUrl(user.getAvatarUrl());
        userSimpleVO.setGender(user.getGender());

        // 6. 返回注册结果
        return new LoginVO(accessToken, refreshToken, expiresIn, userSimpleVO);
    }
}
