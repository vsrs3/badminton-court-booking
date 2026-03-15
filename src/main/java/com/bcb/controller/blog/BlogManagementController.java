package com.bcb.controller.blog;

import com.bcb.dto.blog.BlogPostFilterDTO;
import com.bcb.exception.BusinessException;
import com.bcb.model.Account;
import com.bcb.model.BlogPost;
import com.bcb.service.blog.BlogPostService;
import com.bcb.service.blog.impl.BlogPostServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/blogs/manage/*")
public class BlogManagementController extends HttpServlet {

    private BlogPostService service;

    @Override
    public void init() throws ServletException {
        service = new BlogPostServiceImpl();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getPathInfo();
        if (path == null || "/".equals(path)) {
            resp.sendRedirect(req.getContextPath() + "/blogs/manage/list");
            return;
        }

        try {
            switch (path) {
                case "/list" -> showList(req, resp);
                case "/create" -> showCreateForm(req, resp);
                case "/edit" -> showEditForm(req, resp);
                case "/delete" -> handleDelete(req, resp);
                default -> resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (BusinessException e) {
            req.setAttribute("error", e.getMessage());
            req.getRequestDispatcher("/jsp/blogs/manage/blog-manage-list.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        String path = req.getPathInfo();

        try {
            if ("/create".equals(path)) {
                handleCreate(req, resp);
            } else if ("/update".equals(path)) {
                handleUpdate(req, resp);
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (BusinessException e) {
            req.setAttribute("formError", e.getMessage());
            if ("/create".equals(path)) {
                showCreateForm(req, resp);
            } else {
                showEditForm(req, resp);
            }
        }
    }

    private void showList(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        BlogPostFilterDTO filter = bindManageFilter(req);
        int total = service.countManagePosts(filter);
        req.setAttribute("filter", filter);
        req.setAttribute("posts", service.getManagePosts(filter));
        req.setAttribute("total", total);
        req.getRequestDispatcher("/jsp/blogs/manage/blog-manage-list.jsp").forward(req, resp);
    }

    private void showCreateForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("mode", "create");
        req.getRequestDispatcher("/jsp/blogs/manage/blog-manage-form.jsp").forward(req, resp);
    }

    private void showEditForm(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            int id = parseId(req.getParameter("id"));
            BlogPost post = service.getManagePostById(id);
            req.setAttribute("post", post);
        } catch (BusinessException e) {
            req.setAttribute("error", e.getMessage());
        }
        req.setAttribute("mode", "edit");
        req.getRequestDispatcher("/jsp/blogs/manage/blog-manage-form.jsp").forward(req, resp);
    }

    private void handleCreate(HttpServletRequest req, HttpServletResponse resp) throws IOException, BusinessException {
        BlogPost post = bindPostFromRequest(req);
        Account current = (Account) req.getSession(false).getAttribute("account");
        post.setAuthorAccountId(current.getAccountId());
        int id = service.createPost(post);
        resp.sendRedirect(req.getContextPath() + "/blogs/manage/edit?id=" + id + "&success=created");
    }

    private void handleUpdate(HttpServletRequest req, HttpServletResponse resp) throws IOException, BusinessException {
        BlogPost post = bindPostFromRequest(req);
        post.setPostId(parseId(req.getParameter("postId")));
        service.updatePost(post);
        resp.sendRedirect(req.getContextPath() + "/blogs/manage/edit?id=" + post.getPostId() + "&success=updated");
    }

    private void handleDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException, BusinessException {
        int id = parseId(req.getParameter("id"));
        service.deletePost(id);
        resp.sendRedirect(req.getContextPath() + "/blogs/manage/list?success=deleted");
    }

    private BlogPost bindPostFromRequest(HttpServletRequest req) {
        BlogPost p = new BlogPost();
        p.setTitle(req.getParameter("title"));
        p.setSummary(req.getParameter("summary"));
        p.setContent(req.getParameter("content"));
        p.setThumbnailPath(req.getParameter("thumbnailPath"));
        p.setStatus(req.getParameter("status"));
        return p;
    }

    private BlogPostFilterDTO bindManageFilter(HttpServletRequest req) {
        BlogPostFilterDTO f = new BlogPostFilterDTO();
        f.setKeyword(req.getParameter("q"));
        f.setStatus(req.getParameter("status"));
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

    private int parseId(String value) throws BusinessException {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new BusinessException("INVALID_ID", "ID không hợp lệ.");
        }
    }
}
