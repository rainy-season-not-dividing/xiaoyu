package com.xiaoyua.controller.search;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xiaoyua.dto.user.UserSearchDTO;
import com.xiaoyua.result.Result;
import com.xiaoyua.service.jPostService;
import com.xiaoyua.service.jUserService;
import com.xiaoyua.vo.common.PageResult;
import com.xiaoyua.vo.search.PostSearchVO;
import com.xiaoyua.vo.user.UserSearchVO;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/search")
@Slf4j
public class SearchController {
    @Autowired
    private jPostService jPostService;

    @Autowired
    private jUserService jUserService;

    @GetMapping
    public Result<PageResult<PostSearchVO>> postSearch(
            String keyword,
            @RequestParam(required=false, defaultValue = "1") Integer page,
            @RequestParam(required=false, defaultValue = "20") Integer size) {
        log.info("搜索动态");
        return Result.success(jPostService.searchExact(keyword,page,size));
    }

    /**
     * 搜索用户
     */
    @GetMapping("/users")
    @Operation(summary = "搜索用户", description = "根据关键词搜索用户，数字开头搜索账号，否则搜索昵称")
    public Result<PageResult<UserSearchVO>> searchUsers(@Validated UserSearchDTO searchDTO) {
        try {
            IPage<UserSearchVO> result = jUserService.searchUsers(searchDTO);

            log.info("用户搜索请求: keyword={}, total={}",
                    searchDTO.getKeyword(), result.getTotal());

            return Result.success(new PageResult<>(result.getRecords(), searchDTO.getPage(), searchDTO.getSize(), result.getTotal()));

        } catch (IllegalArgumentException e) {
            log.warn("用户搜索参数错误: {}", e.getMessage());
            return Result.error("参数错误: " + e.getMessage());
        } catch (Exception e) {
            log.error("用户搜索失败: keyword={}, error={}", searchDTO.getKeyword(), e.getMessage(), e);
            return Result.error("搜索失败，请稍后重试");
        }
    }

}
