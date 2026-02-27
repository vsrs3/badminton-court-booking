<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:set var="language" value="${not empty param.language ? param.language : not empty sessionScope.language ? sessionScope.language : 'vi'}" scope="session" />
<fmt:setLocale value="${language}" />
<fmt:setBundle basename="messages" />

<div class="flex flex-col h-full bg-white">
    <div class="p-6 border-b border-gray-100 flex items-center space-x-3">
        <a href="profile?section=settings" class="p-2 hover:bg-gray-100 rounded-full transition-colors text-gray-600">
            <i data-lucide="arrow-left" class="w-6 h-6"></i>
        </a>
        <div class="flex items-center space-x-2">
            <i data-lucide="languages" class="w-6 h-6 text-emerald-700"></i>
            <h1 class="text-xl font-bold text-gray-800"><fmt:message key="language.change" /></h1>
        </div>
    </div>
    <div class="p-6 max-w-4xl space-y-4" id="language-list">
        <!-- Vietnamese -->
        <div data-lang="vi" class="flex items-center justify-between p-4 rounded-xl border-2 cursor-pointer transition-all 
        	${language eq 'vi' ? 'border-emerald-500 bg-emerald-50/30' : 'border-gray-100 bg-white hover:border-gray-200'}">
            <div class="flex items-center space-x-4">
                <img src="https://flagcdn.com/vn.svg" alt="Tiếng Việt" class="w-10 h-7 object-cover rounded shadow-sm" />
                <div class="flex flex-col">
                    <span class="font-bold text-gray-800">Tiếng Việt</span>
                    <span class="text-xs text-gray-400">Vietnamese</span>
                </div>
            </div>
            <div class="bg-emerald-500 rounded-full p-1 ${language eq 'vi' ? '' : 'hidden'}">
                <i data-lucide="check" class="w-4 h-4 text-white"></i>
            </div>
        </div>
        
        <!-- English -->
        <div data-lang="en" class="flex items-center justify-between p-4 rounded-xl border-2 cursor-pointer transition-all 
        	${language eq 'en' ? 'border-emerald-500 bg-emerald-50/30' : 'border-gray-100 bg-white hover:border-gray-200'}">
            <div class="flex items-center space-x-4">
                <img src="https://flagcdn.com/us.svg" alt="English" class="w-10 h-7 object-cover rounded shadow-sm" />
                <div class="flex flex-col">
                    <span class="font-bold text-gray-800">English</span>
                    <span class="text-xs text-gray-400">English</span>
                </div>
            </div>
            <div class="bg-emerald-500 rounded-full p-1 ${language eq 'en' ? '' : 'hidden'}">
                <i data-lucide="check" class="w-4 h-4 text-white"></i>
            </div>
        </div>
        
        <div class="pt-6">
            <button id="saveLanguageBtn" class="flex items-center space-x-2 px-8 py-3 bg-green-600 text-white rounded-lg font-bold shadow-md hover:bg-green-700 transition-all active:scale-95">
                <i data-lucide="save" class="w-4 h-4"></i>
                <span><fmt:message key="language.save" /></span>
            </button>
        </div>
        
        <!-- Success Message -->
        <div id="successMessage" class="hidden p-4 bg-green-50 border border-green-200 rounded-lg">
            <div class="flex items-center space-x-2 text-green-800">
                <i data-lucide="check-circle" class="w-5 h-5"></i>
                <span class="font-medium"><fmt:message key="language.success" /></span>
            </div>
        </div>
    </div>
</div>

<script>
    document.addEventListener('DOMContentLoaded', () => {
        let selectedLanguage = '${language}';
        const languages = document.querySelectorAll('#language-list > div[data-lang]');
        const saveBtn = document.getElementById('saveLanguageBtn');
        const successMessage = document.getElementById('successMessage');
        
        // Language selection
        languages.forEach(lang => {
            lang.addEventListener('click', () => {
                selectedLanguage = lang.dataset.lang;
                
                languages.forEach(l => {
                    l.classList.remove('border-emerald-500', 'bg-emerald-50/30');
                    l.classList.add('border-gray-100', 'bg-white');
                    l.querySelector('.bg-emerald-500').classList.add('hidden');
                });
                
                lang.classList.remove('border-gray-100', 'bg-white');
                lang.classList.add('border-emerald-500', 'bg-emerald-50/30');
                lang.querySelector('.bg-emerald-500').classList.remove('hidden');
            });
        });
        
        // Save language
        saveBtn.addEventListener('click', async () => {
            try {
                saveBtn.disabled = true;
                saveBtn.classList.add('opacity-50', 'cursor-not-allowed');
                
                const response = await fetch('change-language', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/x-www-form-urlencoded',
                    },
                    body: 'language=' + selectedLanguage
                });
                
                if (response.ok) {
                    // Show success message
                    successMessage.classList.remove('hidden');
                    
                    // Reload page after 1 second to apply new language
                    setTimeout(() => {
                        window.location.reload();
                    }, 1000);
                } else {
                    alert('Error saving language preference');
                    saveBtn.disabled = false;
                    saveBtn.classList.remove('opacity-50', 'cursor-not-allowed');
                }
            } catch (error) {
                console.error('Error:', error);
                alert('Error saving language preference');
                saveBtn.disabled = false;
                saveBtn.classList.remove('opacity-50', 'cursor-not-allowed');
            }
        });
    });
</script>
