package com.xiaoyu.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import com.github.xiaoymin.knife4j.core.util.StrUtil;
import com.google.errorprone.annotations.concurrent.LazyInit;
import com.xiaoyu.common.dto.RedisDataDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.function.Function;

@Component
@Slf4j
public class RedisUtil {

    // 线程池（企业中建议用ThreadPoolTaskExecutor，支持配置化）
    @Resource(name = "cacheRebuildExecutor")
    private ThreadPoolTaskExecutor cacheRebuildExecutor;

    // 空值缓存的过期时间（5分钟，单位：秒）
    private static final long CACHE_NULL_TTL_BASE = 300;
    // 正常缓存的过期时间（20分钟，单位：秒）
    private static final long CACHE_SHOP_TTL_BASE = 1200;


    private static final Random RANDOM = new Random();

    private static final long SHOP_TTL_MAX_OFFSET = 300;
    private static final String ID_PREFIX = "thread:id:";

    @Autowired
    @LazyInit
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    // 注入RedisTemplate中配置好的ObjectMapper（复用同一套Jackson规则，避免重复配置）
    @Resource
    private ObjectMapper objectMapper;

    /**
     * 函数名	核心功能（解决的缓存问题）	实现方案概要
     * queryWithMutex	防缓存穿透 + 防缓存击穿	Redis 查询，无数据则抢 Redis 互斥锁，获锁后查库，将空值或真实数据存入 Redis 并释放锁
     * queryWithLogicExpire	防缓存击穿 + 防缓存穿透（逻辑过期）	Redis 查询，有数据则判断逻辑是否过期，未过期直接返回；已过期则抢锁，线程池异步重建缓存并返回过期数据
     * queryShopWithAvalanche	防缓存雪崩 + 防缓存穿透	Redis 查询，无数据则查库，真实数据存入 Redis，空值存入 Redis 时用随机 TTL 避免集中过期
     * queryWithPassThrough	仅防缓存穿透	Redis 查询，无数据则查库，真实数据存入 Redis，空值存入 Redis 且固定 5 分钟过期
     */


    /**
     *  缓存击穿问题 + 缓存穿透问题
     *  互斥锁 + 空值缓存
     * @param keyPrefix
     * @param id
     * @param type
     * @param dbFallBack
     * @param time
     * @param unit
     * @return
     * @param <R>
     * @param <ID>
     */
    public <R,ID> R queryWithMutex(String keyPrefix, ID id, Class<R> type, Function<ID,R> dbFallBack, Long time, TimeUnit unit){
        // redis查询
        String key = keyPrefix + id;
        Object redisValue = redisTemplate.opsForValue().get(key);
        // redis存在--直接返回
        if(redisValue!=null){
            if(redisValue instanceof String && StrUtil.isBlank((String) redisValue)) return null;
            return objectMapper.convertValue(redisValue, type);
        }
        R dbData = null;
        try{
            // redis不存在--获取互斥锁
            boolean getLock = tryLock(key, 5, 10, TimeUnit.SECONDS); // 锁超时5秒，持锁10秒
            // 获取失败 -- 自调用等待
            if(!getLock){
                Thread.sleep(50);
                return queryWithMutex(keyPrefix, id, type, dbFallBack, time, unit);
            }
            // 获取成功--db查询
            dbData = dbFallBack.apply(id);
            // db不存在--缓存空值后返回null
            if(dbData==null){
                redisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL_BASE, unit);
            }
            // db存在，写入redis，结果返回
            redisTemplate.opsForValue().set(key, dbData, time, unit);
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
        finally{
            // 释放锁
            unlock(key);
        }
        return dbData;
    }

