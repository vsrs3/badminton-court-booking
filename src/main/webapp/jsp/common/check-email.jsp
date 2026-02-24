<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
    String token = request.getParameter("token");
    if (token == null) {
        token = "";
    }
%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Kiểm tra email</title>

    <!-- Bootstrap -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css">

    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/badminton-pro.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/auth.css">
</head>

<body>

<div class="auth-page">

    <div class="auth-card text-center">

        <!-- Icon -->
        <div class="auth-logo mb-3">
            <i class="bi bi-envelope-check-fill text-primary" style="font-size: 48px;"></i>
        </div>

        <h2 class="auth-title">Kiểm tra email của bạn</h2>
        <p class="auth-subtitle">
            Chúng tôi đã gửi email xác nhận đến địa chỉ bạn vừa đăng ký.
        </p>

        <div class="alert alert-info mt-3">
            <i class="bi bi-info-circle-fill"></i>
            Vui lòng mở email và <strong>nhấn vào link xác nhận</strong> để hoàn tất đăng ký.
        </div>

        <div class="mt-3">
            <p class="text-muted mb-1">
                ⏳ Link xác nhận có hiệu lực trong
                <strong class="text-danger">60 giây</strong>
            </p>

            <p class="fw-semibold">
                Trang sẽ tự quay lại sau
                <span class="badge bg-danger fs-6" id="countdown">60</span>
                giây.
            </p>
        </div>

<%--        <button type="button"--%>
<%--                id="backBtn"--%>
<%--                class="btn btn-outline-primary">--%>
<%--            <i class="bi bi-arrow-left"></i>--%>
<%--            Quay lại đăng ký--%>
<%--        </button>--%>
    </div>

</div>

<script>

    let seconds = 60;
    const token = "<%= token %>";
    const countdownEl = document.getElementById("countdown");
    const backBtn = document.getElementById("backBtn");
    let interval;
    let timeout;
    // ===============================
    // CLEANUP FUNCTION
    // ===============================
    function cleanupAndRedirect() {

        clearInterval(interval);
        clearTimeout(timeout);

        if (token && token !== "") {

            const formData = new FormData();
            formData.append("token", token);

            navigator.sendBeacon(
                "<%= request.getContextPath() %>/cleanup-email",
                formData
            );
        }
        window.location.href =
            "${pageContext.request.contextPath}/jsp/auth/register.jsp";
    }
    // ===============================
    // COUNTDOWN
    // ===============================
    interval = setInterval(() => {
        seconds--;
        if (countdownEl) {
            countdownEl.innerText = seconds;
        }
        if (seconds <= 0) {
            clearInterval(interval);
        }
    }, 1000);

    // ===============================
    // AUTO TIMEOUT
    // ===============================
    timeout = setTimeout(() => {
        cleanupAndRedirect();
    }, 60000);
    // ===============================
    // CLICK BACK
    // ===============================
    if (backBtn) {
        backBtn.addEventListener("click", function () {
            backBtn.disabled = true;
            backBtn.innerHTML =
                '<i class="bi bi-hourglass-split"></i> Đang xử lý...';
            cleanupAndRedirect();
        });
    }

</script>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>

</body>
</html>