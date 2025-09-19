package com.xiaoyu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaoyu.entity.FavoritesPO;
import com.xiaoyu.mapper.yujiFavoritesMapper;
import com.xiaoyu.service.yujiFavoritesService;
import org.springframework.stereotype.Service;


@Service
public class yujiFavoritesServiceImpl extends ServiceImpl<yujiFavoritesMapper, FavoritesPO> implements yujiFavoritesService {
}
