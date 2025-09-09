package com.xiaoyu.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xiaoyu.entity.BlacklistsPO;
import com.xiaoyu.vo.BlacklistsVO;

public interface BlacklistsService extends IService<BlacklistsPO> {
    void addBlacklist(Long targetId);

    void removeFromBlacklist(Long targetId);

    Page<BlacklistsVO> getBlackList(Page<Object> objectPage, Long userId);
}
