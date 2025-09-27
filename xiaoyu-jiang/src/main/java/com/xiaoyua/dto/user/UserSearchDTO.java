package com.xiaoyua.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 用户搜索请求DTO
 *
 * @author xiaoyu
 */
@Data
@Schema(description = "用户搜索请求")
public class UserSearchDTO {

    /**
     * 搜索关键词
     * 如果以数字开头，则搜索账号；否则搜索昵称
     */
    @NotBlank(message = "搜索关键词不能为空")
    @Size(min = 1, max = 50, message = "搜索关键词长度必须在1-50个字符之间")
    @Schema(description = "搜索关键词", example = "张三", required = true)
    private String keyword;

    /**
     * 页码，从1开始
     */
    @Schema(description = "页码，从1开始", example = "1")
    private Integer page = 1;

    /**
     * 每页大小
     */
    @Schema(description = "每页大小，最大50", example = "10")
    private Integer size = 10;

    /**
     * 校区ID过滤（可选）
     */
    @Schema(description = "校区ID过滤", example = "1")
    private Long campusId;

    /**
     * 是否只搜索已实名用户
     */
    @Schema(description = "是否只搜索已实名用户", example = "false")
    private Boolean onlyRealName = false;
}
