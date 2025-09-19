package com.xiaoyua.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xiaoyua.entity.NotificationPO;
import com.xiaoyua.entity.PostPO;
import com.xiaoyua.entity.SharePO;
import com.xiaoyua.entity.UserPO;
import com.xiaoyua.mapper.jPostMapper;
import com.xiaoyua.mapper.jPostStatMapper;
import com.xiaoyua.mapper.jShareMapper;
import com.xiaoyua.mapper.jUserMapper;
import com.xiaoyua.service.jPushService;
import com.xiaoyua.service.jShareService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class jShareServiceImpl implements jShareService {
    @Autowired
    jShareMapper jShareMapper;
    
    @Autowired
    private jPostMapper jPostMapper;
    
    @Autowired
    private jUserMapper jUserMapper;
    
    @Autowired
    private jPostStatMapper jPostStatMapper;
    
    @Autowired
    private jPushService jPushService;
    
    @Override
    public void addShare(Long postId, Long userId) {
        SharePO share=new SharePO();
        share.setUserId(userId);
        share.setItemId(postId);
        share.setItemType(SharePO.ItemType.POST);
        share.setCreatedAt(LocalDateTime.now());
        jShareMapper.insert(share);
        // 更新统计：分享+1
        try { jPostStatMapper.incShare(postId); } catch (Exception ignored) {}
        
        // 创建分享通知
        createShareNotification(postId, userId);
    }

    @Override
    public long getShareCount(Long postId,String type){
        return jShareMapper.selectCount(
                new QueryWrapper<SharePO>()
                        .eq("item_id", postId)
                        .eq("item_type", type.toUpperCase())
        );
    }
    
    /**
     * 创建分享通知
     */
    private void createShareNotification(Long postId, Long fromUserId) {
        try {
            // 获取动态作者ID
            PostPO post = jPostMapper.selectById(postId);
            if (post == null) {
                return;
            }
            
            Long toUserId = post.getUserId();
            
            // 不给自己发通知
            if (toUserId.equals(fromUserId)) {
                return;
            }
            
            // 获取分享用户信息
            UserPO fromUser = jUserMapper.selectById(fromUserId);
            if (fromUser == null) {
                return;
            }
            
            // 构建通知内容
            String title = "动态被分享";
            String content = String.format("%s 分享了你的动态", fromUser.getNickname());
            
            // 使用PushService发送通知
            jPushService.pushNotification(
                toUserId,
                NotificationPO.Type.SHARE.name(),
                title,
                content,
                postId,
                NotificationPO.RefType.POST.name(),
                fromUserId
            );
            
        } catch (Exception e) {
            // 使用简单的日志记录，避免引入额外依赖
            System.err.println("创建分享通知失败: postId=" + postId + ", fromUserId=" + fromUserId + ", error=" + e.getMessage());
        }
    }

}
