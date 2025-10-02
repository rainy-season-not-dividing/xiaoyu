package com.xiaoyua.dto.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 文本消息内容
 *
 * @author xiaoyu
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TextMessageContent implements MessageContent {

    /** 文本内容 */
    private String text;

    @Override
    @JsonIgnore
    public String getType() {
        // 这个方法由Jackson根据@JsonSubTypes注解自动处理
        // @JsonIgnore防止重复序列化type字段
        return "TEXT";
    }
}
