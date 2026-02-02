<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

<div class="flex flex-col h-full bg-white">
    <div class="p-6 border-b border-gray-100 flex items-center space-x-3">
<%--        <a href="profile?section=profile" class="p-2 hover:bg-gray-100 rounded-full transition-colors text-gray-600">--%>
<%--            <i data-lucide="arrow-left" class="w-6 h-6"></i>--%>
<%--        </a>--%>
        <div class="flex items-center space-x-2">
            <i data-lucide="settings" class="w-6 h-6 text-emerald-700"></i>
            <h1 class="text-xl font-bold text-gray-800">Cài đặt</h1>
        </div>
    </div>
    <div class="flex-1 overflow-y-auto">
        <a href="profile?section=notifications" class="flex items-center justify-between p-4 border-b border-gray-100 hover:bg-gray-50 transition-colors">
            <div class="flex items-center space-x-3">
                <div class="text-blue-500">
                    <i data-lucide="bell" class="w-5 h-5"></i>
                </div>
                <span class="text-sm font-medium text-gray-800">Cài đặt thông báo</span>
            </div>
            <i data-lucide="chevron-right" class="w-5 h-5 text-gray-300"></i>
        </a>
        <a href="profile?section=languages" class="flex items-center justify-between p-4 border-b border-gray-100 hover:bg-gray-50 transition-colors">
            <div class="flex items-center space-x-3">
                <div class="text-purple-500">
                    <i data-lucide="languages" class="w-5 h-5"></i>
                </div>
                <span class="text-sm font-medium text-gray-800">Ngôn ngữ</span>
            </div>
            <i data-lucide="chevron-right" class="w-5 h-5 text-gray-300"></i>
        </a>
        <a href="profile?section=change-password" class="flex items-center justify-between p-4 border-b border-gray-100 hover:bg-gray-50 transition-colors">
            <div class="flex items-center space-x-3">
                <div class="text-green-500">
                    <i data-lucide="key-round" class="w-5 h-5"></i>
                </div>
                <span class="text-sm font-medium text-gray-800">Đổi mật khẩu</span>
            </div>
            <i data-lucide="chevron-right" class="w-5 h-5 text-gray-300"></i>
        </a>
        <form action="customerController" method="GET" id="logoutForm"
              onsubmit="setupConfirm(this);"
              class="flex items-center justify-
              between p-4 border-b border-gray-100 hover:bg-gray-50 transition-colors">
            <input type="hidden" name="action" value="logout">
            <button type="submit" class="flex items-center space-x-3 w-full text-left">
                <div class="text-red-500">
                    <i data-lucide="log-out" class="w-5 h-5"></i>
                </div>
                <span class="text-sm font-medium text-gray-800">Đăng xuất</span>
            </button>
            <i data-lucide="chevron-right" class="w-5 h-5 text-gray-300 pointer-events-none"></i>
        </form>
        <div class="flex items-center justify-between p-4 border-b border-gray-100 hover:bg-gray-50 transition-colors">
            <div class="flex items-center space-x-3">
                <div class="text-red-500">
                    <i data-lucide="user-x" class="w-5 h-5"></i>
                </div>
                <span class="text-sm font-medium text-gray-800">Xóa tài khoản</span>
            </div>
            <i data-lucide="chevron-right" class="w-5 h-5 text-gray-300"></i>
        </div>
    </div>
</div>

<script>
    document.addEventListener('DOMContentLoaded', function() {

        setupConfirm('logoutForm', 'Đăng xuất ?',
            'Bạn có chắc muốn thoát không ?', 'question', 'Đăng xuất');

        setupConfirm('deleteAccount', 'Cảnh báo !',
            'Xóa tài khoản sẽ mất hết dữ liệu vĩnh viễn !', 'warning', 'Xóa ngay');

    });
</script>