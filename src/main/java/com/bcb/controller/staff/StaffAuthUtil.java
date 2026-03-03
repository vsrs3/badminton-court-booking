package com.bcb.controller.staff;

import com.bcb.model.Account;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;

/**
 * Shared auth utility for all Staff API servlets.
 * Eliminates duplicated auth checking code.
 *
 * @author Task7-Cleanup
 */
public final class StaffAuthUtil {

    private StaffAuthUtil() {} // utility class

    /**
     * Result of auth validation.
     * If valid: facilityId > 0, account != null
     * If invalid: response already written, caller should return immediately
     */
    public static class AuthResult {
        public final boolean valid;
        public final Account account;
        public final int facilityId;

        private AuthResult(boolean valid, Account account, int facilityId) {
            this.valid = valid;
            this.account = account;
            this.facilityId = facilityId;
        }

        static AuthResult ok(Account account, int facilityId) {
            return new AuthResult(true, account, facilityId);
        }

        static AuthResult fail() {
            return new AuthResult(false, null, 0);
        }
    }

    /**
     * Validate staff auth from session.
     * If invalid, writes error JSON to response and returns AuthResult.valid = false.
     */
    public static AuthResult validateStaff(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("account") == null) {
            response.setStatus(401);
            out.print("{\"success\":false,\"message\":\"Chưa đăng nhập\"}");
            return AuthResult.fail();
        }

        Account account = (Account) session.getAttribute("account");
        if (!"STAFF".equals(account.getRole())) {
            response.setStatus(403);
            out.print("{\"success\":false,\"message\":\"Không có quyền\"}");
            return AuthResult.fail();
        }

        Integer facilityId = (Integer) session.getAttribute("facilityId");
        if (facilityId == null) {
            response.setStatus(403);
            out.print("{\"success\":false,\"message\":\"Staff chưa được gán cơ sở\"}");
            return AuthResult.fail();
        }

        return AuthResult.ok(account, facilityId);
    }

    /**
     * Common JSON escape for string values.
     */
    public static String escapeJson(String value) {
        if (value == null) return "null";
        return "\"" + value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                + "\"";
    }
}