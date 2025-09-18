package com.xiaoyu_j.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaoyu_j.entity.TopicPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 话题数据访问层
 */
@Mapper
public interface TopicMapper extends BaseMapper<TopicPO> {
}
