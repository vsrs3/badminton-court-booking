<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<div class="flex flex-col h-full bg-white">
    <div class="p-5 bg-gradient-to-br from-[#004d3d] to-[#006b54] text-white">
        <div class="flex items-center justify-between mb-4">
            <div class="flex items-center space-x-3">
                <div class="relative">
                    <img src="https://picsum.photos/seed/user/100/100" alt="Avatar" class="w-14 h-14 rounded-full border-2 border-white/30" />
                    <div class="absolute bottom-0 right-0 w-4 h-4 bg-[#9ef01a] rounded-full border-2 border-[#004d3d]"></div>
                </div>
                <div>
                    <h2 class="font-bold text-base">${sessionScope.customer.getFullName()}</h2>
                    <p class="text-xs text-white/70">${sessionScope.customer.getEmail()}</p>
                </div>
            </div>
            <a href="profile?section=profile-info">
                <i data-lucide="chevron-right" class="w-5 h-5 text-white/50"></i>
            </a>

        </div>
        <div class="bg-[#003d30] rounded-xl p-3 flex items-center justify-between border border-white/10 cursor-pointer hover:bg-[#00352a] transition-colors">
            <div class="flex items-center space-x-2">
                <i data-lucide="diamond" class="w-5 h-5 text-[#9ef01a]"></i>
                <span class="text-xs font-bold text-[#9ef01a] uppercase">Hạng thành viên</span>
            </div>
            <i data-lucide="chevron-right" class="w-4 h-4 text-[#9ef01a]"></i>
        </div>
    </div>
    <div class="p-4 bg-gray-50/50">
        <div class="grid grid-cols-4 gap-2">
            <a href="profile?section=history" class="flex flex-col items-center justify-center p-2 rounded-xl bg-white border border-gray-100 shadow-sm hover:shadow-md transition-shadow cursor-pointer">
                <i data-lucide="calendar" class="w-5 h-5 text-orange-500"></i>
                <span class="text-[10px] text-gray-600 font-medium text-center">Lịch đã đặt</span>
            </a>
            <div class="flex flex-col items-center justify-center p-2 rounded-xl bg-white border border-gray-100 shadow-sm hover:shadow-md transition-shadow cursor-pointer">
                <i data-lucide="bell" class="w-5 h-5 text-yellow-500"></i>
                <span class="text-[10px] text-gray-600 font-medium text-center">Thông báo</span>
            </div>
            <div class="flex flex-col items-center justify-center p-2 rounded-xl bg-white border border-gray-100 shadow-sm hover:shadow-md transition-shadow cursor-pointer">
                <i data-lucide="star" class="w-5 h-5 text-emerald-500"></i>
                <span class="text-[10px] text-gray-600 font-medium text-center">Đánh giá</span>
            </div>
            <div class="flex flex-col items-center justify-center p-2 rounded-xl bg-white border border-gray-100 shadow-sm hover:shadow-md transition-shadow cursor-pointer">
                <i data-lucide="gift" class="w-5 h-5 text-red-500"></i>
                <span class="text-[10px] text-gray-600 font-medium text-center">Ưu đãi</span>
            </div>
        </div>
    </div>
    <div class="flex-1 pb-20">
        <div class="px-4 pt-6 pb-2">
            <h3 class="text-xs font-bold text-gray-400 uppercase tracking-wider">Hoạt động</h3>
        </div>
        <a href="profile?section=profile-info" class="flex items-center justify-between p-4 cursor-pointer border-b border-gray-50 transition-colors hover:bg-gray-50">
            <div class="flex items-center space-x-3">
                <i data-lucide="user" class="w-5 h-5 text-emerald-700"></i>
                <span class="text-sm font-medium text-gray-700">Thông tin cá nhân</span>
            </div>
            <i data-lucide="chevron-right" class="w-4 h-4 text-gray-300"></i>
        </a>
        <div class="flex items-center justify-between p-4 cursor-pointer border-b border-gray-50 transition-colors hover:bg-gray-50">
            <div class="flex items-center space-x-3">
                <i data-lucide="heart" class="w-5 h-5 text-emerald-700"></i>
                <span class="text-sm font-medium text-gray-700">Danh sách yêu thích</span>
            </div>
            <i data-lucide="chevron-right" class="w-4 h-4 text-gray-300"></i>
        </div>
        <div class="flex items-center justify-between p-4 cursor-pointer border-b border-gray-50 transition-colors hover:bg-gray-50">
            <div class="flex items-center space-x-3">
                <i data-lucide="crown" class="w-5 h-5 text-emerald-700"></i>
                <span class="text-sm font-medium text-gray-700">Gói hội viên</span>
            </div>
            <i data-lucide="chevron-right" class="w-4 h-4 text-gray-300"></i>
        </div>
        <div class="px-4 pt-6 pb-2">
            <h3 class="text-xs font-bold text-gray-400 uppercase tracking-wider">Hệ thống</h3>
        </div>
        <a href="profile?section=settings" class="flex items-center justify-between p-4 cursor-pointer border-b border-gray-50 transition-colors hover:bg-gray-50">
            <div class="flex items-center space-x-3">
                <i data-lucide="settings" class="w-5 h-5 text-emerald-700"></i>
                <span class="text-sm font-medium text-gray-700">Cài đặt</span>
            </div>
            <i data-lucide="chevron-right" class="w-4 h-4 text-gray-300"></i>
        </a>
        <div class="flex items-center justify-between p-4 cursor-pointer border-b border-gray-50 transition-colors hover:bg-gray-50">
            <div class="flex items-center space-x-3">
                <i data-lucide="life-buoy" class="w-5 h-5 text-emerald-700"></i>
                <span class="text-sm font-medium text-gray-700">Hỗ trợ</span>
            </div>
            <i data-lucide="chevron-right" class="w-4 h-4 text-gray-300"></i>
        </div>
        <div class="flex items-center justify-between p-4 cursor-pointer border-b border-gray-50 transition-colors hover:bg-gray-50">
            <div class="flex items-center space-x-3">
                <i data-lucide="lock" class="w-5 h-5 text-emerald-700"></i>
                <span class="text-sm font-medium text-gray-700">Bảo mật & quyền riêng tư</span>
            </div>
            <i data-lucide="chevron-right" class="w-4 h-4 text-gray-300"></i>
        </div>
        <div class="flex items-center justify-between p-4 cursor-pointer border-b border-gray-50 transition-colors hover:bg-gray-50">
            <div class="flex items-center space-x-3">
                <i data-lucide="shield-check" class="w-5 h-5 text-emerald-700"></i>
                <span class="text-sm font-medium text-gray-700">Điều khoản và chính sách</span>
            </div>
            <i data-lucide="chevron-right" class="w-4 h-4 text-gray-300"></i>
        </div>
    </div>
</div>