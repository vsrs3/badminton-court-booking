package com.bcb.repository.blog;

import com.bcb.dto.blog.BlogCommentViewDTO;
import com.bcb.model.BlogComment;

import java.util.List;

public interface BlogCommentRepository {
    List<BlogCommentViewDTO> findApprovedByPostId(int postId);

    List<BlogCommentViewDTO> findByPostIdForModeration(int postId);

    List<BlogCommentViewDTO> findByPostIdForCustomer(int postId, int customerAccountId);

    int insert(BlogComment comment);

    int updateContent(int commentId, int authorAccountId, String content);

    int softDeleteByAuthor(int commentId, int authorAccountId);

    int softDeleteByModerator(int commentId);

    int updateStatus(int commentId, String status, int moderatedByAccountId);
}
