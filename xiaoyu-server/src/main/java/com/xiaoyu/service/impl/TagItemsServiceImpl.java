package com.xiaoyu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaoyu.entity.TagItemsPO;
import com.xiaoyu.mapper.TagItemsMapper;
import com.xiaoyu.service.TagItemsService;
import org.springframework.stereotype.Service;


@Service
public class TagItemsServiceImpl extends ServiceImpl<TagItemsMapper, TagItemsPO> implements TagItemsService {
}
