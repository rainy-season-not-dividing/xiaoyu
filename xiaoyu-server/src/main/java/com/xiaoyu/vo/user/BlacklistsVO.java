package com.xiaoyu.vo.user;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class BlacklistsVO {
    private Long id;
    private Long targetId;
    private BlackUserInfo targetUser;
    private LocalDateTime createdAt;

    @Data
    public static class BlackUserInfo {

        private Long id;
        /**
         * 用户昵称
         */
        private String nickname;
        /**
         * 用户头像
         */
        private String avatarUrl;
    }
}
