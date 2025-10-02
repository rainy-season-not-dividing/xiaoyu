package com.xiaoyua.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xiaoyua.entity.LikePO;
import com.xiaoyua.entity.NotificationPO;
import com.xiaoyua.entity.PostPO;
import com.xiaoyua.entity.SharePO;
import com.xiaoyua.mapper.jPostMapper;
import com.xiaoyua.mapper.jPostStatMapper;
import com.xiaoyua.mapper.jShareMapper;
import com.xiaoyua.service.jPushService;
import com.xiaoyua.service.jShareService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.LongFunction;

/**
 * 转发服务实现类
 *
 * @author xiaoyu
 */
@Service
@Slf4j
public class jShareServiceImpl implements jShareService {

    @Autowired
    private jShareMapper jShareMapper;

    @Autowired
    private jPostMapper jPostMapper;

    @Autowired
    private jPostStatMapper jPostStatMapper;

    @Autowired
    private jPushService jPushService;

    @Override
    public void addShare(Long postId, Long userId) {
        // 调用新版本方法，不指定被转发者（保持向后兼容）
        addShare(postId, userId, null, null);
    }

    @Override
    public void addShare(Long postId, Long userId, List<Long> sharedUserIds, String reason) {
        log.info("转发动态: postId={}, userId={}, sharedUserIds={}, reason={}",
                postId, userId, sharedUserIds, reason);
        // 创建转发记录
        SharePO share = new SharePO();
        share.setUserId(userId);
        share.setItemId(postId);
        share.setItemType(SharePO.ItemType.POST);
        share.setCreatedAt(LocalDateTime.now());

        // 设置转发附言
        if (StringUtils.hasText(reason)) {
            share.setReason(reason);
        }

        // 保存转发记录
        int insertResult = jShareMapper.insert(share);
        if (insertResult <= 0) {
            throw new RuntimeException("转发记录保存失败");
        }

        // 更新统计：转发数+1（只增加一次，不管转发给多少人）
        try {
            jPostStatMapper.incShare(postId);
            log.debug("转发统计更新成功: postId={}", postId);
        } catch (Exception e) {
            log.warn("更新转发统计失败: postId={}, error={}", postId, e.getMessage());
            // 统计更新失败不影响主流程
        }

        // 发送转发通知给被转发的用户
        if (sharedUserIds != null && !sharedUserIds.isEmpty()) {
            log.info("开始发送转发通知，目标用户数量: {}, 用户列表: {}", sharedUserIds.size(), sharedUserIds);
            for (Long sharedUserId : sharedUserIds) {
                if (sharedUserId != null) {
                    log.info("正在发送转发通知给用户: {}", sharedUserId);
                    try {
                        createShareNotificationToSharedUser(postId, userId, sharedUserId);
                        log.info("转发通知发送完成: postId={}, fromUserId={}, toUserId={}", postId, userId, sharedUserId);
                    } catch (Exception e) {
                        log.error("发送转发通知失败: postId={}, userId={}, sharedUserId={}, error={}",
                                postId, userId, sharedUserId, e.getMessage(), e);
                        // 通知发送失败不影响主流程
                    }
                } else {
                    log.warn("跳过空的用户ID: postId={}, userId={}", postId, userId);
                }
            }
            log.info("转发通知发送循环结束");
        } else {
            log.debug("未指定转发目标用户，不发送转发通知: postId={}, userId={}", postId, userId);
        }

        log.info("转发动态完成: postId={}, userId={}, shareId={}", postId, userId, share.getId());
    }

    @Override
    public long getShareCount(Long postId, String type) {
        return jShareMapper.selectCount(
                new QueryWrapper<SharePO>()
                        .eq("item_id", postId)
                        .eq("item_type", type.toUpperCase())
        );
    }

    /**
     * 创建转发通知（新逻辑：只向被转发者发送通知）
     *
     * @param postId 动态ID
     * @param fromUserId 转发者ID
     * @param sharedUserId 被转发者ID
     */
    private void createShareNotificationToSharedUser(Long postId, Long fromUserId, Long sharedUserId) {
        try {
            // 如果没有指定被转发者，则不发送通知
            if (sharedUserId == null) {
                log.debug("未指定被转发者，不发送转发通知: postId={}, fromUserId={}", postId, fromUserId);
                return;
            }

            // 不给自己发通知
            if (sharedUserId.equals(fromUserId)) {
                log.debug("不给自己发转发通知: postId={}, userId={}", postId, fromUserId);
                return;
            }

            // 验证动态是否存在
            PostPO post = jPostMapper.selectById(postId);
            if (post == null) {
                log.warn("动态不存在，无法发送转发通知: postId={}", postId);
                return;
            }

            // 使用PushService发送转发通知给被转发者
            jPushService.pushShareNotification(
                    sharedUserId,  // 接收者：被转发者
                    fromUserId,    // 发送者：转发者
                    postId,        // 关联动态ID
                    NotificationPO.RefType.POST.name()
            );

            log.info("转发通知发送成功: postId={}, fromUserId={}, sharedUserId={}",
                    postId, fromUserId, sharedUserId);

        } catch (Exception e) {
            log.error("创建转发通知失败: postId={}, fromUserId={}, sharedUserId={}, error={}",
                    postId, fromUserId, sharedUserId, e.getMessage(), e);
        }
    }
}
