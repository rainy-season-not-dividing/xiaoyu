package com.xiaoyu_j.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaoyu_j.entity.TopicPostPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 话题-动态关联表数据访问层
 */
@Mapper
public interface TopicPostMapper extends BaseMapper<TopicPostPO> {
}
