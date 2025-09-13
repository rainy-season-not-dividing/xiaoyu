package com.xiaoyu.controller.tag;


import com.xiaoyu.entity.TagsPO;
import com.xiaoyu.result.Result;
import com.xiaoyu.service.TagsService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
public class TagController {

    @Resource
    private TagsService tagsService;

    @GetMapping("/tags")
    public Result<List<TagsPO>> getTags() {
        log.info("获取所有的tag");
        // todo：标签权重没有加上
        List<TagsPO> tags = tagsService.list();
        return Result.success(tags);
    }

}
