package com.xiaoyu.util;


import cn.hutool.core.lang.UUID;

import com.xiaoyu.constant.TaskConstant;
import com.xiaoyua.context.BaseContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * 自定义锁类，确保一人一单，分布式锁
 * 锁的格式： preFixkey + userId 即可
 */
@Component
public class RedisLockUtil {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    public static final String PRE_FIXKEY = TaskConstant.TASK_LOCK_USER_PREFIX;
    private static final String ID_PREFIX = UUID.randomUUID().toString(true) + "-";
    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT;
    static {
        UNLOCK_SCRIPT = new DefaultRedisScript<>();
        UNLOCK_SCRIPT.setLocation(new ClassPathResource("unlock.lua"));
        UNLOCK_SCRIPT.setResultType(Long.class);
    }

    public  boolean tryLock(long timeout) {
        // 获取当前用户
        Long currentId = BaseContext.getCurrentId();
        // key
        String key = PRE_FIXKEY + currentId;
        // value
        String value = ID_PREFIX + Thread.currentThread().threadId();
        // 获取锁（成功，返回True）
        return Boolean.TRUE.equals(stringRedisTemplate.opsForValue().
                setIfAbsent(key, value, timeout, TimeUnit.SECONDS));
    }


    public void unlock() {
        stringRedisTemplate.execute(
                UNLOCK_SCRIPT,
                Collections.singletonList(PRE_FIXKEY + BaseContext.getCurrentId()),
                ID_PREFIX + Thread.currentThread().threadId());
    }


}

