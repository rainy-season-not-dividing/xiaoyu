package com.xiaoyu.dto;


import lombok.Data;

@Data
public class SendMessageDTO {
    private Long toId;
    private String content;
}
