package com.bcb.service.blog.impl;

import com.bcb.dto.blog.BlogPostFilterDTO;
import com.bcb.dto.blog.BlogPostListItemDTO;
import com.bcb.exception.BusinessException;
import com.bcb.model.BlogPost;
import com.bcb.repository.blog.BlogPostRepository;
import com.bcb.repository.blog.impl.BlogPostRepositoryImpl;
import com.bcb.service.blog.BlogPostService;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class BlogPostServiceImpl implements BlogPostService {

    private final BlogPostRepository repo;

    public BlogPostServiceImpl() {
        this.repo = new BlogPostRepositoryImpl();
    }

    @Override
    public List<BlogPostListItemDTO> getPublicPosts(BlogPostFilterDTO filter) {
        normalizeFilter(filter, true);
        return repo.findPublicPosts(filter);
    }

    @Override
    public int countPublicPosts(BlogPostFilterDTO filter) {
        normalizeFilter(filter, true);
        return repo.countPublicPosts(filter);
    }

    @Override
    public BlogPost getPublicPostById(int postId) throws BusinessException {
        if (postId <= 0) {
            throw new BusinessException("INVALID_ID", "ID không hợp lệ.");
        }
        Optional<BlogPost> opt = repo.findPublicPostById(postId);
        if (opt.isEmpty()) {
            throw new BusinessException("NOT_FOUND", "Bài viết không tồn tại.");
        }
        return opt.get();
    }

    @Override
    public List<BlogPostListItemDTO> getManagePosts(BlogPostFilterDTO filter) {
        normalizeFilter(filter, false);
        return repo.findManagePosts(filter);
    }

    @Override
    public int countManagePosts(BlogPostFilterDTO filter) {
        normalizeFilter(filter, false);
        return repo.countManagePosts(filter);
    }

    @Override
    public BlogPost getManagePostById(int postId) throws BusinessException {
        if (postId <= 0) {
            throw new BusinessException("INVALID_ID", "ID không hợp lệ.");
        }
        Optional<BlogPost> opt = repo.findById(postId);
        if (opt.isEmpty()) {
            throw new BusinessException("NOT_FOUND", "Bài viết không tồn tại.");
        }
        return opt.get();
    }

    @Override
    public int createPost(BlogPost post) throws BusinessException {
        validatePost(post, false);

        if ("PUBLISHED".equals(post.getStatus())) {
            post.setPublishedAt(LocalDateTime.now());
        } else {
            post.setPublishedAt(null);
        }

        return repo.insert(post);
    }

    @Override
    public void updatePost(BlogPost post) throws BusinessException {
        if (post.getPostId() == null || post.getPostId() <= 0) {
            throw new BusinessException("INVALID_ID", "ID không hợp lệ.");
        }

        validatePost(post, true);

        if ("PUBLISHED".equals(post.getStatus())) {
            if (post.getPublishedAt() == null) {
                post.setPublishedAt(LocalDateTime.now());
            }
        } else {
            post.setPublishedAt(null);
        }

        int updated = repo.update(post);
        if (updated <= 0) {
            throw new BusinessException("NOT_FOUND", "Bài viết không tồn tại.");
        }
    }

    @Override
    public void deletePost(int postId) throws BusinessException {
        if (postId <= 0) {
            throw new BusinessException("INVALID_ID", "ID không hợp lệ.");
        }
        int updated = repo.softDelete(postId);
        if (updated <= 0) {
            throw new BusinessException("NOT_FOUND", "Bài viết không tồn tại.");
        }
    }

    private void normalizeFilter(BlogPostFilterDTO filter, boolean isPublic) {
        if (filter == null) {
            return;
        }

        filter.setPage(filter.getPage());
        filter.setPageSize(filter.getPageSize());

        Set<String> allowedSort = new HashSet<>(Arrays.asList("title", "created_at", "published_at"));
        if (filter.getSortBy() != null && !allowedSort.contains(filter.getSortBy())) {
            filter.setSortBy(isPublic ? "published_at" : "created_at");
        }

        if (filter.getSortDir() != null && !("ASC".equalsIgnoreCase(filter.getSortDir()) || "DESC".equalsIgnoreCase(filter.getSortDir()))) {
            filter.setSortDir("DESC");
        }

        if (!isPublic) {
            if (filter.getStatus() != null && !filter.getStatus().isBlank()) {
                if (!("DRAFT".equals(filter.getStatus()) || "PUBLISHED".equals(filter.getStatus()))) {
                    filter.setStatus(null);
                }
            }
        }
    }

    private void validatePost(BlogPost post, boolean isUpdate) throws BusinessException {
        if (post == null) {
            throw new BusinessException("INVALID_INPUT", "Dữ liệu không hợp lệ.");
        }

        if (!isUpdate) {
            if (post.getAuthorAccountId() == null || post.getAuthorAccountId() <= 0) {
                throw new BusinessException("INVALID_AUTHOR", "Tài khoản tác giả không hợp lệ.");
            }
        }

        String title = post.getTitle() != null ? post.getTitle().trim() : "";
        if (title.isBlank()) {
            throw new BusinessException("TITLE_REQUIRED", "Tiêu đề là bắt buộc.");
        }
        if (title.length() > 200) {
            throw new BusinessException("TITLE_TOO_LONG", "Tiêu đề vượt quá 200 ký tự.");
        }

        if (post.getSummary() != null && post.getSummary().length() > 500) {
            throw new BusinessException("SUMMARY_TOO_LONG", "Tóm tắt vượt quá 500 ký tự.");
        }

        String content = post.getContent() != null ? post.getContent().trim() : "";
        if (content.isBlank()) {
            throw new BusinessException("CONTENT_REQUIRED", "Nội dung là bắt buộc.");
        }

        if (post.getStatus() == null || !("DRAFT".equals(post.getStatus()) || "PUBLISHED".equals(post.getStatus()))) {
            throw new BusinessException("INVALID_STATUS", "Trạng thái không hợp lệ.");
        }

        post.setTitle(title);
    }
}
