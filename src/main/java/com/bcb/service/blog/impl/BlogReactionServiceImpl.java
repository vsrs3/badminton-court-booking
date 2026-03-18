package com.bcb.service.blog.impl;

import com.bcb.dto.blog.BlogReactionCountDTO;
import com.bcb.exception.BusinessException;
import com.bcb.repository.blog.BlogReactionRepository;
import com.bcb.repository.blog.impl.BlogReactionRepositoryImpl;
import com.bcb.service.blog.BlogReactionService;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BlogReactionServiceImpl implements BlogReactionService {

    private final BlogReactionRepository repo;

    public BlogReactionServiceImpl() {
        this.repo = new BlogReactionRepositoryImpl();
    }

    @Override
    public List<BlogReactionCountDTO> countReactions(int postId) {
        if (postId <= 0) return List.of();
        return repo.countByPostId(postId);
    }

    @Override
    public Set<String> getUserReactions(int postId, Integer accountId) {
        if (postId <= 0 || accountId == null || accountId <= 0) return Set.of();
        return repo.findUserReactions(postId, accountId);
    }

    @Override
    public void toggleReaction(int postId, int accountId, String role, String emojiCode, boolean add) throws BusinessException {
        if (accountId <= 0) {
            throw new BusinessException("FORBIDDEN", "Bạn cần đăng nhập để thả cảm xúc.");
        }

        if (!("CUSTOMER".equals(role) || "ADMIN".equals(role) || "OWNER".equals(role) || "STAFF".equals(role))) {
            throw new BusinessException("FORBIDDEN", "Bạn không có quyền thả cảm xúc.");
        }

        if (postId <= 0) {
            throw new BusinessException("INVALID_ID", "ID bài viết không hợp lệ.");
        }

        Set<String> allowed = new HashSet<>(Arrays.asList("LIKE", "HEART", "LAUGH", "WOW", "SAD", "ANGRY"));
        if (emojiCode == null || !allowed.contains(emojiCode)) {
            throw new BusinessException("INVALID_EMOJI", "Emoji không hợp lệ.");
        }

        if (add) {
            repo.insertReaction(postId, accountId, emojiCode);
        } else {
            repo.deleteReaction(postId, accountId, emojiCode);
        }
    }
}
