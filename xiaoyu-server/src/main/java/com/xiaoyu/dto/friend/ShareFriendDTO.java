package com.xiaoyu.dto.friend;

import lombok.Data;

import java.util.List;

@Data
public class ShareFriendDTO {
    List<Long> shareUserIds;
    String message;
}
