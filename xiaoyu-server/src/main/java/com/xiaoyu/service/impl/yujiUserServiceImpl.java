package com.xiaoyu.service.impl;


import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaoyu.constant.UserConstant;
import com.xiaoyu.context.BaseContext;
import com.xiaoyu.dto.user.BindMobileDTO;
import com.xiaoyu.dto.user.UserRealNameDTO;
import com.xiaoyu.dto.user.UserSelfInfoDTO;
import com.xiaoyu.entity.CampusPO;
import com.xiaoyu.entity.FriendshipsPO;
import com.xiaoyu.entity.UsersPO;
import com.xiaoyu.mapper.yujiUserMapper;
import com.xiaoyu.result.PageResult;
import com.xiaoyu.service.*;
import com.xiaoyu.vo.user.BlacklistsVO;
import com.xiaoyu.vo.user.UserVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@Service
public class yujiUserServiceImpl extends ServiceImpl<yujiUserMapper, UsersPO> implements yujiUserService {

    @Resource
    private yujiBlacklistsService yujiBlacklistsService;

    @Resource
    private yujiFilesService fileService;

    @Resource
    private yujiCampusesService campusesService;

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @Resource
    private yujiFriendShipsService yujiFriendShipsService;


    @Override
    public UsersPO getUserSelfInfo(Long id) {
        UsersPO userInfo = getById(id);
        Long campusId = userInfo.getCampusId();
        if(campusId!=null){
            userInfo.setCampusName(campusesService.getById(campusId).getName());
        }
        return userInfo;
    }

    @Override
    public UserVO getUserPublicInfo(Long userId) {
        UsersPO userInfo = getById(userId);
        Long campusId = userInfo.getCampusId();
        userInfo.setCampusName(campusesService.getById(campusId).getName());
        UserVO userVO= BeanUtil.copyProperties(userInfo,UserVO.class);
        Long currentId = BaseContext.getId();
        Long smallId = currentId > userId ? userId : currentId;
        Long bigId = currentId > userId ? currentId : userId;
        Boolean isFriend = yujiFriendShipsService.exists(new LambdaQueryWrapper<FriendshipsPO>()
                .eq(FriendshipsPO::getUserId, smallId)
                .eq(FriendshipsPO::getFriendId, bigId));
        userVO.setIsFriend(isFriend);
        // todo:（暂时不考虑）还要拿到用户粉丝、关注数

        return userVO;
    }

    @Override
    public void updateUserInfo(UserSelfInfoDTO userSelfInfoDTO) {
        // 获取当前用户信息
        Long userId = BaseContext.getId();
        // 封装成PO类
        UsersPO usersPO = BeanUtil.copyProperties(userSelfInfoDTO, UsersPO.class);

        usersPO.setAvatarUrl(userSelfInfoDTO.getAvatarUrl());
        // update更新数据
        usersPO.setId(userId);
        // 查询校区是否存在：不存在则新增，存在则直接拿去校区id
        CampusPO.CampuseName  campusName = userSelfInfoDTO.getCampusName();
        CampusPO campusPO = campusesService.getOne(new LambdaQueryWrapper<CampusPO>().eq(CampusPO::getName, campusName));
        if(campusPO==null){
            campusPO = new CampusPO();
            campusPO.setName(campusName);
            campusesService.save(campusPO);
            log.error("校区不存在，新增校区");
        }
        usersPO.setCampusId(campusPO.getId());
        saveOrUpdate(usersPO);  // 如果id为空，则新增
        // 删除对应的缓存，避免用户数据更新不及时
        redisTemplate.delete(UserConstant.USER_PUBLIC_INFO_PREFIX+userId);
        redisTemplate.delete(UserConstant.USER_SELF_INFO_PREFIX+userId);

    }

    @Override
    public Integer realNameAuth(UserRealNameDTO userRealNameDTO) {
        UsersPO usersPO = BeanUtil.copyProperties(userRealNameDTO, UsersPO.class);
        usersPO.setId(BaseContext.getId());
        updateById(usersPO);
        Integer isRealName = usersPO.getIsRealName();
        return isRealName;
    }

    @Override
    public void bindMobileDTO(BindMobileDTO bindMobileDTO) {
        UsersPO usersPO = new UsersPO();
        String mobliePhone = bindMobileDTO.getMobile();
        String code = bindMobileDTO.getCode();
        // todo:（暂时不需要） 校对验证码
        usersPO.setId(BaseContext.getId());
        usersPO.setMobile(mobliePhone);
        updateById(usersPO);
    }

    @Override
    public PageResult<BlacklistsVO> getBlacklist(Integer page, Integer pageSize) {
        // 获取当前用户信息
        Long userId = BaseContext.getId();
        // 获取用户黑名单列表，简化逻辑
        Page<BlacklistsVO> blackPageInfo = yujiBlacklistsService.getBlackList(new Page<>(page,pageSize),userId);
        log.info("黑名单列表：{}",blackPageInfo);
        log.info("黑名单列表长度：{}",blackPageInfo.getTotal());
        return new PageResult<>(blackPageInfo.getRecords(),page,pageSize,blackPageInfo.getTotal());
    }




}
