package com.xiaoyua.dto.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

/**
 * 转发消息内容（动态/任务转发）
 *
 * @author xiaoyu
 */
@Data
@NoArgsConstructor
public class ForwardMessageContent implements MessageContent {

    /** 转发项目的ID */
    private Long itemId;

    /** 转发项目的标题 */
    private String title;

    /** 转发项目的内容 */
    private String content;

    /** 转发项目的文件列表 */
    private List<ForwardFile> files;

    /** 转发项目的作者信息 */
    private ForwardAuthor author;

    /** 转发类型（POST或TASK） */
    private String forwardType;

    /**
     * 全参构造函数
     */
    public ForwardMessageContent(Long itemId, String title, String content,
                                 List<ForwardFile> files, ForwardAuthor author, String forwardType) {
        this.itemId = itemId;
        this.title = title;
        this.content = content;
        this.files = files;
        this.author = author;
        this.forwardType = forwardType;
    }

    @Override
    @JsonIgnore
    public String getType() {
        // 根据转发类型返回对应的type
        if (forwardType == null) {
            // 记录警告并返回默认值，便于调试
            System.err.println("警告：ForwardMessageContent的forwardType为null，返回默认值POST");
            return "POST";
        }
        return forwardType;
    }

    /**
     * 转发文件信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ForwardFile {
        /** 文件ID */
        private Long fileId;

        /** 文件URL */
        private String url;

        /** 缩略图URL */
        private String thumbnailUrl;

        /** 文件类型 */
        private String fileType;
    }

    /**
     * 转发作者信息
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ForwardAuthor {
        /** 用户ID */
        private Long userId;

        /** 用户昵称 */
        private String nickname;

        /** 用户头像 */
        private String avatarUrl;
    }
}
