package com.xiaoyua.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaoyua.entity.TaskPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 任务Mapper接口
 *
 * @author xiaoyu
 */
@Mapper
public interface jTaskMapper extends BaseMapper<TaskPO> {
}
