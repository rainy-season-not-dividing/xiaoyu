package com.xiaoyu.vo;

import com.xiaoyu.entity.TaskOrdersPO;
import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class TaskOrdersVO {
    private Long orderId;
    private TaskOrdersPO.Status status;
}
