package com.xiaoyu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaoyu.entity.TaskReviewsPO;
import com.xiaoyu.mapper.yujiTaskReviewsMapper;
import com.xiaoyu.service.yujiTaskReviewsService;
import org.springframework.stereotype.Service;


@Service
public class yujiTaskReviewsServiceImpl extends ServiceImpl<yujiTaskReviewsMapper, TaskReviewsPO> implements yujiTaskReviewsService {
}
