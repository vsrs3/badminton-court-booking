package com.bcb.service.blog;

import com.bcb.dto.blog.BlogPostFilterDTO;
import com.bcb.dto.blog.BlogPostListItemDTO;
import com.bcb.exception.BusinessException;
import com.bcb.model.BlogPost;

import java.util.List;

public interface BlogPostService {
    List<BlogPostListItemDTO> getPublicPosts(BlogPostFilterDTO filter);

    int countPublicPosts(BlogPostFilterDTO filter);

    BlogPost getPublicPostById(int postId) throws BusinessException;

    List<BlogPostListItemDTO> getManagePosts(BlogPostFilterDTO filter);

    int countManagePosts(BlogPostFilterDTO filter);

    BlogPost getManagePostById(int postId) throws BusinessException;

    int createPost(BlogPost post) throws BusinessException;

    void updatePost(BlogPost post) throws BusinessException;

    void deletePost(int postId) throws BusinessException;
}
