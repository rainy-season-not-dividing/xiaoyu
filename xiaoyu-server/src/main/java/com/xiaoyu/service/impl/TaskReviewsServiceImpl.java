package com.xiaoyu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaoyu.entity.TaskReviewsPO;
import com.xiaoyu.mapper.TaskReviewsMapper;
import com.xiaoyu.service.TaskReviewsService;
import org.springframework.stereotype.Service;


@Service
public class TaskReviewsServiceImpl extends ServiceImpl<TaskReviewsMapper, TaskReviewsPO> implements TaskReviewsService {
}
