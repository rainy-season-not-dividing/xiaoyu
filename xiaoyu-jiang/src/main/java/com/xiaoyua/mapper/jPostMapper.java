package com.xiaoyua.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaoyua.entity.PostPO;
import com.xiaoyua.vo.post.PostVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface jPostMapper extends BaseMapper<PostPO>{
    /**
     * 查询热门动态（按点赞数倒序）
     * 说明：依赖 post_stats.like_cnt 字段
     */
    @Select("SELECT p.* FROM posts p " +
            "LEFT JOIN post_stats s ON s.post_id = p.id " +
            "WHERE p.status = 'PUBLISHED' AND p.visibility = 'PUBLIC' " +
            "ORDER BY s.view_cnt DESC, p.created_at DESC " +
            "LIMIT #{limit}")
    List<PostPO> selectHotPosts(@Param("limit") int limit);

    /**
     * 查询用户点赞的动态列表
     */
    List<PostPO> selectLikedPostsByUser(@Param("userId") Long userId,
                                        @Param("offset") int offset,
                                        @Param("size") int size,
                                        @Param("sort") String sort);

    /**
     * 统计用户点赞的动态总数
     */
    long countLikedPostsByUser(@Param("userId") Long userId);

    /**
     * 查询用户收藏的动态列表
     */
    List<PostPO> selectFavoritedPostsByUser(@Param("userId") Long userId,
                                            @Param("offset") int offset,
                                            @Param("size") int size,
                                            @Param("sort") String sort);

    /**
     * 统计用户收藏的动态总数
     */
    long countFavoritedPostsByUser(@Param("userId") Long userId);

    /**
     * 分页查询动态列表（联表查询用户信息、统计数据、用户操作状态）
     *
     * @param page 分页参数
     * @param currentUserId 当前用户ID
     * @return 动态VO分页结果
     */
    IPage<PostVO> selectPostsWithDetails(Page<PostVO> page,
                                         @Param("currentUserId") Long currentUserId);

    /**
     * 批量查询动态详情（联表查询用户信息、统计数据、用户操作状态）
     *
     * @param postIds 动态ID列表
     * @param currentUserId 当前用户ID
     * @return 动态VO列表
     */
    List<PostVO> selectPostsWithDetailsByIds(@Param("postIds") List<Long> postIds,
                                             @Param("currentUserId") Long currentUserId);
}
