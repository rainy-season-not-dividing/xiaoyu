package com.xiaoyu.vo.task;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ListTaskOrdersVO {
    private Long id;                // 订单主键
    private Long taskId;            // 任务主键
    private String taskTitle;       // 任务标题

    private User publisher;         // 发布者信息
    private User receiver;          // 接单者信息

    private String status;          // 订单状态

    private LocalDateTime createdAt; // 创建时间

    private LocalDateTime updatedAt; // 更新时间

    /**
     * 用户信息子 VO
     */
    @Data
    public static class User {
        private Long id;          // 用户ID
        private String nickname;  // 昵称
        private String avatarUrl; // 头像地址
    }
}
