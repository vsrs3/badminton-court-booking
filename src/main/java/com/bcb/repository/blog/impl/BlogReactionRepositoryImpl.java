package com.bcb.repository.blog.impl;

import com.bcb.dto.blog.BlogReactionCountDTO;
import com.bcb.exception.DataAccessException;
import com.bcb.repository.blog.BlogReactionRepository;
import com.bcb.utils.DBContext;

import java.sql.*;
import java.util.*;

public class BlogReactionRepositoryImpl implements BlogReactionRepository {

    @Override
    public List<BlogReactionCountDTO> countByPostId(int postId) {
        String sql = "SELECT emoji_code, COUNT(*) AS cnt FROM BlogReaction WHERE post_id = ? GROUP BY emoji_code";
        List<BlogReactionCountDTO> list = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, postId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new BlogReactionCountDTO(rs.getString("emoji_code"), rs.getInt("cnt")));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to count reactions", e);
        }
        return list;
    }

    @Override
    public Set<String> findUserReactions(int postId, int accountId) {
        String sql = "SELECT emoji_code FROM BlogReaction WHERE post_id = ? AND account_id = ?";
        Set<String> set = new HashSet<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, postId);
            ps.setInt(2, accountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) set.add(rs.getString("emoji_code"));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find user reactions", e);
        }
        return set;
    }

    @Override
    public boolean insertReaction(int postId, int accountId, String emojiCode) {
        String sql = "INSERT INTO BlogReaction (post_id, account_id, emoji_code, created_at) VALUES (?, ?, ?, GETDATE())";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, postId);
            ps.setInt(2, accountId);
            ps.setString(3, emojiCode);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().toLowerCase().contains("unique")) {
                return false;
            }
            throw new DataAccessException("Failed to insert reaction", e);
        }
    }

    @Override
    public boolean deleteReaction(int postId, int accountId, String emojiCode) {
        String sql = "DELETE FROM BlogReaction WHERE post_id = ? AND account_id = ? AND emoji_code = ?";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, postId);
            ps.setInt(2, accountId);
            ps.setString(3, emojiCode);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete reaction", e);
        }
    }
}
