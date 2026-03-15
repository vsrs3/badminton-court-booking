package com.bcb.service.blog;

import com.bcb.dto.blog.BlogReactionCountDTO;
import com.bcb.exception.BusinessException;

import java.util.List;
import java.util.Set;

public interface BlogReactionService {
    List<BlogReactionCountDTO> countReactions(int postId);

    Set<String> getUserReactions(int postId, Integer accountId);

    void toggleReaction(int postId, int accountId, String role, String emojiCode, boolean add) throws BusinessException;
}
