package com.xiaoyua.dto.message;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * 消息内容基础接口
 *
 * @author xiaoyu
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = TextMessageContent.class, name = "TEXT"),
        @JsonSubTypes.Type(value = PostForwardMessageContent.class, name = "POST"),
        @JsonSubTypes.Type(value = TaskForwardMessageContent.class, name = "TASK")
})
public interface MessageContent {

    /**
     * 获取消息类型
     */
    String getType();
}
