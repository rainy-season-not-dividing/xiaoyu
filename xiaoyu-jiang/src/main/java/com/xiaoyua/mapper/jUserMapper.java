package com.xiaoyua.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaoyua.entity.UserPO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface jUserMapper extends BaseMapper<UserPO> {
}
