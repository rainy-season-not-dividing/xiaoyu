package com.xiaoyua.dto.post;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.util.List;

/**
 * 更新动态请求DTO
 * 用于接收动态更新请求数据
 *
 * @author xiaoyu
 */
@Data

public class PostUpdateDTO {

    /**
     * 动态标题
     */
    @Size(max = 100, message = "动态标题最多100个字符")
    private String title;

    /**
     * 动态内容
     */
    @Size(max = 2000, message = "动态内容最多2000个字符")
    private String content;

    /**
     * 校区ID
     */
    private Long campusId;

    /**
     * 可见范围：PUBLIC公开 FRIEND好友 CAMPUS校园
     */
    @Pattern(regexp = "^(PUBLIC|FRIEND|CAMPUS)$", message = "可见范围值不正确")
    private String visibility;

    /**
     * 是否置顶：0否 1是
     */
    @Min(value = 0, message = "置顶设置值不正确")
    @Max(value = 1, message = "置顶设置值不正确")
    private Integer isTop;

    private List<Long> topicIds;


    private List<String> files;

}