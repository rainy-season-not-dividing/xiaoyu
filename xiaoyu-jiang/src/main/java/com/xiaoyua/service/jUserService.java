package com.xiaoyua.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xiaoyua.dto.user.UserSearchDTO;
import com.xiaoyua.vo.user.UserSearchVO;
import com.xiaoyua.vo.user.UserVO;

/**
 * 用户服务接口
 *
 * @author xiaoyu
 */
public interface jUserService {

    /**
     * 搜索用户
     * 根据关键词智能搜索用户：
     * - 如果关键词以数字开头，则搜索账号
     * - 否则搜索昵称
     *
     * @param searchDTO 搜索条件
     * @return 搜索结果分页
     */
    IPage<UserSearchVO> searchUsers( UserSearchDTO searchDTO);

    /**
     * 根据用户ID获取用户详细信息
     *
     * @param currentUserId 当前用户ID
     * @param targetUserId 目标用户ID
     * @return 用户详细信息
     */
    UserVO getUserById(Long currentUserId, Long targetUserId);

    /**
     * 检查两个用户是否为好友关系
     *
     * @param userId1 用户1的ID
     * @param userId2 用户2的ID
     * @return 是否为好友
     */
    boolean isFriend(Long userId1, Long userId2);
}
