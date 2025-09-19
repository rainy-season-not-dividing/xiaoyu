package com.xiaoyua.controller.search;

import com.xiaoyua.result.Result;
import com.xiaoyua.service.PostService;
import com.xiaoyua.vo.common.PageResult;
import com.xiaoyua.vo.search.PostSearchVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/search")
@Slf4j
public class SearchController {
    @Autowired
    private PostService postService;

    @GetMapping
    public Result<PageResult<PostSearchVO>> postSearch(
            String keyword,
            @RequestParam(required=false, defaultValue = "1") Integer page,
            @RequestParam(required=false, defaultValue = "20") Integer size) {

        return Result.success(postService.searchExact(keyword,page,size));
    }

}
