package com.xiaoyua.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaoyua.dto.post.TopicSimpleDTO;
import com.xiaoyua.entity.TopicPostPO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 话题-动态关联表数据访问层
 */
@Mapper
public interface jTopicPostMapper extends BaseMapper<TopicPostPO> {

    @Insert({
            "<script>",
            "INSERT INTO topic_posts (post_id, topic_id) VALUES ",
            "<foreach collection='list' item='item' separator=','>",
            "(#{item.postId}, #{item.topicId})",
            "</foreach>",
            "</script>"
    })
    void saveBatch(@Param("list") List<TopicPostPO> list);

    /**
     * 批量查询动态话题信息
     * @param postIds 动态ID列表
     * @return 话题DTO列表
     */
    @Select({
            "<script>",
            "SELECT tp.post_id, t.id, t.name, t.description, t.post_cnt",
            "FROM topic_posts tp",
            "JOIN topics t ON tp.topic_id = t.id",
            "WHERE tp.post_id IN",
            "<foreach collection='postIds' item='id' open='(' separator=',' close=')'>#{id}</foreach>",
            "</script>"
    })
    List<TopicSimpleDTO> selectTopicByPostIds(@Param("postIds") List<Long> postIds);
}