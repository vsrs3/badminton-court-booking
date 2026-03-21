/**
 * BADMINTON PRO - AI Chatbot Widget
 * Handles: Chat UI, OpenAI API communication, Facility card rendering
 */
    (function () {
        'use strict';

        // ============================================
        // STATE
        // ============================================

        const ChatState = {
            isOpen: false,
            isLoading: false,
            conversationHistory: [] // {role: 'user'|'assistant', content: '...'}
        };

        // ============================================
        // DOM REFERENCES
        // ============================================

        let $toggle, $widget, $closeBtn, $messages, $input, $sendBtn,
            $quickActions, $charCount;

        // ============================================
        // HELPERS
        // ============================================

        function getContextPath() {
            const seg = window.location.pathname.split('/')[1];
            return seg ? '/' + seg : '/badminton_court_booking';
        }

        function escapeHtml(text) {
            const div = document.createElement('div');
            div.textContent = text;
            return div.innerHTML;
        }

        function formatTime() {
            const now = new Date();
            return now.getHours().toString().padStart(2, '0') + ':' +
                now.getMinutes().toString().padStart(2, '0');
        }

        function resolveImageUrl(imageUrl) {
            if (!imageUrl) return getContextPath() + '/uploads/facility/default-facility.jpg';
            if (imageUrl.startsWith('http')) return imageUrl;
            return getContextPath() + '/' + imageUrl;
        }

        /**
         * Convert markdown-like bold (**text**) to <strong> tags
         */
        function formatBotText(text) {
            if (!text) return '';
            let html = escapeHtml(text);
            // Bold: **text**
            html = html.replace(/\*\*(.+?)\*\*/g, '<strong>$1</strong>');
            // Newlines to <br>
            html = html.replace(/\n/g, '<br>');
            return html;
        }

        // ============================================
        // WIDGET TOGGLE
        // ============================================

        function openChatbot() {
            ChatState.isOpen = true;
            $widget.classList.add('active');
            $toggle.classList.add('hidden');
            $input.focus();
            scrollToBottom();
        }

        function closeChatbot() {
            ChatState.isOpen = false;
            $widget.classList.remove('active');
            $toggle.classList.remove('hidden');
        }

        function toggleChatbot() {
            if (ChatState.isOpen) {
                closeChatbot();
            } else {
                openChatbot();
            }
        }

        // ============================================
        // MESSAGE RENDERING
        // ============================================

        function scrollToBottom() {
            requestAnimationFrame(function () {
                $messages.scrollTop = $messages.scrollHeight;
            });
        }

        function appendUserMessage(text) {
            var time = formatTime();
            var msgDiv = document.createElement('div');
            msgDiv.className = 'chatbot-msg chatbot-msg-user';
            msgDiv.innerHTML =
                '<div class="chatbot-msg-avatar"><i class="bi bi-person-fill"></i></div>' +
                '<div class="chatbot-msg-content">' +
                '<div class="chatbot-msg-bubble">' + escapeHtml(text) + '</div>' +
                '<span class="chatbot-msg-time">' + time + '</span>' +
                '</div>';
            $messages.appendChild(msgDiv);
            scrollToBottom();
        }

        function appendBotMessage(text, facilities) {
            var time = formatTime();
            var msgDiv = document.createElement('div');
            msgDiv.className = 'chatbot-msg chatbot-msg-bot';

            var html =
                '<div class="chatbot-msg-avatar"><i class="bi bi-robot"></i></div>' +
                '<div class="chatbot-msg-content">' +
                '<div class="chatbot-msg-bubble">' + formatBotText(text);

            // Add facility cards if present
            if (facilities && facilities.length > 0) {
                html += renderFacilityCards(facilities);
            }

            html += '</div>' +
                '<span class="chatbot-msg-time">' + time + '</span>' +
                '</div>';

            msgDiv.innerHTML = html;
            $messages.appendChild(msgDiv);

            // Attach event listeners to "Xem chi tiết" buttons -> redirect to detail page
            var btns = msgDiv.querySelectorAll('.chatbot-facility-detail-btn');
            btns.forEach(function (btn) {
                btn.addEventListener('click', function (e) {
                    e.preventDefault();
                    var facilityId = this.getAttribute('data-facility-id');
                    if (facilityId) {
                        // Try opening court detail panel (home page)
                        if (typeof window.openCourtDetail === 'function') {
                            closeChatbot();
                            window.openCourtDetail(facilityId);
                        } else {
                            // Fallback: navigate to home with facility param
                            window.location.href = getContextPath() + '/home?facilityId=' + facilityId;
                        }
                    }
                });
            });

            scrollToBottom();
        }

        function appendErrorMessage(text) {
            var time = formatTime();
            var msgDiv = document.createElement('div');
            msgDiv.className = 'chatbot-msg chatbot-msg-bot chatbot-msg-error';
            msgDiv.innerHTML =
                '<div class="chatbot-msg-avatar"><i class="bi bi-robot"></i></div>' +
                '<div class="chatbot-msg-content">' +
                '<div class="chatbot-msg-bubble"><i class="bi bi-exclamation-triangle me-1"></i> ' +
                escapeHtml(text) + '</div>' +
                '<span class="chatbot-msg-time">' + time + '</span>' +
                '</div>';
            $messages.appendChild(msgDiv);
            scrollToBottom();
        }

        function showTypingIndicator() {
            var typingDiv = document.createElement('div');
            typingDiv.id = 'chatbotTyping';
            typingDiv.className = 'chatbot-msg chatbot-msg-bot';
            typingDiv.innerHTML =
                '<div class="chatbot-msg-avatar"><i class="bi bi-robot"></i></div>' +
                '<div class="chatbot-msg-content">' +
                '<div class="chatbot-msg-bubble">' +
                '<div class="chatbot-typing">' +
                '<div class="chatbot-typing-dot"></div>' +
                '<div class="chatbot-typing-dot"></div>' +
                '<div class="chatbot-typing-dot"></div>' +
                '</div></div></div>';
            $messages.appendChild(typingDiv);
            scrollToBottom();
        }

        function removeTypingIndicator() {
            var el = document.getElementById('chatbotTyping');
            if (el) el.remove();
        }

        // ============================================
        // FACILITY CARDS RENDERING
        // ============================================

        function renderFacilityCards(facilities) {
            var html = '<div class="chatbot-facility-cards">';

            facilities.forEach(function (f) {
                var imgUrl = resolveImageUrl(f.imageUrl);
                var price = f.priceRange || 'Liên hệ';
                var name = escapeHtml(f.name || 'Sân cầu lông');
                var location = escapeHtml(f.location || '');

                html +=
                    '<div class="chatbot-facility-card">' +
                    '<img class="chatbot-facility-card-img" src="' + imgUrl + '" alt="' + name + '" ' +
                    'onerror="this.src=\'' + getContextPath() + '/uploads/facility/default-facility.jpg\'" />' +
                    '<div class="chatbot-facility-card-body">' +
                    '<p class="chatbot-facility-card-name" title="' + name + '">' + name + '</p>' +
                    '<div class="chatbot-facility-card-info">' +
                    '<span><i class="bi bi-geo-alt-fill"></i> ' + location + '</span>' +
                    '</div>' +
                    '<div class="chatbot-facility-card-bottom">' +
                    '<span class="chatbot-facility-card-price"><i class="bi bi-tag-fill"></i> ' + escapeHtml(price) + '</span>' +
                    '<a class="chatbot-facility-detail-btn" data-facility-id="' + f.facilityId + '" href="#">' +
                    '<i class="bi bi-arrow-right-circle-fill"></i> Xem chi tiết</a>' +
                    '</div>' +
                    '</div>' +
                    '</div>';
            });

            html += '</div>';
            return html;
        }

        // ============================================
        // API COMMUNICATION
        // ============================================

        function sendMessage(text) {
            if (ChatState.isLoading || !text.trim()) return;

            var userMsg = text.trim();

            // Add to UI
            appendUserMessage(userMsg);

            // Add to history
            ChatState.conversationHistory.push({role: 'user', content: userMsg});

            // Clear input
            $input.value = '';
            updateCharCount();
            updateSendButton();

            // Hide quick actions after first message
            if ($quickActions) {
                $quickActions.style.display = 'none';
            }

            // Show loading
            ChatState.isLoading = true;
            showTypingIndicator();
            setInputDisabled(true);

            // Prepare request body
            var body = JSON.stringify({
                message: userMsg,
                history: ChatState.conversationHistory.slice(-10)
            });

            // Call API
            fetch(getContextPath() + '/api/chatbot', {
                method: 'POST',
                headers: {'Content-Type': 'application/json; charset=UTF-8'},
                body: body
            })
                .then(function (res) {
                    return res.json();
                })
                .then(function (data) {
                    removeTypingIndicator();

                    if (data.success) {
                        appendBotMessage(data.message, data.facilities);
                        ChatState.conversationHistory.push({role: 'assistant', content: data.message});
                    } else {
                        var errorMsg = data.message || 'Xin lỗi, có lỗi xảy ra. Vui lòng thử lại! 😊';
                        appendErrorMessage(errorMsg);
                    }
                })
                .catch(function (err) {
                    removeTypingIndicator();
                    console.error('Chatbot API error:', err);
                    appendErrorMessage('Không thể kết nối tới server. Vui lòng thử lại sau! 🔄');
                })
                .finally(function () {
                    ChatState.isLoading = false;
                    setInputDisabled(false);
                    $input.focus();
                });
        }

        // ============================================
        // INPUT HANDLING
        // ============================================

        function setInputDisabled(disabled) {
            $input.disabled = disabled;
            $sendBtn.disabled = disabled || !$input.value.trim();
        }

        function updateSendButton() {
            $sendBtn.disabled = ChatState.isLoading || !$input.value.trim();
        }

        function updateCharCount() {
            if ($charCount) {
                $charCount.textContent = $input.value.length;
            }
        }

        function handleInputKeydown(e) {
            if (e.key === 'Enter' && !e.shiftKey) {
                e.preventDefault();
                if (!$sendBtn.disabled) {
                    sendMessage($input.value);
                }
            }
        }

        function handleQuickAction(e) {
            var btn = e.target.closest('.chatbot-quick-btn');
            if (!btn) return;
            var msg = btn.getAttribute('data-msg');
            if (msg) {
                sendMessage(msg);
            }
        }

        // ============================================
        // INITIALIZATION
        // ============================================

        function init() {
            // Get DOM elements
            $toggle = document.getElementById('chatbotToggle');
            $widget = document.getElementById('chatbotWidget');
            $closeBtn = document.getElementById('chatbotClose');
            $messages = document.getElementById('chatbotMessages');
            $input = document.getElementById('chatbotInput');
            $sendBtn = document.getElementById('chatbotSend');
            $quickActions = document.getElementById('chatbotQuickActions');
            $charCount = document.getElementById('chatbotCharCount');

            if (!$toggle || !$widget) {
                console.warn('Chatbot elements not found in DOM');
                return;
            }

            // Event listeners
            $toggle.addEventListener('click', toggleChatbot);
            $closeBtn.addEventListener('click', closeChatbot);

            $input.addEventListener('input', function () {
                updateSendButton();
                updateCharCount();
            });
            $input.addEventListener('keydown', handleInputKeydown);

            $sendBtn.addEventListener('click', function () {
                sendMessage($input.value);
            });

            if ($quickActions) {
                $quickActions.addEventListener('click', handleQuickAction);
            }

            // Close on Escape
            document.addEventListener('keydown', function (e) {
                if (e.key === 'Escape' && ChatState.isOpen) {
                    closeChatbot();
                }
            });

            console.log('🤖 Chatbot initialized');
        }

        // Start when DOM is ready
        if (document.readyState === 'loading') {
            document.addEventListener('DOMContentLoaded', init);
        } else {
            init();
        }

    })();
