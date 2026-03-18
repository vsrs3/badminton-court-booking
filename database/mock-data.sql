-- ============================================================
-- MOCK DATA - Chạy SAU khi chạy bcbs.sql
-- Tất cả ngày đều tính TƯƠNG ĐỐI so với ngày hiện tại
-- ============================================================
-- Password cho tất cả tài khoản: 123456
-- Hash: $2a$10$l9VhV2BupVyxxagpB243S.AuynE7hf7bSEVjFwaRl9KEO/IPQYFrO
-- ============================================================
USE badminton_court_booking;
GO

-- Insert sample facilities
INSERT INTO Facility (name, province, district, ward, address, latitude, longitude, description, open_time, close_time, is_active)
VALUES
(N'CLB Cầu Lông Hà Đông Star', N'Hà Nội', N'Hà Đông', N'Phú Lãm', N'Đường Phú Lãm', 21.0281, 105.5079,
 N'Sân cầu lông đạt chuẩn thi đấu', '05:30', '22:00', 1),

(N'CLB Cầu Lông Thanh Xuân Pro', N'Hà Nội', N'Thanh Xuân', N'Hạ Đình', N'Nguyễn Xiển', 21.0008, 105.5342,
 N'Sân rộng, trần cao, thoáng mát', '06:00', '23:00', 1),

(N'CLB Cầu Lông Văn Quán', N'Hà Nội', N'Hà Đông', N'Văn Quán', N'Chiến Thắng', 21.0196, 105.5218,
 N'Sân cầu lông cộng đồng', '06:00', '22:00', 1),

(N'CLB Cầu Lông Yên Nghĩa', N'Hà Nội', N'Hà Đông', N'Yên Nghĩa', N'Quốc lộ 6', 21.0325, 105.4987,
 N'Sân mới, ánh sáng tốt', '05:00', '22:00', 1),

(N'CLB Cầu Lông Kiến Hưng', N'Hà Nội', N'Hà Đông', N'Kiến Hưng', N'Phúc La', 21.0113, 105.5426,
 N'Sân tập luyện và thi đấu phong trào', '06:00', '22:30', 1),

 (N'CLB Cầu Lông Mỹ Đình Sport', N'Hà Nội', N'Nam Từ Liêm', N'Mỹ Đình 1', N'Lê Đức Thọ', 21.0289, 105.7732,
 N'Sân tiêu chuẩn thi đấu quốc gia', '05:30', '22:30', 1),

(N'CLB Cầu Lông Đại Mỗ', N'Hà Nội', N'Nam Từ Liêm', N'Đại Mỗ', N'Sa Đôi', 21.0036, 105.7421,
 N'Sân tập luyện giá rẻ', '06:00', '22:00', 1),

(N'CLB Cầu Lông Trung Văn', N'Hà Nội', N'Nam Từ Liêm', N'Trung Văn', N'Tố Hữu', 21.0019, 105.7584,
 N'Sân mới, mặt thảm êm', '05:30', '23:00', 1),

(N'CLB Cầu Lông Dương Nội', N'Hà Nội', N'Hà Đông', N'Dương Nội', N'Lê Trọng Tấn', 21.0364, 105.7093,
 N'Sân rộng, bãi đỗ xe lớn', '06:00', '22:30', 1),

(N'CLB Cầu Lông Vạn Phúc', N'Hà Nội', N'Hà Đông', N'Vạn Phúc', N'Tố Hữu', 21.0187, 105.7398,
 N'Sân cầu lông phong trào', '06:00', '22:00', 1),

 (N'CLB Cầu Lông Cầu Giấy Elite', N'Hà Nội', N'Cầu Giấy', N'Dịch Vọng', N'Trần Thái Tông', 21.0369, 105.7896,
 N'Sân thi đấu chất lượng cao', '05:00', '23:00', 1),

(N'CLB Cầu Lông Tây Hồ', N'Hà Nội', N'Tây Hồ', N'Xuân La', N'Võ Chí Công', 21.0783, 105.8035,
 N'Sân thoáng mát gần hồ', '06:00', '22:00', 1),

(N'CLB Cầu Lông Long Biên Center', N'Hà Nội', N'Long Biên', N'Gia Thụy', N'Nguyễn Văn Cừ', 21.0442, 105.8789,
 N'Sân thi đấu và huấn luyện', '05:30', '22:30', 1),

(N'CLB Cầu Lông Hoàng Mai', N'Hà Nội', N'Hoàng Mai', N'Định Công', N'Giải Phóng', 20.9738, 105.8361,
 N'Sân cầu lông phong trào', '06:00', '22:00', 1),

(N'CLB Cầu Lông Thanh Trì', N'Hà Nội', N'Thanh Trì', N'Tứ Hiệp', N'Ngọc Hồi', 20.9549, 105.8457,
 N'Sân tập luyện cơ bản', '05:30', '22:00', 1),

 (N'CLB Cầu Lông Bắc Ninh Arena', N'Bắc Ninh', N'Bắc Ninh', N'Suối Hoa', N'Nguyễn Gia Thiều', 21.1876, 106.0604,
 N'Sân cầu lông chuyên nghiệp', '05:00', '23:00', 1),

(N'CLB Cầu Lông Hưng Yên Sport', N'Hưng Yên', N'Hưng Yên', N'Hiến Nam', N'Lý Thường Kiệt', 20.6559, 106.0567,
 N'Sân tập luyện và thi đấu', '06:00', '22:00', 1),

(N'CLB Cầu Lông Vĩnh Phúc Center', N'Vĩnh Phúc', N'Vĩnh Yên', N'Tích Sơn', N'Mê Linh', 21.3105, 105.5964,
 N'Sân mới, đạt chuẩn', '05:30', '22:30', 1),

(N'CLB Cầu Lông Hà Nam Pro', N'Hà Nam', N'Phủ Lý', N'Minh Khai', N'Biên Hòa', 20.5458, 105.9136,
 N'Sân cầu lông hiện đại', '06:00', '22:00', 1),

