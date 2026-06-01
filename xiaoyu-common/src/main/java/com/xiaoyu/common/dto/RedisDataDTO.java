package com.xiaoyu.common.dto;


import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class RedisDataDTO<R> {
    @JsonTypeInfo(
            use = JsonTypeInfo.Id.CLASS,      // 用全限定类名
            include = JsonTypeInfo.As.PROPERTY, // 以属性形式出现
            property = "@clazz")              // 属性名
    private List<R> data;
    private LocalDateTime expireTime;

    // 手动生成 getData
    public List<R> getData() {
        return data;
    }

    // 手动生成 setData
    public void setData(List<R> data) {
        this.data = data;
    }

    // 手动生成 getExpireTime
    public LocalDateTime getExpireTime() {
        return expireTime;
    }

    // 手动生成 setExpireTime
    public void setExpireTime(LocalDateTime expireTime) {
        this.expireTime = expireTime;
    }
}
