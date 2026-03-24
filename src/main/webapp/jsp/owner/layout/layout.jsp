<!-- layout.jsp -->

<%@ page contentType="text/html; charset=UTF-8"%>
<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <title>BCB Admin</title>
    <meta name="viewport" content="width=device-width, initial-scale=1">

    <%-- Bootstrap 5 CSS --%>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">

    <%-- Bootstrap 5 JS (Popper included) --%>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>

    <%-- Bootstrap Icons --%>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.1/font/bootstrap-icons.css" rel="stylesheet">

    <%-- Google Fonts --%>
    <link href="https://fonts.googleapis.com/css2?family=Inter:wght@300;400;500;600;700;800;900&display=swap" rel="stylesheet">

    <%-- Leaflet Map --%>
    <link rel="stylesheet" href="https://unpkg.com/leaflet/dist/leaflet.css">
    <link rel="stylesheet" href="https://unpkg.com/leaflet-control-geocoder/dist/Control.Geocoder.css">

    <%-- Custom CSS --%>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/emerald-admin.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/pricing.css">

    <style>
        /* ===== CSS VARIABLES ===== */
        :root {
            --color-green-brand: #064E3B;
            --color-lime-brand:  #A3E635;
            --color-green-50:    #F0FDF4;
            --color-green-100:   #DCFCE7;
            --color-green-200:   #BBF7D0;
            --color-green-300:   #86EFAC;
            --color-green-400:   #4ADE80;
            --color-green-500:   #22C55E;
            --color-green-600:   #16A34A;
            --color-green-700:   #15803D;
            --color-green-800:   #166534;
            --color-green-900:   #14532D;

            --color-gray-50:     #F9FAFB;
            --color-gray-100:    #F3F4F6;
            --color-gray-200:    #E5E7EB;
            --color-gray-300:    #D1D5DB;
            --color-gray-400:    #9CA3AF;
            --color-gray-500:    #6B7280;
            --color-gray-600:    #4B5563;
            --color-gray-700:    #374151;
            --color-gray-800:    #1F2937;
            --color-gray-900:    #111827;

            --color-yellow-500:  #EAB308;
            --color-yellow-600:  #CA8A04;
            --color-red-500:     #EF4444;
            --color-red-600:     #DC2626;
            --color-red-700:     #B91C1C;
            --color-blue-500:    #3B82F6;
            --color-orange-400:  #FB923C;
            --color-orange-600:  #EA580C;

            --font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
        }

        /* ===== BASE ===== */
        body {
            font-family: var(--font-family);
            background-color: var(--color-gray-50);
            color: var(--color-gray-800);
        }

        /* ===== BRAND UTILITIES ===== */
        .bg-brand     { background-color: var(--color-green-brand) !important; }
        .text-brand   { color: var(--color-green-brand) !important; }
        .bg-lime      { background-color: var(--color-lime-brand) !important; }
        .text-lime    { color: var(--color-lime-brand) !important; }
        .border-brand { border-color: var(--color-green-brand) !important; }

        /* ===== BRAND BUTTON ===== */
        .btn-brand {
            background-color: var(--color-green-brand);
            color: #fff;
            border: none;
        }
        .btn-brand:hover,
        .btn-brand:focus {
            background-color: #053d2f;
            color: #fff;
        }
        .btn-brand:disabled {
            background-color: var(--color-gray-400);
            color: #fff;
        }

        /* ===== SIDEBAR ACTIVE ===== */
        /* .sidebar-active {
            background-color: rgba(163, 230, 53, 0.1);
            color: var(--color-lime-brand) !important;
            border-right: 4px solid var(--color-lime-brand);
        } */

        /* ===== FORM FOCUS ===== */
        .form-control:focus,
        .form-select:focus {
            border-color: var(--color-green-brand);
            box-shadow: 0 0 0 0.2rem rgba(6, 78, 59, 0.15);
        }

        /* ===== PAGINATION ===== */
        .page-item.active .page-link {
            background-color: var(--color-green-brand);
            border-color: var(--color-green-brand);
        }
        .page-link { color: var(--color-green-brand); }
        .page-link:hover { color: var(--color-green-700); }
        .compact-pagination .page-link,
        .compact-pagination .page-link-static {
            min-width: 42px;
            height: 42px;
            display: inline-flex;
            align-items: center;
            justify-content: center;
            border-radius: 12px !important;
            border: 1px solid var(--color-gray-200);
            background-color: #fff;
            font-weight: 600;
            padding: 0 0.9rem;
        }
        .compact-pagination .page-link-static {
            color: var(--color-green-brand);
        }
        .compact-pagination .page-item.active .page-link-static {
            background-color: var(--color-green-brand);
            border-color: var(--color-green-brand);
            color: #fff;
        }
        .compact-pagination .page-item.disabled .page-link,
        .compact-pagination .page-item.disabled .page-link-static {
            background-color: var(--color-gray-50);
            border-color: var(--color-gray-200);
            color: var(--color-gray-400);
        }
        .compact-pagination .page-link:hover {
            background-color: var(--color-green-50);
            border-color: var(--color-green-brand);
        }
        .compact-pagination .page-item.active .page-link:hover {
            color: #fff;
            background-color: var(--color-green-brand);
        }
        .compact-pagination .pagination-ellipsis {
            min-width: auto;
            border: none;
            background: transparent;
            color: var(--color-gray-500);
            padding: 0 0.25rem;
        }
        .search-suggestion-wrap {
            position: relative;
        }
        .search-suggestion-menu {
            position: absolute;
            top: calc(100% + 6px);
            left: 0;
            right: 0;
            z-index: 20;
            display: none;
            background: #fff;
            border: 1px solid var(--color-gray-200);
            border-radius: 14px;
            box-shadow: 0 12px 30px rgba(15, 23, 42, 0.12);
            max-height: 276px;
            overflow-y: auto;
            padding: 0.35rem;
        }
        .search-suggestion-menu.is-visible {
            display: block;
        }
        .search-suggestion-item {
            width: 100%;
            border: none;
            background: transparent;
            display: flex;
            flex-direction: column;
            align-items: flex-start;
            gap: 0.15rem;
            border-radius: 10px;
            padding: 0.7rem 0.85rem;
            text-align: left;
        }
        .search-suggestion-item:hover,
        .search-suggestion-item:focus {
            background: var(--color-green-50);
            outline: none;
        }
        .search-suggestion-title {
            color: var(--color-gray-900);
            font-weight: 600;
            line-height: 1.2;
        }
        .search-suggestion-meta {
            color: var(--color-gray-500);
            font-size: 0.82rem;
            line-height: 1.2;
        }
        .search-suggestion-empty {
            color: var(--color-gray-500);
            font-size: 0.9rem;
            padding: 0.8rem 0.9rem;
        }
        .inventory-search-form {
            align-items: stretch !important;
        }
        .inventory-search-form .inventory-search-input {
            display: flex;
            align-items: stretch;
        }
        .inventory-search-form .inventory-search-input .search-suggestion-wrap {
            width: 100%;
        }
        .inventory-search-form .search-suggestion-wrap + .form-text {
            display: none !important;
        }
        .inventory-search-form .form-control,
        .inventory-search-form .form-select,
        .inventory-search-form .btn {
            min-height: 44px;
        }
        .inventory-search-form > .col-auto {
            display: flex;
            flex-wrap: wrap;
            align-items: stretch !important;
            gap: 0.75rem !important;
        }
        .inventory-search-form > .col-auto .btn {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            min-width: 132px;
            padding-inline: 1rem;
        }

        /* ===== BADGE UTILITIES ===== */
        .badge-active   { background-color: var(--color-green-100); color: var(--color-green-800); }
        .badge-inactive { background-color: var(--color-gray-100);  color: var(--color-gray-600);  }
        .badge-banned   { background-color: #FEE2E2; color: var(--color-red-700); }

        /* ===== SCROLLBAR ===== */
        ::-webkit-scrollbar       { width: 6px; }
        ::-webkit-scrollbar-track { background: transparent; }
        ::-webkit-scrollbar-thumb { background: var(--color-gray-300); border-radius: 10px; }
        ::-webkit-scrollbar-thumb:hover { background: var(--color-gray-400); }
    </style>
</head>
<body>
