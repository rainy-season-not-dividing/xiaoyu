package com.xiaoyu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaoyu.entity.TaskStatsPO;
import com.xiaoyu.mapper.yujiTaskStatsMapper;
import com.xiaoyu.service.yujiTaskStatsService;
import org.springframework.stereotype.Service;

@Service
public class yujiTaskStatsServiceImpl extends ServiceImpl<yujiTaskStatsMapper, TaskStatsPO> implements yujiTaskStatsService {
}
