package com.xiaoyua.service.impl;

import com.xiaoyua.dto.message.ForwardMessageContent;
import com.xiaoyua.dto.message.PostForwardMessageContent;
import com.xiaoyua.dto.message.TaskForwardMessageContent;
import com.xiaoyua.entity.FilePO;
import com.xiaoyua.entity.PostPO;
import com.xiaoyua.entity.TaskPO;
import com.xiaoyua.entity.UserPO;
import com.xiaoyua.mapper.jPostMapper;
import com.xiaoyua.mapper.jPostFileMapper;
import com.xiaoyua.mapper.jTaskMapper;
import com.xiaoyua.mapper.jTaskFileMapper;
import com.xiaoyua.mapper.jUserMapper;
import com.xiaoyua.service.ForwardMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 转发消息服务实现类
 *
 * @author xiaoyu
 */
@Service
@Slf4j
public class ForwardMessageServiceImpl implements ForwardMessageService {

    @Autowired
    private jPostMapper jPostMapper;

    @Autowired
    private jTaskMapper jTaskMapper;

    @Autowired
    private jUserMapper jUserMapper;

    @Autowired
    private jPostFileMapper jPostFileMapper;

    @Autowired
    private jTaskFileMapper jTaskFileMapper;

    @Override
    public PostForwardMessageContent createPostForwardContent(Long postId) {
        log.info("创建动态转发内容: postId={}", postId);

        // 查询动态信息
        PostPO post = jPostMapper.selectById(postId);
        if (post == null) {
            throw new RuntimeException("动态不存在: " + postId);
        }

        // 查询作者信息
        UserPO author = jUserMapper.selectById(post.getUserId());
        if (author == null) {
            throw new RuntimeException("动态作者不存在: " + post.getUserId());
        }

        // 查询动态文件
        List<FilePO> postFiles = jPostFileMapper.selectFilesByPostId(postId);
        List<ForwardMessageContent.ForwardFile> files = postFiles.stream()
                .map(this::convertToForwardFile)
                .collect(java.util.stream.Collectors.toList());

        // 构建转发内容
        PostForwardMessageContent content = new PostForwardMessageContent();
        content.setItemId(postId);
        content.setTitle(post.getTitle() != null ? post.getTitle() : "");
        content.setContent(post.getContent());
        content.setFiles(files);
        content.setAuthor(new ForwardMessageContent.ForwardAuthor(
                author.getId(),
                author.getNickname(),
                author.getAvatarUrl()));

        log.info("动态转发内容创建成功: postId={}, title={}", postId, content.getTitle());
        return content;
    }

    @Override
    public TaskForwardMessageContent createTaskForwardContent(Long taskId) {
        log.info("创建任务转发内容: taskId={}", taskId);

        // 查询任务信息
        TaskPO task = jTaskMapper.selectById(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在: " + taskId);
        }

        // 查询发布者信息
        UserPO publisher = jUserMapper.selectById(task.getPublisherId());
        if (publisher == null) {
            throw new RuntimeException("任务发布者不存在: " + task.getPublisherId());
        }

        // 查询任务文件
        List<FilePO> taskFiles = jTaskFileMapper.selectFilesByTaskId(taskId);
        List<ForwardMessageContent.ForwardFile> files = taskFiles.stream()
                .map(this::convertToForwardFile)
                .collect(java.util.stream.Collectors.toList());

        // 构建转发内容
        TaskForwardMessageContent content = new TaskForwardMessageContent();
        content.setItemId(taskId);
        content.setTitle(task.getTitle());
        content.setContent(task.getContent());
        content.setFiles(files);
        content.setAuthor(new ForwardMessageContent.ForwardAuthor(
                publisher.getId(),
                publisher.getNickname(),
                publisher.getAvatarUrl()));

        log.info("任务转发内容创建成功: taskId={}, title={}", taskId, content.getTitle());
        return content;
    }

    /**
     * 转换FilePO为ForwardFile
     */
    private ForwardMessageContent.ForwardFile convertToForwardFile(FilePO filePO) {
        return new ForwardMessageContent.ForwardFile(
                filePO.getId(),
                filePO.getFileUrl(),
                filePO.getThumbUrl(),
                filePO.getBizType() != null ? filePO.getBizType().name() : "UNKNOWN");
    }
}
