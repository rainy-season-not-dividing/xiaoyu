package com.xiaoyua.service;

import java.util.List;

/**
 * 转发服务接口
 *
 * @author xiaoyu
 */
public interface jShareService {

    /**
     * 转发动态（旧版本，兼容性保留）
     *
     * @param postId 动态ID
     * @param userId 转发者ID
     */
    void addShare(Long postId, Long userId);

    /**
     * 转发动态（新版本，支持指定被转发者）
     *
     * @param postId 动态ID
     * @param userId 转发者ID
     * @param sharedUserIds 被转发者ID（可选，如果为null则不发送通知）
     * @param reason 转发附言（可选）
     */
    void addShare(Long postId, Long userId, List<Long> sharedUserIds, String reason);

    /**
     * 获取转发数量
     *
     * @param postId 动态ID
     * @param type 类型
     * @return 转发数量
     */
    long getShareCount(Long postId, String type);
}
