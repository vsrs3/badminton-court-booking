// Facility Form JS handles image previews and gallery management

/**
 * Preview Thumbnail
 */
function previewThumbnail(input) {
    if (!input.files || !input.files[0]) return;

    const reader = new FileReader();
    reader.onload = e => {
        let preview = document.getElementById('thumbnailPreview');

        if (!preview) {
            const container = input.closest('.card-body').querySelector('.mb-3');
            container.innerHTML = `
                <img id="thumbnailPreview"
                     src="${e.target.result}"
                     class="img-fluid rounded border"
                     style="max-height: 200px; width: 100%; object-fit: cover;">
            `;
        } else {
            preview.src = e.target.result;
        }
    };
    reader.readAsDataURL(input.files[0]);
}

/**
 * Preview Gallery Images
 */
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
            col.innerHTML = '<img src="' + e.target.result + '" ' +
                'class="img-fluid rounded border border-success" ' +
                'style="height:100px; width: 100%; object-fit:cover;">';
            preview.appendChild(col);
        };
        reader.readAsDataURL(file);
    });
}

/**
 * Mark Image for Deletion
 */
let deletedImageIds = [];
function markForDelete(imageId) {
    if (confirm('Bạn có chắc muốn xóa ảnh này khi lưu không?')) {
        deletedImageIds.push(imageId);
        document.getElementById('deletedIds').value = deletedImageIds.join(',');

        const element = document.getElementById('img-container-' + imageId);
        if (element) {
            element.style.opacity = '0';
            element.style.transition = 'opacity 0.3s ease';
            setTimeout(() => {
                element.remove();
            }, 300);
        }
    }
}

/* ===============================
   FACILITY FORM - MAP & ADDRESS
   =============================== */

let map;
let marker;
let originalAddress = '';

/* ---------- MODAL ---------- */
function openMapModal() {
    const modalEl = document.getElementById('mapModal');
    const modal = new bootstrap.Modal(modalEl);
    modal.show();

    setTimeout(initMap, 300);
}

/* ---------- MAP INIT ---------- */
function initMap() {
    if (map) return;

    const latInput = document.getElementById('latitude').value;
    const lngInput = document.getElementById('longitude').value;

    const lat = latInput ? parseFloat(latInput) : 21.0285;
    const lng = lngInput ? parseFloat(lngInput) : 105.8542;

    map = L.map('map').setView([lat, lng], 14);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
        attribution: '© OpenStreetMap'
    }).addTo(map);

    const geocoder = L.Control.geocoder({
        defaultMarkGeocode: false
    })
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

/* ---------- REVERSE GEOCODE ---------- */
function reverseGeocode(lat, lng) {
    fetch(`https://nominatim.openstreetmap.org/reverse?format=json&lat=${lat}&lon=${lng}`)
        .then(res => res.json())
        .then(data => {

            console.group('📍 Address mapping');
            console.log(data.address);
            console.groupEnd();

            const a = mapAddress(data.address || {}, data);

            setValue('province', a.province);
            setValue('district', a.district);
            setValue('ward', a.ward);
            setValue('address', a.address);

            setValue('latitude', lat);
            setValue('longitude', lng);

            bootstrap.Modal.getInstance(
                document.getElementById('mapModal')
            ).hide();
        });
}

function mapAddress(addr) {
    let province = '';
    let district = '';
    let ward = '';
    let detailedAddress = [];

    // Province (Tỉnh/Thành phố trực thuộc TW)
    province = addr.state || addr.city || addr.province || addr.region || '';

    // Đặc biệt Hà Nội/TP.HCM: city thường là tỉnh, county/city_district là quận/phường
    if (province.toLowerCase().includes('hà nội') || province.toLowerCase().includes('hồ chí minh') || province.toLowerCase().includes('ho chi minh')) {
        // Ở HN/TP.HCM, city_district thường là phường/quận lẫn lộn
        if (addr.city_district) {
            if (addr.city_district.includes('Quận') || addr.city_district.includes('District')) {
                district = addr.city_district;
            } else {
                ward = addr.city_district;
            }
        }
        district = district || addr.county || addr.suburb || '';
    } else {
        // Tỉnh khác: city thường là huyện/thị xã, suburb là xã/phường
        district = addr.city || addr.county || addr.municipality || '';
        ward = addr.suburb || addr.neighbourhood || addr.village || addr.hamlet || addr.city_district || '';
    }

    // Ward fallback thêm
    if (!ward) {
        ward = addr.neighbourhood || addr.suburb || addr.village || '';
    }

    // Detailed address: thu thập nhiều thành phần nhất có thể
    if (addr.house_number) detailedAddress.push(addr.house_number);
    if (addr.road) detailedAddress.push(addr.road);
    if (addr.pedestrian) detailedAddress.push(addr.pedestrian); // nếu là đường đi bộ
    if (addr.path) detailedAddress.push(addr.path);
    if (addr.building) detailedAddress.push(addr.building);
    if (addr.amenity || addr.shop || addr.tourism || addr.name) {
        // Nếu click vào POI (quán ăn, cửa hàng), thêm tên
        detailedAddress.push(addr.name || addr.amenity || addr.shop || addr.tourism || '');
    }

    // Nếu vẫn thiếu, thêm suburb/neighbourhood nếu không trùng ward
    if (addr.neighbourhood && ward !== addr.neighbourhood && !detailedAddress.includes(addr.neighbourhood)) {
        detailedAddress.push(addr.neighbourhood);
    }

    detailedAddress = detailedAddress.filter(Boolean).join(', ').trim();

    // Fallback cuối: nếu detailedAddress rỗng, parse từ display_name (bỏ phần cuối: ward, district, province, Vietnam)
    if (!detailedAddress && data.display_name) {  // 'data' là response đầy đủ
        const parts = data.display_name.split(', ');
        // Bỏ Vietnam, postcode nếu có, province, district, ward (thường 3-4 phần cuối)
        let skipCount = 1; // Vietnam
        if (/^\d{5}$/.test(parts[parts.length - 2])) skipCount = 2; // có postcode
        const detailParts = parts.slice(0, parts.length - (skipCount + 3)); // giả sử 3 phần hành chính cuối
        detailedAddress = detailParts.join(', ').trim();
    }

    // Clean ward/district nếu có "Ward"/"Phường" thừa (tùy nhu cầu)
    ward = ward.replace(/( Ward| Phường)$/i, '').trim();
    district = district.replace(/( District| Quận)$/i, '').trim();

    return {
        province: province.trim() || null,
        district: district.trim() || null,
        ward: ward.trim() || null,
        address: detailedAddress || null
    };
}


document.addEventListener("DOMContentLoaded", function () {
    ["province", "district", "ward", "address"].forEach(id => {
        const el = document.getElementById(id);
        if (el) {
            el.addEventListener("change", () => {
                document.getElementById("latitude").value = "";
                document.getElementById("longitude").value = "";
            });
        }
    });
});

/* ---------- UTIL ---------- */
function setValue(id, value) {
    const el = document.getElementById(id);
    if (el) el.value = value;
}
