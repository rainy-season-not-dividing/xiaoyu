package com.xiaoyu_j.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xiaoyu_j.entity.FriendPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * 好友关系Mapper接口
 * 
 * @author xiaoyu
 */
@Mapper
public interface FriendMapper extends BaseMapper<FriendPO> {
}
