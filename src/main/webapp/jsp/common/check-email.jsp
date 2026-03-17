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

    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/badminton-pro.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/auth.css">
</head>
<body>
<div class="auth-page">
    <div class="auth-card text-center">
        <div class="auth-logo mb-3">
            <i class="bi bi-envelope-check-fill text-primary" style="font-size: 48px;"></i>
        </div>

        <h2 class="auth-title">Kiểm tra email của bạn</h2>
        <p class="auth-subtitle">
            Chúng tôi đã gửi email xác nhận đến địa chỉ bạn vừa đăng ký.
        </p>

        <div class="alert alert-info mt-3">
            <i class="bi bi-info-circle-fill"></i>
            Vui lòng mở email và nhấn vào link xác nhận để hoàn tất đăng ký.
        </div>

        <div class="mt-3">
            <p class="text-muted mb-1">
                Link xác nhận có hiệu lực trong
                <strong class="text-danger">60 giây</strong>
            </p>

            <p class="fw-semibold">
                Trang sẽ tự quay lại sau
                <span class="badge bg-danger fs-6" id="countdown">60</span>
                giây.
            </p>
        </div>

        <button type="button" id="backBtn" class="btn btn-outline-primary">
            <i class="bi bi-arrow-left"></i>
            Quay lại đăng ký
        </button>
    </div>
</div>

<script>
    const REGISTER_SYNC_KEY = "bcb.register.verification";
    const currentToken = "<%= token %>";
    const countdownEl = document.getElementById("countdown");
    const backBtn = document.getElementById("backBtn");
    let seconds = 60;
    let countdownInterval = null;
    let timeoutHandle = null;
    let syncPoll = null;

    function parsePayload(rawValue) {
        if (!rawValue) {
            return null;
        }

        try {
            return JSON.parse(rawValue);
        } catch (error) {
            return null;
        }
    }

    function clearWaitingTimers() {
        if (countdownInterval) {
            clearInterval(countdownInterval);
        }
        if (timeoutHandle) {
            clearTimeout(timeoutHandle);
        }
        if (syncPoll) {
            clearInterval(syncPoll);
        }
    }

    async function cleanupAndRedirect() {
        clearWaitingTimers();

        if (currentToken) {
            try {
                await fetch("${pageContext.request.contextPath}/cleanup-email", {
                    method: "POST",
                    headers: {
                        "Content-Type": "application/x-www-form-urlencoded"
                    },
                    body: "token=" + encodeURIComponent(currentToken)
                });
            } catch (error) {
                console.log("Cleanup lỗi:", error);
            }
        }

        window.location.href = "${pageContext.request.contextPath}/jsp/auth/register.jsp";
    }

    function handleRegisterVerification(payload) {
        if (!payload || payload.token !== currentToken || !payload.redirectUrl) {
            return false;
        }

        clearWaitingTimers();
        localStorage.removeItem(REGISTER_SYNC_KEY);
        window.location.href = payload.redirectUrl;
        return true;
    }

    function checkRegisterVerification() {
        return handleRegisterVerification(parsePayload(localStorage.getItem(REGISTER_SYNC_KEY)));
    }

    async function checkRegisterVerificationFromServer() {
        if (!currentToken) {
            return false;
        }

        try {
            const response = await fetch(
                "${pageContext.request.contextPath}/email-action-status?purpose=register&token="
                + encodeURIComponent(currentToken),
                {
                    cache: "no-store"
                }
            );

            if (!response.ok) {
                return false;
            }

            const payload = await response.json();
            if (payload.status === "confirmed" && payload.continueUrl) {
                clearWaitingTimers();
                window.location.href = payload.continueUrl;
                return true;
            }
        } catch (error) {
            console.log("Không kiểm tra được trạng thái xác nhận:", error);
        }

        return false;
    }

    countdownInterval = setInterval(function() {
        seconds--;
        if (countdownEl) {
            countdownEl.innerText = seconds;
        }
        if (seconds <= 0) {
            clearInterval(countdownInterval);
        }
    }, 1000);

    timeoutHandle = setTimeout(function() {
        cleanupAndRedirect();
    }, 60000);

    syncPoll = setInterval(function() {
        checkRegisterVerification();
        checkRegisterVerificationFromServer();
    }, 1000);

    window.addEventListener("storage", function(event) {
        if (event.key === REGISTER_SYNC_KEY) {
            handleRegisterVerification(parsePayload(event.newValue));
        }
    });

    document.addEventListener("visibilitychange", function() {
        if (!document.hidden) {
            checkRegisterVerification();
            checkRegisterVerificationFromServer();
        }
    });

    if (backBtn) {
        backBtn.addEventListener("click", function() {
            backBtn.disabled = true;
            backBtn.innerHTML = '<i class="bi bi-hourglass-split"></i> Đang xử lý...';
            cleanupAndRedirect();
        });
    }

    checkRegisterVerification();
    checkRegisterVerificationFromServer();
</script>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>
