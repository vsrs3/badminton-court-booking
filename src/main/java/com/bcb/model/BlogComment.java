package com.bcb.model;

import java.time.LocalDateTime;

public class BlogComment {
    private Integer commentId;
    private Integer postId;
    private Integer authorAccountId;
    private String content;
    private String status;
    private Integer moderatedByAccountId;
    private LocalDateTime moderatedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isDeleted;

    public Integer getCommentId() {
        return commentId;
    }

    public void setCommentId(Integer commentId) {
        this.commentId = commentId;
    }

    public Integer getPostId() {
        return postId;
    }

    public void setPostId(Integer postId) {
        this.postId = postId;
    }

    public Integer getAuthorAccountId() {
        return authorAccountId;
    }

    public void setAuthorAccountId(Integer authorAccountId) {
        this.authorAccountId = authorAccountId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getModeratedByAccountId() {
        return moderatedByAccountId;
    }

    public void setModeratedByAccountId(Integer moderatedByAccountId) {
        this.moderatedByAccountId = moderatedByAccountId;
    }

    public LocalDateTime getModeratedAt() {
        return moderatedAt;
    }

    public void setModeratedAt(LocalDateTime moderatedAt) {
        this.moderatedAt = moderatedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Boolean getIsDeleted() {
        return isDeleted;
    }

    public void setIsDeleted(Boolean deleted) {
        isDeleted = deleted;
    }
}
