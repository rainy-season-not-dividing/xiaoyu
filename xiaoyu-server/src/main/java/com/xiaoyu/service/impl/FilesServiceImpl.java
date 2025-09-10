package com.xiaoyu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaoyu.entity.TaskFilesPO;
import com.xiaoyu.mapper.FilesMapper;
import com.xiaoyu.service.FilesService;
import org.springframework.stereotype.Service;


@Service
public class FilesServiceImpl extends ServiceImpl<FilesMapper, TaskFilesPO> implements FilesService {
}
