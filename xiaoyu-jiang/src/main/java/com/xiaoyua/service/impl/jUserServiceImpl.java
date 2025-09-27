package com.xiaoyua.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaoyua.context.BaseContext;
import com.xiaoyua.dto.user.UserSearchDTO;
import com.xiaoyua.entity.FriendPO;
import com.xiaoyua.entity.UserPO;
import com.xiaoyua.mapper.jFriendMapper;
import com.xiaoyua.mapper.jUserMapper;
import com.xiaoyua.service.jUserService;
import com.xiaoyua.vo.user.UserSearchVO;
import com.xiaoyua.vo.user.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

/**
 * 用户服务实现类
 *
 * @author xiaoyu
 */
@Service
@Slf4j
public class jUserServiceImpl implements jUserService {

    @Autowired
    private jUserMapper jUserMapper;

    @Autowired
    private jFriendMapper jFriendMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String FRIEND_CACHE_KEY = "friend:relation:";
    private static final String USER_CACHE_KEY = "user:info:";

    // 数字开头的正则表达式
    private static final Pattern DIGIT_START_PATTERN = Pattern.compile("^\\d.*");

    @Override
    public IPage<UserSearchVO> searchUsers( UserSearchDTO searchDTO) {
        String keyword = searchDTO.getKeyword().trim();
        Long currentUserId = BaseContext.getCurrentId();
        if (keyword.isEmpty()) {
            return new Page<>(searchDTO.getPage(), searchDTO.getSize());
        }

        Page<UserSearchVO> page = new Page<>(searchDTO.getPage(), searchDTO.getSize());
        IPage<UserSearchVO> result;
        // 判断搜索类型：数字开头搜索账号，否则搜索昵称
        if (DIGIT_START_PATTERN.matcher(keyword).matches()) {
            log.info("搜索用户账号: keyword={}, userId={}", keyword, currentUserId);
            result = jUserMapper.searchUsersByAccount(page, keyword,
                    searchDTO.getCampusId(), searchDTO.getOnlyRealName());
            // 设置匹配类型
            result.getRecords().forEach(user -> user.setMatchType("account"));
        } else {
            log.info("搜索用户昵称: keyword={}, userId={}", keyword, currentUserId);
            result = jUserMapper.searchUsersByNickname(page, keyword,
                    searchDTO.getCampusId(), searchDTO.getOnlyRealName());
            // 设置匹配类型
            result.getRecords().forEach(user -> user.setMatchType("nickname"));
        }

        // 设置好友关系状态
        if (currentUserId != null) {
            result.getRecords().forEach(user -> {
                if (!user.getId().equals(currentUserId)) {
                    user.setIsFriend(isFriend(currentUserId, user.getId()));
                }
            });
        }

        log.info("用户搜索完成: keyword={}, matchType={}, total={}",
                keyword,
                DIGIT_START_PATTERN.matcher(keyword).matches() ? "account" : "nickname",
                result.getTotal());

        return result;
    }

    @Override
    public UserVO getUserById(Long currentUserId, Long targetUserId) {
        if (targetUserId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        // 先从缓存获取
        String cacheKey = USER_CACHE_KEY + targetUserId;
        UserVO cachedUser = (UserVO) redisTemplate.opsForValue().get(cacheKey);
        if (cachedUser != null) {
            log.debug("从缓存获取用户信息: userId={}", targetUserId);
            return cachedUser;
        }

        // 从数据库查询
        UserPO userPO = jUserMapper.selectById(targetUserId);
        if (userPO == null) {
            throw new RuntimeException("用户不存在");
        }

        // 转换为VO
        UserVO userVO = convertToUserVO(userPO);

        // 缓存用户信息（30分钟）
        redisTemplate.opsForValue().set(cacheKey, userVO, 30, TimeUnit.MINUTES);

        log.info("获取用户详细信息: targetUserId={}, currentUserId={}", targetUserId, currentUserId);
        return userVO;
    }

    @Override
    public boolean isFriend(Long userId1, Long userId2) {
        if (userId1 == null || userId2 == null || userId1.equals(userId2)) {
            return false;
        }

        // 先从Redis缓存中查询
        String cacheKey = FRIEND_CACHE_KEY + Math.min(userId1, userId2) + ":" + Math.max(userId1, userId2);
        Boolean cached = (Boolean) redisTemplate.opsForValue().get(cacheKey);

        if (cached != null) {
            return cached;
        }

        // 缓存中没有，从数据库查询
        QueryWrapper<FriendPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.and(wrapper ->
                wrapper.eq("user_id", userId1).eq("friend_id", userId2)
                        .or()
                        .eq("user_id", userId2).eq("friend_id", userId1)
        );
        queryWrapper.eq("status", "ACCEPTED");

        long count = jFriendMapper.selectCount(queryWrapper);
        boolean isFriend = count > 0;

        // 更新缓存，缓存30分钟
        redisTemplate.opsForValue().set(cacheKey, isFriend, 30, TimeUnit.MINUTES);

        return isFriend;
    }

    /**
     * 转换UserPO为UserVO
     */
    private UserVO convertToUserVO(UserPO userPO) {
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userPO, userVO);
        return userVO;
    }
}