(N'CLB Cầu Lông Hòa Bình Arena', N'Hòa Bình', N'Hòa Bình', N'Tân Thịnh', N'Chi Lăng', 20.8126, 105.3384,
 N'Sân thi đấu khu vực', '05:30', '22:00', 1)

GO

-- ============================================================
-- 1. TÀI KHOẢN ADMIN (1 tài khoản)
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM Account WHERE email = 'admin@test.com')
BEGIN
INSERT INTO Account (email, password_hash, full_name, phone, role, is_active, created_at)
VALUES (
           'admin@test.com',
           '$2a$10$l9VhV2BupVyxxagpB243S.AuynE7hf7bSEVjFwaRl9KEO/IPQYFrO',
           N'Phạm Thị Admin',
           '0904234567',
           'ADMIN',
           1,
           GETDATE()
       );
END
GO

-- ============================================================
-- 2. TÀI KHOẢN OWNER (1 tài khoản)
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM Account WHERE email = 'owner@test.com')
BEGIN
INSERT INTO Account (email, password_hash, full_name, phone, role, is_active, created_at)
VALUES (
           'owner@test.com',
           '$2a$10$l9VhV2BupVyxxagpB243S.AuynE7hf7bSEVjFwaRl9KEO/IPQYFrO',
           N'Lê Văn Owner',
           '0903234567',
           'OWNER',
           1,
           GETDATE()
       );
END
GO

-- ============================================================
-- 3. TÀI KHOẢN STAFF (2 tài khoản) + Liên kết bảng Staff
-- ============================================================

-- Staff 1: Gán vào facility_id = 1 (CLB Cầu Lông Hà Đông Star)
IF NOT EXISTS (SELECT 1 FROM Account WHERE email = 'staff1@test.com')
BEGIN
INSERT INTO Account (email, password_hash, full_name, phone, role, is_active, created_at)
VALUES (
           'staff1@test.com',
           '$2a$10$l9VhV2BupVyxxagpB243S.AuynE7hf7bSEVjFwaRl9KEO/IPQYFrO',
           N'Trần Thị Staff Một',
           '0902234567',
           'STAFF',
           1,
           GETDATE()
       );
END
GO

-- Liên kết Staff 1 → bảng Staff → facility_id = 1
DECLARE @staff1AccId INT = (SELECT account_id FROM Account WHERE email = 'staff1@test.com');
IF @staff1AccId IS NOT NULL AND NOT EXISTS (SELECT 1 FROM Staff WHERE account_id = @staff1AccId)
BEGIN
INSERT INTO Staff (account_id, facility_id, is_active)
VALUES (@staff1AccId, 1, 1);
END
GO

-- Staff 2: Gán vào facility_id = 2 (CLB Cầu Lông Thanh Xuân Pro)
IF NOT EXISTS (SELECT 1 FROM Account WHERE email = 'staff2@test.com')
BEGIN
INSERT INTO Account (email, password_hash, full_name, phone, role, is_active, created_at)
VALUES (
           'staff2@test.com',
           '$2a$10$l9VhV2BupVyxxagpB243S.AuynE7hf7bSEVjFwaRl9KEO/IPQYFrO',
           N'Nguyễn Văn Staff Hai',
           '0905234567',
           'STAFF',
           1,
           GETDATE()
       );
END
GO

-- Liên kết Staff 2 → bảng Staff → facility_id = 2
DECLARE @staff2AccId INT = (SELECT account_id FROM Account WHERE email = 'staff2@test.com');
IF @staff2AccId IS NOT NULL AND NOT EXISTS (SELECT 1 FROM Staff WHERE account_id = @staff2AccId)
BEGIN
INSERT INTO Staff (account_id, facility_id, is_active)
VALUES (@staff2AccId, 2, 1);
END
GO

-- ============================================================
-- 4. TÀI KHOẢN CUSTOMER (5 tài khoản)
-- ============================================================

-- Customer 1
IF NOT EXISTS (SELECT 1 FROM Account WHERE email = 'customer1@test.com')
BEGIN
INSERT INTO Account (email, password_hash, full_name, phone, role, is_active, created_at)
VALUES (
           'customer1@test.com',
           '$2a$10$l9VhV2BupVyxxagpB243S.AuynE7hf7bSEVjFwaRl9KEO/IPQYFrO',
           N'Nguyễn Văn Khách Một',
           '0901234567',
           'CUSTOMER',
           1,
           GETDATE()
       );
END
GO

-- Customer 2
IF NOT EXISTS (SELECT 1 FROM Account WHERE email = 'customer2@test.com')
BEGIN
INSERT INTO Account (email, password_hash, full_name, phone, role, is_active, created_at)
VALUES (
           'customer2@test.com',
           '$2a$10$l9VhV2BupVyxxagpB243S.AuynE7hf7bSEVjFwaRl9KEO/IPQYFrO',
           N'Trần Thị Khách Hai',
           '0906234567',
           'CUSTOMER',
           1,
           GETDATE()
       );
END
GO

-- Customer 3
IF NOT EXISTS (SELECT 1 FROM Account WHERE email = 'customer3@test.com')
BEGIN
INSERT INTO Account (email, password_hash, full_name, phone, role, is_active, created_at)
VALUES (
           'customer3@test.com',
           '$2a$10$l9VhV2BupVyxxagpB243S.AuynE7hf7bSEVjFwaRl9KEO/IPQYFrO',
           N'Lê Hoàng Khách Ba',
           '0907234567',
           'CUSTOMER',
           1,
           GETDATE()
       );
END
GO

-- Customer 4
IF NOT EXISTS (SELECT 1 FROM Account WHERE email = 'customer4@test.com')
BEGIN
INSERT INTO Account (email, password_hash, full_name, phone, role, is_active, created_at)
VALUES (
           'customer4@test.com',
           '$2a$10$l9VhV2BupVyxxagpB243S.AuynE7hf7bSEVjFwaRl9KEO/IPQYFrO',
           N'Phạm Minh Khách Bốn',
           '0908234567',
           'CUSTOMER',
           1,
           GETDATE()
       );
