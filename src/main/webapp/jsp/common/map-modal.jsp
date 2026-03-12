<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%-- MAP PICKER MODAL --%>
<div class="modal fade" id="mapModal" tabindex="-1" aria-labelledby="mapModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-xl modal-dialog-centered">
        <div class="modal-content">
            <div class="modal-header py-2">
                <div>
                    <h5 class="modal-title mb-0" id="mapModalLabel">
                        <i class="bi bi-geo-alt-fill text-success me-2"></i>Chọn vị trí trên bản đồ
                    </h5>
                    <small class="text-muted">Click vào vị trí bất kỳ trên bản đồ để ghim. Địa chỉ sẽ tự động điền vào form.</small>
                </div>
                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Đóng"></button>
            </div>
            <div class="modal-body p-0">
                <div id="map" style="height: 520px;"></div>
            </div>
            <div class="modal-footer py-2 text-muted small justify-content-start">
                <i class="bi bi-info-circle me-1"></i>
                Dữ liệu bản đồ từ OpenStreetMap. Nếu vị trí chưa chính xác, bạn có thể nhập địa chỉ trực tiếp vào form.
            </div>
        </div>
    </div>
</div>

<%-- ============================================================
     LOCATION WARNING MODAL
     Hai panel được pre-render trong JSP (UTF-8 an toàn).
     JS chỉ toggle display — không inject text tiếng Việt từ JS.
     ============================================================ --%>
<div class="modal fade" id="locationWarningModal" tabindex="-1"
     aria-labelledby="locationWarningModalLabel" aria-hidden="true">
    <div class="modal-dialog modal-dialog-centered" style="max-width: 480px;">
        <div class="modal-content border-0 shadow-lg">

            <%-- ── PANEL: UNVERIFIED (có lat/lng cũ, địa chỉ không khớp) ── --%>
            <div id="warnPanelUnverified" style="display:none;">
                <div class="modal-header border-0 pb-1 align-items-start">
                    <div class="d-flex align-items-center gap-2">
                        <i class="bi bi-exclamation-triangle-fill text-warning fs-4 flex-shrink-0"></i>
                        <h5 class="modal-title mb-0 lh-sm">Địa chỉ chưa khớp với toạ độ bản đồ</h5>
                    </div>
                    <button type="button" class="btn-close ms-auto flex-shrink-0"
                            data-bs-dismiss="modal" aria-label="Đóng"></button>
                </div>
                <div class="modal-body pt-1 pb-2">
                    <p class="mb-2">
                        Bạn đã thay đổi địa chỉ nhưng hệ thống <strong>không tìm được toạ độ mới</strong> tương ứng.
                    </p>
                    <p class="mb-2">
                        Nếu tiếp tục lưu, toạ độ cũ sẽ được giữ lại —
                        vị trí hiển thị trên bản đồ có thể <span class="text-warning fw-semibold">không chính xác</span>.
                    </p>
                    <p class="mb-0 p-2 bg-warning bg-opacity-10 rounded border border-warning border-opacity-25 small">
                        <i class="bi bi-lightbulb-fill text-warning me-1"></i>
                        <strong>Khuyến nghị:</strong> hãy
                        <a href="#" onclick="document.getElementById('locationWarningModal').querySelector('[data-bs-dismiss]').click(); setTimeout(openMapModal, 400); return false;">
                            chọn lại vị trí trên bản đồ
                        </a> để đảm bảo độ chính xác.
                    </p>
                </div>
                <div class="modal-footer border-0 pt-0 gap-2 flex-wrap">
                    <button type="button" class="btn btn-sm btn-outline-secondary"
                            onclick="document.getElementById('locationWarningModal').querySelector('[data-bs-dismiss]').click(); setTimeout(openMapModal, 400);">
                        <i class="bi bi-map me-1"></i>Chọn trên bản đồ
                    </button>
                    <button type="button" class="btn btn-sm btn-outline-secondary" data-bs-dismiss="modal">
                        <i class="bi bi-pencil me-1"></i>Sửa địa chỉ
                    </button>
                    <button type="button" id="locationWarningConfirmBtn"
                            class="btn btn-sm btn-warning ms-auto"
                            onclick="confirmSaveWithoutLocation()">
                        Vẫn lưu (giữ toạ độ cũ)
                    </button>
                </div>
            </div>

            <%-- ── PANEL: NULL (không có lat/lng nào cả) ── --%>
            <div id="warnPanelNull" style="display:none;">
                <div class="modal-header border-0 pb-1 align-items-start">
                    <div class="d-flex align-items-center gap-2">
                        <i class="bi bi-x-circle-fill text-danger fs-4 flex-shrink-0"></i>
                        <h5 class="modal-title mb-0 lh-sm">Không xác định được vị trí</h5>
                    </div>
                    <button type="button" class="btn-close ms-auto flex-shrink-0"
                            data-bs-dismiss="modal" aria-label="Đóng"></button>
                </div>
                <div class="modal-body pt-1 pb-2">
                    <p class="mb-2">
                        Hệ thống <strong>không tìm được toạ độ</strong> từ địa chỉ bạn nhập.
                    </p>
                    <p class="mb-2">
                        Nếu tiếp tục lưu, <code>latitude</code> và <code>longitude</code> sẽ lưu là
                        <strong>null</strong> — địa điểm sẽ
                        <span class="text-danger fw-semibold">không hiển thị trên bản đồ tìm kiếm</span>.
                    </p>
                    <p class="mb-0 p-2 bg-danger bg-opacity-10 rounded border border-danger border-opacity-25 small">
                        <i class="bi bi-lightbulb-fill text-warning me-1"></i>
                        <strong>Khuyến nghị:</strong> hãy
                        <a href="#" onclick="document.getElementById('locationWarningModal').querySelector('[data-bs-dismiss]').click(); setTimeout(openMapModal, 400); return false;">
                            ghim vị trí trên bản đồ
                        </a> để khách hàng tìm thấy địa điểm của bạn.
                    </p>
                </div>
                <div class="modal-footer border-0 pt-0 gap-2 flex-wrap">
                    <button type="button" class="btn btn-sm btn-outline-secondary"
                            onclick="document.getElementById('locationWarningModal').querySelector('[data-bs-dismiss]').click(); setTimeout(openMapModal, 400);">
                        <i class="bi bi-map me-1"></i>Ghim trên bản đồ
                    </button>
                    <button type="button" class="btn btn-sm btn-outline-secondary" data-bs-dismiss="modal">
                        <i class="bi bi-pencil me-1"></i>Sửa địa chỉ
                    </button>
                    <button type="button" id="locationWarningConfirmBtnNull"
                            class="btn btn-sm btn-danger ms-auto"
                            onclick="confirmSaveWithoutLocation()">
                        Vẫn lưu (lat/lng = null)
                    </button>
                </div>
            </div>

        </div>
    </div>
</div>
