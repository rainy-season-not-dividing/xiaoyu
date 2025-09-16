package com.xiaoyu.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaoyu.entity.FriendshipsPO;
import com.xiaoyu.vo.friend.FriendlistVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FriendShipsMapper extends BaseMapper<FriendshipsPO> {
    Page<FriendlistVO> getFriendlist(Page<FriendlistVO> pageSet, Long userId, FriendshipsPO.Status status);
}