END
GO

-- Customer 5
IF NOT EXISTS (SELECT 1 FROM Account WHERE email = 'customer5@test.com')
BEGIN
INSERT INTO Account (email, password_hash, full_name, phone, role, is_active, created_at)
VALUES (
           'customer5@test.com',
           '$2a$10$l9VhV2BupVyxxagpB243S.AuynE7hf7bSEVjFwaRl9KEO/IPQYFrO',
           N'Hoàng Thị Khách Năm',
           '0909234567',
           'CUSTOMER',
           1,
           GETDATE()
       );
END
GO

-- ============================================================
-- 5. COURTS cho facility_id = 1 (CLB Cầu Lông Hà Đông Star)
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM Court WHERE facility_id = 1)
BEGIN
INSERT INTO Court (facility_id, court_type_id, court_name, description, is_active)
VALUES
    (1, 1, N'Sân A1', N'Sân tiêu chuẩn tầng 1', 1),
    (1, 1, N'Sân A2', N'Sân tiêu chuẩn tầng 1', 1),
    (1, 2, N'Sân VIP 1', N'Sân VIP có điều hòa', 1),
    (1, 1, N'Sân A3', N'Sân tiêu chuẩn tầng 2', 1);
END
GO

-- Courts cho facility_id = 2 (CLB Cầu Lông Thanh Xuân Pro)
IF NOT EXISTS (SELECT 1 FROM Court WHERE facility_id = 2)
BEGIN
INSERT INTO Court (facility_id, court_type_id, court_name, description, is_active)
VALUES
    (2, 1, N'Sân B1', N'Sân tiêu chuẩn', 1),
    (2, 1, N'Sân B2', N'Sân tiêu chuẩn', 1),
    (2, 2, N'Sân VIP B', N'Sân VIP đặc biệt', 1);
END
GO

-- Courts cho facility_id = 3 (CLB Cầu Lông Văn Quán)
IF NOT EXISTS (SELECT 1 FROM Court WHERE facility_id = 3)
BEGIN
INSERT INTO Court (facility_id, court_type_id, court_name, description, is_active)
VALUES
    (3, 1, N'Sân C1', N'Sân tiêu chuẩn', 1),
    (3, 1, N'Sân C2', N'Sân tiêu chuẩn', 1);
END
GO

-- ============================================================
-- 6. PRICE RULES cho facility 1, 2, 3
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM FacilityPriceRule WHERE facility_id = 1)
BEGIN
INSERT INTO FacilityPriceRule (facility_id, court_type_id, day_type, start_time, end_time, price)
VALUES
    -- Facility 1, NORMAL court
    (1, 1, 'WEEKDAY', '05:30', '17:00', 60000),
    (1, 1, 'WEEKDAY', '17:00', '22:00', 90000),
    (1, 1, 'WEEKEND', '05:30', '17:00', 80000),
    (1, 1, 'WEEKEND', '17:00', '22:00', 120000),
    -- Facility 1, VIP court
    (1, 2, 'WEEKDAY', '05:30', '17:00', 100000),
    (1, 2, 'WEEKDAY', '17:00', '22:00', 150000),
    (1, 2, 'WEEKEND', '05:30', '17:00', 130000),
    (1, 2, 'WEEKEND', '17:00', '22:00', 180000);
END
GO

IF NOT EXISTS (SELECT 1 FROM FacilityPriceRule WHERE facility_id = 2)
BEGIN
INSERT INTO FacilityPriceRule (facility_id, court_type_id, day_type, start_time, end_time, price)
VALUES
    -- Facility 2, NORMAL court
    (2, 1, 'WEEKDAY', '06:00', '17:00', 55000),
    (2, 1, 'WEEKDAY', '17:00', '23:00', 85000),
    (2, 1, 'WEEKEND', '06:00', '17:00', 75000),
    (2, 1, 'WEEKEND', '17:00', '23:00', 110000),
    -- Facility 2, VIP court
    (2, 2, 'WEEKDAY', '06:00', '17:00', 90000),
    (2, 2, 'WEEKDAY', '17:00', '23:00', 140000),
    (2, 2, 'WEEKEND', '06:00', '17:00', 120000),
    (2, 2, 'WEEKEND', '17:00', '23:00', 170000);
END
GO

IF NOT EXISTS (SELECT 1 FROM FacilityPriceRule WHERE facility_id = 3)
BEGIN
INSERT INTO FacilityPriceRule (facility_id, court_type_id, day_type, start_time, end_time, price)
VALUES
    (3, 1, 'WEEKDAY', '06:00', '17:00', 50000),
    (3, 1, 'WEEKDAY', '17:00', '22:00', 80000),
    (3, 1, 'WEEKEND', '06:00', '17:00', 70000),
    (3, 1, 'WEEKEND', '17:00', '22:00', 100000);
END
GO

-- ============================================================
-- 7. INVENTORY (Vợt cho thuê)
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM Inventory WHERE name = N'Vợt Yonex Astrox 88D')
BEGIN
INSERT INTO Inventory (name, brand, description, rental_price, is_active)
VALUES
    (N'Vợt Yonex Astrox 88D', 'Yonex', N'Vợt tấn công chuyên nghiệp', 50000, 1),
    (N'Vợt Lining Windstorm 72', 'Lining', N'Vợt nhẹ, phù hợp phòng thủ', 40000, 1),
    (N'Vợt Victor Thruster K 9900', 'Victor', N'Vợt cao cấp', 60000, 1);
END
GO

-- Gán inventory vào facility 1 và 2
IF NOT EXISTS (SELECT 1 FROM FacilityInventory WHERE facility_id = 1)
BEGIN
INSERT INTO FacilityInventory (facility_id, inventory_id, total_quantity, available_quantity)
VALUES
    (1, 1, 10, 10),
    (1, 2, 8, 8),
    (1, 3, 5, 5);
END
GO

