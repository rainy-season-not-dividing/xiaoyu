package com.xiaoyua.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * 转发创建DTO
 *
 * @author xiaoyu
 */
@Data
@Schema(description = "转发创建请求")
public class ShareCreateDTO {

    /**
     * 被转发者用户ID（可选）
     * 如果指定，则会向该用户发送转发通知
     */
    @Schema(description = "被转发者用户ID", example = "123")
    private List<Long> shareUserIds;

    /**
     * 转发附言（可选）
     */
    @Schema(description = "转发附言", example = "分享一个不错的内容")
    @Size(max = 200, message = "转发附言不能超过200个字符")
    private String reason;
}