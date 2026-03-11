
<section class="p-6">
    <div class="flex items-center justify-between mb-5">
        <div>
            <h2 class="text-xl font-semibold text-gray-800">Favorite Facilities</h2>
            <p class="text-sm text-gray-500 mt-1">Facilities you saved for quick booking</p>
        </div>
        <a href="${pageContext.request.contextPath}/home" class="px-4 py-2 rounded-lg bg-emerald-600 text-white text-sm hover:bg-emerald-700 transition-colors">
            Go to home
        </a>
    </div>

    <div class="mb-4">
        <input
                type="text"
                id="favoriteSearchInput"
                class="w-full rounded-xl border border-gray-200 px-4 py-3 text-sm focus:outline-none focus:ring-2 focus:ring-emerald-500"
                placeholder="Search by facility name, address, province or district..."
        />
    </div>

    <div id="favoriteListLoading" class="text-sm text-gray-500">Loading favorites...</div>
    <div id="favoriteListEmpty" class="hidden text-sm text-gray-500">No favorite facility found.</div>
    <div id="favoriteList" class="grid grid-cols-1 xl:grid-cols-2 gap-4"></div>
</section>

<script>
    (function () {
        'use strict';

        const contextPath = '${pageContext.request.contextPath}';
        const listEl = document.getElementById('favoriteList');
        const emptyEl = document.getElementById('favoriteListEmpty');
        const loadingEl = document.getElementById('favoriteListLoading');
        const searchInput = document.getElementById('favoriteSearchInput');

        if (!listEl || !emptyEl || !loadingEl || !searchInput) {
            return;
        }

        let currentKeyword = '';

        function debounce(fn, wait) {
            let timeout;
            return function (...args) {
                clearTimeout(timeout);
                timeout = setTimeout(() => fn.apply(this, args), wait);
            };
        }

        function escapeHtml(text) {
            return String(text || '')
                .replace(/&/g, '&amp;')
                .replace(/</g, '&lt;')
                .replace(/>/g, '&gt;')
                .replace(/"/g, '&quot;')
                .replace(/'/g, '&#039;');
        }

        function renderItems(items) {
            if (!items || items.length === 0) {
                listEl.innerHTML = '';
                emptyEl.classList.remove('hidden');
                return;
            }

            emptyEl.classList.add('hidden');
            listEl.innerHTML = items.map(item => `
                <article class="rounded-2xl border border-gray-200 bg-white p-4 shadow-sm">
                    <div class="flex items-start justify-between gap-3">
                        <div>
                            <h3 class="font-semibold text-gray-800">${escapeHtml(item.name)}</h3>
                            <p class="text-sm text-gray-500 mt-1">${escapeHtml(item.location || '')}</p>
                            <p class="text-xs text-gray-400 mt-2">Rating: ${Number(item.rating || 0).toFixed(1)} | Open: ${escapeHtml(item.openTime || '')}</p>
                        </div>
                        <button
                            type="button"
                            class="favorite-remove-btn px-3 py-2 rounded-lg border border-red-200 text-red-600 text-xs hover:bg-red-50"
                            data-facility-id="${escapeHtml(item.id)}"
                        >
                            Remove
                        </button>
                    </div>
                </article>
            `).join('');

            listEl.querySelectorAll('.favorite-remove-btn').forEach(btn => {
                btn.addEventListener('click', async function () {
                    const facilityId = this.dataset.facilityId;
                    await removeFavorite(facilityId);
                });
            });
        }

        async function loadFavorites() {
            loadingEl.classList.remove('hidden');
            emptyEl.classList.add('hidden');

            try {
                const params = new URLSearchParams({
                    page: '0',
                    pageSize: '100',
                    favoritesOnly: 'true'
                });

                const q = currentKeyword.trim();
                if (q) {
                    params.append('q', q);
                }

                const response = await fetch(`${contextPath}/api/facilities?${params.toString()}`);
                if (response.status === 401) {
                    window.location.href = `${contextPath}/auth/login`;
                    return;
                }
                if (!response.ok) {
                    throw new Error(`HTTP ${response.status}`);
                }

                const result = await response.json();
                if (!result.success) {
                    throw new Error(result.error || 'Failed to load favorites');
                }

                renderItems(result.data || []);
            } catch (error) {
                console.error('Load favorites error:', error);
                listEl.innerHTML = '<div class="text-sm text-red-600">Failed to load favorites.</div>';
            } finally {
                loadingEl.classList.add('hidden');
            }
        }

        async function removeFavorite(facilityId) {
            try {
                const response = await fetch(`${contextPath}/api/facilities/favorites/${encodeURIComponent(facilityId)}`, {
                    method: 'DELETE'
                });

                if (response.status === 401) {
                    window.location.href = `${contextPath}/auth/login`;
                    return;
                }
                if (!response.ok) {
                    throw new Error(`HTTP ${response.status}`);
                }

                await loadFavorites();
            } catch (error) {
                console.error('Remove favorite error:', error);
            }
        }

        searchInput.addEventListener('input', debounce(function (e) {
            currentKeyword = e.target.value || '';
            loadFavorites();
        }, 300));

        loadFavorites();
    })();
</script>