IF NOT EXISTS (SELECT 1 FROM FacilityInventory WHERE facility_id = 2)
BEGIN
INSERT INTO FacilityInventory (facility_id, inventory_id, total_quantity, available_quantity)
VALUES
    (2, 1, 6, 6),
    (2, 2, 4, 4);
END
GO

-- ============================================================
-- 8. BOOKINGS - Sử dụng NGÀY TƯƠNG ĐỐI
-- ============================================================

-- Xóa mock bookings cũ (nếu có) để tránh trùng lặp khi chạy lại
-- Chỉ xóa booking do test customer accounts tạo
DECLARE @testCustIds TABLE (id INT);
INSERT INTO @testCustIds
SELECT account_id FROM Account
WHERE email IN ('customer1@test.com','customer2@test.com','customer3@test.com',
                'customer4@test.com','customer5@test.com');

-- Xóa theo thứ tự FK: CourtSlotBooking → BookingSlot → Invoice → Review → Booking
DELETE csb FROM CourtSlotBooking csb
    INNER JOIN BookingSlot bs ON csb.booking_slot_id = bs.booking_slot_id
    INNER JOIN Booking b ON bs.booking_id = b.booking_id
WHERE b.account_id IN (SELECT id FROM @testCustIds);

DELETE FROM Review WHERE booking_id IN
                         (SELECT booking_id FROM Booking WHERE account_id IN (SELECT id FROM @testCustIds));

DELETE inv FROM Invoice inv
    INNER JOIN Booking b ON inv.booking_id = b.booking_id
WHERE b.account_id IN (SELECT id FROM @testCustIds);

DELETE bs FROM BookingSlot bs
    INNER JOIN Booking b ON bs.booking_id = b.booking_id
WHERE b.account_id IN (SELECT id FROM @testCustIds);

DELETE FROM Booking WHERE account_id IN (SELECT id FROM @testCustIds);

-- Xóa guest bookings cũ do test staff tạo
DECLARE @testStaffIds TABLE (id INT);
INSERT INTO @testStaffIds
SELECT s.staff_id FROM Staff s
                           INNER JOIN Account a ON s.account_id = a.account_id
WHERE a.email IN ('staff1@test.com','staff2@test.com');

DELETE csb FROM CourtSlotBooking csb
    INNER JOIN BookingSlot bs ON csb.booking_slot_id = bs.booking_slot_id
    INNER JOIN Booking b ON bs.booking_id = b.booking_id
WHERE b.staff_id IN (SELECT id FROM @testStaffIds);

DELETE inv FROM Invoice inv
    INNER JOIN Booking b ON inv.booking_id = b.booking_id
WHERE b.staff_id IN (SELECT id FROM @testStaffIds) AND b.guest_id IS NOT NULL;

DELETE bs FROM BookingSlot bs
    INNER JOIN Booking b ON bs.booking_id = b.booking_id
WHERE b.staff_id IN (SELECT id FROM @testStaffIds) AND b.guest_id IS NOT NULL;

DELETE FROM Booking
WHERE staff_id IN (SELECT id FROM @testStaffIds) AND guest_id IS NOT NULL;

DELETE FROM Guest WHERE guest_name IN (N'Đỗ Văn Khách Vãng Lai', N'Bùi Thị Walk-in');
GO

-- ============================================================
-- Bắt đầu insert mock bookings
-- ============================================================
DECLARE @today      DATE = CAST(GETDATE() AS DATE);
DECLARE @tomorrow   DATE = DATEADD(DAY, 1, @today);
DECLARE @dayAfter   DATE = DATEADD(DAY, 2, @today);
DECLARE @in3Days    DATE = DATEADD(DAY, 3, @today);
DECLARE @in5Days    DATE = DATEADD(DAY, 5, @today);
DECLARE @in7Days    DATE = DATEADD(DAY, 7, @today);
DECLARE @yesterday  DATE = DATEADD(DAY, -1, @today);
DECLARE @twoDaysAgo DATE = DATEADD(DAY, -2, @today);

DECLARE @cust1Id INT = (SELECT account_id FROM Account WHERE email = 'customer1@test.com');
DECLARE @cust2Id INT = (SELECT account_id FROM Account WHERE email = 'customer2@test.com');
DECLARE @cust3Id INT = (SELECT account_id FROM Account WHERE email = 'customer3@test.com');
DECLARE @cust4Id INT = (SELECT account_id FROM Account WHERE email = 'customer4@test.com');
DECLARE @cust5Id INT = (SELECT account_id FROM Account WHERE email = 'customer5@test.com');

DECLARE @staff1Id INT = (SELECT s.staff_id FROM Staff s
    INNER JOIN Account a ON s.account_id = a.account_id
    WHERE a.email = 'staff1@test.com');
DECLARE @staff2Id INT = (SELECT s.staff_id FROM Staff s
    INNER JOIN Account a ON s.account_id = a.account_id
    WHERE a.email = 'staff2@test.com');

-- ============================================================
-- BOOKING 1: Customer1, CONFIRMED, Ngày mai
-- Facility 1, Sân A1, slot 17:00-18:00 (slot_id 35,36)
-- ============================================================
INSERT INTO Booking (facility_id, booking_date, account_id, booking_status, created_at)
VALUES (1, @tomorrow, @cust1Id, 'CONFIRMED', GETDATE());

DECLARE @bk1 INT = SCOPE_IDENTITY();

INSERT INTO BookingSlot (booking_id, court_id, slot_id, price)
VALUES
    (@bk1, 1, 35, 90000),   -- Sân A1, 17:00-17:30
    (@bk1, 1, 36, 90000);   -- Sân A1, 17:30-18:00

INSERT INTO CourtSlotBooking (court_id, booking_date, slot_id, booking_slot_id)
SELECT 1, @tomorrow, slot_id, booking_slot_id
FROM BookingSlot WHERE booking_id = @bk1;

INSERT INTO Invoice (booking_id, total_amount, paid_amount, deposit_percent, payment_status)
VALUES (@bk1, 180000, 180000, 100, 'PAID');

