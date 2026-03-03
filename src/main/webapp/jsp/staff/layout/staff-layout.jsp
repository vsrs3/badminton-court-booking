<%-- staff-layout.jsp --%>

<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>BCB Staff</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <%-- Bootstrap 5 CSS --%>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">

    <%-- Bootstrap 5 JS (Popper included) --%>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>

    <%-- Bootstrap Icons --%>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css" rel="stylesheet">

    <%-- Google Fonts --%>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&display=swap" rel="stylesheet">

    <%-- Design System CSS (shared) --%>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/badminton-pro.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/components.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/emerald-admin.css">

    <style>
        /* ===== STAFF LAYOUT OVERRIDES ===== */

        /* Override emerald-admin .main-content for staff context */
        body {
            font-family: var(--font-family);
            background-color: var(--bg-light);
            color: var(--text-dark);
        }

        /* Staff brand utilities */
        .bg-brand     { background-color: var(--color-green-brand, #064E3B) !important; }
        .text-brand   { color: var(--color-green-brand, #064E3B) !important; }
        .bg-lime      { background-color: var(--color-lime-brand, #A3E635) !important; }
        .text-lime    { color: var(--color-lime-brand, #A3E635) !important; }
        .border-brand { border-color: var(--color-green-brand, #064E3B) !important; }

        .btn-brand {
            background-color: var(--color-green-brand, #064E3B);
            color: #fff;
            border: none;
        }
        .btn-brand:hover,
        .btn-brand:focus {
            background-color: #053d2f;
            color: #fff;
        }

        /* Form focus */
        .form-control:focus,
        .form-select:focus {
            border-color: var(--color-green-brand, #064E3B);
            box-shadow: 0 0 0 0.2rem rgba(6, 78, 59, 0.15);
        }

        /* Pagination brand */
        .page-item.active .page-link {
            background-color: var(--color-green-brand, #064E3B);
            border-color: var(--color-green-brand, #064E3B);
        }
        .page-link { color: var(--color-green-brand, #064E3B); }
        .page-link:hover { color: #15803D; }

        /* Badge utilities */
        .badge-pending   { background-color: #FEF3C7; color: #92400E; }
        .badge-confirmed { background-color: #D1FAE5; color: #065F46; }
        .badge-completed { background-color: #DBEAFE; color: #1E40AF; }
        .badge-cancelled { background-color: #FEE2E2; color: #991B1B; }

        .badge-unpaid    { background-color: #FEE2E2; color: #991B1B; }
        .badge-partial   { background-color: #FEF3C7; color: #92400E; }
        .badge-paid      { background-color: #D1FAE5; color: #065F46; }

        /* Scrollbar */
        ::-webkit-scrollbar       { width: 6px; }
        ::-webkit-scrollbar-track { background: transparent; }
        ::-webkit-scrollbar-thumb { background: var(--color-gray-300, #D1D5DB); border-radius: 10px; }
        ::-webkit-scrollbar-thumb:hover { background: var(--color-gray-400, #9CA3AF); }

        /* Staff header top bar — override emerald-admin .header-top */
        .staff-header-bar {
            height: var(--header-height, 70px);
            background: white;
            border-bottom: 1px solid #e2e8f0;
            display: flex;
            align-items: center;
            padding: 0 2rem;
            position: sticky;
            top: 0;
            z-index: 1000;
        }

        /* Mobile sidebar toggle */
        .st-sidebar-toggle {
            display: none;
            background: none;
            border: none;
            color: var(--color-gray-600, #4B5563);
            font-size: 1.5rem;
            padding: 0.25rem;
            cursor: pointer;
            line-height: 1;
        }
        .st-sidebar-toggle:hover {
            color: var(--color-green-brand, #064E3B);
        }

        /* Sidebar backdrop for mobile */
        .st-sidebar-backdrop {
            display: none;
            position: fixed;
            inset: 0;
            background: rgba(0, 0, 0, 0.4);
            z-index: 1040;
        }
        .st-sidebar-backdrop.active {
            display: block;
        }

        @media (max-width: 991.98px) {
            .st-sidebar-toggle {
                display: block;
            }
        }

        /* Footer */
        .staff-footer {
            background: white;
            border-top: 1px solid #e2e8f0;
            padding: 1rem 2rem;
            text-align: center;
            color: var(--text-muted, #64748b);
            font-size: 0.8125rem;
        }
    </style>
</head>
<body>