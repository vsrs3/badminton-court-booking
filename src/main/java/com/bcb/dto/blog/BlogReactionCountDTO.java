package com.bcb.dto.blog;

public class BlogReactionCountDTO {
    private String emojiCode;
    private int count;

    public BlogReactionCountDTO() {
    }

    public BlogReactionCountDTO(String emojiCode, int count) {
        this.emojiCode = emojiCode;
        this.count = count;
    }

    public String getEmojiCode() {
        return emojiCode;
    }

    public void setEmojiCode(String emojiCode) {
        this.emojiCode = emojiCode;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
