package com.xiaoyu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaoyu.dto.SendMessageDTO;
import com.xiaoyu.entity.FriendMessagesPO;
import com.xiaoyu.result.PageResult;
import com.xiaoyu.vo.SendMessageVO;

public interface FriendMessagesService extends IService<FriendMessagesPO> {
    SendMessageVO sendMessage(SendMessageDTO message);

    PageResult<FriendMessagesPO> getMessages(Integer page, Integer size, Long friendId);
}
