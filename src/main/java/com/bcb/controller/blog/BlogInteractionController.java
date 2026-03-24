package com.bcb.controller.blog;

import com.bcb.exception.BusinessException;
import com.bcb.model.Account;
import com.bcb.service.blog.BlogCommentService;
import com.bcb.service.blog.BlogReactionService;
import com.bcb.service.blog.impl.BlogCommentServiceImpl;
import com.bcb.service.blog.impl.BlogReactionServiceImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/blogs/interact")
public class BlogInteractionController extends HttpServlet {

    private BlogCommentService commentService;
    private BlogReactionService reactionService;

    @Override
    public void init() throws ServletException {
        commentService = new BlogCommentServiceImpl();
        reactionService = new BlogReactionServiceImpl();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");

        String action = req.getParameter("action");
        int postId = safeParseInt(req.getParameter("postId"));

        Account acc = null;
        String role = null;
        if (req.getSession(false) != null) {
            acc = (Account) req.getSession(false).getAttribute("account");
            if (acc != null) role = acc.getRole();
        }

        String redirect = req.getContextPath() + "/blogs/detail?id=" + postId + "#comments";

        if (action == null || action.isBlank() || postId <= 0) {
            resp.sendRedirect(redirect);
            return;
        }

        try {
            switch (action) {
                case "comment_add" -> {
                    requireLogin(acc);
                    commentService.submitComment(postId, acc.getAccountId(), role, req.getParameter("content"));
                }
                case "comment_edit" -> {
                    requireLogin(acc);
                    int commentId = safeParseInt(req.getParameter("commentId"));
                    commentService.editComment(commentId, acc.getAccountId(), role, req.getParameter("content"));
                }
                case "comment_delete" -> {
                    requireLogin(acc);
                    int commentId = safeParseInt(req.getParameter("commentId"));
                    commentService.deleteCommentByAuthor(commentId, acc.getAccountId(), role);
                }
                case "comment_moderate" -> {
                    requireLogin(acc);
                    int commentId = safeParseInt(req.getParameter("commentId"));
                    String modAction = req.getParameter("modAction");
                    commentService.moderateComment(commentId, acc.getAccountId(), role, modAction);
                }
                case "comment_delete_mod" -> {
                    requireLogin(acc);
                    int commentId = safeParseInt(req.getParameter("commentId"));
                    commentService.deleteCommentByModerator(commentId, acc.getAccountId(), role);
                }
                case "reaction_add" -> {
                    requireLogin(acc);
                    String emojiCode = req.getParameter("emojiCode");
                    reactionService.toggleReaction(postId, acc.getAccountId(), role, emojiCode, true);
                }
                case "reaction_remove" -> {
                    requireLogin(acc);
                    String emojiCode = req.getParameter("emojiCode");
                    reactionService.toggleReaction(postId, acc.getAccountId(), role, emojiCode, false);
                }
                default -> {
                }
            }
        } catch (BusinessException e) {
            // Keep UX simple: redirect back to detail. Error can be added later.
        }

        resp.sendRedirect(redirect);
    }

    private int safeParseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return 0;
        }
    }

    private void requireLogin(Account account) throws BusinessException {
        if (account == null || account.getAccountId() == null) {
            throw new BusinessException("FORBIDDEN", "Bạn cần đăng nhập.");
        }
    }
}
