package com.xiaoyu.dto.task;

import lombok.Data;

//import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PublishTaskDTO {

    /** 任务标题（最多100字符） */
//    @Size(max=100,message="标题长度不能超过100字符")  // 超过长度会抛出异常
    private String title;

    /** 任务详情 */
    private String content;

    /** 悬赏金额 */
    private BigDecimal reward;

    /** 可见范围：PUBLIC/FRIEND/CAMPUS，默认PUBLIC */
    private String visibility;

    /** 截止报名时间（ISO8601格式） */
    private LocalDateTime expireAt;

    /** 文件ID数组 */
    private List<Long> fileIds;

    /** 标签ID数组 */
    private List<Integer> tagIds;
}