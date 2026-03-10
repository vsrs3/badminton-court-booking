// Facility Form JS
/* ---------- IMAGE PREVIEW: THUMBNAIL ---------- */
function previewThumbnail(input) {
    if (!input.files || !input.files[0]) return;
    const reader = new FileReader();
    reader.onload = e => {
        let preview = document.getElementById('thumbnailPreview');
        if (!preview) {
            const container = input.closest('.card-body').querySelector('.mb-3');
            container.innerHTML = '<img id="thumbnailPreview" src="' + e.target.result + '" class="img-fluid rounded border" style="max-height:200px;width:100%;object-fit:cover;" alt="thumbnail">';
        } else {
            preview.src = e.target.result;
        }
    };
    reader.readAsDataURL(input.files[0]);
}
/* ---------- IMAGE PREVIEW: GALLERY ---------- */
function previewGallery(input) {
    const preview = document.getElementById('newGalleryPreview');
    preview.innerHTML = '';
    if (!input.files || input.files.length === 0) return;
    Array.from(input.files).forEach(file => {
        if (!file.type.startsWith('image/')) return;
        const reader = new FileReader();
        reader.onload = e => {
            const col = document.createElement('div');
            col.className = 'col-3 animate__animated animate__fadeIn';
            col.innerHTML = '<img src="' + e.target.result + '" class="img-fluid rounded border border-success" style="height:100px;width:100%;object-fit:cover;" alt="gallery">';
            preview.appendChild(col);
        };
        reader.readAsDataURL(file);
    });
}
/* ---------- GALLERY: MARK FOR DELETE ---------- */
let deletedImageIds = [];
function markForDelete(imageId) {
    if (confirm('Ban co chac muon xoa anh nay khi luu khong?')) {
        deletedImageIds.push(imageId);
        document.getElementById('deletedIds').value = deletedImageIds.join(',');
        const element = document.getElementById('img-container-' + imageId);
        if (element) {
            element.style.opacity = '0';
            element.style.transition = 'opacity 0.3s ease';
            setTimeout(() => element.remove(), 300);
        }
    }
}
/* ===============================
   FACILITY FORM - MAP & ADDRESS
   =============================== */
let map;
let marker;
/*
 * locationStatus:
 *   'valid'      - lat/lng confirmed (map click OR forward geocode success)
 *   'unverified' - user edited address; geocode failed but OLD lat/lng kept
 *   null         - no lat/lng at all
 */
