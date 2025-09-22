package com.xiaoyu.controller.tag;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xiaoyu.constant.TagConstant;
import com.xiaoyu.entity.TagsPO;
import com.xiaoyu.result.Result;
import com.xiaoyu.service.yujiTagsService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RestController
@Slf4j
public class TagController {

    @Resource
    private yujiTagsService yujiTagsService;

    @Resource
    private RedisTemplate<String,Object> redisTempalte;

    @Resource
    private ObjectMapper objectMapper;

    @GetMapping("/tags")
    public Result<List<TagsPO>> getTags() throws InterruptedException{
        log.info("获取所有的tag");

        // 先查缓存
        Object redisValue = redisTempalte.opsForValue().get(TagConstant.TAG_ALL);
        List<TagsPO> tags = null;
        if(redisValue != null){
            tags = objectMapper.convertValue(redisValue, new TypeReference<List<TagsPO>>() {} );
        }
        else{
            // todo：标签权重没有加上
            tags = yujiTagsService.list();
            // 加入缓存
            // todo： 写入缓存后，不用修改的哦！！后续把这个代码注释掉
            redisTempalte.opsForValue().set(TagConstant.TAG_ALL,tags);
        }

        return Result.success(tags);
    }

}
