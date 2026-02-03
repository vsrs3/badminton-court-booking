/**
 * BADMINTON PRO - Main Application JavaScript
 * Handles: Tabs, Search, Filters, Favorites, Court Details, API Integration
 */

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
     * TODO: Implement actual session check
     */
    function isUserLoggedIn() {
        // For now, always return false (guest mode)
        // Later: Check session/cookie/localStorage
        return false;
    }

    /**
     * Require login for a feature
     */
    function requireLogin(featureName) {
        if (!isUserLoggedIn()) {
            console.log('🔒 Login required for:', featureName);
            showAuthModal();
            return false;
        }
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

        if (isLoading) {
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

            const response = await fetch(apiUrl);

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
            console.error('Error loading facilities:', error);
            showToast('Lỗi kết nối. Vui lòng thử lại');
        } finally {
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

        // 1. Search filter
        if (AppState.searchQuery.trim()) {
            const query = AppState.searchQuery.toLowerCase().trim();
            courts = courts.filter(c =>
                c.name.toLowerCase().includes(query) ||
                c.location.toLowerCase().includes(query)
            );
        }

        // 2. Province filter
        if (AppState.filters.province) {
            courts = courts.filter(c => c.province === AppState.filters.province);
        }

        // 3. District filter
        if (AppState.filters.district) {
            courts = courts.filter(c => c.district === AppState.filters.district);
        }

        // 4. Distance filter
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
                .replace(/{imageUrl}/g, court.imageUrl)
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

        // Book buttons
        document.querySelectorAll('.btn-book').forEach(btn => {
            btn.addEventListener('click', function(e) {
                e.stopPropagation();

                // ✅ Require login
                if (!requireLogin('Đặt sân')) {
                    return;
                }

                const courtId = this.dataset.courtId;
                const court = AppState.courts.find(c => c.id === courtId);
                if (court) {
                    // TODO: Navigate to booking page
                    console.log('📅 Opening booking form for:', court.name);
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
        if (tabName === TABS.BOOKING || tabName === TABS.OFFER || tabName === TABS.PROFILE) {
            if (!requireLogin('Tính năng này')) {
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
            AppState.isShowingFavorites = false;
            updateFavoriteButton();
        }

        // ✅ NEW: Initialize map if switching to map tab
        if (tabName === TABS.MAP) {
            setTimeout(() => {
                if (window.initMap) {
                    window.initMap();

                    // Update markers with current courts data
                    if (window.updateMapMarkers && AppState.courts.length > 0) {
                        window.updateMapMarkers(AppState.courts);
                    }
                }
            }, 100);
        }
    }

    // ============================================
    // FAVORITES
    // ============================================

    function toggleFavorite(courtId) {
        const court = AppState.courts.find(c => c.id === courtId);
        if (court) {
            court.isFavorite = !court.isFavorite;
            applyFiltersAndSearch();

            // Update detail panel if open
            if (AppState.selectedCourt && AppState.selectedCourt.id === courtId) {
                AppState.selectedCourt = court;
                updateDetailFavoriteButton();
            }
        }
    }

    function toggleFavoriteFilter() {
        const favoriteCount = AppState.courts.filter(c => c.isFavorite).length;

        if (favoriteCount === 0) {
            showToast("Chưa có sân yêu thích");
            return;
        }

        AppState.isShowingFavorites = !AppState.isShowingFavorites;
        updateFavoriteButton();
        applyFiltersAndSearch();
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

    function openCourtDetail(courtId) {
        const court = AppState.courts.find(c => c.id === courtId);
        if (!court) return;

        AppState.selectedCourt = court;

        // Populate detail panel
        document.getElementById('detailBannerImg').src = court.imageUrl;
        document.getElementById('detailBannerImg').alt = court.name;
        document.getElementById('detailRating').textContent = `★ ${court.rating.toFixed(1)} (0 đánh giá)`;
        document.getElementById('detailTitle').textContent = court.name;
        document.getElementById('detailLocation').textContent = court.location;
        document.getElementById('detailOpenTime').textContent = court.openTime;
        document.getElementById('detailNameInOverview').textContent = court.name;

        // Logo
        const logoImg = document.getElementById('detailLogoImg');
        const firstLetter = court.name.charAt(0);
        logoImg.src = `https://placehold.co/100x100/orange/white?text=${firstLetter}`;
        logoImg.alt = 'logo';

        // Update favorite button
        updateDetailFavoriteButton();

        // Show panel
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
        applyFiltersAndSearch();
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

        applyFiltersAndSearch();
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
                console.log('🔍 Search query:', AppState.searchQuery); // ✅ DEBUG
                applyFiltersAndSearch();
            }, 300));

            // ✅ Clear search button
            searchInput.addEventListener('keydown', function(e) {
                if (e.key === 'Escape') {
                    this.value = '';
                    AppState.searchQuery = '';
                    applyFiltersAndSearch();
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
                applyFiltersAndSearch();
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
                    closeCourtDetail(); // Close detail first
                    return;
                }

                if (AppState.selectedCourt) {
                    // TODO: Navigate to booking page
                    console.log('📅 Opening booking form for:', AppState.selectedCourt.name);
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

        // ✅ NEW: History button requires login
        const historyBtn = document.getElementById('historyBtn');
        if (historyBtn) {
            historyBtn.addEventListener('click', function() {
                if (requireLogin('Lịch sử đặt sân')) {
                    // Navigate to history page
                    console.log('📅 Navigate to booking history');
                }
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