-- ============================================================
-- BOOKING 2: Customer1, PENDING (đang giữ chỗ), Ngày kia
-- Facility 1, Sân A2, slot 18:00-19:00 (slot_id 37,38)
-- ============================================================
INSERT INTO Booking (facility_id, booking_date, account_id, booking_status, hold_expired_at, created_at)
VALUES (1, @dayAfter, @cust1Id, 'PENDING', DATEADD(MINUTE, 10, GETDATE()), GETDATE());

DECLARE @bk2 INT = SCOPE_IDENTITY();

INSERT INTO BookingSlot (booking_id, court_id, slot_id, price)
VALUES
    (@bk2, 2, 37, 90000),   -- Sân A2, 18:00-18:30
    (@bk2, 2, 38, 90000);   -- Sân A2, 18:30-19:00

INSERT INTO CourtSlotBooking (court_id, booking_date, slot_id, booking_slot_id)
SELECT 2, @dayAfter, slot_id, booking_slot_id
FROM BookingSlot WHERE booking_id = @bk2;

INSERT INTO Invoice (booking_id, total_amount, paid_amount, deposit_percent, payment_status)
VALUES (@bk2, 180000, 0, 100, 'UNPAID');

-- ============================================================
-- BOOKING 3: Customer1, COMPLETED (đã hoàn thành hôm qua)
-- Facility 1, Sân VIP 1, slot 17:00-18:00
-- ============================================================
INSERT INTO Booking (facility_id, booking_date, account_id, booking_status,
                     checkin_time, checkout_time, created_at)
VALUES (1, @yesterday, @cust1Id, 'COMPLETED',
        DATEADD(HOUR, 17, CAST(@yesterday AS DATETIME)),
        DATEADD(HOUR, 18, CAST(@yesterday AS DATETIME)),
        DATEADD(DAY, -2, GETDATE()));

DECLARE @bk3 INT = SCOPE_IDENTITY();

INSERT INTO BookingSlot (booking_id, court_id, slot_id, price, slot_status, checkin_time, checkout_time)
VALUES
    (@bk3, 3, 35, 150000, 'CHECK_OUT',
     DATEADD(HOUR, 17, CAST(@yesterday AS DATETIME)),
     DATEADD(MINUTE, 30, DATEADD(HOUR, 17, CAST(@yesterday AS DATETIME)))),
    (@bk3, 3, 36, 150000, 'CHECK_OUT',
     DATEADD(MINUTE, 30, DATEADD(HOUR, 17, CAST(@yesterday AS DATETIME))),
     DATEADD(HOUR, 18, CAST(@yesterday AS DATETIME)));

INSERT INTO Invoice (booking_id, total_amount, paid_amount, deposit_percent, payment_status)
VALUES (@bk3, 300000, 300000, 100, 'PAID');

-- ============================================================
-- BOOKING 4: Customer1, CANCELLED (2 ngày trước)
-- Facility 1, Sân A1
-- ============================================================
INSERT INTO Booking (facility_id, booking_date, account_id, booking_status, created_at)
VALUES (1, @twoDaysAgo, @cust1Id, 'CANCELLED', DATEADD(DAY, -3, GETDATE()));

DECLARE @bk4 INT = SCOPE_IDENTITY();

INSERT INTO BookingSlot (booking_id, court_id, slot_id, price, slot_status)
VALUES (@bk4, 1, 39, 90000, 'CANCELLED');

INSERT INTO Invoice (booking_id, total_amount, paid_amount, deposit_percent, payment_status)
VALUES (@bk4, 90000, 0, 100, 'UNPAID');

-- ============================================================
-- BOOKING 5: Customer2, CONFIRMED, Hôm nay
-- Facility 1, Sân VIP 1, slot 19:00-20:00 (slot_id 39,40)
-- ============================================================
INSERT INTO Booking (facility_id, booking_date, account_id, booking_status, created_at)
VALUES (1, @today, @cust2Id, 'CONFIRMED', DATEADD(HOUR, -3, GETDATE()));

DECLARE @bk5 INT = SCOPE_IDENTITY();

INSERT INTO BookingSlot (booking_id, court_id, slot_id, price)
VALUES
    (@bk5, 3, 39, 150000),  -- Sân VIP 1, 19:00-19:30
    (@bk5, 3, 40, 150000);  -- Sân VIP 1, 19:30-20:00

INSERT INTO CourtSlotBooking (court_id, booking_date, slot_id, booking_slot_id)
SELECT 3, @today, slot_id, booking_slot_id
FROM BookingSlot WHERE booking_id = @bk5;

INSERT INTO Invoice (booking_id, total_amount, paid_amount, deposit_percent, payment_status)
VALUES (@bk5, 300000, 300000, 100, 'PAID');

-- ============================================================
-- BOOKING 6: Customer3, CONFIRMED, 5 ngày sau
-- Facility 2, Sân B1, slot 17:00-19:00 (4 slot)
-- ============================================================
INSERT INTO Booking (facility_id, booking_date, account_id, booking_status, created_at)
VALUES (2, @in5Days, @cust3Id, 'CONFIRMED', GETDATE());

DECLARE @bk6 INT = SCOPE_IDENTITY();

INSERT INTO BookingSlot (booking_id, court_id, slot_id, price)
VALUES
    (@bk6, 5, 35, 85000),   -- Sân B1, 17:00-17:30
    (@bk6, 5, 36, 85000),   -- 17:30-18:00
    (@bk6, 5, 37, 85000),   -- 18:00-18:30
    (@bk6, 5, 38, 85000);   -- 18:30-19:00

INSERT INTO CourtSlotBooking (court_id, booking_date, slot_id, booking_slot_id)
SELECT 5, @in5Days, slot_id, booking_slot_id
FROM BookingSlot WHERE booking_id = @bk6;

INSERT INTO Invoice (booking_id, total_amount, paid_amount, deposit_percent, payment_status)
VALUES (@bk6, 340000, 340000, 100, 'PAID');

-- ============================================================
-- BOOKING 7: Customer4, CONFIRMED, 3 ngày sau
-- Facility 2, Sân VIP B, slot 18:00-19:00
-- ============================================================
INSERT INTO Booking (facility_id, booking_date, account_id, booking_status, created_at)
VALUES (2, @in3Days, @cust4Id, 'CONFIRMED', GETDATE());

