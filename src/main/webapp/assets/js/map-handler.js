(function() {
    'use strict';

    let mapInstance = null;
    let markers = [];
    let userMarker = null;
    let userLocation = null;

    async function initMap() {
        const mapContainer = document.getElementById('leafletMap');
        if (!mapContainer) {
            console.error('Map container not found');
            return;
        }

        if (window.__USER_LOCATION__ && window.__USER_LOCATION__.lat && window.__USER_LOCATION__.lng) {
            userLocation = window.__USER_LOCATION__;
        }

        let courts = [];
        try {
            courts = await fetchAllCourtsForMap();
        } catch (error) {
            console.error('Failed to load all courts for map:', error);
            courts = window.COURTS_DATA || [];
        }

        if (mapInstance) {
            addCourtMarkers(courts);
            fitMapToCourts(courts);
            mapInstance.invalidateSize();
            return;
        }

        let center = [21.0285, 105.8542];
        let zoom = 11;
        const firstWithCoord = courts.find(c => c.lat && c.lng);
        if (firstWithCoord) {
            center = [firstWithCoord.lat, firstWithCoord.lng];
            zoom = 12;
        }

        mapInstance = L.map(mapContainer, {
            zoomControl: false,
            attributionControl: false
        }).setView(center, zoom);

        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            maxZoom: 19,
            attribution: ''
        }).addTo(mapInstance);

        L.control.zoom({ position: 'bottomright' }).addTo(mapInstance);

        addCourtMarkers(courts);
        fitMapToCourts(courts);

        addUserLocationMarker();
        attachMapSearchListener();
        attachMapFilterListeners();
    }

    async function fetchAllCourtsForMap() {
        if (Array.isArray(window.MAP_COURTS_DATA) && window.MAP_COURTS_DATA.length > 0) {
            return window.MAP_COURTS_DATA;
        }

        const contextPath = getMapContextPath();
        const pageSize = 100;
        let page = 0;
        let hasMore = true;
        const all = [];

        while (hasMore) {
            const params = new URLSearchParams({
                page: String(page),
                pageSize: String(pageSize)
            });

            const response = await fetch(`${contextPath}/api/facilities?${params.toString()}`);
            if (!response.ok) {
                throw new Error(`Map facilities request failed: HTTP ${response.status}`);
            }

            const result = await response.json();
            if (!result.success) {
                throw new Error(result.error || 'Map facilities request failed');
            }

            const pageItems = Array.isArray(result.data) ? result.data : [];
            all.push(...pageItems);

            hasMore = Boolean(result.pagination && result.pagination.hasMore);
            console.log('[Map] fetched page', page, 'items:', pageItems.length, 'hasMore:', hasMore);
            page += 1;
        }

        window.MAP_COURTS_DATA = all;
        console.log('[Map] total courts loaded:', all.length);
        return all;
    }
    function getMapContextPath() {
        const seg = window.location.pathname.split('/')[1];
        return seg ? `/${seg}` : '';
    }

    function getMapCourtsDataset() {
        return window.MAP_COURTS_DATA || window.COURTS_DATA || [];
    }

    function resolveAssetUrl(path) {
        if (!path) {
            return '';
        }
        if (/^https?:\/\//i.test(path) || path.startsWith('/') || path.startsWith('data:')) {
            return path;
        }
        return `${getMapContextPath()}/${path}`;
    }

    function calculateDistance(lat1, lon1, lat2, lon2) {
        const R = 6371;
        const dLat = (lat2 - lat1) * Math.PI / 180;
        const dLon = (lon2 - lon1) * Math.PI / 180;
        const a =
            Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2);
        const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    function formatDistance(distKm) {
        if (distKm === null || !Number.isFinite(distKm)) return 'Dang tinh...';
        return distKm < 1 ? `${Math.round(distKm * 1000)}m` : `${distKm.toFixed(1)}km`;
    }
    function fitMapToCourts(courts) {
        if (!mapInstance || !Array.isArray(courts) || courts.length === 0) {
            return;
        }

        const validCourts = courts.filter(c => c.lat && c.lng);
        if (validCourts.length === 0) {
            return;
        }

        const bounds = L.latLngBounds(validCourts.map(c => [c.lat, c.lng]));
        mapInstance.fitBounds(bounds, { padding: [50, 50] });
    }

    function addCourtMarkers(courts) {
        markers.forEach(marker => marker.remove());
        markers = [];

        courts.forEach(court => {
            if (!court.lat || !court.lng) {
                return;
            }

            if (userLocation && court.lat && court.lng) {
                const distKm = calculateDistance(userLocation.lat, userLocation.lng, court.lat, court.lng);
                court.distance = formatDistance(distKm);
            }
            const customIcon = L.divIcon({
                className: 'custom-div-icon',
                html: `
                    <div class="court-marker" style="
                        width: 48px;
                        height: 48px;
                        background-color: #064E3B;
                        border-radius: 50%;
                        display: flex;
                        align-items: center;
                        justify-content: center;
                        border: 3px solid white;
                        box-shadow: 0 4px 12px rgba(0,0,0,0.3);
                        color: #A3E635;
                        cursor: pointer;
                        transition: transform 0.2s;
                    "
                    onmouseover="this.style.transform='scale(1.2)'"
                    onmouseout="this.style.transform='scale(1)'">
                        <svg viewBox="0 0 24 24" width="24" height="24" fill="currentColor">
                            <path d="M12,2L4.5,20.29L5.21,21L12,18L18.79,21L19.5,20.29L12,2Z" />
                        </svg>
                    </div>
                `,
                iconSize: [48, 48],
                iconAnchor: [24, 48],
                popupAnchor: [0, -48]
            });

            const imageUrl = resolveAssetUrl(court.imageUrl);
            const safeName = court.name || 'San';
            const rating = Number(court.rating || 0).toFixed(1);
            const distance = court.distance || '';
            const location = court.location || '';
            const distanceText = distance ? "\uD83D\uDCCD" + distance : "";
            const metaText = "\u2605 " + rating + distanceText;

            const marker = L.marker([court.lat, court.lng], {
                icon: customIcon,
                title: safeName
            }).addTo(mapInstance);

            const popupContent = `
                <div style="padding: 12px; min-width: 200px; font-family: 'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif">
                    <img src="${imageUrl}" style="width: 100%; height: 100px; object-fit: cover; border-radius: 8px; margin-bottom: 8px;" alt="${safeName}" />
                    <h4 style="font-size: 13px; font-weight: 900; color: #111827; text-transform: uppercase; line-height: 1.3; margin: 0 0 6px 0; letter-spacing: -0.02em;">
                        ${safeName}
                    </h4>
                    <div style="display: flex; align-items: center; gap: 6px; font-size: 11px; color: #6B7280; margin-bottom: 4px;">
                        <span style="color: #F97316;">${metaText}</span>
                    </div>
                    <div style="font-size: 11px; color: #6B7280; margin-bottom: 8px;">
                        <i class="bi bi-geo-alt-fill" style="color: #10B981;"></i> ${location}
                    </div>
                    <button id="map-view-btn-${court.id}" style="width: 100%; background-color: #064E3B; color: white; padding: 8px; border-radius: 6px; font-size: 11px; font-weight: 900; text-transform: uppercase; letter-spacing: 0.05em; border: none; cursor: pointer; transition: background-color 0.2s;" onmouseover="this.style.backgroundColor='#065F46'" onmouseout="this.style.backgroundColor='#064E3B'">
                        Xem chi tiết
                    </button>
                </div>
            `;

            marker.bindPopup(popupContent, {
                maxWidth: 250,
                className: 'custom-popup'
            });

            marker.on('popupopen', () => {
                const btn = document.getElementById(`map-view-btn-${court.id}`);
                if (btn) {
                    btn.onclick = (e) => {
                        e.preventDefault();
                        e.stopPropagation();
                        mapInstance.closePopup();
                        if (window.openCourtDetail) {
                            window.openCourtDetail(court.id);
                        }
                    };
                }
            });

            markers.push(marker);
        });
    }

    function addUserLocationMarker() {
        if (!('geolocation' in navigator)) {
            return;
        }

        navigator.geolocation.getCurrentPosition(
            (position) => {
                const userLat = position.coords.latitude;
                const userLng = position.coords.longitude;
                userLocation = { lat: userLat, lng: userLng };
                window.__USER_LOCATION__ = userLocation;

                const userIcon = L.divIcon({
                    className: 'custom-div-icon',
                    html: `
                        <div style="position: relative;">
                            <div style="width: 20px; height: 20px; background-color: #3B82F6; border-radius: 50%; border: 3px solid white; box-shadow: 0 2px 8px rgba(59,130,246,0.6);"></div>
                            <div style="position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%); width: 40px; height: 40px; background-color: rgba(59, 130, 246, 0.2); border-radius: 50%; animation: pulse 2s infinite;"></div>
                        </div>
                    `,
                    iconSize: [20, 20],
                    iconAnchor: [10, 10]
                });

                if (userMarker) {
                    userMarker.remove();
                }

                userMarker = L.marker([userLat, userLng], {
                    icon: userIcon,
                    zIndexOffset: 1000
                }).addTo(mapInstance);

                userMarker.bindPopup('<div style="padding: 8px; text-align: center;"><strong style="font-size: 12px; color: #3B82F6;">Vị trí của bạn</strong></div>');

                // Update markers to show distance once user location is available
                addCourtMarkers(getMapCourtsDataset());
            },
            (error) => {
                console.error('Error getting user location for map:', error);
            }
        );
    }

    function attachMapSearchListener() {
        const searchInput = document.getElementById('mapSearchInput');
        if (!searchInput) {
            return;
        }

        searchInput.addEventListener('input', debounce(function(e) {
            const query = e.target.value.toLowerCase().trim();
            const courts = getMapCourtsDataset();

            if (!query) {
                addCourtMarkers(courts);
                fitMapToCourts(courts);
                return;
            }

            const filtered = courts.filter(court =>
                court.name.toLowerCase().includes(query) ||
                court.location.toLowerCase().includes(query) ||
                (court.province && court.province.toLowerCase().includes(query)) ||
                (court.district && court.district.toLowerCase().includes(query))
            );

            addCourtMarkers(filtered);
            fitMapToCourts(filtered);
        }, 300));

        searchInput.addEventListener('keydown', function(e) {
            if (e.key === 'Escape') {
                this.value = '';
                this.dispatchEvent(new Event('input'));
            }
        });
    }

    function attachMapFilterListeners() {
        const filterChips = document.querySelectorAll('.map-filter-chip');
        filterChips.forEach(chip => {
            chip.addEventListener('click', function() {
                const filter = this.dataset.mapFilter;
                filterChips.forEach(c => c.classList.remove('active'));
                this.classList.add('active');
                applyMapFilter(filter);
            });
        });
    }

    function applyMapFilter(filterType) {
        const courts = getMapCourtsDataset();
        let filtered = [...courts];

        switch (filterType) {
            case 'near-me':
                filtered.sort((a, b) => {
                    const getDistValue = (distStr) => {
                        if (!distStr || distStr === 'Dang tinh...') return Infinity;
                        if (distStr.endsWith('km')) return parseFloat(distStr);
                        if (distStr.endsWith('m')) return parseFloat(distStr) / 1000;
                        return Infinity;
                    };
                    return getDistValue(a.distance) - getDistValue(b.distance);
                });
                filtered = filtered.slice(0, 10);
                break;
            case 'high-rating':
                filtered = courts.filter(c => Number(c.rating || 0) >= 4.0);
                if (filtered.length === 0) {
                    filtered = [...courts].sort((a, b) => Number(b.rating || 0) - Number(a.rating || 0));
                }
                break;
            case 'cheap':
                filtered = courts;
                break;
            case 'new':
                filtered = [...courts].reverse();
                break;
            default:
                filtered = courts;
        }

        addCourtMarkers(filtered);
        fitMapToCourts(filtered);
    }

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

    window.initMap = initMap;

    window.setUserLocationForMap = function(location) {
        if (!location || !Number.isFinite(location.lat) || !Number.isFinite(location.lng)) {
            return;
        }
        userLocation = { lat: location.lat, lng: location.lng };
        window.__USER_LOCATION__ = userLocation;

        if (mapInstance) {
            addCourtMarkers(getMapCourtsDataset());
        }
    };

    window.updateMapMarkers = function(courts) {
        if (!Array.isArray(courts)) {
            return;
        }

        window.MAP_COURTS_DATA = courts;

        if (mapInstance) {
            addCourtMarkers(courts);
            fitMapToCourts(courts);
        }
    };

    const style = document.createElement('style');
    style.textContent = `
        @keyframes pulse {
            0%, 100% {
                opacity: 1;
                transform: translate(-50%, -50%) scale(1);
            }
            50% {
                opacity: 0.5;
                transform: translate(-50%, -50%) scale(1.5);
            }
        }
    `;
    document.head.appendChild(style);
})();
