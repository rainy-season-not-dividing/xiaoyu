package com.xiaoyu_j.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.xiaoyu_j.dto.comment.CommentCreateDTO;
import com.xiaoyu_j.vo.comment.CommentVO;

public interface CommentService {
    public void addComment(CommentCreateDTO comment);

    public void deleteComment(Long commentId);

    public IPage<CommentVO> getComments(Long postId, int page, int size, String sort);
    public long getCommentCount(Long postId, String type);
}