DECLARE @bk7 INT = SCOPE_IDENTITY();

INSERT INTO BookingSlot (booking_id, court_id, slot_id, price)
VALUES
    (@bk7, 7, 37, 140000),  -- Sân VIP B, 18:00-18:30
    (@bk7, 7, 38, 140000);  -- 18:30-19:00

INSERT INTO CourtSlotBooking (court_id, booking_date, slot_id, booking_slot_id)
SELECT 7, @in3Days, slot_id, booking_slot_id
FROM BookingSlot WHERE booking_id = @bk7;

INSERT INTO Invoice (booking_id, total_amount, paid_amount, deposit_percent, payment_status)
VALUES (@bk7, 280000, 280000, 100, 'PAID');

-- ============================================================
-- BOOKING 8: Customer5, COMPLETED, hôm qua
-- Facility 3, Sân C1, slot 08:00-09:00
-- ============================================================
INSERT INTO Booking (facility_id, booking_date, account_id, booking_status,
                     checkin_time, checkout_time, created_at)
VALUES (3, @yesterday, @cust5Id, 'COMPLETED',
        DATEADD(HOUR, 8, CAST(@yesterday AS DATETIME)),
        DATEADD(HOUR, 9, CAST(@yesterday AS DATETIME)),
        DATEADD(DAY, -2, GETDATE()));

DECLARE @bk8 INT = SCOPE_IDENTITY();

INSERT INTO BookingSlot (booking_id, court_id, slot_id, price, slot_status, checkin_time, checkout_time)
VALUES
    (@bk8, 8, 17, 50000, 'CHECK_OUT',
     DATEADD(HOUR, 8, CAST(@yesterday AS DATETIME)),
     DATEADD(MINUTE, 30, DATEADD(HOUR, 8, CAST(@yesterday AS DATETIME)))),
    (@bk8, 8, 18, 50000, 'CHECK_OUT',
     DATEADD(MINUTE, 30, DATEADD(HOUR, 8, CAST(@yesterday AS DATETIME))),
     DATEADD(HOUR, 9, CAST(@yesterday AS DATETIME)));

INSERT INTO Invoice (booking_id, total_amount, paid_amount, deposit_percent, payment_status)
VALUES (@bk8, 100000, 100000, 100, 'PAID');

-- ============================================================
-- BOOKING 9: Staff1 đặt cho khách vãng lai (Guest), CONFIRMED, ngày mai
-- Facility 1, Sân A3
-- ============================================================
INSERT INTO Guest (guest_name, phone)
VALUES (N'Đỗ Văn Khách Vãng Lai', '0911111111');

DECLARE @guest1Id INT = SCOPE_IDENTITY();

INSERT INTO Booking (facility_id, booking_date, guest_id, staff_id, booking_status, created_at)
VALUES (1, @tomorrow, @guest1Id, @staff1Id, 'CONFIRMED', GETDATE());

DECLARE @bk9 INT = SCOPE_IDENTITY();

INSERT INTO BookingSlot (booking_id, court_id, slot_id, price)
VALUES
    (@bk9, 4, 35, 90000),   -- Sân A3, 17:00-17:30
    (@bk9, 4, 36, 90000);   -- 17:30-18:00

INSERT INTO CourtSlotBooking (court_id, booking_date, slot_id, booking_slot_id)
SELECT 4, @tomorrow, slot_id, booking_slot_id
FROM BookingSlot WHERE booking_id = @bk9;

INSERT INTO Invoice (booking_id, total_amount, paid_amount, deposit_percent, payment_status)
VALUES (@bk9, 180000, 180000, 100, 'PAID');

-- ============================================================
-- BOOKING 10: Staff2 đặt cho khách vãng lai (Guest), CONFIRMED, hôm nay
-- Facility 2, Sân B2
-- ============================================================
INSERT INTO Guest (guest_name, phone)
VALUES (N'Bùi Thị Walk-in', '0922222222');

DECLARE @guest2Id INT = SCOPE_IDENTITY();

INSERT INTO Booking (facility_id, booking_date, guest_id, staff_id, booking_status, created_at)
VALUES (2, @today, @guest2Id, @staff2Id, 'CONFIRMED', GETDATE());

DECLARE @bk10 INT = SCOPE_IDENTITY();

INSERT INTO BookingSlot (booking_id, court_id, slot_id, price)
VALUES
    (@bk10, 6, 39, 85000),  -- Sân B2, 19:00-19:30
    (@bk10, 6, 40, 85000);  -- 19:30-20:00

INSERT INTO CourtSlotBooking (court_id, booking_date, slot_id, booking_slot_id)
SELECT 6, @today, slot_id, booking_slot_id
FROM BookingSlot WHERE booking_id = @bk10;

INSERT INTO Invoice (booking_id, total_amount, paid_amount, deposit_percent, payment_status)
VALUES (@bk10, 170000, 0, 100, 'UNPAID');

-- ============================================================
-- BOOKING 11: Customer2, PENDING, 7 ngày sau (tuần sau)
-- Facility 1, Sân A1, slot 09:00-10:00
-- ============================================================
INSERT INTO Booking (facility_id, booking_date, account_id, booking_status, hold_expired_at, created_at)
VALUES (1, @in7Days, @cust2Id, 'PENDING', DATEADD(MINUTE, 10, GETDATE()), GETDATE());

DECLARE @bk11 INT = SCOPE_IDENTITY();

INSERT INTO BookingSlot (booking_id, court_id, slot_id, price)
VALUES
    (@bk11, 1, 19, 60000),  -- Sân A1, 09:00-09:30
    (@bk11, 1, 20, 60000);  -- 09:30-10:00

INSERT INTO CourtSlotBooking (court_id, booking_date, slot_id, booking_slot_id)
SELECT 1, @in7Days, slot_id, booking_slot_id
FROM BookingSlot WHERE booking_id = @bk11;

