package com.xiaoyua.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaoyua.entity.CommentPO;
import com.xiaoyua.vo.comment.CommentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface jCommentMapper extends BaseMapper<CommentPO> {

    /**
     * 分页查询评论列表（联表查询用户信息和点赞状态）
     *
     * @param page 分页参数
     * @param postId 动态ID
     * @param sort 排序方式
     * @param currentUserId 当前用户ID
     * @return 评论VO分页结果
     */
    IPage<CommentVO> selectCommentsWithDetails(Page<CommentVO> page,
                                               @Param("postId") Long postId,
                                               @Param("sort") String sort,
                                               @Param("currentUserId") Long currentUserId);

    /**
     * 批量查询二级评论（联表查询用户信息和点赞状态）
     *
     * @param parentIds 父评论ID列表
     * @param currentUserId 当前用户ID
     * @return 二级评论VO列表
     */
    List<CommentVO> selectSubCommentsWithDetails(@Param("parentIds") List<Long> parentIds,
                                                 @Param("currentUserId") Long currentUserId);
}
