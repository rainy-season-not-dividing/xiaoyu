package com.xiaoyua.dto.message;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * 动态转发消息内容
 *
 * @author xiaoyu
 */
@Data
@NoArgsConstructor
public class PostForwardMessageContent implements MessageContent {

    /** 转发项目的ID */
    private Long itemId;

    /** 转发项目的标题 */
    private String title;

    /** 转发项目的内容 */
    private String content;

    /** 转发项目的文件列表 */
    private List<ForwardMessageContent.ForwardFile> files;

    /** 转发项目的作者信息 */
    private ForwardMessageContent.ForwardAuthor author;

    /**
     * 全参构造函数
     */
    public PostForwardMessageContent(Long itemId, String title, String content,
                                     List<ForwardMessageContent.ForwardFile> files,
                                     ForwardMessageContent.ForwardAuthor author) {
        this.itemId = itemId;
        this.title = title;
        this.content = content;
        this.files = files;
        this.author = author;
    }

    @Override
    @JsonIgnore
    public String getType() {
        return "POST";
    }
}