INSERT INTO Invoice (booking_id, total_amount, paid_amount, deposit_percent, payment_status)
VALUES (@bk11, 120000, 0, 100, 'UNPAID');

-- ============================================================
-- 9. REVIEWS cho các booking đã COMPLETED
-- ============================================================
-- Review cho Booking 3 (Customer1 - hôm qua)
IF NOT EXISTS (SELECT 1 FROM Review WHERE booking_id = @bk3)
BEGIN
INSERT INTO Review (booking_id, account_id, rating, comment, created_at)
VALUES (@bk3, @cust1Id, 5, N'Sân rất đẹp, dịch vụ tốt, sẽ quay lại!', GETDATE());
END

-- Review cho Booking 8 (Customer5 - hôm qua)
IF NOT EXISTS (SELECT 1 FROM Review WHERE booking_id = @bk8)
BEGIN
INSERT INTO Review (booking_id, account_id, rating, comment, created_at)
VALUES (@bk8, @cust5Id, 4, N'Sân ổn, giá hợp lý. Nhân viên thân thiện.', GETDATE());
END

-- ============================================================
-- 10. NOTIFICATIONS
-- ============================================================
INSERT INTO Notification (account_id, title, content, type, is_sent, created_at)
VALUES
    (@cust1Id, N'Xác nhận đặt sân',
     N'Đặt sân ngày mai tại CLB Cầu Lông Hà Đông Star đã được xác nhận.',
     'SYSTEM', 1, GETDATE()),

    (@cust1Id, N'Nhắc nhở lịch đặt sân',
     N'Bạn có lịch đặt sân vào ngày mai lúc 17:00 tại Sân A1.',
     'EMAIL', 0, GETDATE()),

    (@cust2Id, N'Xác nhận đặt sân',
     N'Đặt sân hôm nay tại CLB Cầu Lông Hà Đông Star đã được xác nhận.',
     'SYSTEM', 1, GETDATE()),

    (@cust3Id, N'Xác nhận đặt sân',
     N'Đặt sân tại CLB Cầu Lông Thanh Xuân Pro đã được xác nhận.',
     'SYSTEM', 1, GETDATE()),

    (@cust4Id, N'Thanh toán thành công',
     N'Bạn đã thanh toán thành công 280,000đ cho lịch đặt sân VIP.',
     'SYSTEM', 1, GETDATE());

-- ============================================================
-- 11. FAVORITE FACILITIES
-- ============================================================
IF NOT EXISTS (SELECT 1 FROM CustomerFavoriteFacility WHERE account_id = @cust1Id AND facility_id = 1)
BEGIN
INSERT INTO CustomerFavoriteFacility (account_id, facility_id) VALUES (@cust1Id, 1);
INSERT INTO CustomerFavoriteFacility (account_id, facility_id) VALUES (@cust1Id, 2);
END

IF NOT EXISTS (SELECT 1 FROM CustomerFavoriteFacility WHERE account_id = @cust2Id AND facility_id = 1)
BEGIN
INSERT INTO CustomerFavoriteFacility (account_id, facility_id) VALUES (@cust2Id, 1);
END

IF NOT EXISTS (SELECT 1 FROM CustomerFavoriteFacility WHERE account_id = @cust3Id AND facility_id = 2)
BEGIN
INSERT INTO CustomerFavoriteFacility (account_id, facility_id) VALUES (@cust3Id, 2);
INSERT INTO CustomerFavoriteFacility (account_id, facility_id) VALUES (@cust3Id, 3);
END

-- ============================================================
-- 12. PAYMENT cho các booking đã PAID
-- ============================================================
-- Payment cho Booking 1 (Customer1 - CONFIRMED ngày mai)
INSERT INTO Payment (invoice_id, transaction_code, paid_amount, payment_time,
                     payment_type, method, payment_status)
SELECT invoice_id, 'VNP' + CAST(@bk1 AS VARCHAR) + '001', 180000,
       DATEADD(HOUR, -1, GETDATE()), 'FULL', 'VNPAY', 'SUCCESS'
FROM Invoice WHERE booking_id = @bk1;

-- Payment cho Booking 5 (Customer2 - CONFIRMED hôm nay)
INSERT INTO Payment (invoice_id, transaction_code, paid_amount, payment_time,
                     payment_type, method, payment_status)
SELECT invoice_id, 'VNP' + CAST(@bk5 AS VARCHAR) + '002', 300000,
       DATEADD(HOUR, -3, GETDATE()), 'FULL', 'VNPAY', 'SUCCESS'
FROM Invoice WHERE booking_id = @bk5;

-- Payment cho Booking 3 (Customer1 - COMPLETED hôm qua, trả tiền mặt)
INSERT INTO Payment (invoice_id, paid_amount, payment_time,
                     payment_type, method, payment_status, staff_confirm_id, confirm_time)
SELECT invoice_id, 300000,
       DATEADD(HOUR, 17, CAST(@yesterday AS DATETIME)),
       'FULL', 'CASH', 'SUCCESS', @staff1Id,
       DATEADD(HOUR, 17, CAST(@yesterday AS DATETIME))
FROM Invoice WHERE booking_id = @bk3;

-- Payment cho Booking 9 (Guest - Staff1 tạo, tiền mặt)
INSERT INTO Payment (invoice_id, paid_amount, payment_time,
                     payment_type, method, payment_status, staff_confirm_id, confirm_time)
SELECT invoice_id, 180000, GETDATE(),
       'FULL', 'CASH', 'SUCCESS', @staff1Id, GETDATE()
FROM Invoice WHERE booking_id = @bk9;

-- Voucher
INSERT INTO Voucher (code,name,description,discount_type,discount_value,min_order_amount,max_discount_amount,valid_from,valid_to,usage_limit,per_user_limit,applicable_booking_type,is_active) VALUES (N'PROSTART',N'Voucher khởi đầu PRO',N'Giảm 20% cho người dùng mới','PERCENTAGE',20.00,0,NULL,GETDATE(),DATEADD(YEAR,5,GETDATE()),NULL,1,'BOTH',1);

