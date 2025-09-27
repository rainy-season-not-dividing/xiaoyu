package com.xiaoyua.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xiaoyua.entity.NotificationPO;
import com.xiaoyua.mapper.jNotificationMapper;
import com.xiaoyua.service.jNotificationService;
import com.xiaoyua.vo.notification.NotificationVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Locale;

/**
 * 通知服务实现类
 *
 * @author xiaoyu
 */
@Service
@Slf4j
public class jNotificationServiceImpl implements jNotificationService {

    @Autowired
    private jNotificationMapper jNotificationMapper;

    @Override
    public IPage<NotificationVO> getNotifications(Long userId, String type , Integer page, Integer size) {
        log.info("获取通知列表: userId={}, type={},page={}, size={}", userId, type, page, size);

        // 参数校验和处理
        int p = (page == null || page <= 0) ? 1 : page;
        int sz = (size == null || size <= 0 || size > 100) ? 20 : size; // 限制单页最大100

        // 类型参数处理
        String typeParam = null;
        if (StringUtils.hasText(type)) {
            try {
                NotificationPO.Type t = NotificationPO.Type.valueOf(type.trim().toUpperCase(Locale.ROOT));
                typeParam = t.name();
            } catch (IllegalArgumentException ex) {
                log.warn("忽略非法通知类型筛选: {}", type);
            }
        }

        // 使用联表查询，一次SQL获取所有数据
        Page<NotificationVO> pageParam = new Page<>(p, sz);
        IPage<NotificationVO> result = jNotificationMapper.selectNotificationsWithUser(pageParam, userId, typeParam);

        log.info("获取通知列表完成: userId={}, total={}, pages={}", userId, result.getTotal(), result.getPages());
        return result;
    }

    @Override
    public boolean markAsRead(Long userId, Long notificationId) {
        log.info("标记通知为已读: userId={}, notificationId={}", userId, notificationId);

        UpdateWrapper<NotificationPO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("id", notificationId)
                .eq("user_id", userId)
                .set("status", NotificationPO.Status.READ);

        int updated = jNotificationMapper.update(null, updateWrapper);
        boolean success = updated > 0;

        if (success) {
            log.info("通知标记已读成功: notificationId={}", notificationId);
            // TODO: 如果需要，接入 PushService 推送未读数更新事件
        } else {
            log.warn("通知标记已读失败: notificationId={}", notificationId);
        }

        return success;
    }

    @Override
    public int markAllAsRead(Long userId) {
        log.info("标记所有通知为已读: userId={}", userId);

        UpdateWrapper<NotificationPO> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("user_id", userId)
                .eq("status", NotificationPO.Status.UNREAD)
                .set("status", NotificationPO.Status.READ);

        int updated = jNotificationMapper.update(null, updateWrapper);

        log.info("标记所有通知为已读完成: userId={}, updated={}", userId, updated);
        return updated;
    }

    @Override
    public long getUnreadCount(Long userId) {
        log.debug("获取未读通知数量: userId={}", userId);

        QueryWrapper<NotificationPO> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId)
                .eq("status", NotificationPO.Status.UNREAD);

        long count = jNotificationMapper.selectCount(queryWrapper);
        log.debug("未读通知数量: userId={}, count={}", userId, count);

        return count;
    }

    @Override
    public boolean createNotification(NotificationPO notification) {
        log.info("创建通知: userId={}, type={}, title={}", notification.getUserId(), notification.getType(), notification.getTitle());

        // 设置默认值
        if (notification.getStatus() == null) {
            notification.setStatus(NotificationPO.Status.UNREAD);
        }
        if (notification.getCreatedAt() == null) {
            notification.setCreatedAt(LocalDateTime.now());
        }

        int inserted = jNotificationMapper.insert(notification);
        boolean success = inserted > 0;

        return success;
    }


}
