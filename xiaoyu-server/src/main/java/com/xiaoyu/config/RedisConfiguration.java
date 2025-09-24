package com.xiaoyu.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Redis配置类
 * 功能：配置RedisTemplate的序列化方式规则，解决默认JDK序列化性能差、数据不可读问题
 * 适配场景：苍穹外卖项目中对象缓存（分类、菜品、统计-据等）、字符串缓存（店铺店铺状态等）
 */
@Configuration
@Slf4j
public class RedisConfiguration {


    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper om =  new ObjectMapper()
                .setVisibility(PropertyAccessor.ALL,
                        JsonAutoDetect.Visibility.ANY)
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                // 关键：忽略 POJO 中不认识的字段
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);;

        // 不加这种全局改变的，会导致字段污染，直接修改DTO类，让他在写入redis前，加上类的信息即可。
//                .activateDefaultTyping(
//                        LaissezFaireSubTypeValidator.instance,
//                        ObjectMapper.DefaultTyping.NON_FINAL,
//                        JsonTypeInfo.As.PROPERTY);

        // 兼容 LocalDate 空格格式
        om.configOverride(LocalDate.class)
                .setFormat(JsonFormat.Value.forPattern("yyyy-MM-dd"));

        // 兼容 LocalDateTime 空格格式
        om.configOverride(LocalDateTime.class)
                .setFormat(JsonFormat.Value.forPattern("yyyy-MM-dd HH:mm:ss"));

        return om;
    }

    /**
     * 自定义自定义RedisTemplate实例（自定义序列化规则）
     * 键：String类型（用StringRedisSerializer，保证key可读）
     * 值：Object类型（用Jackson2JsonRedisSerializer，支持复杂对象JSON序列化、JDK8时间类型、多态）
     * @param redisConnectionFactory Redis连接工厂（由Spring Boot自动配置注入）
     * @return 配置完成的RedisTemplate
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory, ObjectMapper objectMapper) {
        log.info("开始初始化RedisTemplate（自定义序列化规则）...");

        // 1. 创建RedisTemplate核心对象
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        // 绑定Redis连接工厂（必不可少，建立与Redis的连接）
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // 2. 配置Key的序列化器（String类型，保证Redis中key为可读字符串）
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringRedisSerializer);          // 普通Key序列化
        redisTemplate.setHashKeySerializer(stringRedisSerializer);     // Hash结构的Key序列化




        // 3.2  配置Value的序列化器（Jackson JSON，支持对象自动序列化）  3.x的版本要在new序列化器的时候，通过构造函数传入objectmapper
        Jackson2JsonRedisSerializer<Object> jacksonSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);




        // 3.3 绑定Value序列化器到RedisTemplate
        redisTemplate.setValueSerializer(jacksonSerializer);          // 普通Value序列化
        redisTemplate.setHashValueSerializer(jacksonSerializer);     // Hash结构的Value序列化



        // 3.3 用普通的String序列化器对值进行序列化
//        redisTemplate.setValueSerializer(stringRedisSerializer);          // 普通Value序列化
//        redisTemplate.setHashValueSerializer(stringRedisSerializer);     // Hash结构的Value序列化



        // 4. 初始化RedisTemplate（应用配置，必须调用，否则序列化规则不生效）
        redisTemplate.afterPropertiesSet();
        log.info("RedisTemplate初始化完成（自定义序列化规则已生效）");

        return redisTemplate;
    }

}
