package com.bcb.service.blog.impl;

import com.bcb.dto.blog.BlogCommentViewDTO;
import com.bcb.exception.BusinessException;
import com.bcb.model.BlogComment;
import com.bcb.repository.blog.BlogCommentRepository;
import com.bcb.repository.blog.impl.BlogCommentRepositoryImpl;
import com.bcb.service.blog.BlogCommentService;

import java.util.List;

public class BlogCommentServiceImpl implements BlogCommentService {

    private final BlogCommentRepository repo;

    public BlogCommentServiceImpl() {
        this.repo = new BlogCommentRepositoryImpl();
    }

    @Override
    public List<BlogCommentViewDTO> getCommentsForPublicDetail(int postId, Integer accountId, String role) {
        if (postId <= 0) {
            return List.of();
        }

        if (role != null && ("ADMIN".equals(role) || "OWNER".equals(role) || "STAFF".equals(role))) {
            return repo.findByPostIdForModeration(postId);
        }

        if (accountId != null && accountId > 0 && "CUSTOMER".equals(role)) {
            return repo.findByPostIdForCustomer(postId, accountId);
        }

        return repo.findApprovedByPostId(postId);
    }

    @Override
    public int submitComment(int postId, int authorAccountId, String role, String content) throws BusinessException {
        requireCustomer(role);
        if (postId <= 0) {
            throw new BusinessException("INVALID_ID", "ID bài viết không hợp lệ.");
        }
        String normalized = normalizeContent(content);

        BlogComment c = new BlogComment();
        c.setPostId(postId);
        c.setAuthorAccountId(authorAccountId);
        c.setContent(normalized);
        c.setStatus("PENDING");
        return repo.insert(c);
    }

    @Override
    public void editComment(int commentId, int authorAccountId, String role, String content) throws BusinessException {
        requireCustomer(role);
        if (commentId <= 0) {
            throw new BusinessException("INVALID_ID", "ID bình luận không hợp lệ.");
        }
        String normalized = normalizeContent(content);
        int updated = repo.updateContent(commentId, authorAccountId, normalized);
        if (updated <= 0) {
            throw new BusinessException("NOT_FOUND", "Không tìm thấy bình luận hoặc bạn không có quyền sửa.");
        }
    }

    @Override
    public void deleteCommentByAuthor(int commentId, int authorAccountId, String role) throws BusinessException {
        requireCustomer(role);
        if (commentId <= 0) {
            throw new BusinessException("INVALID_ID", "ID bình luận không hợp lệ.");
        }
        int updated = repo.softDeleteByAuthor(commentId, authorAccountId);
        if (updated <= 0) {
            throw new BusinessException("NOT_FOUND", "Không tìm thấy bình luận hoặc bạn không có quyền xóa.");
        }
    }

    @Override
    public void moderateComment(int commentId, int moderatorAccountId, String moderatorRole, String action) throws BusinessException {
        requireModerator(moderatorRole);
        if (commentId <= 0) {
            throw new BusinessException("INVALID_ID", "ID bình luận không hợp lệ.");
        }

        String status;
        if ("approve".equalsIgnoreCase(action)) {
            status = "APPROVED";
        } else if ("reject".equalsIgnoreCase(action)) {
            status = "REJECTED";
        } else {
            throw new BusinessException("INVALID_ACTION", "Thao tác không hợp lệ.");
        }

        int updated = repo.updateStatus(commentId, status, moderatorAccountId);
        if (updated <= 0) {
            throw new BusinessException("NOT_FOUND", "Không tìm thấy bình luận.");
        }
    }

    @Override
    public void deleteCommentByModerator(int commentId, int moderatorAccountId, String moderatorRole) throws BusinessException {
        requireModerator(moderatorRole);
        if (commentId <= 0) {
            throw new BusinessException("INVALID_ID", "ID bình luận không hợp lệ.");
        }
        int updated = repo.softDeleteByModerator(commentId);
        if (updated <= 0) {
            throw new BusinessException("NOT_FOUND", "Không tìm thấy bình luận.");
        }
    }

    private void requireCustomer(String role) throws BusinessException {
        if (!"CUSTOMER".equals(role)) {
            throw new BusinessException("FORBIDDEN", "Chỉ CUSTOMER mới được bình luận.");
        }
    }

    private void requireModerator(String role) throws BusinessException {
        if (!("ADMIN".equals(role) || "OWNER".equals(role) || "STAFF".equals(role))) {
            throw new BusinessException("FORBIDDEN", "Bạn không có quyền duyệt bình luận.");
        }
    }

    private String normalizeContent(String content) throws BusinessException {
        String normalized = content != null ? content.trim() : "";
        if (normalized.isBlank()) {
            throw new BusinessException("CONTENT_REQUIRED", "Nội dung bình luận là bắt buộc.");
        }
        if (normalized.length() > 1000) {
            throw new BusinessException("CONTENT_TOO_LONG", "Bình luận vượt quá 1000 ký tự.");
        }
        return normalized;
    }
}
