package com.xiaoyu.common.utils;



import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@Component
public class RedisIdUtil {

    // 算时间戳的起始时间
    private static final long BEGIN_TIMESTAMP = 1640995200L;
    // 左移位数
    private static final long COUNT_TIMESTAMP = 32L;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    public long nextId(String keyPrefix){
        // ID构造方式：0+相对时间戳（31位）+redis自增序列（32位）
        // 前31位的时间
        LocalDateTime now = LocalDateTime.now();
        long nowSecond = now.toEpochSecond(ZoneOffset.UTC);
        long timeStamp = nowSecond - BEGIN_TIMESTAMP;
        // 后32位的redis increment（）
        String today = now.format(DateTimeFormatter.ofPattern("yyyy:MM:dd"));
        long count = stringRedisTemplate.opsForValue().increment("irc:"+keyPrefix+":"+today);
        // 拼接后返回
        return timeStamp<<COUNT_TIMESTAMP | count;
    }

}

