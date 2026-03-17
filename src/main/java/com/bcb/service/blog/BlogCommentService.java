package com.bcb.service.blog;

import com.bcb.dto.blog.BlogCommentViewDTO;
import com.bcb.exception.BusinessException;

import java.util.List;

public interface BlogCommentService {
    List<BlogCommentViewDTO> getCommentsForPublicDetail(int postId, Integer accountId, String role);

    int submitComment(int postId, int authorAccountId, String role, String content) throws BusinessException;

    void editComment(int commentId, int authorAccountId, String role, String content) throws BusinessException;

    void deleteCommentByAuthor(int commentId, int authorAccountId, String role) throws BusinessException;

    void moderateComment(int commentId, int moderatorAccountId, String moderatorRole, String action) throws BusinessException;

    void deleteCommentByModerator(int commentId, int moderatorAccountId, String moderatorRole) throws BusinessException;
}
