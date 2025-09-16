package com.xiaoyu.vo.message;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SendMessageVO {
    private Long id;
    private Long fromId;
    private Long toId;
    private String content;
    private LocalDateTime createdAt;
}
