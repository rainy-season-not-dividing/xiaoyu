package com.xiaoyua.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaoyua.context.BaseContext;
import com.xiaoyua.entity.TopicPO;
import com.xiaoyua.mapper.jTopicMapper;
import com.xiaoyua.service.jTopicService;
import com.xiaoyua.vo.post.PostVO;
import com.xiaoyua.vo.topic.TopicSimpleVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 话题服务实现类
 */
@Service
@Slf4j
public class jTopicServiceImpl implements jTopicService {

    @Autowired
    private jTopicMapper jTopicMapper;

    @Override
    public List<TopicSimpleVO> getHotTopics(Integer limit) {
        if (limit == null || limit <= 0) {
            limit = 10;
        }
        log.info("获取热门话题列表，limit={}", limit);

        QueryWrapper<TopicPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.gt("post_cnt", 0)
                .orderByDesc("post_cnt")
                .last("LIMIT " + limit);

        List<TopicPO> topicPOs = jTopicMapper.selectList(queryWrapper);
        return topicPOs.stream().map(this::convertToSimpleVO).collect(Collectors.toList());
    }

    @Override
    public List<TopicSimpleVO> getAllTopics() {
        log.info("获取所有话题列表");
        QueryWrapper<TopicPO> wrapper = new QueryWrapper<>();
        wrapper.orderByDesc("post_cnt").orderByAsc("name");
        List<TopicPO> topics = jTopicMapper.selectList(wrapper);
        return topics.stream().map(this::convertToSimpleVO).collect(Collectors.toList());
    }

    @Override
    public TopicSimpleVO getTopicById(Long topicId) {
        log.info("获取话题详情，topicId={}", topicId);

        TopicPO topicPO = jTopicMapper.selectById(topicId);
        if (topicPO == null) {
            return null;
        }

        return convertToSimpleVO(topicPO);
    }

    @Override
    public IPage<PostVO> getPostsByTopicId(Long topicId, Integer page, Integer size, String sort) {
        if (page == null || page <= 0) {
            page = 1;
        }
        if (size == null || size <= 0) {
            size = 20;
        }
        if (sort == null) {
            sort = "latest";
        }

        log.info("获取话题下的动态列表，topicId={}, page={}, size={}, sort={}", topicId, page, size, sort);

        // 获取当前用户ID（用于查询点赞收藏状态）
        Long currentUserId = BaseContext.getCurrentId();

        // 使用联表查询，一次SQL获取所有数据
        Page<PostVO> pageParam = new Page<>(page, size);
        IPage<PostVO> result = jTopicMapper.selectPostsByTopicIdWithDetails(pageParam, topicId, sort, currentUserId);

        log.info("获取话题下的动态列表完成: topicId={}, total={}, pages={}", topicId, result.getTotal(), result.getPages());
        return result;
    }

    /**
     * 转换TopicPO为TopicSimpleVO
     */
    private TopicSimpleVO convertToSimpleVO(TopicPO topicPO) {
        TopicSimpleVO vo = new TopicSimpleVO();
        vo.setId(topicPO.getId());
        vo.setName(topicPO.getName());
        vo.setDescription(topicPO.getDescription());
        vo.setPostCount(topicPO.getPostCnt());
        return vo;
    }

}
