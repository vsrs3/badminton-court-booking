package com.bcb.repository.blog;

import com.bcb.dto.blog.BlogPostFilterDTO;
import com.bcb.dto.blog.BlogPostListItemDTO;
import com.bcb.model.BlogPost;

import java.util.List;
import java.util.Optional;

public interface BlogPostRepository {
    List<BlogPostListItemDTO> findPublicPosts(BlogPostFilterDTO filter);

    int countPublicPosts(BlogPostFilterDTO filter);

    Optional<BlogPost> findPublicPostById(int postId);

    List<BlogPostListItemDTO> findManagePosts(BlogPostFilterDTO filter);

    int countManagePosts(BlogPostFilterDTO filter);

    Optional<BlogPost> findById(int postId);

    int insert(BlogPost post);

    int update(BlogPost post);

    int softDelete(int postId);
}
