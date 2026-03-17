package com.bcb.model;

import java.time.LocalDateTime;

public class BlogReaction {
    private Integer reactionId;
    private Integer postId;
    private Integer accountId;
    private String emojiCode;
    private LocalDateTime createdAt;

    public Integer getReactionId() {
        return reactionId;
    }

    public void setReactionId(Integer reactionId) {
        this.reactionId = reactionId;
    }

    public Integer getPostId() {
        return postId;
    }

    public void setPostId(Integer postId) {
        this.postId = postId;
    }

    public Integer getAccountId() {
        return accountId;
    }

    public void setAccountId(Integer accountId) {
        this.accountId = accountId;
    }

    public String getEmojiCode() {
        return emojiCode;
    }

    public void setEmojiCode(String emojiCode) {
        this.emojiCode = emojiCode;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
