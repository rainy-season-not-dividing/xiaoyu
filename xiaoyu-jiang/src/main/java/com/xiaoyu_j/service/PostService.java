package com.xiaoyu_j.service;

import com.xiaoyu_j.dto.post.PostQueryDTO;
import com.xiaoyu_j.dto.post.PostUpdateDTO;
import com.xiaoyu_j.vo.common.PageResult;
import com.xiaoyu_j.vo.post.PostVO;
import com.xiaoyu_j.vo.search.PostSearchVO;

public interface PostService {

    /**
     * 获取动态列表
     * @param postQueryDTO 查询条件
     * @return 分页结果
     */
    PageResult<PostVO> getPosts(PostQueryDTO postQueryDTO);
    
    /**
     * 获取动态详情
     * @param postId 动态ID
     * @return 动态详情
     */
    PostVO getPostDetail(Long postId);

    /**
     * 更新动态
     * @param postUpdateDTO
     * @param postId
     */
    void updatePost(PostUpdateDTO postUpdateDTO,Long postId);

    /**
     * 搜索动态
     *
     * @param keyword
     * @param page
     * @param size
     * @return
     */
    PageResult<PostSearchVO> searchExact(String keyword, Integer page, Integer size);
}
