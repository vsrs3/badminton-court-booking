package com.bcb.dto.blog;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BlogCommentViewDTO {
    private Integer commentId;
    private Integer postId;
    private Integer authorAccountId;
    private String authorName;
    private String content;
    private String status;
    private LocalDateTime createdAt;

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

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    private static final DateTimeFormatter VN_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public String getCreatedAtFormatted() {
        return createdAt != null ? createdAt.format(VN_FORMAT) : "";
    }
}
