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
            throw new BusinessException("INVALID_ID", "ID khong hop le.");
        }
        Optional<BlogPost> opt = repo.findPublicPostById(postId);
        if (opt.isEmpty()) {
            throw new BusinessException("NOT_FOUND", "Bai viet khong ton tai.");
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
            throw new BusinessException("INVALID_ID", "ID khong hop le.");
        }
        Optional<BlogPost> opt = repo.findById(postId);
        if (opt.isEmpty()) {
            throw new BusinessException("NOT_FOUND", "Bai viet khong ton tai.");
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
            throw new BusinessException("INVALID_ID", "ID khong hop le.");
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
            throw new BusinessException("NOT_FOUND", "Bai viet khong ton tai.");
        }
    }

    @Override
    public void deletePost(int postId) throws BusinessException {
        if (postId <= 0) {
            throw new BusinessException("INVALID_ID", "ID khong hop le.");
        }
        int updated = repo.softDelete(postId);
        if (updated <= 0) {
            throw new BusinessException("NOT_FOUND", "Bai viet khong ton tai.");
        }
    }

    private void normalizeFilter(BlogPostFilterDTO filter, boolean isPublic) {
        if (filter == null) {
            return;
        }

        filter.setPage(filter.getPage());
        filter.setPageSize(filter.getPageSize());
        filter.setKeyword(normalizeKeyword(filter.getKeyword()));

        Set<String> allowedSort = new HashSet<>(Arrays.asList("title", "created_at", "published_at"));
        if (filter.getSortBy() != null && !allowedSort.contains(filter.getSortBy())) {
            filter.setSortBy(isPublic ? "published_at" : "created_at");
        }

        if (filter.getSortDir() != null
                && !("ASC".equalsIgnoreCase(filter.getSortDir()) || "DESC".equalsIgnoreCase(filter.getSortDir()))) {
            filter.setSortDir("DESC");
        }

        if (!isPublic && filter.getStatus() != null && !filter.getStatus().isBlank()) {
            if (!("DRAFT".equals(filter.getStatus()) || "PUBLISHED".equals(filter.getStatus()))) {
                filter.setStatus(null);
            }
        }
    }

    private String normalizeKeyword(String keyword) {
        if (keyword == null) {
            return null;
        }

        String normalized = keyword.trim().replaceAll("\\s+", " ");
        return normalized.isBlank() ? null : normalized;
    }

    private void validatePost(BlogPost post, boolean isUpdate) throws BusinessException {
        if (post == null) {
            throw new BusinessException("INVALID_INPUT", "Du lieu khong hop le.");
        }

        if (!isUpdate && (post.getAuthorAccountId() == null || post.getAuthorAccountId() <= 0)) {
            throw new BusinessException("INVALID_AUTHOR", "Tai khoan tac gia khong hop le.");
        }

        String title = post.getTitle() != null ? post.getTitle().trim() : "";
        if (title.isBlank()) {
            throw new BusinessException("TITLE_REQUIRED", "Tieu de la bat buoc.");
        }
        if (title.length() > 200) {
            throw new BusinessException("TITLE_TOO_LONG", "Tieu de vuot qua 200 ky tu.");
        }

        if (post.getSummary() != null && post.getSummary().length() > 500) {
            throw new BusinessException("SUMMARY_TOO_LONG", "Tom tat vuot qua 500 ky tu.");
        }

        String content = post.getContent() != null ? post.getContent().trim() : "";
        if (content.isBlank()) {
            throw new BusinessException("CONTENT_REQUIRED", "Noi dung la bat buoc.");
        }

        if (post.getStatus() == null || !("DRAFT".equals(post.getStatus()) || "PUBLISHED".equals(post.getStatus()))) {
            throw new BusinessException("INVALID_STATUS", "Trang thai khong hop le.");
        }

        post.setTitle(title);
    }
}
