package com.xiaoyu.common.dto;


import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class RedisDataDTO<T> {
    @JsonTypeInfo(
            use = JsonTypeInfo.Id.CLASS,      // 用全限定类名
            include = JsonTypeInfo.As.PROPERTY, // 以属性形式出现
            property = "@clazz")              // 属性名
    private List<T> data;
    private LocalDateTime expireTime;
}
