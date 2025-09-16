package com.xiaoyu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaoyu.dto.friend.SendFriendRequestDTO;
import com.xiaoyu.entity.FriendshipsPO;
import com.xiaoyu.result.PageResult;
import com.xiaoyu.vo.friend.FriendlistVO;

public interface FriendShipsService extends IService<FriendshipsPO> {
    void sendFriendRequest(SendFriendRequestDTO sendFriendRequestDTO);

    void acceptFriendRequest(Long friendId);

    void refuseFriendRequest(Long friendId);

    void deleteFriendships(Long friendId);

    PageResult<FriendlistVO> getFriendlist(Integer page, Integer pageSize, FriendshipsPO.Status status);
}
