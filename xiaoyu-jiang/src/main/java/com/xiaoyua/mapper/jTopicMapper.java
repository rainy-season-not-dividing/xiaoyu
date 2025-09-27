package com.xiaoyua.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaoyua.entity.TopicPO;
import com.xiaoyua.vo.post.PostVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 话题数据访问层
 */
@Mapper
public interface jTopicMapper extends BaseMapper<TopicPO> {

    /**
     * 分页查询话题下的动态列表（联表查询用户信息和统计数据）
     *
     * @param page 分页参数
     * @param topicId 话题ID
     * @param sort 排序方式
     * @param currentUserId 当前用户ID（用于查询点赞收藏状态）
     * @return 动态VO分页结果
     */
    IPage<PostVO> selectPostsByTopicIdWithDetails(Page<PostVO> page,
                                                  @Param("topicId") Long topicId,
                                                  @Param("sort") String sort,
                                                  @Param("currentUserId") Long currentUserId);
}
