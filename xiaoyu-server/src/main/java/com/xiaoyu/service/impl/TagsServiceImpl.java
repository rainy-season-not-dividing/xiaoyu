package com.xiaoyu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaoyu.entity.TagsPO;
import com.xiaoyu.mapper.TagsMapper;
import com.xiaoyu.service.TagsService;
import org.springframework.stereotype.Service;


@Service
public class TagsServiceImpl extends ServiceImpl<TagsMapper, TagsPO> implements TagsService {
}
