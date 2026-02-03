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

            const a = mapAddress(data.address || {});

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
    return {
        province: addr.state || addr.city || '',
        district: addr.county || addr.suburb || '',
        ward: addr.city_district || addr.village || addr.neighbourhood || '',
        address: addr.road || ''
    };
}

/* ---------- ADDRESS CHANGE ---------- */
document.addEventListener('DOMContentLoaded', function () {
    const addressInput = document.getElementById('address');
    const form = document.querySelector('form');

    if (!addressInput || !form) return;

    addressInput.addEventListener('focus', () => {
        originalAddress = addressInput.value;
    });

    form.addEventListener('submit', function (e) {
        if (addressInput.value && addressInput.value !== originalAddress) {
            e.preventDefault();
            geocodeAddress(addressInput.value);
        }
    });
});

/* ---------- GEOCODE ---------- */
function geocodeAddress(address) {
    fetch(`https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(address)}`)
        .then(res => res.json())
        .then(results => {
            if (!results || results.length === 0) {
                alert('Không tìm thấy địa điểm');
                return;
            }

            const r = results[0];
            setValue('latitude', r.lat);
            setValue('longitude', r.lon);

            document.querySelector('form').submit();
        })
        .catch(() => alert('Lỗi khi định vị địa chỉ'));
}

document.addEventListener("DOMContentLoaded", function () {

    const form = document.getElementById("facilityForm");

    form.addEventListener("submit", function (e) {
        e.preventDefault(); // CHẶN submit

        geocodeAndSubmit();
    });

    function geocodeAndSubmit() {
        const address = buildFullAddress();

        if (!address) {
            alert("Vui lòng nhập địa chỉ");
            return;
        }

        // Dùng Nominatim geocoder
        fetch(`https://nominatim.openstreetmap.org/search?format=json&q=${encodeURIComponent(address)}`)
            .then(res => res.json())
            .then(data => {
                if (!data || data.length === 0) {
                    alert("Không tìm được vị trí. Vui lòng chọn lại trên bản đồ.");
                    return;
                }

                const lat = data[0].lat;
                const lng = data[0].lon;

                document.getElementById("latitude").value = lat;
                document.getElementById("longitude").value = lng;

                // SUBMIT THẬT
                form.submit();
            })
            .catch(err => {
                console.error(err);
                alert("Lỗi khi xác định vị trí.");
            });
    }

    function buildFullAddress() {
        const parts = [
            document.getElementById("address").value,
            document.getElementById("ward").value,
            document.getElementById("district").value,
            document.getElementById("province").value
        ];

        return parts
            .map(p => p?.trim())
            .filter(p => p)
            .join(", ");
    }

});

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
