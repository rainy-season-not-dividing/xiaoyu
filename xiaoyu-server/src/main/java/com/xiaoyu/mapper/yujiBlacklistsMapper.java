package com.xiaoyu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaoyu.entity.BlacklistsPO;
import com.xiaoyu.vo.user.BlacklistsVO;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface yujiBlacklistsMapper extends BaseMapper<BlacklistsPO> {

    Page<BlacklistsVO> getBlackList(Page<Object> objectPage, Long userId);
}
