package com.xiaoyu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaoyu.entity.TaskFilesPO;
import com.xiaoyu.mapper.TaskFilesMapper;
import com.xiaoyu.service.TaskFilesService;
import org.springframework.stereotype.Service;


@Service
public class TaskFilesServiceImpl extends ServiceImpl<TaskFilesMapper, TaskFilesPO> implements TaskFilesService {
}