    /**
     * 缓存击穿
     * 逻辑过期（对象进一步封装，加上逻辑过期时间） + 空值缓存 + 互斥锁 + 线程池异步
     */
    /**
     * 逻辑过期
     * @param keyPrefix
     * @param id
     * @param type
     * @param dbFallBack
     * @param time
     * @param unit
     * @return
     * @param <R>
     * @param <ID>
     * @throws InterruptedException
     */
    public <R,ID> List<R> queryWithLogicExpire(
            String keyPrefix,
            ID id,
            Class<R> type,
            Function<ID,List<R>> dbFallBack,
            Long time,
            TimeUnit unit
    ) throws InterruptedException {
        // 1、redis查询
        String key = keyPrefix + id;
        Object redisValue = redisTemplate.opsForValue().get(key);
        // 构造 JavaType：RedisDataDTO<R>
        JavaType javaType = objectMapper.getTypeFactory()
                .constructParametricType(RedisDataDTO.class, type);
        // 1.1、不存在--控制缓存
        if(redisValue!=null){
            if(redisValue instanceof String && StrUtil.isBlank((String) redisValue)){
                return null;
            }
        }
        else{
            List<R> dbData = dbFallBack.apply(id);
            if(dbData!=null){
                RedisDataDTO<R> cacheData = new RedisDataDTO<>();
                cacheData.setData(dbData);
                cacheData.setExpireTime(LocalDateTime.now().plusSeconds(CACHE_SHOP_TTL_BASE));
//                try {
//                    String json = objectMapper.writerFor(javaType).writeValueAsString(cacheData);
//                    redisTemplate.opsForValue().set(key, json);
//                } catch (JsonProcessingException e) {
//                    throw new RuntimeException("缓存序列化失败", e);
//                }
                redisTemplate.opsForValue().set(key, cacheData);
            }
            else{
                redisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL_BASE, TimeUnit.SECONDS);
            }
            return dbData;
        }
//        log.info("缓存命中：{}",redisValue);
        //1.2、存在
        // 构建RedisDataDTO<List<R>>的完整类型
        // 3. 反序列化时使用完整类型
//        RedisDataDTO<R> redisData = null;
//        try {
//            // 1. 先整段 JSON 字符串 → JsonNode
//            // 1. 去掉最外层转义（得到合法 JSON）
//            String unescaped = objectMapper.readValue(redisValue, String.class);
//
//// 2. 再反序列化成对象
//            redisData = objectMapper.readValue(unescaped, javaType);
//        } catch (JsonProcessingException e) {
//            // 数据被污染，淘汰掉
//            log.error("缓存反序列化失败", e);   // ← 打印完整堆栈
//            log.error("缓存数据被污染：{}",redisValue);
//            redisTemplate.delete(key);
//            return null;
//        }
        // 3. 缓存命中 -> 反序列化（Jackson 已帮我们做好）
        RedisDataDTO<R> redisData = objectMapper.convertValue(redisValue,javaType);