let locationStatus = null;
let geocodeTimer   = null;
/* ---------- STATUS BADGE ---------- */
function updateLocationBadge() {
    const badge = document.getElementById('locationStatusBadge');
    if (!badge) return;
    const lat = document.getElementById('latitude').value;
    const lng = document.getElementById('longitude').value;
    if (locationStatus === 'valid' && lat && lng) {
        badge.className = 'badge bg-success ms-2 align-middle';
        badge.innerHTML = '<i class="bi bi-geo-alt-fill me-1"></i>&#272;&#227; x&#225;c &#273;&#7883;nh v&#7883; tr&#237;';
    } else if (locationStatus === 'unverified' && lat && lng) {
        badge.className = 'badge bg-warning text-dark ms-2 align-middle';
        badge.innerHTML = '<i class="bi bi-exclamation-triangle-fill me-1"></i>To&#7841; &#273;&#7897; ch&#432;a kh&#7899;p &#273;&#7883;a ch&#7881;';
    } else {
        badge.className = 'badge bg-secondary ms-2 align-middle';
        badge.innerHTML = '<i class="bi bi-geo me-1"></i>Ch&#432;a x&#225;c &#273;&#7883;nh v&#7883; tr&#237;';
    }
}
/* ---------- INLINE GEOCODE HINT ---------- */
function showGeocodeHint(type) {
    const hint = document.getElementById('geocodeHint');
    if (!hint) return;
    switch (type) {
        case 'loading':
            hint.className = 'form-text text-muted';
            hint.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span>&#272;ang t&#236;m to&#7841; &#273;&#7897;...';
            hint.style.display = 'block';
            break;
        case 'success':
            hint.className = 'form-text text-success';
            hint.innerHTML = '<i class="bi bi-check-circle-fill me-1"></i>&#272;&#227; t&#236;m th&#7845;y to&#7841; &#273;&#7897; v&#224; c&#7853;p nh&#7853;t b&#7843;n &#273;&#7891;.';
            hint.style.display = 'block';
            setTimeout(() => { hint.style.display = 'none'; }, 4000);
            break;
        case 'warn':
            hint.className = 'form-text text-warning';
            hint.innerHTML = '<i class="bi bi-exclamation-triangle-fill me-1"></i>Kh&#244;ng t&#236;m &#273;&#432;&#7907;c to&#7841; &#273;&#7897; t&#7915; &#273;&#7883;a ch&#7881; n&#224;y. To&#7841; &#273;&#7897; c&#361; &#273;&#432;&#7907;c gi&#7919; l&#7841;i &mdash; <a href="#" onclick="openMapModal();return false;">ch&#7885;n l&#7841;i tr&#234;n b&#7843;n &#273;&#7891;</a>.';
            hint.style.display = 'block';
            break;
        case 'null':
            hint.className = 'form-text text-danger';
            hint.innerHTML = '<i class="bi bi-x-circle-fill me-1"></i>Kh&#244;ng t&#236;m &#273;&#432;&#7907;c to&#7841; &#273;&#7897;. &#272;&#7883;a &#273;i&#7875;m s&#7869; kh&#244;ng hi&#7875;n th&#7883; tr&#234;n b&#7843;n &#273;&#7891; t&#236;m ki&#7871;m &mdash; <a href="#" onclick="openMapModal();return false;">ch&#7885;n v&#7883; tr&#237; tr&#234;n b&#7843;n &#273;&#7891;</a>.';
            hint.style.display = 'block';
            break;
        default:
            hint.style.display = 'none';
    }
}
/* ---------- MAP MODAL ---------- */
function openMapModal() {
    const modalEl = document.getElementById('mapModal');
    const modal = new bootstrap.Modal(modalEl);
    modal.show();
    setTimeout(initMap, 300);
}
/* ---------- MAP INIT ---------- */
function initMap() {
    if (map) {
        map.invalidateSize();
        return;
    }
    const latInput = document.getElementById('latitude').value;
    const lngInput = document.getElementById('longitude').value;
    const lat = latInput ? parseFloat(latInput) : 21.0285;
    const lng = lngInput ? parseFloat(lngInput) : 105.8542;
    map = L.map('map').setView([lat, lng], 14);
    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '&copy; OpenStreetMap'
    }).addTo(map);
    L.Control.geocoder({ defaultMarkGeocode: false })
        .on('markgeocode', function (e) {
            const center = e.geocode.center;
            map.setView(center, 17);
            setMarker(center.lat, center.lng);
        })
        .addTo(map);
    if (latInput && lngInput) {
        setMarker(lat, lng);
    }
    map.on('click', function (e) {
        setMarker(e.latlng.lat, e.latlng.lng);
        reverseGeocode(e.latlng.lat, e.latlng.lng);
    });
}
/* ---------- MARKER ---------- */
function setMarker(lat, lng) {
    if (marker) {
        marker.setLatLng([lat, lng]);
    } else {
        marker = L.marker([lat, lng]).addTo(map);
    }
}
/* ---------- REVERSE GEOCODE (map click => fill form) ---------- */
function reverseGeocode(lat, lng) {
    fetch('https://nominatim.openstreetmap.org/reverse?format=json&lat=' + lat + '&lon=' + lng)
        .then(res => res.json())
        .then(data => {
            console.group('[Map] Reverse geocode');
            console.log(data.address);
            console.groupEnd();
            const a = mapAddress(data.address || {}, data);
            setValue('province', a.province);
            setValue('district', a.district);
            setValue('ward', a.ward);
            setValue('address', a.address);
            setValue('latitude', lat);
            setValue('longitude', lng);
            locationStatus = 'valid';
            updateLocationBadge();
            showGeocodeHint('none');
            bootstrap.Modal.getInstance(document.getElementById('mapModal')).hide();
        })
        .catch(() => {
            setValue('latitude', lat);
            setValue('longitude', lng);
            locationStatus = 'valid';
            updateLocationBadge();
            bootstrap.Modal.getInstance(document.getElementById('mapModal')).hide();
        });
}
/* ---------- FORWARD GEOCODE (address text => find lat/lng) ---------- */
function forwardGeocode() {
    const address  = (document.getElementById('address')  ? document.getElementById('address').value  : '').trim();
    const ward     = (document.getElementById('ward')     ? document.getElementById('ward').value     : '').trim();
    const district = (document.getElementById('district') ? document.getElementById('district').value : '').trim();
    const province = (document.getElementById('province') ? document.getElementById('province').value : '').trim();
    const query = [address, ward, district, province, 'Viet Nam'].filter(Boolean).join(', ');
    if (!query.replace(/[,\s]/g, '')) return;
    showGeocodeHint('loading');
    const prevLat = document.getElementById('latitude').value;
    const prevLng = document.getElementById('longitude').value;
    fetch('https://nominatim.openstreetmap.org/search?format=json&q=' + encodeURIComponent(query) + '&limit=1&countrycodes=vn')
        .then(res => res.json())
        .then(results => {
            if (results && results.length > 0) {
                const r = results[0];
                setValue('latitude',  r.lat);
                setValue('longitude', r.lon);
                locationStatus = 'valid';
                updateLocationBadge();
                showGeocodeHint('success');
                if (map) {
                    const latF = parseFloat(r.lat);
                    const lngF = parseFloat(r.lon);
                    map.setView([latF, lngF], 16);
                    setMarker(latF, lngF);
                }
            } else {
                if (prevLat && prevLng) {
                    locationStatus = 'unverified';
                    updateLocationBadge();
                    showGeocodeHint('warn');
                } else {
                    setValue('latitude', '');
                    setValue('longitude', '');
                    locationStatus = null;
                    updateLocationBadge();
                    showGeocodeHint('null');
                }
            }
        })
        .catch(() => {
            locationStatus = (prevLat && prevLng) ? 'unverified' : null;
            updateLocationBadge();
            showGeocodeHint((prevLat && prevLng) ? 'warn' : 'null');
        });
}
/* ---------- LOCATION WARNING MODAL ---------- */
function showLocationWarningModal(status, lat, lng) {
    const modalEl = document.getElementById('locationWarningModal');
    if (!modalEl) return;
    // Toggle between the two pre-rendered panels (all text lives in JSP, never in JS)
    const panelUnverified = document.getElementById('warnPanelUnverified');
    const panelNull       = document.getElementById('warnPanelNull');
    if (status === 'unverified' && lat && lng) {
        if (panelUnverified) panelUnverified.style.display = '';
        if (panelNull)       panelNull.style.display = 'none';
    } else {
        if (panelUnverified) panelUnverified.style.display = 'none';
        if (panelNull)       panelNull.style.display = '';
    }
    new bootstrap.Modal(modalEl).show();
}
function confirmSaveWithoutLocation() {
    bootstrap.Modal.getInstance(document.getElementById('locationWarningModal')).hide();
    const form = document.getElementById('facilityForm');
    const old = document.getElementById('_skipLocationCheck');
    if (old) old.remove();
    const flag = document.createElement('input');
    flag.type = 'hidden';
    flag.id   = '_skipLocationCheck';
    flag.name = '_skipLocationCheck';
    flag.value = '1';
    form.appendChild(flag);
    form.requestSubmit ? form.requestSubmit() : form.submit();
}
/* ---------- INIT ON DOM READY ---------- */
document.addEventListener("DOMContentLoaded", function () {
    const initLat = document.getElementById('latitude') ? document.getElementById('latitude').value : '';
    const initLng = document.getElementById('longitude') ? document.getElementById('longitude').value : '';
    if (initLat && initLng) locationStatus = 'valid';
    updateLocationBadge();
    ["province", "district", "ward", "address"].forEach(id => {
        const el = document.getElementById(id);
        if (!el) return;
        el.addEventListener("input", () => {
            clearTimeout(geocodeTimer);
            geocodeTimer = setTimeout(forwardGeocode, 1200);
        });
        el.addEventListener("change", () => {
            clearTimeout(geocodeTimer);
            geocodeTimer = setTimeout(forwardGeocode, 500);
        });
    });
    const form = document.getElementById('facilityForm');
    if (form) {
        form.addEventListener('submit', function (e) {
            if (document.getElementById('_skipLocationCheck')) return true;
            const lat = document.getElementById('latitude').value;
            const lng = document.getElementById('longitude').value;
            if (locationStatus === 'valid' && lat && lng) return true;
            e.preventDefault();
            showLocationWarningModal(locationStatus, lat, lng);
        });
    }
});
/* ---------- mapAddress: Nominatim reverse-geocode response => form fields ---------- */
function mapAddress(addr, data) {
    let province = '';
    let district = '';
    let ward = '';
    let detailedAddress = [];
    province = addr.state || addr.city || addr.province || addr.region || '';
    if (province.toLowerCase().indexOf('ha noi') >= 0 ||
        province.toLowerCase().indexOf('h\u00e0 n\u1ed9i') >= 0 ||
        province.toLowerCase().indexOf('h\u1ed3 ch\u00ed minh') >= 0 ||
        province.toLowerCase().indexOf('ho chi minh') >= 0) {
        if (addr.city_district) {
            if (addr.city_district.indexOf('Qu\u1eadn') >= 0 || addr.city_district.indexOf('District') >= 0) {
                district = addr.city_district;
            } else {
                ward = addr.city_district;
            }
        }
        district = district || addr.county || addr.suburb || '';
    } else {
        district = addr.city || addr.county || addr.municipality || '';
        ward = addr.suburb || addr.neighbourhood || addr.village || addr.hamlet || addr.city_district || '';
    }
    if (!ward) {
        ward = addr.neighbourhood || addr.suburb || addr.village || '';
    }
    if (addr.house_number) detailedAddress.push(addr.house_number);
    if (addr.road)         detailedAddress.push(addr.road);
    if (addr.pedestrian)   detailedAddress.push(addr.pedestrian);
    if (addr.path)         detailedAddress.push(addr.path);
    if (addr.building)     detailedAddress.push(addr.building);
    if (addr.amenity || addr.shop || addr.tourism || addr.name) {
        detailedAddress.push(addr.name || addr.amenity || addr.shop || addr.tourism || '');
    }
    if (addr.neighbourhood && ward !== addr.neighbourhood && detailedAddress.indexOf(addr.neighbourhood) < 0) {
        detailedAddress.push(addr.neighbourhood);
    }
    detailedAddress = detailedAddress.filter(Boolean).join(', ').trim();
    if (!detailedAddress && data && data.display_name) {
        const parts = data.display_name.split(', ');
        let skipCount = 1;
        if (/^\d{5}$/.test(parts[parts.length - 2])) skipCount = 2;
        detailedAddress = parts.slice(0, parts.length - (skipCount + 3)).join(', ').trim();
    }
    ward     = ward.replace(/(Ward|Ph\u01b0\u1eddng)$/i, '').trim();
    district = district.replace(/(District|Qu\u1eadn)$/i, '').trim();
    return {
        province: province.trim() || null,
        district: district.trim() || null,
        ward:     ward.trim()     || null,
        address:  detailedAddress || null
    };
}
/* ---------- UTIL ---------- */
function setValue(id, value) {
    const el = document.getElementById(id);
    if (el) el.value = (value !== null && value !== undefined) ? value : '';
}