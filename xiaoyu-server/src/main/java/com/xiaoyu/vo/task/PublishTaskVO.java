package com.xiaoyu.vo.task;


import com.xiaoyu.entity.TasksPO;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PublishTaskVO {
    private Long id;
    private Long publisherId;
    private String title;
    private BigDecimal reward;
    private TasksPO.Status status;
    private LocalDateTime createdAt;
}
