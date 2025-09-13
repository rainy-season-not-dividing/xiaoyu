package com.xiaoyu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaoyu.entity.FavoritesPO;
import com.xiaoyu.mapper.FavoritesMapper;
import com.xiaoyu.service.FavoritesService;
import org.springframework.stereotype.Service;


@Service
public class FavoritesServiceImpl extends ServiceImpl<FavoritesMapper, FavoritesPO> implements FavoritesService {
}