-- ============================================================
PRINT N'';
PRINT N'============================================================';
PRINT N'✅ MOCK DATA ĐÃ ĐƯỢC TẠO THÀNH CÔNG!';
PRINT N'============================================================';
PRINT N'';
PRINT N'📋 TÀI KHOẢN (Password: 123456)';
PRINT N'──────────────────────────────────────────';
PRINT N'  ADMIN  : admin@test.com';
PRINT N'  OWNER  : owner@test.com';
PRINT N'  STAFF 1: staff1@test.com  → Facility 1 (CLB Hà Đông Star)';
PRINT N'  STAFF 2: staff2@test.com  → Facility 2 (CLB Thanh Xuân Pro)';
PRINT N'  CUST 1 : customer1@test.com';
PRINT N'  CUST 2 : customer2@test.com';
PRINT N'  CUST 3 : customer3@test.com';
PRINT N'  CUST 4 : customer4@test.com';
PRINT N'  CUST 5 : customer5@test.com';
PRINT N'';
PRINT N'📅 NGÀY TƯƠNG ĐỐI (tính từ hôm nay)';
PRINT N'──────────────────────────────────────────';
PRINT N'  Hôm nay    : ' + CAST(@today AS NVARCHAR);
PRINT N'  Ngày mai   : ' + CAST(@tomorrow AS NVARCHAR);
PRINT N'  Ngày kia   : ' + CAST(@dayAfter AS NVARCHAR);
PRINT N'  Hôm qua    : ' + CAST(@yesterday AS NVARCHAR);
PRINT N'';
PRINT N'📊 BOOKINGS';
PRINT N'──────────────────────────────────────────';
PRINT N'  BK1  : Cust1 CONFIRMED  ngày mai     (Sân A1)';
PRINT N'  BK2  : Cust1 PENDING    ngày kia     (Sân A2)';
PRINT N'  BK3  : Cust1 COMPLETED  hôm qua      (Sân VIP 1)';
PRINT N'  BK4  : Cust1 CANCELLED  2 ngày trước (Sân A1)';
PRINT N'  BK5  : Cust2 CONFIRMED  hôm nay      (Sân VIP 1)';
PRINT N'  BK6  : Cust3 CONFIRMED  5 ngày sau   (Sân B1)';
PRINT N'  BK7  : Cust4 CONFIRMED  3 ngày sau   (Sân VIP B)';
PRINT N'  BK8  : Cust5 COMPLETED  hôm qua      (Sân C1)';
PRINT N'  BK9  : Guest CONFIRMED  ngày mai     (Staff1 tạo)';
PRINT N'  BK10 : Guest CONFIRMED  hôm nay      (Staff2 tạo)';
PRINT N'  BK11 : Cust2 PENDING    7 ngày sau   (Sân A1)';
PRINT N'============================================================';
GO
 -- ============================================================
-- 13. Blog
-- ============================================================
-- 13. Blog
-- ============================================================
 USE [badminton_court_booking]
GO

-- BlogPost
SET IDENTITY_INSERT [dbo].[BlogPost] ON
INSERT [dbo].[BlogPost]
    ([post_id], [author_account_id], [title], [summary], [content], [status], [published_at], [created_at], [updated_at], [is_deleted])
VALUES
    (1, 1, N'Chào mừng đến với Cộng đồng', N'Bài viết demo để test danh sách/chi tiết.',
     N'Nội dung demo\n\n- Có comment\n- Có reaction\n- Có kiểm duyệt',
     N'PUBLISHED', CAST(N'2026-03-13T12:36:20.010' AS DateTime), CAST(N'2026-03-15T12:36:20.010' AS DateTime), NULL, 0),
    (2, 6, N'Cách bảo quản đồ', N'Rat gap can chu y',
     N'Việc xây dựng nội dung cho blog ("blog nội dung") là yếu tố cốt lõi...',
     N'PUBLISHED', CAST(N'2026-03-15T13:14:17.623' AS DateTime), CAST(N'2026-03-15T13:13:26.860' AS DateTime), CAST(N'2026-03-15T13:14:17.643' AS DateTime), 0)
SET IDENTITY_INSERT [dbo].[BlogPost] OFF
GO

-- BlogComment
SET IDENTITY_INSERT [dbo].[BlogComment] ON
INSERT [dbo].[BlogComment]
    ([comment_id], [post_id], [author_account_id], [content], [status], [moderated_by_account_id], [moderated_at], [created_at], [updated_at], [is_deleted])
VALUES
    (1, 1, 3, N'Bài viết hay quá!', N'APPROVED', 1, CAST(N'2026-03-15T12:36:20.013' AS DateTime), CAST(N'2026-03-15T12:36:20.013' AS DateTime), NULL, 0),
    (2, 1, 3, N'Khi nào có thêm bài mới ạ?', N'PENDING', NULL, NULL, CAST(N'2026-03-15T12:36:20.013' AS DateTime), NULL, 0),
    (3, 2, 6, N'Mình thấy bài viết rất hay', N'APPROVED', 6, CAST(N'2026-03-15T17:27:42.530' AS DateTime), CAST(N'2026-03-15T13:15:56.497' AS DateTime), CAST(N'2026-03-15T17:28:14.550' AS DateTime), 0)
SET IDENTITY_INSERT [dbo].[BlogComment] OFF
GO

-- BlogReaction
SET IDENTITY_INSERT [dbo].[BlogReaction] ON
INSERT [dbo].[BlogReaction]
    ([reaction_id], [post_id], [account_id], [emoji_code], [created_at])
VALUES
    (1, 1, 3, N'LIKE', CAST(N'2026-03-15T12:36:20.013' AS DateTime)),
    (2, 1, 1, N'HEART', CAST(N'2026-03-15T12:36:20.013' AS DateTime)),
    (3, 2, 6, N'LIKE', CAST(N'2026-03-15T13:15:10.287' AS DateTime))
SET IDENTITY_INSERT [dbo].[BlogReaction] OFF
GO