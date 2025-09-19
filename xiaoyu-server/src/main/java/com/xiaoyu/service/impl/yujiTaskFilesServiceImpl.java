package com.xiaoyu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaoyu.entity.TaskFilesPO;
import com.xiaoyu.mapper.yujiTaskFilesMapper;
import com.xiaoyu.service.yujiTaskFilesService;
import org.springframework.stereotype.Service;


@Service
public class yujiTaskFilesServiceImpl extends ServiceImpl<yujiTaskFilesMapper, TaskFilesPO> implements yujiTaskFilesService {
}
