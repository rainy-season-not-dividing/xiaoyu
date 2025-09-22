package com.xiaoyua.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaoyua.entity.PostFilePO;
import com.xiaoyua.mapper.jPostFileMapper;
import com.xiaoyua.service.jPostFileService;
import org.springframework.stereotype.Service;


@Service
public class jPostFileServiceImpl extends ServiceImpl<jPostFileMapper, PostFilePO> implements jPostFileService {
}
