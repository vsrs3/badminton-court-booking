<%@page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%
    String syncKey = request.getAttribute("syncKey") == null ? "" : request.getAttribute("syncKey").toString();
    String syncToken = request.getAttribute("syncToken") == null ? "" : request.getAttribute("syncToken").toString();
    String syncEmail = request.getAttribute("syncEmail") == null ? "" : request.getAttribute("syncEmail").toString();
    String syncRedirectUrl = request.getAttribute("syncRedirectUrl") == null ? "" : request.getAttribute("syncRedirectUrl").toString();
    String message = request.getAttribute("message") == null ? "" : request.getAttribute("message").toString();
    String instruction = request.getAttribute("instruction") == null ? "" : request.getAttribute("instruction").toString();
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Xác nhận email</title>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/badminton-pro.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/auth.css">
</head>
<body>
<div class="auth-page">
    <div class="auth-card text-center">
        <div class="auth-logo mb-3">
            <i class="bi bi-envelope-check-fill text-success" style="font-size: 48px;"></i>
        </div>

        <h2 class="auth-title">Email đã được xác nhận</h2>
        <p class="auth-subtitle"><%= message %></p>

        <div class="alert alert-success mt-3">
            <i class="bi bi-check-circle-fill"></i>
            <span><%= instruction %></span>
        </div>

        <button type="button" id="closePageBtn" class="btn btn-outline-success mt-3">
            <i class="bi bi-x-circle"></i>
            Đóng trang này
        </button>
    </div>
</div>

<input type="hidden" id="syncKey" value="<%= syncKey %>">
<input type="hidden" id="syncToken" value="<%= syncToken %>">
<input type="hidden" id="syncEmail" value="<%= syncEmail %>">
<input type="hidden" id="syncRedirectUrl" value="<%= syncRedirectUrl %>">

<script>
    (function () {
        const syncKey = document.getElementById("syncKey").value;
        const syncToken = document.getElementById("syncToken").value;
        const syncEmail = document.getElementById("syncEmail").value;
        const syncRedirectUrl = document.getElementById("syncRedirectUrl").value;

        if (syncKey && syncToken && syncRedirectUrl) {
            const payload = JSON.stringify({
                token: syncToken,
                email: syncEmail,
                redirectUrl: syncRedirectUrl,
                timestamp: Date.now()
            });

            try {
                localStorage.removeItem(syncKey);
                localStorage.setItem(syncKey, payload);
            } catch (error) {
                console.log("Không thể đồng bộ trạng thái xác nhận email:", error);
            }
        }
    })();

    document.getElementById("closePageBtn").addEventListener("click", function () {
        window.close();
    });
</script>
</body>
</html>
