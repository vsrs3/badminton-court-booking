/**
 * Handles: Leaflet Map Initialization, Markers, Popups
 */

(function() {
    'use strict';

    // ============================================
    // MAP STATE
    // ============================================

    let mapInstance = null;
    let markers = [];
    let userMarker = null;
    let userLocation = null;

    // ============================================
    // MAP INITIALIZATION
    // ============================================

    function initMap() {

        // Check if map already initialized
        if (mapInstance) {
            mapInstance.invalidateSize();
            return;
        }

        const mapContainer = document.getElementById('leafletMap');
        if (!mapContainer) {
            return;
        }

        // Get courts data from global state
        const courts = window.COURTS_DATA || [];

        // Default center (Vietnam)
        let center = [21.0285, 105.8542]; // Hanoi
        let zoom = 11;

        // If we have courts, center on first court
        if (courts.length > 0 && courts[0].lat && courts[0].lng) {
            center = [courts[0].lat, courts[0].lng];
            zoom = 12;
        }

        // Initialize Leaflet map
        mapInstance = L.map(mapContainer, {
            zoomControl: false,
            attributionControl: false
        }).setView(center, zoom);

        // Add tile layer (OpenStreetMap)
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            maxZoom: 19,
            attribution: ''
        }).addTo(mapInstance);

        // Add zoom control to bottom right
        L.control.zoom({
            position: 'bottomright'
        }).addTo(mapInstance);

        // Add markers for all courts
        if (courts.length > 0) {
            addCourtMarkers(courts);

            // Fit bounds to show all courts
            const bounds = L.latLngBounds(courts.map(c => [c.lat, c.lng]));
            mapInstance.fitBounds(bounds, { padding: [50, 50] });
        }

        // Try to add user location marker
        addUserLocationMarker();

        // Attach map search listener
        attachMapSearchListener();

        // Attach map filter listeners
        attachMapFilterListeners();

    }

    // ============================================
    // MARKERS
    // ============================================

    function addCourtMarkers(courts) {

        // Clear existing markers
        markers.forEach(marker => marker.remove());
        markers = [];

        courts.forEach((court, index) => {
            // Skip if no coordinates
            if (!court.lat || !court.lng) {
                console.warn('Court missing coordinates:', court.name);
                return;
            }

            // Create custom icon (green badminton shuttlecock)
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

            // Create marker
            const marker = L.marker([court.lat, court.lng], {
                icon: customIcon,
                title: court.name
            }).addTo(mapInstance);

            // Create popup content
            const popupContent = `
                <div style="padding: 12px; min-width: 200px;">
                    <img src="${court.imageUrl}" 
                         style="width: 100%; height: 100px; object-fit: cover; border-radius: 8px; margin-bottom: 8px;" 
                         alt="${court.name}" />
                    <h4 style="font-size: 13px; font-weight: 900; color: #111827; text-transform: uppercase; 
                                line-height: 1.3; margin: 0 0 6px 0; letter-spacing: -0.02em;">
                        ${court.name}
                    </h4>
                    <div style="display: flex; align-items: center; gap: 6px; font-size: 11px; 
                                color: #6B7280; margin-bottom: 4px;">
                        <span style="color: #F97316;">★ ${court.rating.toFixed(1)}</span>
                        <span>•</span>
                        <span>${court.distance}</span>
                    </div>
                    <div style="font-size: 11px; color: #6B7280; margin-bottom: 8px;">
                        <i class="bi bi-geo-alt-fill" style="color: #10B981;"></i> ${court.location}
                    </div>
                    <button id="map-view-btn-${court.id}" 
                            style="width: 100%; background-color: #064E3B; color: white; 
                                   padding: 8px; border-radius: 6px; font-size: 11px; 
                                   font-weight: 900; text-transform: uppercase; 
                                   letter-spacing: 0.05em; border: none; cursor: pointer;
                                   transition: background-color 0.2s;"
                            onmouseover="this.style.backgroundColor='#065F46'"
                            onmouseout="this.style.backgroundColor='#064E3B'">
                        Xem chi tiết
                    </button>
                </div>
            `;

            // Bind popup
            marker.bindPopup(popupContent, {
                maxWidth: 250,
                className: 'custom-popup'
            });

            // Attach click event to button after popup opens
            marker.on('popupopen', () => {
                const btn = document.getElementById(`map-view-btn-${court.id}`);
                if (btn) {
                    btn.onclick = (e) => {
                        e.preventDefault();
                        e.stopPropagation();

                        // Close popup
                        mapInstance.closePopup();

                        // Open court detail
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
        if ("geolocation" in navigator) {
            navigator.geolocation.getCurrentPosition(
                (position) => {
                    const userLat = position.coords.latitude;
                    const userLng = position.coords.longitude;

                    userLocation = { lat: userLat, lng: userLng };

                    // Create user location icon (blue dot)
                    const userIcon = L.divIcon({
                        className: 'custom-div-icon',
                        html: `
                            <div style="position: relative;">
                                <div style="
                                    width: 20px; 
                                    height: 20px; 
                                    background-color: #3B82F6; 
                                    border-radius: 50%; 
                                    border: 3px solid white; 
                                    box-shadow: 0 2px 8px rgba(59,130,246,0.6);
                                "></div>
                                <div style="
                                    position: absolute;
                                    top: 50%;
                                    left: 50%;
                                    transform: translate(-50%, -50%);
                                    width: 40px;
                                    height: 40px;
                                    background-color: rgba(59, 130, 246, 0.2);
                                    border-radius: 50%;
                                    animation: pulse 2s infinite;
                                "></div>
                            </div>
                        `,
                        iconSize: [20, 20],
                        iconAnchor: [10, 10]
                    });

                    // Remove old user marker if exists
                    if (userMarker) {
                        userMarker.remove();
                    }

                    // Add user marker
                    userMarker = L.marker([userLat, userLng], {
                        icon: userIcon,
                        zIndexOffset: 1000 // Always on top
                    }).addTo(mapInstance);

                    userMarker.bindPopup(`
                        <div style="padding: 8px; text-align: center;">
                            <strong style="font-size: 12px; color: #3B82F6;">📍 Vị trí của bạn</strong>
                        </div>
                    `);

                    // Optional: Pan to user location
                    // mapInstance.setView([userLat, userLng], 14);
                },
                (error) => {
                    console.error("Error getting user location for map:", error);
                }
            );
        }
    }

    // ============================================
    // MAP SEARCH
    // ============================================

    function attachMapSearchListener() {
        const searchInput = document.getElementById('mapSearchInput');
        if (!searchInput) {
            return;
        }

        searchInput.addEventListener('input', debounce(function(e) {
            const query = e.target.value.toLowerCase().trim();

            const courts = window.COURTS_DATA || [];


            if (!query) {
                // Show all markers
                addCourtMarkers(courts);

                // Fit bounds
                if (courts.length > 0) {
                    const validCourts = courts.filter(c => c.lat && c.lng);
                    if (validCourts.length > 0) {
                        const bounds = L.latLngBounds(validCourts.map(c => [c.lat, c.lng]));
                        mapInstance.fitBounds(bounds, { padding: [50, 50] });
                    }
                }
                return;
            }

            // Filter courts
            const filtered = courts.filter(court =>
                court.name.toLowerCase().includes(query) ||
                court.location.toLowerCase().includes(query) ||
                (court.province && court.province.toLowerCase().includes(query)) ||
                (court.district && court.district.toLowerCase().includes(query))
            );

            // Update markers
            addCourtMarkers(filtered);

            // Fit bounds if we have results
            if (filtered.length > 0 && mapInstance) {
                const validFiltered = filtered.filter(c => c.lat && c.lng);
                if (validFiltered.length > 0) {
                    const bounds = L.latLngBounds(validFiltered.map(c => [c.lat, c.lng]));
                    mapInstance.fitBounds(bounds, { padding: [50, 50] });
                }
            } else {
                // No results - show notification
                console.warn('No results for:', query);
                // Optional: Show toast or message on map
            }
        }, 300));

        // Clear on Escape key
        searchInput.addEventListener('keydown', function(e) {
            if (e.key === 'Escape') {
                this.value = '';
                this.dispatchEvent(new Event('input'));
            }
        });
    }

    // ============================================
    // MAP FILTERS
    // ============================================

    function attachMapFilterListeners() {
        const filterChips = document.querySelectorAll('.map-filter-chip');

        filterChips.forEach(chip => {
            chip.addEventListener('click', function() {
                const filter = this.dataset.mapFilter;

                // Toggle active state
                filterChips.forEach(c => c.classList.remove('active'));
                this.classList.add('active');

                // Apply filter
                applyMapFilter(filter);
            });
        });
    }

    function applyMapFilter(filterType) {
        const courts = window.COURTS_DATA || [];
        let filtered = [...courts];

        switch(filterType) {
            case 'near-me':
                // Sort by distance (assuming distance is already calculated)
                filtered.sort((a, b) => {
                    // Parse distance string
                    const getDistValue = (distStr) => {
                        if (distStr === 'Đang tính...') return Infinity;
                        if (distStr.endsWith('km')) return parseFloat(distStr);
                        if (distStr.endsWith('m')) return parseFloat(distStr) / 1000;
                        return Infinity;
                    };

                    return getDistValue(a.distance) - getDistValue(b.distance);
                });
                // Show top 10 nearest
                filtered = filtered.slice(0, 10);
                break;

            case 'high-rating':
                // Filter courts with rating > 4.0
                filtered = courts.filter(c => c.rating >= 4.0);
                // If none, show all sorted by rating
                if (filtered.length === 0) {
                    filtered = [...courts].sort((a, b) => b.rating - a.rating);
                }
                break;

            case 'cheap':
                // This is mock
                // For now, just show all
                filtered = courts;
                break;

            case 'new':
                // This is mock
                // For now, reverse order
                filtered = [...courts].reverse();
                break;

            default:
                filtered = courts;
        }

        // Update markers
        addCourtMarkers(filtered);

        // Fit bounds
        if (filtered.length > 0 && mapInstance) {
            const bounds = L.latLngBounds(filtered.map(c => [c.lat, c.lng]));
            mapInstance.fitBounds(bounds, { padding: [80, 80] });
        }
    }

    // ============================================
    // UTILITY FUNCTIONS
    // ============================================

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
    // PUBLIC API
    // ============================================

    // Expose initMap globally
    window.initMap = initMap;

    // Expose method to update markers when data changes
    window.updateMapMarkers = function(courts) {
        if (mapInstance && courts && courts.length > 0) {
            addCourtMarkers(courts);

            // Fit bounds
            const bounds = L.latLngBounds(courts.map(c => [c.lat, c.lng]));
            mapInstance.fitBounds(bounds, { padding: [50, 50] });
        }
    };

    // Auto-initialize if map container exists on page load
    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', () => {
            // Let tab switching handle auto-init
        });
    }

    // Add CSS for pulse animation
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