package com.xiaoyu.controller.user;


import com.xiaoyu.dto.friend.SendFriendRequestDTO;
import com.xiaoyu.dto.friend.SendMessageDTO;
import com.xiaoyu.entity.FriendMessagesPO;
import com.xiaoyu.entity.FriendshipsPO;
import com.xiaoyu.result.PageResult;
import com.xiaoyu.result.Result;
import com.xiaoyu.service.yujiFriendMessagesService;
import com.xiaoyu.service.yujiFriendShipsService;
import com.xiaoyu.vo.friend.FriendlistVO;
import com.xiaoyu.vo.message.SendMessageVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/friends")
public class FriendController {

    @Resource
    private yujiFriendShipsService yujiFriendShipsService;

    @Resource
    private yujiFriendMessagesService yujiFriendMessagesService;


    @PostMapping("/request")
    public Result sendFriendRequest(SendFriendRequestDTO sendFriendRequestDTO) {
        log.info("发送好友请求：{}",sendFriendRequestDTO);
        yujiFriendShipsService.sendFriendRequest(sendFriendRequestDTO);
        return Result.success("好友请求已发送");
    }

    @PutMapping("/{friendId}/accept")
    public Result acceptFriendRequest(@PathVariable("friend_id") Long friendId) {
        log.info("接受好友请求：{}",friendId);
        yujiFriendShipsService.acceptFriendRequest(friendId);
        return Result.success("已成为好友");
    }

    @PutMapping("/{friendId}/refuse")
    public Result refuseFriendRequest(@PathVariable("friend_id") Long friendId) {
        log.info("拒绝好友请求：{}",friendId);
        yujiFriendShipsService.refuseFriendRequest(friendId);
        return Result.success("已拒绝好友请求");
    }


    @DeleteMapping("/{friendId}")
    public Result deleteFriend(@PathVariable("friend_id") Long friendId) {
        log.info("删除好友：{}",friendId);
        yujiFriendShipsService.deleteFriendships(friendId);
        return Result.success("已删除好友");
    }

    @GetMapping
    public Result<PageResult<FriendlistVO>> getFriendlist(
            @RequestParam(required=false,defaultValue="1") Integer page,
            @RequestParam(required=false,defaultValue="20") Integer size,
            @RequestParam(required=true) FriendshipsPO.Status status
    ){
        log.info("获取好友列表：{}",status);
        return Result.success(yujiFriendShipsService.getFriendlist(page,size,status));
    }

    @PostMapping("/messages")
    public Result<SendMessageVO> sendMessage(@RequestBody SendMessageDTO message){
        log.info("发送消息：{}",message);

        return Result.success(yujiFriendMessagesService.sendMessage(message));
    }

    @GetMapping("/messages")
    public Result<PageResult<FriendMessagesPO>> getMessages(
            @RequestParam(required=true) Long friendId,
            @RequestParam(required=false,defaultValue="1") Integer page,
            @RequestParam(required=false,defaultValue="20") Integer size
    ){
        log.info("获取消息列表：{}",page);
        return Result.success(yujiFriendMessagesService.getMessages(page,size,friendId));
    }

}
