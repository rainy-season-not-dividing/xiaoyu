package com.xiaoyu.vo;


import com.xiaoyu.entity.FriendshipsPO;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FriendlistVO {
    /**
     * 好友表的主键id
     */
    private Long id;
    /**
     * 好友id
     */
    private Long friendId;
    /**
     * 好友信息
     */
    private FriendlistFriendInfo friendInfo;

    /**
     * 好友状态
     */
    private FriendshipsPO.Status status;


    /**
     * 加好友时间
     */
    private LocalDateTime createdAt;

    /**
     * 申请好友的接收者
     */

    private Status isReceiver;

    public enum Status{
        SENDER,
        RECEIVER
    }


    @Data
    public static class FriendlistFriendInfo{
        /**
         * 好友用户id
         */
        private Long id;
        /**
         * 昵称
         */
        private String nickname;
        /**
         * 头像
         */
        private String avatarUrl;
        /**
         * 校区名称
         */
        private String campusName;
    }

}
