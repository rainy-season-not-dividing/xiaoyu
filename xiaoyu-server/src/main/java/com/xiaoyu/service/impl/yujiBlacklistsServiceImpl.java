package com.xiaoyu.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaoyu.context.BaseContext;
import com.xiaoyu.entity.BlacklistsPO;
import com.xiaoyu.exception.NotAllowedToBlackYourselfException;
import com.xiaoyu.mapper.yujiBlacklistsMapper;
import com.xiaoyu.service.yujiBlacklistsService;
import com.xiaoyu.vo.user.BlacklistsVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class yujiBlacklistsServiceImpl extends ServiceImpl<yujiBlacklistsMapper, BlacklistsPO> implements yujiBlacklistsService {

    @Resource
    private yujiBlacklistsMapper yujiBlacklistsMapper;

    @Override
    public void addBlacklist(Long targetId) {
        Long userId = BaseContext.getId();
        if(targetId == userId)
            throw new NotAllowedToBlackYourselfException("不能将自己加入黑名单");
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
        return yujiBlacklistsMapper.getBlackList(objectPage,userId);
    }


}
