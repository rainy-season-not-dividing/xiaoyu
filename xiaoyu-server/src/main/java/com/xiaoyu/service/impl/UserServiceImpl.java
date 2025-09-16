package com.xiaoyu.service.impl;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaoyu.context.BaseContext;
import com.xiaoyu.dto.user.BindMobileDTO;
import com.xiaoyu.dto.user.UserRealNameDTO;
import com.xiaoyu.dto.user.UserSelfInfoDTO;
import com.xiaoyu.entity.UsersPO;
import com.xiaoyu.mapper.UserMapper;
import com.xiaoyu.result.PageResult;
import com.xiaoyu.service.BlacklistsService;
import com.xiaoyu.service.UserService;
import com.xiaoyu.vo.user.BlacklistsVO;
import com.xiaoyu.vo.user.UserVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UsersPO> implements UserService {

    @Resource
    private BlacklistsService blacklistsService;



    @Override
    public UserVO getUserPublicInfo(Long userId) {
        UsersPO userInfo = getById(userId);
        // todo:还要拿到用户粉丝、关注数
        return BeanUtil.copyProperties(userInfo,UserVO.class);
    }

    @Override
    public void updateUserInfo(UserSelfInfoDTO userSelfInfoDTO) {
        // 获取当前用户信息
        Long userId = BaseContext.getId();
        // 封装成PO类
        UsersPO usersPO = BeanUtil.copyProperties(userSelfInfoDTO, UsersPO.class);
        // update更新数据
        saveOrUpdate(usersPO);  // 如果id为空，则新增
    }

    @Override
    public Integer realNameAuth(UserRealNameDTO userRealNameDTO) {
        UsersPO usersPO = BeanUtil.copyProperties(userRealNameDTO, UsersPO.class);
        usersPO.setId(BaseContext.getId());
        updateById(usersPO);
        Integer isRealName = usersPO.getIsRealName();
        // todo: 是否需要返回这个数据
        return isRealName;
    }

    @Override
    public void bindMobileDTO(BindMobileDTO bindMobileDTO) {
        UsersPO usersPO = new UsersPO();
        String mobliePhone = bindMobileDTO.getMobile();
        String code = bindMobileDTO.getCode();
        // todo: 校对验证码
        usersPO.setId(BaseContext.getId());
        usersPO.setMobile(mobliePhone);
        updateById(usersPO);
    }

    @Override
    public PageResult<BlacklistsVO> getBlacklist(Integer page, Integer pageSize) {
        // 获取当前用户信息
        // todo: 待测试
        Long userId = BaseContext.getId();
        // 获取用户黑名单列表，简化逻辑
        Page<BlacklistsVO> blackPageInfo = blacklistsService.getBlackList(new Page<>(page,pageSize),userId);
        return new PageResult<>(blackPageInfo.getRecords(),page,pageSize,blackPageInfo.getTotal());
    }


}
