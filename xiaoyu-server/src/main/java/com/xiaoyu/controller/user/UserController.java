package com.xiaoyu.controller.user;


import cn.hutool.core.bean.BeanUtil;
import com.xiaoyu.context.BaseContext;
import com.xiaoyu.dto.BindMobileDTO;
import com.xiaoyu.dto.UserRealNameDTO;
import com.xiaoyu.dto.UserSelfInfoDTO;
import com.xiaoyu.entity.UserPO;
import com.xiaoyu.result.PageResult;
import com.xiaoyu.result.Result;
import com.xiaoyu.service.BlacklistsService;
import com.xiaoyu.service.UserService;
import com.xiaoyu.vo.BlacklistsVO;
import com.xiaoyu.vo.UserVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private BlacklistsService blacklistsService;

    @GetMapping
    public Result<UserVO> getUserPublicInfo(@PathVariable Long userId){
        log.info("获取用户的公开信息：{}",userId);
        return Result.success(userService.getUserPublicInfo(userId));
    }

    @GetMapping("/me")
    public Result<UserPO> getUserSelfInfo(){
        log.info("获取当前用户的信息");
        return Result.success(userService.getById(BaseContext.getId()));
    }

    @PutMapping("/profile")
    public Result updateUserInfo(@RequestBody UserSelfInfoDTO userSelfInfoDTO){
        log.info("更新当前用户信息：{}",userSelfInfoDTO);
        userService.updateUserInfo(userSelfInfoDTO);
        return Result.success("更新成功");
    }

    @PostMapping("real-name-auth")
    public Result<Integer> realNameAuth(@RequestBody UserRealNameDTO userRealNameDTO){
        log.info("用户实名认证：{}",userRealNameDTO);

        return Result.success("实名认证成功",userService.realNameAuth(userRealNameDTO));
    }

    @PostMapping("bind-mobile")
    public Result bindMobile(@RequestBody BindMobileDTO bindMobileDTO){
        log.info("用户绑定手机：{}",bindMobileDTO);
        userService.bindMobileDTO(bindMobileDTO);
        return Result.success("绑定成功");
    }

    @PostMapping("/blacklist")
    public Result addToBlacklist(@RequestBody Long targetId){
        log.info("将用户加入黑名单：{}",targetId);
        blacklistsService.addBlacklist(targetId);
        return Result.success("拉黑成功");
    }

    @DeleteMapping("/blacklist/{target_id}")
    public Result removeFromBlacklist(@PathVariable Long targetId){
        log.info("将用户移出黑名单：{}",targetId);
        blacklistsService.removeFromBlacklist(targetId);
        return Result.success("移出成功");
    }

    @GetMapping("/blacklist")
    public Result<PageResult<BlacklistsVO>> getBlacklist(@RequestParam(required=false,defaultValue = "1") Integer page, @RequestParam(required=false,defaultValue = "20") Integer pageSize){
        log.info("获取黑名单列表：{}",page);
        return Result.success(userService.getBlacklist(page,pageSize));
    }

}
