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
        .sidebar-active {
            background-color: rgba(163, 230, 53, 0.1);
            color: var(--color-lime-brand) !important;
            border-right: 4px solid var(--color-lime-brand);
        }

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
