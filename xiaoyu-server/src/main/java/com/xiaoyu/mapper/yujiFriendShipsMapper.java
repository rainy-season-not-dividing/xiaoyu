package com.xiaoyu.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaoyu.entity.FriendshipsPO;
import com.xiaoyu.vo.friend.FriendlistVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface yujiFriendShipsMapper extends BaseMapper<FriendshipsPO> {
    Page<FriendlistVO> getFriendlist(Page<FriendlistVO> pageSet, Long userId, FriendshipsPO.Status status);
    List<FriendlistVO> getFriendlist(Long userId, FriendshipsPO.Status status);
}
