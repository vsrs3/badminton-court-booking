<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<div class="flex flex-col h-full bg-white">
    <div class="p-6 border-b border-gray-100 flex items-center space-x-3">
        <a href="profile?section=settings" class="p-2 hover:bg-gray-100 rounded-full transition-colors text-gray-600">
            <i data-lucide="arrow-left" class="w-6 h-6"></i>
        </a>
        <div class="flex items-center space-x-2">
            <i data-lucide="languages" class="w-6 h-6 text-emerald-700"></i>
            <h1 class="text-xl font-bold text-gray-800">Đổi ngôn ngữ</h1>
        </div>
    </div>
    <div class="p-6 max-w-4xl space-y-4" id="language-list">
        <div data-lang="vi" class="flex items-center justify-between p-4 rounded-xl border-2 cursor-pointer transition-all border-emerald-500 bg-emerald-50/30">
            <div class="flex items-center space-x-4">
                <img src="https://flagcdn.com/vn.svg" alt="Tiếng Việt" class="w-10 h-7 object-cover rounded shadow-sm" />
                <div class="flex flex-col">
                    <span class="font-bold text-gray-800">Tiếng Việt</span>
                    <span class="text-xs text-gray-400">Vietnamese</span>
                </div>
            </div>
            <div class="bg-emerald-500 rounded-full p-1">
                <i data-lucide="check" class="w-4 h-4 text-white"></i>
            </div>
        </div>
        <div data-lang="en" class="flex items-center justify-between p-4 rounded-xl border-2 cursor-pointer transition-all border-gray-100 bg-white hover:border-gray-200">
            <div class="flex items-center space-x-4">
                <img src="https://flagcdn.com/us.svg" alt="English" class="w-10 h-7 object-cover rounded shadow-sm" />
                <div class="flex flex-col">
                    <span class="font-bold text-gray-800">English</span>
                    <span class="text-xs text-gray-400">English</span>
                </div>
            </div>
            <div class="bg-emerald-500 rounded-full p-1 hidden">
                <i data-lucide="check" class="w-4 h-4 text-white"></i>
            </div>
        </div>
        <div class="pt-6">
            <button class="flex items-center space-x-2 px-8 py-3 bg-green-600 text-white rounded-lg font-bold shadow-md hover:bg-green-700 transition-all active:scale-95">
                <i data-lucide="save" class="w-4 h-4"></i>
                <span>Lưu thay đổi</span>
            </button>
        </div>
    </div>
</div>
<script>
    document.addEventListener('DOMContentLoaded', () => {
        const languages = document.querySelectorAll('#language-list > div[data-lang]');
        languages.forEach(lang => {
            lang.addEventListener('click', () => {
                languages.forEach(l => {
                    l.classList.remove('border-emerald-500', 'bg-emerald-50/30');
                    l.querySelector('.bg-emerald-500').classList.add('hidden');
                });
                lang.classList.add('border-emerald-500', 'bg-emerald-50/30');
                lang.querySelector('.bg-emerald-500').classList.remove('hidden');
            });
        });
        // Default vi
        languages[0].click();
    });
</script>