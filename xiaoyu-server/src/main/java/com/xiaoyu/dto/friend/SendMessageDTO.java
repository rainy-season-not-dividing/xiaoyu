package com.xiaoyu.dto.friend;


import lombok.Data;

@Data
public class SendMessageDTO {
    private Long toId;
    private String content;
}
