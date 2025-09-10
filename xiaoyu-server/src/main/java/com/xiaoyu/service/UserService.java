package com.xiaoyu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaoyu.dto.BindMobileDTO;
import com.xiaoyu.dto.UserRealNameDTO;
import com.xiaoyu.dto.UserSelfInfoDTO;
import com.xiaoyu.entity.UsersPO;
import com.xiaoyu.result.PageResult;
import com.xiaoyu.vo.BlacklistsVO;
import com.xiaoyu.vo.UserVO;

public interface UserService extends IService<UsersPO> {

    UserVO getUserPublicInfo(Long userId);

    void updateUserInfo(UserSelfInfoDTO userSelfInfoDTO);

    Integer realNameAuth(UserRealNameDTO userRealNameDTO);

    void bindMobileDTO(BindMobileDTO bindMobileDTO);

    PageResult<BlacklistsVO> getBlacklist(Integer page, Integer pageSize);
}
