package com.bcb.repository.blog;

import com.bcb.dto.blog.BlogReactionCountDTO;

import java.util.List;
import java.util.Set;

public interface BlogReactionRepository {
    List<BlogReactionCountDTO> countByPostId(int postId);

    Set<String> findUserReactions(int postId, int accountId);

    boolean insertReaction(int postId, int accountId, String emojiCode);

    boolean deleteReaction(int postId, int accountId, String emojiCode);
}
