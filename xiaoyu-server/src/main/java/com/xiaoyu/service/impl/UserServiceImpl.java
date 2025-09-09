package com.xiaoyu.service.impl;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaoyu.context.BaseContext;
import com.xiaoyu.dto.BindMobileDTO;
import com.xiaoyu.dto.UserRealNameDTO;
import com.xiaoyu.dto.UserSelfInfoDTO;
import com.xiaoyu.entity.UserPO;
import com.xiaoyu.mapper.UserMapper;
import com.xiaoyu.service.UserService;
import com.xiaoyu.vo.UserVO;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, UserPO> implements UserService {

    @Override
    public UserVO getUserPublicInfo(Long userId) {
        UserPO userInfo = getById(userId);
        // todo:还要拿到用户粉丝、关注数
        return BeanUtil.copyProperties(userInfo,UserVO.class);
    }

    @Override
    public void updateUserInfo(UserSelfInfoDTO userSelfInfoDTO) {
        // 获取当前用户信息
        Long userId = BaseContext.getId();
        // 封装成PO类
        UserPO userPO = BeanUtil.copyProperties(userSelfInfoDTO,UserPO.class);
        // update更新数据
        updateById(userPO);
    }

    @Override
    public Integer realNameAuth(UserRealNameDTO userRealNameDTO) {
        UserPO userPO = BeanUtil.copyProperties(userRealNameDTO,UserPO.class);
        userPO.setId(BaseContext.getId());
        updateById(userPO);
        Integer isRealName = userPO.getIsRealName();
        // todo: 是否需要返回这个数据
        return isRealName;
    }

    @Override
    public void bindMobileDTO(BindMobileDTO bindMobileDTO) {
        UserPO userPO = new UserPO();
        String mobliePhone = bindMobileDTO.getMobile();
        String code = bindMobileDTO.getCode();
        // todo: 校对验证码
        userPO.setId(BaseContext.getId());
        userPO.setMobile(mobliePhone);
        updateById(userPO);
    }


}
