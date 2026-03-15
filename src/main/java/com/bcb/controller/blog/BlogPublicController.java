package com.bcb.controller.blog;

import com.bcb.dto.blog.BlogPostFilterDTO;
import com.bcb.model.Account;
import com.bcb.exception.BusinessException;
import com.bcb.model.BlogPost;
import com.bcb.service.blog.BlogCommentService;
import com.bcb.service.blog.BlogPostService;
import com.bcb.service.blog.BlogReactionService;
import com.bcb.service.blog.impl.BlogCommentServiceImpl;
import com.bcb.service.blog.impl.BlogPostServiceImpl;
import com.bcb.service.blog.impl.BlogReactionServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/blogs/*")
public class BlogPublicController extends HttpServlet {

    private BlogPostService service;
    private BlogCommentService commentService;
    private BlogReactionService reactionService;

    @Override
    public void init() throws ServletException {
        service = new BlogPostServiceImpl();
        commentService = new BlogCommentServiceImpl();
        reactionService = new BlogReactionServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null || "/".equals(path)) {
            showList(req, resp);
            return;
        }

        switch (path) {
            case "/detail" -> showDetail(req, resp);
            default -> resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private void showList(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        BlogPostFilterDTO filter = bindFilter(req);
        int total = service.countPublicPosts(filter);
        req.setAttribute("filter", filter);
        req.setAttribute("posts", service.getPublicPosts(filter));
        req.setAttribute("total", total);
        req.getRequestDispatcher("/jsp/blogs/blog-list.jsp").forward(req, resp);
    }

    private void showDetail(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int id;
        try {
            id = Integer.parseInt(req.getParameter("id"));
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            BlogPost post = service.getPublicPostById(id);
            req.setAttribute("post", post);

            Account acc = null;
            if (req.getSession(false) != null) {
                acc = (Account) req.getSession(false).getAttribute("account");
            }
            Integer accountId = acc != null ? acc.getAccountId() : null;
            String role = acc != null ? acc.getRole() : null;

            req.setAttribute("comments", commentService.getCommentsForPublicDetail(id, accountId, role));
            req.setAttribute("reactionCounts", reactionService.countReactions(id));
            req.setAttribute("userReactions", reactionService.getUserReactions(id, accountId));

            req.getRequestDispatcher("/jsp/blogs/blog-detail.jsp").forward(req, resp);
        } catch (BusinessException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private BlogPostFilterDTO bindFilter(HttpServletRequest req) {
        BlogPostFilterDTO f = new BlogPostFilterDTO();
        f.setKeyword(req.getParameter("q"));
        f.setSortBy(req.getParameter("sortBy"));
        f.setSortDir(req.getParameter("sortDir"));

        try {
            if (req.getParameter("page") != null) {
                f.setPage(Integer.parseInt(req.getParameter("page")));
            }
        } catch (NumberFormatException ignore) {
        }

        try {
            if (req.getParameter("pageSize") != null) {
                f.setPageSize(Integer.parseInt(req.getParameter("pageSize")));
            }
        } catch (NumberFormatException ignore) {
        }

        return f;
    }
}
