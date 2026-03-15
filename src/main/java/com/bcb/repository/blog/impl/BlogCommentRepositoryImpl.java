package com.bcb.repository.blog.impl;

import com.bcb.dto.blog.BlogCommentViewDTO;
import com.bcb.exception.DataAccessException;
import com.bcb.model.BlogComment;
import com.bcb.repository.blog.BlogCommentRepository;
import com.bcb.utils.DBContext;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BlogCommentRepositoryImpl implements BlogCommentRepository {

    @Override
    public List<BlogCommentViewDTO> findApprovedByPostId(int postId) {
        String sql = "SELECT c.comment_id, c.post_id, c.author_account_id, a.full_name AS author_name, c.content, c.status, c.created_at " +
            "FROM BlogComment c JOIN Account a ON a.account_id = c.author_account_id " +
            "WHERE c.post_id = ? AND c.is_deleted = 0 AND c.status = 'APPROVED' ORDER BY c.created_at DESC";
        return queryList(sql, postId);
    }

    @Override
    public List<BlogCommentViewDTO> findByPostIdForModeration(int postId) {
        String sql = "SELECT c.comment_id, c.post_id, c.author_account_id, a.full_name AS author_name, c.content, c.status, c.created_at " +
            "FROM BlogComment c JOIN Account a ON a.account_id = c.author_account_id " +
            "WHERE c.post_id = ? AND c.is_deleted = 0 ORDER BY c.created_at DESC";
        return queryList(sql, postId);
    }

    @Override
    public List<BlogCommentViewDTO> findByPostIdForCustomer(int postId, int customerAccountId) {
        String sql = "SELECT c.comment_id, c.post_id, c.author_account_id, a.full_name AS author_name, c.content, c.status, c.created_at " +
            "FROM BlogComment c JOIN Account a ON a.account_id = c.author_account_id " +
            "WHERE c.post_id = ? AND c.is_deleted = 0 AND (c.status = 'APPROVED' OR c.author_account_id = ?) " +
            "ORDER BY c.created_at DESC";

        List<BlogCommentViewDTO> list = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, postId);
            ps.setInt(2, customerAccountId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapView(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to query blog comments", e);
        }
        return list;
    }

    @Override
    public int insert(BlogComment comment) {
        String sql = "INSERT INTO BlogComment (post_id, author_account_id, content, status, created_at, is_deleted) VALUES (?, ?, ?, ?, GETDATE(), 0)";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, comment.getPostId());
            ps.setInt(2, comment.getAuthorAccountId());
            ps.setString(3, comment.getContent());
            ps.setString(4, comment.getStatus());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to insert blog comment", e);
        }
        return 0;
    }

    @Override
    public int updateContent(int commentId, int authorAccountId, String content) {
        String sql = "UPDATE BlogComment SET content=?, status='PENDING', updated_at=GETDATE() WHERE comment_id=? AND author_account_id=? AND is_deleted=0";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, content);
            ps.setInt(2, commentId);
            ps.setInt(3, authorAccountId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update blog comment", e);
        }
    }

    @Override
    public int softDeleteByAuthor(int commentId, int authorAccountId) {
        String sql = "UPDATE BlogComment SET is_deleted=1, updated_at=GETDATE() WHERE comment_id=? AND author_account_id=? AND is_deleted=0";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, commentId);
            ps.setInt(2, authorAccountId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete blog comment", e);
        }
    }

    @Override
    public int softDeleteByModerator(int commentId) {
        String sql = "UPDATE BlogComment SET is_deleted=1, updated_at=GETDATE() WHERE comment_id=? AND is_deleted=0";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, commentId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete blog comment", e);
        }
    }

    @Override
    public int updateStatus(int commentId, String status, int moderatedByAccountId) {
        String sql = "UPDATE BlogComment SET status=?, moderated_by_account_id=?, moderated_at=GETDATE(), updated_at=GETDATE() WHERE comment_id=? AND is_deleted=0";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, moderatedByAccountId);
            ps.setInt(3, commentId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to moderate blog comment", e);
        }
    }

    private List<BlogCommentViewDTO> queryList(String sql, int postId) {
        List<BlogCommentViewDTO> list = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, postId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapView(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to query blog comments", e);
        }
        return list;
    }

    private BlogCommentViewDTO mapView(ResultSet rs) throws SQLException {
        BlogCommentViewDTO dto = new BlogCommentViewDTO();
        dto.setCommentId(rs.getInt("comment_id"));
        dto.setPostId(rs.getInt("post_id"));
        dto.setAuthorAccountId(rs.getInt("author_account_id"));
        dto.setAuthorName(rs.getString("author_name"));
        dto.setContent(rs.getString("content"));
        dto.setStatus(rs.getString("status"));
        Timestamp ts = rs.getTimestamp("created_at");
        if (ts != null) dto.setCreatedAt(ts.toLocalDateTime());
        return dto;
    }
}
