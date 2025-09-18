package com.xiaoyu_j.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis配置类
 * 
 * @author xiaoyu
 */
@Configuration
public class RedisConfig {
    
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {

        // 1. 创建RedisTemplate核心对象
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        // 绑定Redis连接工厂（必不可少，建立与Redis的连接）
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        // 2. 配置Key的序列化器（String类型，保证Redis中key为可读字符串）
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
        redisTemplate.setKeySerializer(stringRedisSerializer);          // 普通Key序列化
        redisTemplate.setHashKeySerializer(stringRedisSerializer);     // Hash结构的Key序列化

        // 3. 配置Value的序列化器（Jackson JSON，支持对象自动序列化）
//        Jackson2JsonRedisSerializer<Object> jacksonSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        // 3.1 配置Jackson的ObjectMapper（控制JSON序列化规则）
        ObjectMapper objectMapper = new ObjectMapper();
        // 3.1.1 允许Jackson访问对象的所有字段（包括private、protected）
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        // 3.1.2 支持多态类型（序列化时添加@class字段，反序列化时能识别具体子类）
        // 注意：LaissezFaireSubTypeValidator是Jackson 2.10+推荐的安全校验器，避免类型注入风险
        objectMapper.activateDefaultTyping(
                LaissezFaireSubTypeValidator.instance,
                ObjectMapper.DefaultTyping.NON_FINAL,  // 仅对非final类启用多态
                JsonTypeInfo.As.PROPERTY               // @class字段以属性形式存入JSON
        );
        // 3.1.3 解决JDK8时间类型（LocalDateTime/LocalDate）序列化问题（避免转为时间戳）
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);  // 禁用时间戳序列化
        objectMapper.registerModule(new JavaTimeModule());                     // 注册JDK8时间模块

        // 3.2 将ObjectMapper绑定到Jackson序列化器
//        jacksonSerializer.setObjectMapper(objectMapper);
        Jackson2JsonRedisSerializer<Object> jacksonSerializer =
                new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);


        // 3.3 绑定Value序列化器到RedisTemplate
        redisTemplate.setValueSerializer(jacksonSerializer);          // 普通Value序列化
        redisTemplate.setHashValueSerializer(jacksonSerializer);     // Hash结构的Value序列化

        // 4. 初始化RedisTemplate（应用配置，必须调用，否则序列化规则不生效）
        redisTemplate.afterPropertiesSet();

        return redisTemplate;
    }
}
