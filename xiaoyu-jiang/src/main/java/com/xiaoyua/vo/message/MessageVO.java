package com.xiaoyua.vo.message;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.xiaoyua.vo.user.UserSimpleVO;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 私信消息VO
 *
 * @author xiaoyu
 */
@Data
public class MessageVO {

    /**
     * 消息ID
     */
    private Long id;

    /**
     * 发送者信息
     */
    private UserSimpleVO fromUser;

    /**
     * 接收者信息
     */
    private UserSimpleVO toUser;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 是否为当前用户发送的消息
     */
    private Boolean isSelf;

    /**
     * 发送时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
}
