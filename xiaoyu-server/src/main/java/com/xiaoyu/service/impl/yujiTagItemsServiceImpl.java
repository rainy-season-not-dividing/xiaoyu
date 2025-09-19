package com.xiaoyu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaoyu.entity.TagItemsPO;
import com.xiaoyu.mapper.yujiTagItemsMapper;
import com.xiaoyu.service.yujiTagItemsService;
import org.springframework.stereotype.Service;


@Service
public class yujiTagItemsServiceImpl extends ServiceImpl<yujiTagItemsMapper, TagItemsPO> implements yujiTagItemsService {
}
