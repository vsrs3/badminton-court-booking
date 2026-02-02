<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<div class="flex flex-col h-full bg-white">
    <div class="p-6">
        <div class="flex items-center space-x-2 mb-6">
            <div class="text-green-600">
                <i data-lucide="volleyball" class="w-6 h-6"></i>
            </div>
            <h1 class="text-xl font-bold text-gray-800">Lịch sử đặt sân</h1>
        </div>
        <div class="flex items-center space-x-2 overflow-x-auto pb-4 no-scrollbar" id="filter-tabs">
            <button data-tab="all" class="px-4 py-2 rounded-full text-sm font-medium whitespace-nowrap transition-all border bg-green-600 text-white border-green-600 shadow-sm">Tất cả</button>
            <button data-tab="pending" class="px-4 py-2 rounded-full text-sm font-medium whitespace-nowrap transition-all border bg-white text-gray-600 border-gray-100 hover:bg-gray-50">Chờ xác nhận</button>
            <button data-tab="confirmed" class="px-4 py-2 rounded-full text-sm font-medium whitespace-nowrap transition-all border bg-white text-gray-600 border-gray-100 hover:bg-gray-50">Đã xác nhận</button>
            <button data-tab="completed" class="px-4 py-2 rounded-full text-sm font-medium whitespace-nowrap transition-all border bg-white text-gray-600 border-gray-100 hover:bg-gray-50">Hoàn thành</button>
            <button data-tab="cancelled" class="px-4 py-2 rounded-full text-sm font-medium whitespace-nowrap transition-all border bg-white text-gray-600 border-gray-100 hover:bg-gray-50">Đã hủy</button>
        </div>
        <div class="h-[1px] bg-gray-100 w-full mt-2"></div>
    </div>
    <div class="flex-1 flex flex-col items-center justify-center p-8 text-center bg-gray-50/50">
        <div class="mb-6 opacity-40">
            <div class="w-32 h-32 mx-auto bg-white rounded-full flex items-center justify-center border-2 border-dashed border-gray-200 shadow-inner">
                <i data-lucide="search" class="w-12 h-12 text-gray-300"></i>
            </div>
        </div>
        <p class="text-gray-500 font-semibold text-lg">Bạn chưa có lịch đặt</p>
        <p class="text-sm text-gray-400 mt-2 max-w-xs mx-auto">Các lịch thi đấu hoặc luyện tập của bạn sẽ xuất hiện tại đây sau khi bạn đặt sân thành công.</p>
        <button class="mt-8 bg-[#9ef01a] text-[#004d3d] px-10 py-3.5 rounded-full font-bold text-sm shadow-md hover:shadow-lg transform transition-all hover:scale-105 active:scale-95">Đặt sân ngay</button>
    </div>
</div>
<script>
    document.addEventListener('DOMContentLoaded', () => {
        const tabs = document.querySelectorAll('#filter-tabs button');
        tabs.forEach(tab => {
            tab.addEventListener('click', () => {
                tabs.forEach(t => t.classList.remove('bg-green-600', 'text-white', 'border-green-600', 'shadow-sm'));
                tab.classList.add('bg-green-600', 'text-white', 'border-green-600', 'shadow-sm');
            });
        });
    });
</script>