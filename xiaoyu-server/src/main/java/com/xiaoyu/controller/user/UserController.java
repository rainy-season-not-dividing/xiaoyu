package com.xiaoyu.controller.user;


import com.xiaoyu.context.BaseContext;
import com.xiaoyu.dto.user.BindMobileDTO;
import com.xiaoyu.dto.user.UserRealNameDTO;
import com.xiaoyu.dto.user.UserSelfInfoDTO;
import com.xiaoyu.dto.friend.AddToBlacklistDTO;
import com.xiaoyu.entity.UsersPO;
import com.xiaoyu.result.PageResult;
import com.xiaoyu.result.Result;
import com.xiaoyu.service.yujiBlacklistsService;
import com.xiaoyu.service.yujiUserService;
import com.xiaoyu.vo.user.BlacklistsVO;
import com.xiaoyu.vo.user.UserVO;
import com.xiaoyu.common.utils.RedisUtil;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import com.xiaoyu.constant.UserConstant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;


@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    @Resource
    private yujiUserService yujiUserService;

    @Resource
    private yujiBlacklistsService yujiBlacklistsService;

    @Resource
    private RedisUtil redisUtil;

    @GetMapping("/{userId}")
    public Result<UserVO> getUserPublicInfo(@PathVariable Long userId) throws InterruptedException{
        log.info("获取用户的公开信息：{}",userId);
        List<UserVO> list = redisUtil.<UserVO, Long>queryWithLogicExpire(
                UserConstant.USER_PUBLIC_INFO_PREFIX,
                userId, UserVO.class,
                id-> Collections.singletonList(yujiUserService.getUserPublicInfo(id)),
                UserConstant.USER_PUBLIC_INFO_TIMEOUT, TimeUnit.SECONDS
        );
        return Result.success(list!=null?list.getFirst():null);
//        return Result.success(yujiUserService.getUserPublicInfo(userId));
    }

    @GetMapping("/me")
    public Result<UsersPO> getUserSelfInfo() throws InterruptedException{
        log.info("获取当前用户的信息");
        Long currentId = BaseContext.getId();
        List<UsersPO> list = redisUtil.<UsersPO, Long>queryWithLogicExpire(
                UserConstant.USER_SELF_INFO_PREFIX,
                currentId, UsersPO.class,
                id-> {
                    UsersPO user = yujiUserService.getUserSelfInfo(id);
                    List<UsersPO> resultList = new ArrayList<>();
                    if (user != null) {
                        resultList.add(user);
                        return resultList;
                    }
                    return null;
                    },
                UserConstant.USER_SELF_INFO_TIMEOUT, TimeUnit.SECONDS
                );
        return Result.success(list!=null?list.getFirst():null);
    }


    @PutMapping(value="/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result updateUserInfo(@RequestBody UserSelfInfoDTO userSelfInfoDTO){
        log.info("更新当前用户信息：{}",userSelfInfoDTO);
        yujiUserService.updateUserInfo(userSelfInfoDTO);
        return Result.success("更新成功");
    }

    @PostMapping("real-name-auth")
    public Result<Integer> realNameAuth(@RequestBody UserRealNameDTO userRealNameDTO){
        log.info("用户实名认证：{}",userRealNameDTO);

        return Result.success("实名认证成功", yujiUserService.realNameAuth(userRealNameDTO));
    }

    @PostMapping("bind-mobile")
    public Result bindMobile(@RequestBody BindMobileDTO bindMobileDTO){
        log.info("用户绑定手机：{}",bindMobileDTO);
        yujiUserService.bindMobileDTO(bindMobileDTO);
        return Result.success("绑定成功");
    }

    @PostMapping("/blacklist")
    public Result addToBlacklist(@RequestBody AddToBlacklistDTO addToBlacklist){
        log.info("将用户加入黑名单：{}",addToBlacklist);
        Long targetId = addToBlacklist.getTargetId();
        yujiBlacklistsService.addBlacklist(targetId);
        return Result.success("拉黑成功");
    }

    @DeleteMapping("/blacklist/{targetId}")
    public Result removeFromBlacklist(@PathVariable Long targetId){
        log.info("将用户移出黑名单：{}",targetId);
        yujiBlacklistsService.removeFromBlacklist(targetId);
        return Result.success("移出成功");
    }

    @GetMapping("/blacklist")
    public Result<PageResult<BlacklistsVO>> getBlacklist(
            @RequestParam(required=false,defaultValue = "1") Integer page,
            @RequestParam(required=false,defaultValue = "20") Integer size){
        log.info("获取黑名单列表：{}",page);
        return Result.success(yujiUserService.getBlacklist(page,size));
    }



}
