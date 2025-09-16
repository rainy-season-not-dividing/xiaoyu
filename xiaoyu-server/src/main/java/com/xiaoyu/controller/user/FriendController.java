package com.xiaoyu.controller.user;


import com.xiaoyu.dto.friend.SendFriendRequestDTO;
import com.xiaoyu.dto.friend.SendMessageDTO;
import com.xiaoyu.entity.FriendMessagesPO;
import com.xiaoyu.entity.FriendshipsPO;
import com.xiaoyu.result.PageResult;
import com.xiaoyu.result.Result;
import com.xiaoyu.service.FriendMessagesService;
import com.xiaoyu.service.FriendShipsService;
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
    private FriendShipsService friendShipsService;

    @Resource
    private FriendMessagesService friendMessagesService;


    @PostMapping("/request")
    public Result sendFriendRequest(SendFriendRequestDTO sendFriendRequestDTO) {
        log.info("发送好友请求：{}",sendFriendRequestDTO);
        friendShipsService.sendFriendRequest(sendFriendRequestDTO);
        return Result.success("好友请求已发送");
    }

    @PutMapping("/{friend_id}/accept")
    public Result acceptFriendRequest(@PathVariable("friend_id") Long friendId) {
        log.info("接受好友请求：{}",friendId);
        friendShipsService.acceptFriendRequest(friendId);
        return Result.success("已成为好友");
    }

    @PutMapping("/{friend_id}/refuse")
    public Result refuseFriendRequest(@PathVariable("friend_id") Long friendId) {
        log.info("拒绝好友请求：{}",friendId);
        friendShipsService.refuseFriendRequest(friendId);
        return Result.success("已拒绝好友请求");
    }


    @DeleteMapping("/{friend_id}")
    public Result deleteFriend(@PathVariable("friend_id") Long friendId) {
        log.info("删除好友：{}",friendId);
        friendShipsService.deleteFriendships(friendId);
        return Result.success("已删除好友");
    }

    @GetMapping
    public Result<PageResult<FriendlistVO>> getFriendlist(
            @RequestParam(required=false,defaultValue="1") Integer page,
            @RequestParam(required=false,defaultValue="20") Integer size,
            @RequestParam(required=true) FriendshipsPO.Status status
    ){
        log.info("获取好友列表：{}",status);
        return Result.success(friendShipsService.getFriendlist(page,size,status));
    }

    @PostMapping("/messages")
    public Result<SendMessageVO> sendMessage(@RequestBody SendMessageDTO message){
        log.info("发送消息：{}",message);

        return Result.success(friendMessagesService.sendMessage(message));
    }

    @GetMapping("/messages")
    public Result<PageResult<FriendMessagesPO>> getMessages(
            @RequestParam(required=true) Long friendId,
            @RequestParam(required=false,defaultValue="1") Integer page,
            @RequestParam(required=false,defaultValue="20") Integer size
    ){
        log.info("获取消息列表：{}",page);
        return Result.success(friendMessagesService.getMessages(page,size,friendId));
    }

}