        List<R> data = redisData.getData();  // 直接获取List<R>
        //1.2.1、判断是否逻辑过期
        // 1.2.1.1、逻辑未过期--返回结果
        if(redisData.getExpireTime().isAfter(LocalDateTime.now())){
            return data;
        }
        //1.2.1.2、逻辑过期--获取锁异步更新数据，并返回过期结果
        boolean getLock = tryLock(key ,5, 10, TimeUnit.SECONDS); // 锁超时5秒，持锁10秒
        if(getLock){
            try{
                // 异步更新redis,缓存重建
                cacheRebuildExecutor.submit(()->{
                    List<R> dbData = dbFallBack.apply(id);
                    if(dbData!=null){
                        RedisDataDTO<R> cacheData = new RedisDataDTO<>();
                        cacheData.setData(dbData);
                        cacheData.setExpireTime(LocalDateTime.now().plusSeconds(CACHE_SHOP_TTL_BASE));
//                        try {
//                            String json = objectMapper.writerFor(javaType).writeValueAsString(cacheData);
//                            redisTemplate.opsForValue().set(key, json);
//                        } catch (JsonProcessingException e) {
//                            throw new RuntimeException("缓存序列化失败", e);
//                        }
                        redisTemplate.opsForValue().set(key, cacheData);
                    }
                    else{
                        redisTemplate.opsForValue().set(key, "", CACHE_NULL_TTL_BASE, TimeUnit.SECONDS);
                    }
                });
            }
            catch(Exception e){
                // todo: 这里不能简单的抛出异常，还要做后续的异常处理，或者是记录日志什么的
                throw new RuntimeException(e);
            }
            finally{
                unlock(key);
            }
        }
        return data;
    }



    /*
    * 缓存雪崩问题
    * 随机ttl + 空值缓存
    *
     */
    public <R,ID> R queryShopWithAvalanche(String keyPrefix, ID id, Class<R> type, Function<ID,R> dbFallBack, Long time, TimeUnit unit){
        // redis查询
        String key = keyPrefix + id;
        Object redisValue =  redisTemplate.opsForValue().get(key);
        // redis有--直接返回
        // 3. 缓存命中：分2种情况处理
        if (redisValue != null) {
            // 3.1 命中“空值缓存”（redisValue是""字符串）
            if (redisValue instanceof String && StrUtil.isBlank((String) redisValue)) {
                return null;
            }
            // 3.2 命中“真实数据”：显式反序列化为指定类型（适配你的Jackson配置）
            // 用RedisTemplate配置好的ObjectMapper，避免类型不匹配
            return objectMapper.convertValue(redisValue, type);
        }
        // redis没有-db查询
        R dbData = dbFallBack.apply(id);
        // db有--写入redis后返回
        if(dbData!=null){
            redisTemplate.opsForValue().set(key,dbData,time,unit);
            return dbData;
        }
        // db没有--空值写入redis后返回
        redisTemplate.opsForValue().set(key,"",getRandomOffset(SHOP_TTL_MAX_OFFSET),unit);
        return null;
    }


    /*
    * 缓存穿透问题
    * 空值缓存解决
     */
    public <R,ID> R queryWithPassThrough(String keyPrefix, ID id, Class<R> type, Function<ID,R> dbFallBack, Long time, TimeUnit unit){
        // redis查询
        String key = keyPrefix + id;
        Object redisValue =  redisTemplate.opsForValue().get(key);
        // redis有--直接返回
        // 3. 缓存命中：分2种情况处理
        if (redisValue != null) {
            // 3.1 命中“空值缓存”（redisValue是""字符串）
            if (redisValue instanceof String && StrUtil.isBlank((String) redisValue)) {
                return null;
            }
            // 3.2 命中“真实数据”：显式反序列化为指定类型（适配你的Jackson配置）
            // 用RedisTemplate配置好的ObjectMapper，避免类型不匹配
            return objectMapper.convertValue(redisValue, type);
        }
        // redis没有-db查询
        R dbData = dbFallBack.apply(id);
        // db有--写入redis后返回
        if(dbData!=null){
            redisTemplate.opsForValue().set(key,dbData,time,unit);
            return dbData;
        }
        // db没有--空值写入redis后返回
        redisTemplate.opsForValue().set(key,"",CACHE_NULL_TTL_BASE,unit);
        return null;
    }


    private boolean tryLock(String key, long watiTime, long leaseTime, TimeUnit unit){
        String threadId = ID_PREFIX + Thread.currentThread().getId();
        try{
            Boolean success = stringRedisTemplate.opsForValue()
                    .setIfAbsent(key,threadId,leaseTime,unit);
            return Boolean.TRUE.equals(success);
        } catch (Exception e){
            return false;
        }
    }

    private void unlock(String key){
        // 获取锁标识
        String threadId = ID_PREFIX + Thread.currentThread().getId();
        if(stringRedisTemplate.opsForValue().get(key).equals(threadId))
            stringRedisTemplate.delete(key);
    }

    /**
     * JDK8+兼容的随机偏移生成：返回[0, maxOffset]的非负整数
     * 解决Random无nextLong(bound)的问题
     */
    private long getRandomOffset(long maxOffset) {
        if (maxOffset <= 0) {
            return 0; // 偏移量无效时返回0，避免异常
        }
        // 无参nextLong()生成任意长整型，通过取模+绝对值确保非负且在[0, maxOffset]范围内
        long randomLong = RANDOM.nextLong();
        return Math.abs(randomLong) % (maxOffset + 1); // +1确保包含maxOffset（如max=120，可返回0~120）
    }


}
