<%-- <%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%> --%>
    
<%-- ═══════════════════════════════════════════════
     MODAL 1 — UPLOAD AVATAR
     Trigger: click vào vùng avatar ở top bar
═══════════════════════════════════════════════ --%>
<%-- <div class="sd-modal-bg" id="sd-av-modal">
	<div class="sd-modal sd-av-modal">
		<div class="sd-modal-hd">
			<div class="d-flex align-items-center gap-3">
				<div class="sd-modal-hd-icon">
					<i class="bi bi-camera-fill"></i>
				</div>
				<h3 class="sd-modal-title">Cập nhật ảnh đại diện</h3>
			</div>
			<button class="sd-modal-x" onclick="closeAvatarModal()">
				<i class="bi bi-x-lg"></i>
			</button>
		</div>
		<div class="sd-modal-bd">

			Preview ảnh hiện tại / ảnh mới chọn
			<div class="sd-av-preview-wrap">
				<c:choose>
					<c:when test="${not empty staff.avatarPath}">
						<img id="sd-av-preview-img" class="sd-av-preview"
							src="${pageContext.request.contextPath}/${staff.avatarPath}"
							alt="preview">
						<div id="sd-av-preview-initials"
							class="sd-av-preview-initials d-none">${not empty staff.fullName ? staff.fullName.substring(0,1).toUpperCase() : '?'}
						</div>
					</c:when>
					<c:otherwise>
						<img id="sd-av-preview-img" class="sd-av-preview d-none" src=""
							alt="preview">
						<div id="sd-av-preview-initials" class="sd-av-preview-initials">
							${not empty staff.fullName ? staff.fullName.substring(0,1).toUpperCase() : '?'}
						</div>
					</c:otherwise>
				</c:choose>
			</div>

			Drop zone
			<div class="sd-drop-zone" id="sd-drop-zone"
				onclick="document.getElementById('sd-av-input').click()"
				ondragover="onAvatarDragOver(event)"
				ondragleave="onAvatarDragLeave()" ondrop="onAvatarDrop(event)">
				<i class="bi bi-cloud-arrow-up sd-drop-icon"></i>
				<p class="sd-drop-label">
					Kéo thả hoặc <span>chọn ảnh từ máy tính</span>
				</p>
				<p class="sd-drop-hint">JPG, PNG, WEBP — tối đa 2MB</p>
			</div>

			Input file ẩn
			<input type="file" id="sd-av-input"
				accept="image/jpeg,image/png,image/webp,image/gif"
				style="display: none" onchange="onAvatarInputChange(event)">

			Thông báo lỗi
			<div class="sd-av-error" id="sd-av-error">
				<i class="bi bi-exclamation-circle-fill"></i> <span></span>
			</div>

			Buttons
			<div class="d-flex gap-3 mt-4">
				<button class="sd-btn-m-cancel" onclick="closeAvatarModal()">Hủy</button>
				<button class="sd-btn-m-confirm" id="sd-av-save-btn"
					onclick="uploadAvatar()" disabled>
					<span id="sd-av-save-txt">Lưu ảnh</span>
				</button>
			</div>

		</div>
	</div>
</div>
 --%>