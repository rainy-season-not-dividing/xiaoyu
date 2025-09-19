package com.xiaoyu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaoyu.entity.TagsPO;
import com.xiaoyu.mapper.yujiTagsMapper;
import com.xiaoyu.service.yujiTagsService;
import org.springframework.stereotype.Service;


@Service
public class yujiTagsServiceImpl extends ServiceImpl<yujiTagsMapper, TagsPO> implements yujiTagsService {
}
