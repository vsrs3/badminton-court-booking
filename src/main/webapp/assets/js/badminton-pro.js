/**
 * BADMINTON PRO - Main Application JavaScript
 * Handles: Tabs, Search, Filters, Favorites, Court Details, API Integration
 */
const contextPath = window.location.pathname.split('/')[1]
    ? '/' + window.location.pathname.split('/')[1]
    : '';

(function() {
    'use strict';

    // ============================================
    // STATE MANAGEMENT
    // ============================================

    const AppState = {
        courts: [],
        filteredCourts: [],
        currentTab: 'home',
        isShowingFavorites: false,
        userLocation: null,
        filters: {
            province: '',
            district: '',
            maxDistance: null
        },
        searchQuery: '',
        selectedCourt: null
    };

    // ============================================
    // CONSTANTS
    // ============================================

    const TABS = {
        HOME: 'home',
        MAP: 'map',
        BOOKING: 'booking',
        OFFER: 'offer',
        PROFILE: 'profile'
    };

    // Pagination & Infinite Scroll
    let currentPage = 0;
    const PAGE_SIZE = 12;
    let isLoading = false;
    let hasMore = true;
    let inFlightRequestController = null;

    // ============================================
    // AUTH MODAL FUNCTIONS
    // ============================================

    /**
     * Show auth modal (login required)
     */
    function showAuthModal() {
        const backdrop = document.getElementById('authModalBackdrop');
        const modal = document.getElementById('authModal');

        if (backdrop && modal) {
            backdrop.classList.add('active');
            modal.classList.add('active');
            document.body.style.overflow = 'hidden';
        }
    }

    /**
     * Close auth modal
     */
    function closeAuthModal() {
        const backdrop = document.getElementById('authModalBackdrop');
        const modal = document.getElementById('authModal');

        if (backdrop && modal) {
            backdrop.classList.remove('active');
            modal.classList.remove('active');
            document.body.style.overflow = '';
        }
    }

    /**
     * Check if user is logged in
     * ✅ CRITICAL FIX: Check window.IS_LOGGED_IN (set by JSP)
     */
    function isUserLoggedIn() {
        // Check global variable set by JSP
        const loggedIn = window.IS_LOGGED_IN === true;
        console.log('🔐 isUserLoggedIn check:', {
            windowIsLoggedIn: window.IS_LOGGED_IN,
            result: loggedIn,
            currentUser: window.CURRENT_USER
        });
        return loggedIn;
    }

    /**
     * Require login for a feature
     */
    function requireLogin(featureName) {
        const isLoggedIn = isUserLoggedIn();

        console.log('🔒 Login check for "' + featureName + '":', {
            isLoggedIn: isLoggedIn,
            windowIsLoggedIn: window.IS_LOGGED_IN,
            currentUser: window.CURRENT_USER
        });

        if (!isLoggedIn) {
            console.log('❌ Login required, showing auth modal');
            showAuthModal();
            return false;
        }

        console.log('✅ User is logged in, feature accessible');
        return true;
    }

    // ============================================
    // UTILITY FUNCTIONS
    // ============================================

    /**
     * Calculate distance between two coordinates (Haversine formula)
     */
    function calculateDistance(lat1, lon1, lat2, lon2) {
        const R = 6371; // Earth's radius in km
        const dLat = (lat2 - lat1) * Math.PI / 180;
        const dLon = (lon2 - lon1) * Math.PI / 180;
        const a =
            Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2);
        const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    /**
     * Format distance
     */
    function formatDistance(distKm) {
        if (distKm === null) return 'Đang tính...';
        return distKm < 1 ? `${Math.round(distKm * 1000)}m` : `${distKm.toFixed(1)}km`;
    }

    /**
     * Show toast notification
     */
    function showToast(message) {
        const toastEl = document.getElementById('toast');
        const toastMessage = document.getElementById('toastMessage');

        if (toastEl && toastMessage) {
            toastMessage.textContent = message;
            const toast = new bootstrap.Toast(toastEl, {
                animation: true,
                autohide: true,
                delay: 3000
            });
            toast.show();
        }
    }

    /**
     * Debounce function
     */
    function debounce(func, wait) {
        let timeout;
        return function executedFunction(...args) {
            const later = () => {
                clearTimeout(timeout);
                func(...args);
            };
            clearTimeout(timeout);
            timeout = setTimeout(later, wait);
        };
    }

    // ============================================
    // API INTEGRATION
    // ============================================

    /**
     * Load facilities from API
     */
    async function loadFacilitiesFromAPI(page = 0) {
        console.log('Loading facilities from API, page:', page);

        if (isLoading && page > 0) {
            console.log('Already loading, skipping...');
            return;
        }

        // Allow loading first page even if hasMore is false (for refresh)
        if (page > 0 && !hasMore) {
            console.log('No more data, skipping...');
            return;
        }

        isLoading = true;

        try {
            // Build API URL
            const params = new URLSearchParams({
                page: page,
                pageSize: PAGE_SIZE
            });
            const trimmedQuery = AppState.searchQuery.trim();
            if (trimmedQuery) {
                params.append("q", trimmedQuery);
            }
            if (AppState.filters.province) {
                params.append("province", AppState.filters.province);
            }
            if (AppState.filters.district) {
                params.append("district", AppState.filters.district);
            }

            if (AppState.isShowingFavorites) {
                params.append("favoritesOnly", "true");
            }


            // ✅ IMPORTANT: Add user location if available
            if (AppState.userLocation) {
                params.append('userLat', AppState.userLocation.lat);
                params.append('userLng', AppState.userLocation.lng);
                console.log('Sending user location:', AppState.userLocation);
            } else {
                console.log('No user location available');
            }

            // Get context path dynamically
            const contextPath = window.location.pathname.split('/')[1] || 'badminton_court_booking';
            const apiUrl = `/${contextPath}/api/facilities?${params}`;

            console.log('Fetching from:', apiUrl);

            if (page === 0 && inFlightRequestController) {
                inFlightRequestController.abort();
            }

            const requestController = new AbortController();
            inFlightRequestController = requestController;

            const response = await fetch(apiUrl, {
                signal: requestController.signal
            });

            if (requestController !== inFlightRequestController) {
                return;
            }

            console.log('Response status:', response.status);

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const result = await response.json();
            console.log('API result:', result);

            if (result.success) {
                const newFacilities = result.data;
                console.log('Loaded', newFacilities.length, 'facilities');

                // Append to existing courts (for infinite scroll)
                if (page === 0) {
                    AppState.courts = newFacilities;
                } else {
                    AppState.courts = [...AppState.courts, ...newFacilities];
                }

                console.log('Total courts in state:', AppState.courts.length);

                // Update pagination state
                hasMore = result.pagination.hasMore;
                currentPage = page;

                // ✅ Sync to global for map
                window.COURTS_DATA = AppState.courts;
                // Apply filters and render
                applyFiltersAndSearch();

            } else {
                console.error('API error:', result.error);
                showToast('Không thể tải dữ liệu sân');
            }

        } catch (error) {
            if (error.name === 'AbortError') {
                return;
            }
            console.error('Error loading facilities:', error);
            inFlightRequestController = null;
            showToast('Lỗi kết nối. Vui lòng thử lại');
        } finally {
            if (inFlightRequestController && inFlightRequestController.signal.aborted) {
                inFlightRequestController = null;
            }
            isLoading = false;
        }
    }

    // ============================================
    // GEOLOCATION
    // ============================================

    function getUserLocation() {
        if ("geolocation" in navigator) {
            navigator.geolocation.getCurrentPosition(
                (position) => {
                    AppState.userLocation = {
                        lat: position.coords.latitude,
                        lng: position.coords.longitude
                    };
                    console.log('✅ User location obtained:', AppState.userLocation);

                    // ✅ Reload facilities with location to get distance calculation
                    loadFacilitiesFromAPI(0);
                },
                (error) => {
                    console.error("Error getting location:", error);
                    showToast("Không thể lấy vị trí của bạn");

                    // Still load facilities without location
                    loadFacilitiesFromAPI(0);
                }
            );
        } else {
            console.log("Geolocation not supported");
            // Load without location
            loadFacilitiesFromAPI(0);
        }
    }

    // ============================================
    // FILTER & SEARCH LOGIC
    // ============================================

    function applyFiltersAndSearch() {
        console.log('Applying filters and search...');
        let courts = [...AppState.courts];

        // Search/province/district are filtered on API side (database).

        // 1. Distance filter
        if (AppState.filters.maxDistance !== null && AppState.userLocation) {
            courts = courts.filter(c => {
                // Parse distance string back to number
                const distStr = c.distance;
                if (distStr === 'Đang tính...') return true;

                let distKm = 0;
                if (distStr.endsWith('km')) {
                    distKm = parseFloat(distStr);
                } else if (distStr.endsWith('m')) {
                    distKm = parseFloat(distStr) / 1000;
                }

                return distKm <= AppState.filters.maxDistance;
            });
        }

        // 5. Favorites filter
        if (AppState.isShowingFavorites) {
            courts = courts.filter(c => c.isFavorite);
        }

        AppState.filteredCourts = courts;
        console.log('Filtered courts:', AppState.filteredCourts.length);

        renderCourts();
    }

    // ============================================
    // RENDERING
    // ============================================

    function renderCourts() {
        const grid = document.getElementById('courtsGrid');
        const noResults = document.getElementById('noResults');
        const favoritesHeader = document.getElementById('favoritesHeader');

        if (!grid || !noResults) {
            console.error('Grid or noResults element not found');
            return;
        }

        // Show/hide favorites header
        if (favoritesHeader) {
            favoritesHeader.style.display = AppState.isShowingFavorites ? 'block' : 'none';
        }

        // Check if we have courts to display
        if (AppState.filteredCourts.length === 0) {
            grid.innerHTML = '';
            noResults.style.display = 'flex';
            return;
        }

        noResults.style.display = 'none';

        // Get template
        const template = document.getElementById('courtCardTemplate');
        if (!template) {
            console.error('Court card template not found');
            return;
        }

        const templateHTML = template.innerHTML;

        // Render cards
        grid.innerHTML = AppState.filteredCourts.map(court => {
            return templateHTML
                .replace(/{courtId}/g, court.id)
                .replace(/{imageUrl}/g, contextPath + '/' + court.imageUrl)
                .replace(/{courtName}/g, court.name)
                .replace(/{rating}/g, court.rating.toFixed(1))
                .replace(/{favoriteClass}/g, court.isFavorite ? 'is-favorite' : '')
                .replace(/{favoriteFill}/g, court.isFavorite ? '-fill' : '')
                .replace(/{distance}/g, court.distance)
                .replace(/{location}/g, court.location)
                .replace(/{openTime}/g, court.openTime);
        }).join('');

        // Attach event listeners
        attachCourtCardListeners();
    }

    function attachCourtCardListeners() {
        // Card click - open detail
        document.querySelectorAll('.court-card').forEach(card => {
            card.addEventListener('click', function(e) {
                // Don't open detail if clicking on action buttons
                if (e.target.closest('.card-action-btn') || e.target.closest('.btn-book')) {
                    return;
                }
                const courtId = this.dataset.courtId;
                openCourtDetail(courtId);
            });
        });

        // Favorite buttons
        document.querySelectorAll('.btn-favorite').forEach(btn => {
            btn.addEventListener('click', function(e) {
                e.stopPropagation();
                const courtId = this.dataset.courtId;
                toggleFavorite(courtId);
            });
        });

        // Navigate buttons
        document.querySelectorAll('.btn-navigate').forEach(btn => {
            btn.addEventListener('click', function(e) {
                e.stopPropagation();
                const courtId = this.dataset.courtId;
                const court = AppState.courts.find(c => c.id === courtId);

                if (court) {
                    // ✅ Option 1: Open with exact coordinates if available
                    if (court.lat && court.lng) {
                        // Format: https://www.google.com/maps/search/?api=1&query=21.0321,105.7834
                        const mapsUrl = `https://www.google.com/maps/search/?api=1&query=${court.lat},${court.lng}`;
                        window.open(mapsUrl, '_blank');

                        console.log('📍 Opening Google Maps:', mapsUrl);
                    }
                    // ✅ Option 2: Fallback with text if coordinates not available
                    else {
                        const encodedLocation = encodeURIComponent(`${court.name} ${court.location}`);
                        const mapsUrl = `https://www.google.com/maps/search/?api=1&query=${encodedLocation}`;
                        window.open(mapsUrl, '_blank');

                        console.log('📍 Opening Google Maps (text search):', mapsUrl);
                    }
                }
            });
        });

        // Book buttons — open Booking Type Modal (booking-type-modal.jsp)
        document.querySelectorAll('.btn-book').forEach(btn => {
            if (btn.dataset.bkBound === '1') return; // skip if already bound by modal script
            btn.addEventListener('click', function(e) {
                e.stopPropagation();

                // ✅ Require login
                if (!requireLogin('Đặt sân')) {
                    return;
                }

                const courtId   = this.dataset.courtId;
                const court     = AppState.courts.find(c => String(c.id) === String(courtId));
                const courtName = court ? court.name : 'Sân cầu lông';
                // venueId = facilityId = court.facilityId (từ API trả về)
                const venueId   = court ? (court.facilityId || court.id) : courtId;

                console.log('📅 Opening booking modal for:', courtName, 'venueId:', venueId);

                // Ưu tiên dùng BookingTypeModal nếu đã load từ jsp/components
                if (window.BookingTypeModal) {
                    window.BookingTypeModal.open(venueId, courtName);
                } else {
                    // Fallback: navigate thẳng tới single-booking
                    const today = new Date();
                    const dateStr = today.getFullYear() + '-'
                        + String(today.getMonth()+1).padStart(2,'0') + '-'
                        + String(today.getDate()).padStart(2,'0');
                    window.location.href = contextPath
                        + '/jsp/booking/singlebooking/single-booking.jsp'
                        + '?facilityId=' + encodeURIComponent(venueId)
                        + '&date='   + encodeURIComponent(dateStr);
                }
            });
        });
    }

    // ============================================
    // TAB SWITCHING
    // ============================================

    function switchTab(tabName) {
        console.log('🔄 Switching to tab:', tabName);

        // ✅ Check login requirement for certain tabs
        const loginRequiredTabs = [TABS.BOOKING, TABS.OFFER, TABS.PROFILE];

        if (loginRequiredTabs.includes(tabName)) {
            if (!requireLogin('Tab: ' + tabName)) {
                return; // Don't switch tab if not logged in
            }
        }

        if (AppState.currentTab === tabName) return;

        AppState.currentTab = tabName;

        // Update tab content visibility
        document.querySelectorAll('.tab-content').forEach(tab => {
            tab.classList.remove('active');
        });

        const activeTab = document.getElementById(`${tabName}Tab`);
        if (activeTab) {
            activeTab.classList.add('active');
        }

        // Update bottom nav
        document.querySelectorAll('.bottom-nav .nav-item, .bottom-nav .nav-center-btn').forEach(btn => {
            btn.classList.remove('active');
        });

        const activeNavBtn = document.querySelector(`[data-tab="${tabName}"]`);
        if (activeNavBtn) {
            activeNavBtn.classList.add('active');
        }

        // Hide/show header based on tab
        const header = document.getElementById('mainHeader');
        if (header) {
            header.style.display = tabName === TABS.MAP ? 'none' : 'block';
        }

        // Reset favorites when changing tabs
        if (tabName !== TABS.HOME) {
            const wasShowingFavorites = AppState.isShowingFavorites;
            AppState.isShowingFavorites = false;
            updateFavoriteButton();

            if (wasShowingFavorites) {
                currentPage = 0;
                hasMore = true;
                loadFacilitiesFromAPI(0);
            }
        }

        // Initialize map if switching to map tab
        if (tabName === TABS.MAP) {
            setTimeout(() => {
                if (window.initMap) {
                    window.initMap();

                    // Update markers with current courts data
                    if (window.COURTS_DATA && window.COURTS_DATA.length > 0) {
                        if (window.updateMapMarkers) {
                            window.updateMapMarkers(window.COURTS_DATA);
                        }
                    }
                }
            }, 100);
        }
    }

    // ============================================
    // FAVORITES
    // ============================================

    async function toggleFavorite(courtId) {
        if (!requireLogin('Yeu thich')) {
            return;
        }

        const court = AppState.courts.find(c => String(c.id) === String(courtId));
        if (!court) return;

        const nextFavoriteState = !court.isFavorite;
        const previousFavoriteState = court.isFavorite;

        // Optimistic UI update
        court.isFavorite = nextFavoriteState;
        applyFiltersAndSearch();

        if (AppState.selectedCourt && String(AppState.selectedCourt.id) === String(courtId)) {
            AppState.selectedCourt = court;
            updateDetailFavoriteButton();
        }

        const currentContextPath = window.location.pathname.split('/')[1] || 'badminton_court_booking';
        const favoriteUrl = `/${currentContextPath}/api/facilities/favorites/${encodeURIComponent(courtId)}`;

        try {
            const response = await fetch(favoriteUrl, {
                method: nextFavoriteState ? 'POST' : 'DELETE'
            });

            if (response.status === 401) {
                court.isFavorite = previousFavoriteState;
                applyFiltersAndSearch();
                showAuthModal();
                return;
            }

            if (!response.ok) {
                throw new Error(`HTTP ${response.status}`);
            }

            const result = await response.json();
            if (!result.success) {
                throw new Error(result.error || 'Favorite update failed');
            }

            // If showing favorites-only list, removing one should refresh DB list immediately
            if (AppState.isShowingFavorites && !nextFavoriteState) {
                currentPage = 0;
                hasMore = true;
                loadFacilitiesFromAPI(0);
            }
        } catch (error) {
            console.error('Favorite API error:', error);
            court.isFavorite = previousFavoriteState;
            applyFiltersAndSearch();
            showToast('Khong the cap nhat yeu thich');
        }
    }

    function toggleFavoriteFilter() {
        if (!AppState.isShowingFavorites && !requireLogin('Danh sach yeu thich')) {
            return;
        }

        AppState.isShowingFavorites = !AppState.isShowingFavorites;
        updateFavoriteButton();
        currentPage = 0;
        hasMore = true;
        loadFacilitiesFromAPI(0);
    }


    function updateFavoriteButton() {
        const favoriteBtn = document.getElementById('favoriteBtn');
        if (favoriteBtn) {
            if (AppState.isShowingFavorites) {
                favoriteBtn.classList.add('active');
            } else {
                favoriteBtn.classList.remove('active');
            }
        }
    }

    // ============================================
    // COURT DETAIL MODAL
    // ============================================

    async function openCourtDetail(courtId) {
        const court = AppState.courts.find(c => c.id === courtId);
        if (!court) return;

        AppState.selectedCourt = { ...court };

        renderBaseCourtDetail(court);
        renderDetailLoadingState();
        switchDetailTab('info');
        updateDetailFavoriteButton();
        showCourtDetailPanel();

        await loadCourtDetail(court.id);
    }

    function renderBaseCourtDetail(court) {
        const bannerImg = document.getElementById('detailBannerImg');
        if (bannerImg) {
            bannerImg.src = resolveAssetUrl(court.imageUrl);
            bannerImg.alt = court.name || '';
        }

        const rating = Number(court.rating || 0);
        const detailRating = document.getElementById('detailRating');
        if (detailRating) {
            detailRating.textContent = `${"\u2605"} ${rating.toFixed(1)} (0 \u0111\u00e1nh gi\u00e1)`;
        }

        setText('detailTitle', court.name || '');
        setText('detailLocation', court.location || '');
        setText('detailOpenTime', court.openTime || '');

        const logoImg = document.getElementById('detailLogoImg');
        if (logoImg) {
            const firstLetter = (court.name || 'B').charAt(0).toUpperCase();
            logoImg.src = `https://placehold.co/100x100/orange/white?text=${firstLetter}`;
            logoImg.alt = 'logo';
        }
    }

    function renderDetailLoadingState() {
        setText('detailOverview', '\u0111ang t\u1ea3i...');
        setHtml('detailPricingContent', '<div class="detail-empty-state">\u0111ang t\u1ea3i...</div>');
        setHtml('detailImagesContent', '<div class="detail-empty-state">\u0111ang t\u1ea3i...</div>');
        setHtml('detailReviewsContent', '<div class="detail-empty-state">\u0111ang t\u1ea3i...</div>');
    }

    async function loadCourtDetail(courtId) {
        try {
            const detail = await fetchCourtDetail(courtId);
            if (!AppState.selectedCourt || String(AppState.selectedCourt.id) !== String(courtId)) {
                return;
            }
            applyCourtDetail(detail);
        } catch (error) {
            console.error('Error loading facility detail:', error);
            setText('detailOverview', 'ch\u01b0a c\u00f3');
            setHtml('detailPricingContent', '<div class="detail-empty-state">ch\u01b0a c\u00f3</div>');
            setHtml('detailImagesContent', '<div class="detail-empty-state"><i class="bi bi-image"></i><span>ch\u01b0a c\u00f3 h\u00ecnh \u1ea3nh</span></div>');
            setHtml('detailReviewsContent', '<div class="detail-empty-state">ch\u01b0a c\u00f3 comment n\u00e0o</div>');
        }
    }

    async function fetchCourtDetail(courtId) {
        const response = await fetch(`${contextPath}/api/facilities/${encodeURIComponent(courtId)}`);
        if (!response.ok) {
            throw new Error(`HTTP ${response.status}`);
        }

        const result = await response.json();
        if (!result.success || !result.data) {
            throw new Error(result.error || 'Invalid facility detail response');
        }

        return result.data;
    }

    function applyCourtDetail(detail) {
        AppState.selectedCourt = { ...AppState.selectedCourt, ...detail };
        const selected = AppState.selectedCourt;

        const rating = Number(selected.rating || 0);
        const reviewCount = Number(selected.reviewCount || 0);
        setText('detailRating', `${"\u2605"} ${rating.toFixed(1)} (${reviewCount} \u0111\u00e1nh gi\u00e1)`);

        setText('detailOverview', selected.description || 'ch\u01b0a c\u00f3');
        renderDetailPricing(selected.priceRules || []);
        renderDetailImages(selected.galleryImages || [], selected.name || 'Facility');
        renderDetailReviews(selected.reviews || []);
        updateDetailFavoriteButton();
    }

    function renderDetailPricing(priceRules) {
        if (!priceRules.length) {
            setHtml('detailPricingContent', '<div class="detail-empty-state"><i class="bi bi-table"></i><span>ch\u01b0a c\u00f3 b\u1ea3ng gi\u00e1</span></div>');
            return;
        }

        const normalizedRules = priceRules.map(rule => ({
            courtTypeName: rule.courtTypeName || 'ch\u01b0a c\u00f3',
            dayType: rule.dayType || 'UNKNOWN',
            dayTypeLabel: getDayTypeLabel(rule.dayType),
            dayTypeBadgeClass: getDayTypeBadgeClass(rule.dayType),
            timeRange: `${rule.startTime || '--:--'} - ${rule.endTime || '--:--'}`,
            priceText: formatCurrencyVnd(rule.price)
        }));

        let rowsHtml = '';
        let courtStart = 0;

        while (courtStart < normalizedRules.length) {
            const courtName = normalizedRules[courtStart].courtTypeName;
            let courtEnd = courtStart;
            while (courtEnd < normalizedRules.length && normalizedRules[courtEnd].courtTypeName === courtName) {
                courtEnd++;
            }
            const courtSpan = courtEnd - courtStart;

            let dayStart = courtStart;
            while (dayStart < courtEnd) {
                const dayType = normalizedRules[dayStart].dayType;
                const dayLabel = normalizedRules[dayStart].dayTypeLabel;
                const dayBadgeClass = normalizedRules[dayStart].dayTypeBadgeClass;

                let dayEnd = dayStart;
                while (dayEnd < courtEnd && normalizedRules[dayEnd].dayType === dayType) {
                    dayEnd++;
                }
                const daySpan = dayEnd - dayStart;

                for (let i = dayStart; i < dayEnd; i++) {
                    const row = normalizedRules[i];
                    rowsHtml += '<tr>';

                    if (i === courtStart) {
                        const rowSpanAttr = courtSpan > 1 ? ` rowspan="${courtSpan}"` : '';
                        rowsHtml += `<td class="detail-cell-court-type detail-merged-cell"${rowSpanAttr}>${escapeHtml(courtName)}</td>`;
                    }

                    if (i === dayStart) {
                        const rowSpanAttr = daySpan > 1 ? ` rowspan="${daySpan}"` : '';
                        rowsHtml += `<td class="detail-merged-cell"${rowSpanAttr}><span class="detail-day-badge ${escapeHtml(dayBadgeClass)}">${escapeHtml(dayLabel)}</span></td>`;
                    }

                    rowsHtml += `<td class="detail-time-cell">${escapeHtml(row.timeRange)}</td>`;
                    rowsHtml += `<td class="detail-price-cell">${escapeHtml(row.priceText)}</td>`;
                    rowsHtml += '</tr>';
                }

                dayStart = dayEnd;
            }

            courtStart = courtEnd;
        }

        setHtml('detailPricingContent', `
            <div class="detail-table-card">
                <div class="detail-table-wrap">
                    <table class="detail-data-table">
                        <thead>
                            <tr>
                                <th>Lo\u1ea1i s\u00e2n</th>
                                <th>Ng\u00e0y \u00e1p d\u1ee5ng</th>
                                <th>Khung gi\u1edd</th>
                                <th class="detail-price-header">Gi\u00e1 (VN\u0110/30 ph\u00fat)</th>
                            </tr>
                        </thead>
                        <tbody>${rowsHtml}</tbody>
                    </table>
                </div>
            </div>
        `);
    }
    function renderDetailImages(images, facilityName) {
        if (!images.length) {
            setHtml('detailImagesContent', '<div class="detail-empty-state"><i class="bi bi-image"></i><span>ch\u01b0a c\u00f3 h\u00ecnh \u1ea3nh</span></div>');
            return;
        }

        const html = images.map((imagePath, index) => {
            const src = resolveAssetUrl(imagePath);
            const safeSrc = encodeURI(src).replace(/'/g, '%27');
            const alt = escapeHtml(`${facilityName} image ${index + 1}`);
            return `
                <figure class="detail-gallery-item">
                    <img class="detail-gallery-image" src="${safeSrc}" alt="${alt}" loading="lazy" />
                </figure>
            `;
        }).join('');

        setHtml('detailImagesContent', `<div class="detail-gallery-grid">${html}</div>`);
    }

    function renderDetailReviews(reviews) {
        if (!reviews.length) {
            setHtml('detailReviewsContent', '<div class="detail-empty-state">ch\u01b0a c\u00f3 comment n\u00e0o</div>');
            return;
        }

        const html = reviews.map(review => {
            const reviewer = escapeHtml(review.reviewerName || 'Ng\u01b0\u1eddi d\u00f9ng');
            const rating = Number(review.rating || 0);
            const comment = escapeHtml((review.comment || '').trim() || 'ch\u01b0a c\u00f3 comment n\u00e0o');
            return `
                <article class="detail-review-item">
                    <div class="detail-review-head">
                        <strong class="detail-review-author">${reviewer}</strong>
                        <span class="detail-review-stars">${renderStars(rating)}</span>
                    </div>
                    <p class="detail-review-comment">${comment}</p>
                </article>
            `;
        }).join('');

        setHtml('detailReviewsContent', `<div class="detail-review-list">${html}</div>`);
    }

    function getDayTypeBadgeClass(dayType) {
        if (dayType === 'WEEKDAY') return 'detail-day-badge-weekday';
        if (dayType === 'WEEKEND') return 'detail-day-badge-weekend';
        return 'detail-day-badge-default';
    }

    function renderStars(rating) {
        let stars = '';
        const safeRating = Math.max(0, Math.min(5, Math.round(rating)));
        for (let i = 1; i <= 5; i++) {
            stars += i <= safeRating ? '<i class="bi bi-star-fill"></i>' : '<i class="bi bi-star"></i>';
        }
        return stars;
    }

    function getDayTypeLabel(dayType) {
        if (dayType === 'WEEKDAY') return 'Trong tu\u1ea7n';
        if (dayType === 'WEEKEND') return 'Cu\u1ed1i tu\u1ea7n';
        return dayType || 'ch\u01b0a c\u00f3';
    }

    function formatCurrencyVnd(value) {
        const amount = Number(value || 0);
        if (!Number.isFinite(amount) || amount <= 0) {
            return 'ch\u01b0a c\u00f3';
        }
        return `${amount.toLocaleString('vi-VN')} \u20ab`;
    }

    function resolveAssetUrl(path) {
        if (!path) return '';
        if (/^https?:\/\//i.test(path) || path.startsWith('/') || path.startsWith('data:')) {
            return path;
        }
        return `${contextPath}/${path}`;
    }

    function escapeHtml(input) {
        return String(input)
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#39;');
    }

    function setText(elementId, value) {
        const el = document.getElementById(elementId);
        if (el) {
            el.textContent = value;
        }
    }

    function setHtml(elementId, value) {
        const el = document.getElementById(elementId);
        if (el) {
            if (elementId === 'detailPricingContent' || elementId === 'detailImagesContent' || elementId === 'detailReviewsContent') {
                el.classList.remove('detail-empty-state');
            }
            el.innerHTML = value;
        }
    }

    function showCourtDetailPanel() {
        const backdrop = document.getElementById('courtDetailBackdrop');
        const panel = document.getElementById('courtDetailPanel');

        if (backdrop && panel) {
            backdrop.classList.add('active');
            panel.classList.add('active');
            document.body.style.overflow = 'hidden';
        }
    }

    function closeCourtDetail() {
        const backdrop = document.getElementById('courtDetailBackdrop');
        const panel = document.getElementById('courtDetailPanel');

        if (backdrop && panel) {
            backdrop.classList.remove('active');
            panel.classList.remove('active');
            document.body.style.overflow = '';
        }

        AppState.selectedCourt = null;
    }

    function updateDetailFavoriteButton() {
        const btn = document.getElementById('detailFavoriteBtn');
        if (btn && AppState.selectedCourt) {
            if (AppState.selectedCourt.isFavorite) {
                btn.classList.add('is-favorite');
                btn.innerHTML = '<i class="bi bi-heart-fill"></i>';
            } else {
                btn.classList.remove('is-favorite');
                btn.innerHTML = '<i class="bi bi-heart"></i>';
            }
        }
    }
    // ============================================
    // FILTER PANEL
    // ============================================

    function openFilterPanel() {
        const backdrop = document.getElementById('filterBackdrop');
        const panel = document.getElementById('filterPanel');

        if (backdrop && panel) {
            backdrop.classList.add('active');
            panel.classList.add('active');
            document.body.style.overflow = 'hidden';
        }
    }

    function closeFilterPanel() {
        const backdrop = document.getElementById('filterBackdrop');
        const panel = document.getElementById('filterPanel');

        if (backdrop && panel) {
            backdrop.classList.remove('active');
            panel.classList.remove('active');
            document.body.style.overflow = '';
        }
    }

    function applyFilters() {
        closeFilterPanel();
        currentPage = 0;
        hasMore = true;
        loadFacilitiesFromAPI(0);
    }

    function resetFilters() {
        AppState.filters = {
            province: '',
            district: '',
            maxDistance: null
        };

        // Reset UI
        document.getElementById('filterProvince').value = '';
        document.getElementById('filterDistrict').value = '';
        document.getElementById('filterDistrict').disabled = true;

        document.querySelectorAll('.filter-distance-btn').forEach(btn => {
            btn.classList.remove('active');
        });

        currentPage = 0;
        hasMore = true;
        loadFacilitiesFromAPI(0);
    }

    function updateDistrictOptions() {
        const province = AppState.filters.province;
        const districtSelect = document.getElementById('filterDistrict');

        if (!districtSelect) return;

        if (!province) {
            districtSelect.disabled = true;
            districtSelect.innerHTML = '<option value="">Tất cả quận huyện</option>';
            return;
        }

        districtSelect.disabled = false;

        // ✅ Use Vietnam data
        const districts = window.getDistrictsByProvince ?
            window.getDistrictsByProvince(province) : [];

        console.log('📍 Loading', districts.length, 'districts for', province);

        districtSelect.innerHTML = '<option value="">Tất cả quận huyện</option>' +
            districts.map(d => `<option value="${d}">${d}</option>`).join('');
    }

    /**
     * Initialize filter select options with Vietnam locations data
     */
    function initializeFilterSelects() {
        const provinceSelect = document.getElementById('filterProvince');

        if (!provinceSelect) {
            console.warn('⚠️ Filter province select not found');
            return;
        }

        // Check if Vietnam data is loaded
        if (!window.getAllProvinces) {
            console.error('❌ Vietnam locations data not loaded!');
            return;
        }

        // Get all provinces
        const provinces = window.getAllProvinces();
        console.log('🇻🇳 Loading', provinces.length, 'provinces');

        // Clear existing options (except first "Tất cả")
        provinceSelect.innerHTML = '<option value="">Tất cả tỉnh thành</option>';

        // Add province options
        provinces.forEach(province => {
            const option = document.createElement('option');
            option.value = province;
            option.textContent = province;
            provinceSelect.appendChild(option);
        });

        console.log('✅ Filter selects initialized with Vietnam data');
    }

    // ============================================
    // DETAIL TABS
    // ============================================

    function switchDetailTab(tabName) {
        // Update tab buttons
        document.querySelectorAll('.detail-tab').forEach(tab => {
            tab.classList.remove('active');
        });

        const activeTab = document.querySelector(`[data-detail-tab="${tabName}"]`);
        if (activeTab) {
            activeTab.classList.add('active');
        }

        // Update tab panes
        document.querySelectorAll('.detail-tab-pane').forEach(pane => {
            pane.classList.remove('active');
        });

        const activePane = document.getElementById(`detailTab${tabName.charAt(0).toUpperCase() + tabName.slice(1)}`);
        if (activePane) {
            activePane.classList.add('active');
        }
    }

    // ============================================
    // INFINITE SCROLL
    // ============================================

    function initInfiniteScroll() {
        console.log('✅ Infinite scroll initialized on window');

        window.addEventListener('scroll', debounce(function() {
            if (AppState.currentTab !== TABS.HOME || isLoading || !hasMore) {
                return;
            }

            const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
            const scrollHeight = document.documentElement.scrollHeight;
            const clientHeight = document.documentElement.clientHeight;

            const scrollPercentage = (scrollTop + clientHeight) / scrollHeight;

            console.log('📜 Window scroll:', (scrollPercentage * 100).toFixed(2) + '%');

            if (scrollPercentage > 0.8) {
                console.log('🚀 Loading more...');
                loadFacilitiesFromAPI(currentPage + 1);
            }
        }, 300));
    }

    // ============================================
    // EVENT LISTENERS
    // ============================================

    function attachEventListeners() {
        // Bottom Navigation
        document.querySelectorAll('.bottom-nav .nav-item, .bottom-nav .nav-center-btn').forEach(btn => {
            btn.addEventListener('click', function() {
                const tab = this.dataset.tab;
                if (tab) {
                    switchTab(tab);
                }
            });
        });

        // Search input
        const searchInput = document.getElementById('searchInput');
        if (searchInput) {
            searchInput.addEventListener('input', debounce(function(e) {
                AppState.searchQuery = e.target.value;
                currentPage = 0;
                hasMore = true;
                loadFacilitiesFromAPI(0);
            }, 300));

            // ✅ Clear search button
            searchInput.addEventListener('keydown', function(e) {
                if (e.key === 'Escape') {
                    this.value = '';
                    AppState.searchQuery = '';
                    currentPage = 0;
                    hasMore = true;
                    loadFacilitiesFromAPI(0);
                }
            });
        }

        // Header buttons
        const favoriteBtn = document.getElementById('favoriteBtn');
        if (favoriteBtn) {
            favoriteBtn.addEventListener('click', toggleFavoriteFilter);
        }

        const mapBtn = document.getElementById('mapBtn');
        if (mapBtn) {
            mapBtn.addEventListener('click', () => switchTab(TABS.MAP));
        }

        const filterToggleBtn = document.getElementById('filterToggleBtn');
        if (filterToggleBtn) {
            filterToggleBtn.addEventListener('click', openFilterPanel);
        }

        // Filter Panel
        const filterBackdrop = document.getElementById('filterBackdrop');
        if (filterBackdrop) {
            filterBackdrop.addEventListener('click', closeFilterPanel);
        }

        const filterCloseBtn = document.getElementById('filterCloseBtn');
        if (filterCloseBtn) {
            filterCloseBtn.addEventListener('click', closeFilterPanel);
        }

        const filterApplyBtn = document.getElementById('filterApplyBtn');
        if (filterApplyBtn) {
            filterApplyBtn.addEventListener('click', applyFilters);
        }

        const filterResetBtn = document.getElementById('filterResetBtn');
        if (filterResetBtn) {
            filterResetBtn.addEventListener('click', resetFilters);
        }

        // Filter inputs
        const filterProvince = document.getElementById('filterProvince');
        if (filterProvince) {
            filterProvince.addEventListener('change', function(e) {
                AppState.filters.province = e.target.value;
                AppState.filters.district = '';
                updateDistrictOptions();
            });
        }

        const filterDistrict = document.getElementById('filterDistrict');
        if (filterDistrict) {
            filterDistrict.addEventListener('change', function(e) {
                AppState.filters.district = e.target.value;
            });
        }

        // Distance filter buttons
        document.querySelectorAll('.filter-distance-btn').forEach(btn => {
            btn.addEventListener('click', function() {
                const distance = parseInt(this.dataset.distance);

                if (AppState.filters.maxDistance === distance) {
                    AppState.filters.maxDistance = null;
                    this.classList.remove('active');
                } else {
                    AppState.filters.maxDistance = distance;
                    document.querySelectorAll('.filter-distance-btn').forEach(b => {
                        b.classList.remove('active');
                    });
                    this.classList.add('active');
                }
            });
        });

        // Show all button (favorites)
        const showAllBtn = document.getElementById('showAllBtn');
        if (showAllBtn) {
            showAllBtn.addEventListener('click', function() {
                AppState.isShowingFavorites = false;
                updateFavoriteButton();
                currentPage = 0;
                hasMore = true;
                loadFacilitiesFromAPI(0);
            });
        }

        // Clear filters button
        const clearFiltersBtn = document.getElementById('clearFiltersBtn');
        if (clearFiltersBtn) {
            clearFiltersBtn.addEventListener('click', function() {
                AppState.searchQuery = '';
                const searchInput = document.getElementById('searchInput');
                if (searchInput) searchInput.value = '';
                resetFilters();
            });
        }

        // Court Detail
        const detailBackdrop = document.getElementById('courtDetailBackdrop');
        if (detailBackdrop) {
            detailBackdrop.addEventListener('click', closeCourtDetail);
        }

        const detailBackBtn = document.getElementById('detailBackBtn');
        if (detailBackBtn) {
            detailBackBtn.addEventListener('click', closeCourtDetail);
        }

        const detailFavoriteBtn = document.getElementById('detailFavoriteBtn');
        if (detailFavoriteBtn) {
            detailFavoriteBtn.addEventListener('click', function() {
                if (AppState.selectedCourt) {
                    toggleFavorite(AppState.selectedCourt.id);
                }
            });
        }

        const detailBookBtn = document.getElementById('detailBookBtn');
        if (detailBookBtn) {
            detailBookBtn.addEventListener('click', function() {
                // ✅ Require login
                if (!requireLogin('Đặt sân')) {
                    closeCourtDetail();
                    return;
                }

                if (AppState.selectedCourt) {
                    const court   = AppState.selectedCourt;
                    const venueId = court.facilityId || court.id;
                    const courtName = court.name;

                    closeCourtDetail();

                    if (window.BookingTypeModal) {
                        window.BookingTypeModal.open(venueId, courtName);
                    } else {
                        const today = new Date();
                        const ds = today.getFullYear()+'-'+String(today.getMonth()+1).padStart(2,'0')+'-'+String(today.getDate()).padStart(2,'0');
                        window.location.href = contextPath
                            +'/jsp/booking/singlebooking/single-booking.jsp'
                            +'?facilityId='+encodeURIComponent(venueId)+'&date='+encodeURIComponent(ds);
                    }
                }
            });
        }

        // Detail tabs
        document.querySelectorAll('.detail-tab').forEach(tab => {
            tab.addEventListener('click', function() {
                const tabName = this.dataset.detailTab;
                if (tabName) {
                    switchDetailTab(tabName);
                }
            });
        });

        // ✅ NEW: Auth Modal listeners
        const authModalBackdrop = document.getElementById('authModalBackdrop');
        if (authModalBackdrop) {
            authModalBackdrop.addEventListener('click', closeAuthModal);
        }

        const authModalCloseBtn = document.getElementById('authModalCloseBtn');
        if (authModalCloseBtn) {
            authModalCloseBtn.addEventListener('click', closeAuthModal);
        }

        const authLoginBtn = document.getElementById('authLoginBtn');
        if (authLoginBtn) {
            authLoginBtn.addEventListener('click', function() {
                closeAuthModal();
                // ✅ Redirect to login page
                const contextPath = window.location.pathname.split('/')[1] || 'badminton_court_booking';
                window.location.href = `/${contextPath}/auth/login`;
            });
        }

        const authRegisterBtn = document.getElementById('authRegisterBtn');
        if (authRegisterBtn) {
            authRegisterBtn.addEventListener('click', function() {
                closeAuthModal();
                // TODO: Navigate to register page
                console.log('📝 Navigate to register page');
                // window.location.href = '/auth/register';
            });
        }

        // ✅ History button requires login — <a> tag, block if not logged in
        const historyBtn = document.getElementById('historyBtn');
        if (historyBtn) {
            historyBtn.addEventListener('click', function(e) {
                if (!isUserLoggedIn()) {
                    e.preventDefault();
                    requireLogin('Lịch sử đặt sân');
                }
                // If logged in, let <a> navigate naturally to /my-bookings
            });
        }
    }

    // ============================================
    // INITIALIZATION
    // ============================================

    function init() {
        console.log('Initializing BadmintonPro app...');

        // Get user location FIRST (will trigger API load in callback)
        getUserLocation();

        // Attach event listeners
        attachEventListeners();

        // Initialize infinite scroll
        initInfiniteScroll();

        // Set initial tab
        switchTab(TABS.HOME);

        // ✅ NEW: Initialize filter selects with Vietnam data
        initializeFilterSelects();

        console.log('App initialized');
    }

    // Start app when DOM is ready
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

    // Expose openCourtDetail globally for map integration
    window.openCourtDetail = openCourtDetail;

})();





















