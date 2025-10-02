package com.xiaoyua.service;

import com.xiaoyua.dto.message.PostForwardMessageContent;
import com.xiaoyua.dto.message.TaskForwardMessageContent;

/**
 * 转发消息服务接口
 *
 * @author xiaoyu
 */
public interface ForwardMessageService {

    /**
     * 创建动态转发内容
     *
     * @param postId 动态ID
     * @return 动态转发消息内容
     */
    PostForwardMessageContent createPostForwardContent(Long postId);

    /**
     * 创建任务转发内容
     *
     * @param taskId 任务ID
     * @return 任务转发消息内容
     */
    TaskForwardMessageContent createTaskForwardContent(Long taskId);
}
