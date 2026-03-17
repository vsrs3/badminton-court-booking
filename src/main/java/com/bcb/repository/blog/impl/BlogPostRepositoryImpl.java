package com.bcb.repository.blog.impl;

import com.bcb.dto.blog.BlogPostFilterDTO;
import com.bcb.dto.blog.BlogPostListItemDTO;
import com.bcb.exception.DataAccessException;
import com.bcb.model.BlogPost;
import com.bcb.repository.blog.BlogPostRepository;
import com.bcb.utils.DBContext;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class BlogPostRepositoryImpl implements BlogPostRepository {

    @Override
    public List<BlogPostListItemDTO> findPublicPosts(BlogPostFilterDTO filter) {
        StringBuilder sql = new StringBuilder(
            "SELECT p.post_id, p.title, p.summary, p.thumbnail_path, p.status, p.published_at, p.created_at, a.full_name AS author_name " +
            "FROM BlogPost p " +
            "JOIN Account a ON a.account_id = p.author_account_id " +
            "WHERE p.is_deleted = 0 AND p.status = 'PUBLISHED'"
        );

        List<Object> params = new ArrayList<>();
        applyKeyword(sql, params, filter);
        applySorting(sql, filter, true);
        sql.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        params.add(filter.getOffset());
        params.add(filter.getPageSize());

        return queryList(sql.toString(), params);
    }

    @Override
    public int countPublicPosts(BlogPostFilterDTO filter) {
        StringBuilder sql = new StringBuilder(
            "SELECT COUNT(*) FROM BlogPost p WHERE p.is_deleted = 0 AND p.status = 'PUBLISHED'"
        );
        List<Object> params = new ArrayList<>();
        applyKeyword(sql, params, filter);
        return queryCount(sql.toString(), params);
    }

    @Override
    public Optional<BlogPost> findPublicPostById(int postId) {
        String sql = "SELECT * FROM BlogPost WHERE post_id = ? AND is_deleted = 0 AND status = 'PUBLISHED'";
        return queryEntityById(sql, postId);
    }

    @Override
    public List<BlogPostListItemDTO> findManagePosts(BlogPostFilterDTO filter) {
        StringBuilder sql = new StringBuilder(
            "SELECT p.post_id, p.title, p.summary, p.thumbnail_path, p.status, p.published_at, p.created_at, a.full_name AS author_name " +
            "FROM BlogPost p " +
            "JOIN Account a ON a.account_id = p.author_account_id " +
            "WHERE p.is_deleted = 0"
        );

        List<Object> params = new ArrayList<>();
        applyKeyword(sql, params, filter);

        if (filter.getStatus() != null && !filter.getStatus().isBlank()) {
            sql.append(" AND p.status = ?");
            params.add(filter.getStatus());
        }

        applySorting(sql, filter, false);
        sql.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        params.add(filter.getOffset());
        params.add(filter.getPageSize());

        return queryList(sql.toString(), params);
    }

    @Override
    public int countManagePosts(BlogPostFilterDTO filter) {
        StringBuilder sql = new StringBuilder(
            "SELECT COUNT(*) FROM BlogPost p WHERE p.is_deleted = 0"
        );
        List<Object> params = new ArrayList<>();
        applyKeyword(sql, params, filter);

        if (filter.getStatus() != null && !filter.getStatus().isBlank()) {
            sql.append(" AND p.status = ?");
            params.add(filter.getStatus());
        }

        return queryCount(sql.toString(), params);
    }

    @Override
    public Optional<BlogPost> findById(int postId) {
        String sql = "SELECT * FROM BlogPost WHERE post_id = ? AND is_deleted = 0";
        return queryEntityById(sql, postId);
    }

    @Override
    public int insert(BlogPost post) {
        String sql = "INSERT INTO BlogPost (author_account_id, title, summary, content, thumbnail_path, status, published_at, created_at, is_deleted) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, GETDATE(), 0)";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, post.getAuthorAccountId());
            ps.setString(2, post.getTitle());
            if (post.getSummary() != null) ps.setString(3, post.getSummary()); else ps.setNull(3, Types.NVARCHAR);
            ps.setString(4, post.getContent());
            if (post.getThumbnailPath() != null) ps.setString(5, post.getThumbnailPath()); else ps.setNull(5, Types.NVARCHAR);
            ps.setString(6, post.getStatus());
            if (post.getPublishedAt() != null) {
                ps.setTimestamp(7, Timestamp.valueOf(post.getPublishedAt()));
            } else {
                ps.setNull(7, Types.TIMESTAMP);
            }
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to insert blog post", e);
        }
        return 0;
    }

    @Override
    public int update(BlogPost post) {
        String sql = "UPDATE BlogPost SET title=?, summary=?, content=?, thumbnail_path=?, status=?, published_at=?, updated_at=GETDATE() " +
                     "WHERE post_id=? AND is_deleted=0";

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, post.getTitle());
            if (post.getSummary() != null) ps.setString(2, post.getSummary()); else ps.setNull(2, Types.NVARCHAR);
            ps.setString(3, post.getContent());
            if (post.getThumbnailPath() != null) ps.setString(4, post.getThumbnailPath()); else ps.setNull(4, Types.NVARCHAR);
            ps.setString(5, post.getStatus());
            if (post.getPublishedAt() != null) {
                ps.setTimestamp(6, Timestamp.valueOf(post.getPublishedAt()));
            } else {
                ps.setNull(6, Types.TIMESTAMP);
            }
            ps.setInt(7, post.getPostId());
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update blog post", e);
        }
    }

    @Override
    public int softDelete(int postId) {
        String sql = "UPDATE BlogPost SET is_deleted=1, updated_at=GETDATE() WHERE post_id=? AND is_deleted=0";
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, postId);
            return ps.executeUpdate();
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete blog post", e);
        }
    }

    private void applyKeyword(StringBuilder sql, List<Object> params, BlogPostFilterDTO filter) {
        if (filter.getKeyword() != null && !filter.getKeyword().trim().isEmpty()) {
            sql.append(" AND (p.title LIKE ? OR p.summary LIKE ?)");
            String kw = "%" + filter.getKeyword().trim() + "%";
            params.add(kw);
            params.add(kw);
        }
    }

    private void applySorting(StringBuilder sql, BlogPostFilterDTO filter, boolean isPublic) {
        Set<String> allowed = new HashSet<>(Arrays.asList("title", "created_at", "published_at"));
        String sortBy = (filter.getSortBy() != null && allowed.contains(filter.getSortBy()))
            ? filter.getSortBy()
            : (isPublic ? "published_at" : "created_at");

        String dir = "ASC".equalsIgnoreCase(filter.getSortDir()) ? "ASC" : "DESC";
        sql.append(" ORDER BY p.").append(sortBy).append(" ").append(dir);
    }

    private List<BlogPostListItemDTO> queryList(String sql, List<Object> params) {
        List<BlogPostListItemDTO> list = new ArrayList<>();
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    BlogPostListItemDTO dto = new BlogPostListItemDTO();
                    dto.setPostId(rs.getInt("post_id"));
                    dto.setTitle(rs.getString("title"));
                    dto.setSummary(rs.getString("summary"));
                    dto.setThumbnailPath(rs.getString("thumbnail_path"));
                    dto.setStatus(rs.getString("status"));
                    Timestamp pub = rs.getTimestamp("published_at");
                    if (pub != null) dto.setPublishedAt(pub.toLocalDateTime());
                    Timestamp created = rs.getTimestamp("created_at");
                    if (created != null) dto.setCreatedAt(created.toLocalDateTime());
                    dto.setAuthorName(rs.getString("author_name"));
                    list.add(dto);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to query blog posts", e);
        }
        return list;
    }

    private int queryCount(String sql, List<Object> params) {
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to count blog posts", e);
        }
        return 0;
    }

    private Optional<BlogPost> queryEntityById(String sql, int postId) {
        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, postId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return Optional.empty();
                return Optional.of(mapEntity(rs));
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to find blog post", e);
        }
    }

    private BlogPost mapEntity(ResultSet rs) throws SQLException {
        BlogPost p = new BlogPost();
        p.setPostId(rs.getInt("post_id"));
        p.setAuthorAccountId(rs.getInt("author_account_id"));
        p.setTitle(rs.getString("title"));
        p.setSummary(rs.getString("summary"));
        p.setContent(rs.getString("content"));
        p.setThumbnailPath(rs.getString("thumbnail_path"));
        p.setStatus(rs.getString("status"));

        Timestamp publishedAt = rs.getTimestamp("published_at");
        if (publishedAt != null) p.setPublishedAt(publishedAt.toLocalDateTime());

        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) p.setCreatedAt(createdAt.toLocalDateTime());

        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) p.setUpdatedAt(updatedAt.toLocalDateTime());

        if (rs.getObject("is_deleted") != null) p.setIsDeleted(rs.getBoolean("is_deleted"));
        return p;
    }
}
