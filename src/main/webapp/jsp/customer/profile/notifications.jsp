<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<div class="flex flex-col h-full bg-white">
    <div class="p-6 border-b border-gray-100 flex items-center space-x-3">
        <a href="profile?section=settings" class="p-2 hover:bg-gray-100 rounded-full transition-colors text-gray-600">
            <i data-lucide="arrow-left" class="w-6 h-6"></i>
        </a>
        <div class="flex items-center space-x-2">
            <i data-lucide="bell" class="w-6 h-6 text-emerald-700"></i>
            <h1 class="text-xl font-bold text-gray-800">Cài đặt thông báo</h1>
        </div>
    </div>
    <div class="p-6 space-y-4">
        <div class="flex items-center justify-between">
            <span class="text-sm font-medium text-gray-800">Thông báo đặt sân</span>
            <input type="checkbox" checked class="w-5 h-5 rounded border-gray-300 text-emerald-600 focus:ring-emerald-500" />
        </div>
        <div class="flex items-center justify-between">
            <span class="text-sm font-medium text-gray-800">Thông báo ưu đãi</span>
            <input type="checkbox" checked class="w-5 h-5 rounded border-gray-300 text-emerald-600 focus:ring-emerald-500" />
        </div>
        <div class="flex items-center justify-between">
            <span class="text-sm font-medium text-gray-800">Thông báo tin nhắn</span>
            <input type="checkbox" class="w-5 h-5 rounded border-gray-300 text-emerald-600 focus:ring-emerald-500" />
        </div>
        <button class="w-full py-4 bg-green-600 text-white rounded-xl font-bold shadow-md hover:bg-green-700">Lưu thay đổi</button>
    </div>
</div>