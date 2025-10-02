package com.xiaoyua.dto.message;

import lombok.Data;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;

/**
 * 发送私信DTO
 *
 * @author xiaoyu
 */
@Data
public class MessageCreateDTO {

    /**
     * 接收者ID
     */
    @NotNull(message = "接收者ID不能为空")
    private Long toId;

    /**
     * 消息类型：TEXT/POST/TASK
     */
    @NotNull(message = "消息类型不能为空")
    private String type;

    /**
     * 消息内容（根据type不同，内容格式不同）
     * TEXT: 直接文本内容
     * POST/TASK: 转发的项目ID
     */
    @Valid
    private MessageContent content;
}
