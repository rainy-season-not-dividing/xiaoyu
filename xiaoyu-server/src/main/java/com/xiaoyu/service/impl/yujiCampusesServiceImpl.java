package com.xiaoyu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaoyu.entity.CampusPO;
import com.xiaoyu.mapper.yujiCampusesMapper;
import com.xiaoyu.service.yujiCampusesService;
import org.springframework.stereotype.Service;


@Service
public class yujiCampusesServiceImpl extends ServiceImpl<yujiCampusesMapper, CampusPO> implements yujiCampusesService {
}
