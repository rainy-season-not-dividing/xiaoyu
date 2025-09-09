package com.xiaoyu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaoyu.context.BaseContext;
import com.xiaoyu.entity.BlacklistsPO;
import com.xiaoyu.mapper.BlacklistsMapper;
import com.xiaoyu.service.BlacklistsService;
import com.xiaoyu.vo.BlacklistsVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class BlacklistsServiceImpl extends ServiceImpl<BlacklistsMapper, BlacklistsPO> implements BlacklistsService {

    @Resource
    private BlacklistsMapper blacklistsMapper;

    @Override
    public void addBlacklist(Long targetId) {
        Long userId = BaseContext.getId();
        BlacklistsPO blacklistsPO = new BlacklistsPO();
        blacklistsPO.setOwnerId(userId);
        blacklistsPO.setTargetId(targetId);
        save(blacklistsPO);
    }

    @Override
    public void removeFromBlacklist(Long targetId) {
        LambdaQueryWrapper<BlacklistsPO> queryWrapper = new LambdaQueryWrapper<BlacklistsPO>()
                .eq(BlacklistsPO::getOwnerId, BaseContext.getId())
                .eq(BlacklistsPO::getTargetId, targetId);
        remove(queryWrapper);
    }

    @Override
    public Page<BlacklistsVO> getBlackList(Page<Object> objectPage, Long userId) {
        return blacklistsMapper.getBlackList(objectPage,userId);
    }


}
