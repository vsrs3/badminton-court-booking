package com.bcb.dto.blog;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BlogPostListItemDTO {
    private Integer postId;
    private String title;
    private String summary;
    private String thumbnailPath;
    private String status;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private String authorName;

    public Integer getPostId() {
        return postId;
    }

    public void setPostId(Integer postId) {
        this.postId = postId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getPublishedAt() {
        return publishedAt;
    }

    public void setPublishedAt(LocalDateTime publishedAt) {
        this.publishedAt = publishedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    private static final DateTimeFormatter VN_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public String getPublishedAtFormatted() {
        return publishedAt != null ? publishedAt.format(VN_FORMAT) : "";
    }

    public String getCreatedAtFormatted() {
        return createdAt != null ? createdAt.format(VN_FORMAT) : "";
    }
}
