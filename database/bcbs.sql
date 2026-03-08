USE master;
GO

IF EXISTS (SELECT name FROM sys.databases WHERE name = 'badminton_court_booking')
BEGIN
    ALTER DATABASE badminton_court_booking
    SET SINGLE_USER WITH ROLLBACK IMMEDIATE;

    DROP DATABASE badminton_court_booking;
END
GO

-- Create new database
CREATE DATABASE badminton_court_booking;
GO

-- Use the newly created database
USE badminton_court_booking;
GO
-- Account
CREATE TABLE Account (
                         account_id INT IDENTITY PRIMARY KEY,
                         email NVARCHAR(255) UNIQUE NULL,
                         password_hash NVARCHAR(255),
                         google_id NVARCHAR(255) NULL,
                         full_name NVARCHAR(255) NOT NULL,
                         phone NVARCHAR(20) UNIQUE NULL,
                         avatar_path NVARCHAR(500) NULL,
                         role VARCHAR(10)
                         CHECK (role IN ('ADMIN','OWNER','STAFF','CUSTOMER')) NOT NULL,
                         is_active BIT DEFAULT 1,
                         created_at DATETIME DEFAULT GETDATE()
);
GO
--Email Verification
CREATE TABLE [dbo].[EmailVerification](
[id] [int] IDENTITY(1,1) NOT NULL,
[email] [nvarchar](255) NOT NULL,
[password_hash] [nvarchar](255) NOT NULL,
[full_name] [nvarchar](255) NOT NULL,
[phone] [nvarchar](20) NULL,
[role] [varchar](10) NOT NULL,
    [token] [nvarchar](255) NOT NULL,
    [expire_at] [datetime] NOT NULL,
    [created_at] [datetime] NULL);
GO
-- Facility
CREATE TABLE Facility (
facility_id INT IDENTITY PRIMARY KEY,

 name NVARCHAR(255) NOT NULL,

 province NVARCHAR(100),
district NVARCHAR(100),
                          ward NVARCHAR(100),
                          address NVARCHAR(255) NOT NULL,

                          latitude DECIMAL(10, 8) NULL,
                          longitude DECIMAL(11, 8) NULL,

                          description NVARCHAR(MAX),
                          open_time TIME NOT NULL,
                          close_time TIME NOT NULL,

                          is_active BIT DEFAULT 1

);
GO

-- FacilityImage
CREATE TABLE FacilityImage (
                               image_id INT IDENTITY PRIMARY KEY,
                               facility_id INT NOT NULL,
                               image_path NVARCHAR(500) NOT NULL,
                               is_thumbnail BIT DEFAULT 0,
                               created_at DATETIME DEFAULT GETDATE(),
                               FOREIGN KEY (facility_id) REFERENCES Facility(facility_id) ON DELETE CASCADE
);
GO

CREATE UNIQUE INDEX UX_Facility_Thumbnail
    ON FacilityImage(facility_id)
    WHERE is_thumbnail = 1;


-- Staff
CREATE TABLE Staff (
                       staff_id INT IDENTITY PRIMARY KEY,
                       account_id INT UNIQUE NOT NULL,
                       facility_id INT NOT NULL,
                       is_active BIT DEFAULT 1,

                       FOREIGN KEY (account_id) REFERENCES Account(account_id),
                       FOREIGN KEY (facility_id) REFERENCES Facility(facility_id)
);
GO


-- Court type
CREATE TABLE CourtType (
                           court_type_id INT IDENTITY PRIMARY KEY,
                           type_code VARCHAR(20) UNIQUE, -- NORMAL, VIP
                           description NVARCHAR(255)
);
GO

INSERT INTO CourtType (type_code, description)
VALUES
    ('NORMAL', N'Sân tiêu chuẩn'),
    ('VIP',    N'Sân VIP chất lượng cao');




-- Court
CREATE TABLE Court (
                       court_id INT IDENTITY PRIMARY KEY,
                       facility_id INT NOT NULL,
                       court_type_id INT NOT NULL,
                       court_name NVARCHAR(100),
                       description NVARCHAR(500),
                       is_active BIT DEFAULT 1,

                       FOREIGN KEY (facility_id) REFERENCES Facility(facility_id),
                       FOREIGN KEY (court_type_id) REFERENCES CourtType(court_type_id)
);
GO


-- Timeslot
CREATE TABLE TimeSlot (
                          slot_id INT IDENTITY PRIMARY KEY,
                          start_time TIME NOT NULL,
                          end_time TIME NOT NULL

);
GO

INSERT INTO TimeSlot (start_time, end_time)
VALUES
-- 00:00 - 06:00 (12 slots)
('00:00', '00:30'),
('00:30', '01:00'),
('01:00', '01:30'),
('01:30', '02:00'),
('02:00', '02:30'),
('02:30', '03:00'),
('03:00', '03:30'),
('03:30', '04:00'),
('04:00', '04:30'),
('04:30', '05:00'),
('05:00', '05:30'),
('05:30', '06:00'),

-- 06:00 - 12:00 (12 slots)
('06:00', '06:30'),
('06:30', '07:00'),
('07:00', '07:30'),
('07:30', '08:00'),
('08:00', '08:30'),
('08:30', '09:00'),
('09:00', '09:30'),
('09:30', '10:00'),
('10:00', '10:30'),
('10:30', '11:00'),
('11:00', '11:30'),
('11:30', '12:00'),

-- 12:00 - 18:00 (12 slots)
('12:00', '12:30'),
('12:30', '13:00'),
('13:00', '13:30'),
('13:30', '14:00'),
('14:00', '14:30'),
('14:30', '15:00'),
('15:00', '15:30'),
('15:30', '16:00'),
('16:00', '16:30'),
('16:30', '17:00'),
('17:00', '17:30'),
('17:30', '18:00'),

-- 18:00 - 24:00 (12 slots)
('18:00', '18:30'),
('18:30', '19:00'),
('19:00', '19:30'),
('19:30', '20:00'),
('20:00', '20:30'),
('20:30', '21:00'),
('21:00', '21:30'),
('21:30', '22:00'),
('22:00', '22:30'),
('22:30', '23:00'),
('23:00', '23:30'),
('23:30', '23:59:59');


CREATE TABLE FacilityPriceRule (
                                   price_id INT IDENTITY PRIMARY KEY,

                                   facility_id INT NOT NULL,
                                   court_type_id INT NOT NULL,
                                   day_type VARCHAR(10)
                                       CHECK (day_type IN ('WEEKDAY','WEEKEND')) NOT NULL,

                                   start_time TIME NOT NULL,
                                   end_time TIME NOT NULL,

                                   price DECIMAL(10,2) NOT NULL, -- cho slot 30p backend se xu ly 1h

                                   FOREIGN KEY (facility_id) REFERENCES Facility(facility_id),
                                   FOREIGN KEY (court_type_id) REFERENCES CourtType(court_type_id),

                                   CHECK (end_time > start_time)
);
GO

-- Inventory
CREATE TABLE Inventory (
                           inventory_id INT IDENTITY PRIMARY KEY,
                           name NVARCHAR(255) NOT NULL,
                           brand NVARCHAR(100),
                           description NVARCHAR(500),
                           rental_price DECIMAL(10,2) NOT NULL, -- giá thuê/buổi

                           is_active BIT DEFAULT 1
);
GO


-- FACILITY INVENTORY
CREATE TABLE FacilityInventory (
                                   facility_inventory_id INT IDENTITY PRIMARY KEY,

                                   facility_id INT NOT NULL,
                                   inventory_id INT NOT NULL,

                                   total_quantity INT NOT NULL,
                                   available_quantity INT NOT NULL,

                                   FOREIGN KEY (facility_id) REFERENCES Facility(facility_id),
                                   FOREIGN KEY (inventory_id) REFERENCES Inventory(inventory_id),

                                   UNIQUE (facility_id, inventory_id),
                                   CHECK (available_quantity >= 0 AND available_quantity <= total_quantity)
);
GO


-- Recurring Booking
CREATE TABLE RecurringBooking (
                                  recurring_id INT IDENTITY PRIMARY KEY,

                                  facility_id INT NOT NULL,
                                  account_id INT NOT NULL,

                                  start_date DATE NOT NULL,
                                  end_date DATE NOT NULL,
                                  status VARCHAR(20)
                                      CHECK (status IN ('ACTIVE','PAUSED','CANCELLED'))
                                                      DEFAULT 'ACTIVE',
                                  created_at DATETIME DEFAULT GETDATE(),

                                  FOREIGN KEY (facility_id) REFERENCES Facility(facility_id),
                                  FOREIGN KEY (account_id) REFERENCES Account(account_id)
);
GO

-- Recurring Partern
CREATE TABLE RecurringPattern (
                                  pattern_id INT IDENTITY PRIMARY KEY,
                                  recurring_id INT NOT NULL,

                                  day_of_week INT CHECK (day_of_week BETWEEN 1 AND 7),
                                  slot_id INT NOT NULL,

                                  FOREIGN KEY (recurring_id) REFERENCES RecurringBooking(recurring_id) ON DELETE CASCADE,
                                  FOREIGN KEY (slot_id) REFERENCES TimeSlot(slot_id),

                                  UNIQUE (recurring_id, day_of_week, slot_id)
);
GO

-- Guest
CREATE TABLE Guest (
                       guest_id INT IDENTITY PRIMARY KEY,
                       guest_name NVARCHAR(255) NOT NULL,
                       phone NVARCHAR(20) NOT NULL
);
GO

-- Booking
CREATE TABLE Booking (
                         booking_id INT IDENTITY PRIMARY KEY,

                         recurring_id INT NULL,
                         facility_id INT NOT NULL, -- them de nhat quan, giam quer
                         booking_date DATE NOT NULL,

                         account_id INT NULL,     -- user online
                         guest_id INT NULL,       -- walk-in / phone
                         staff_id INT NULL,       -- staff created

                         booking_status VARCHAR(20)
                             CHECK (booking_status IN ('PENDING','CONFIRMED','EXPIRED','CANCELLED','COMPLETED'))
                                             DEFAULT 'PENDING',

                         hold_expired_at DATETIME NULL,   -- +10 minutes hold
                         checkin_time DATETIME NULL,
                         checkout_time DATETIME NULL,
                         created_at DATETIME DEFAULT GETDATE(),

                         FOREIGN KEY (recurring_id) REFERENCES RecurringBooking(recurring_id),
                         FOREIGN KEY (account_id) REFERENCES Account(account_id),
                         FOREIGN KEY (guest_id) REFERENCES Guest(guest_id),
                         FOREIGN KEY (staff_id) REFERENCES Staff(staff_id),
                         FOREIGN KEY (facility_id) REFERENCES Facility(facility_id),

                         CHECK (
                             -- khách tự đặt
                             (staff_id IS NULL AND account_id IS NOT NULL AND guest_id IS NULL)
                                 -- staff đặt cho guest
                                 OR (staff_id IS NOT NULL AND guest_id IS NOT NULL)
                                 -- staff đặt cho account
                                 OR (staff_id IS NOT NULL AND account_id IS NOT NULL)
                             )
);
GO

-- Booking slot
CREATE TABLE BookingSlot (
                             booking_slot_id INT IDENTITY PRIMARY KEY,
                             booking_id INT NOT NULL,
                             court_id INT NOT NULL,        -- FIX: gắn sân tại slot
                             slot_id INT NOT NULL,
                             price DECIMAL(10,2) NOT NULL,
                             slot_status VARCHAR(20)
                                 CHECK (slot_status IN ('PENDING','CHECKED_IN','CHECK_OUT','NO_SHOW','CANCELLED'))
                                 DEFAULT 'PENDING',
                             checkin_time DATETIME NULL,
                             checkout_time DATETIME NULL,

                             FOREIGN KEY (booking_id) REFERENCES Booking(booking_id) ON DELETE CASCADE,
                             FOREIGN KEY (court_id) REFERENCES Court(court_id),
                             FOREIGN KEY (slot_id) REFERENCES TimeSlot(slot_id),

                             UNIQUE (booking_id, court_id, slot_id)
);
GO


-- CourtSlotBooking Avoid duplicate booking
CREATE TABLE CourtSlotBooking (
                                  court_id INT NOT NULL,
                                  booking_date DATE NOT NULL,
                                  slot_id INT NOT NULL,
                                  booking_slot_id INT NOT NULL,

                                  PRIMARY KEY (court_id, booking_date, slot_id),

                                  FOREIGN KEY (court_id) REFERENCES Court(court_id),
                                  FOREIGN KEY (slot_id) REFERENCES TimeSlot(slot_id),
                                  FOREIGN KEY (booking_slot_id) REFERENCES BookingSlot(booking_slot_id)
);
GO



-- Booking ChangeLog
CREATE TABLE BookingChangeLog (
                                  change_id INT IDENTITY PRIMARY KEY,
                                  booking_id INT NOT NULL,

                                  old_court_id INT NULL,
                                  new_court_id INT NULL,

                                  old_slot_id INT NULL,
                                  new_slot_id INT NULL,

                                  old_booking_date DATE NULL,
                                  new_booking_date DATE NULL,

                                  change_type VARCHAR(20)
                                      CHECK (change_type IN ('CHANGE_COURT','CHANGE_SLOT','CHANGE_DATE','CHANGE_BOTH','CHANGE_ALL')),

                                  change_time DATETIME DEFAULT GETDATE(),
                                  note NVARCHAR(255),

                                  FOREIGN KEY (booking_id) REFERENCES Booking(booking_id)
);
GO

-- Skip booking
CREATE TABLE BookingSkip (
                             skip_id INT IDENTITY PRIMARY KEY,
                             recurring_id INT NOT NULL,
                             skip_date DATE NOT NULL,
                             reason NVARCHAR(100),

                             UNIQUE (recurring_id, skip_date),
                             FOREIGN KEY (recurring_id) REFERENCES RecurringBooking(recurring_id)
);
GO

-- Voucher
CREATE TABLE Voucher (
                         voucher_id INT IDENTITY(1,1) PRIMARY KEY,
                         code NVARCHAR(50) COLLATE SQL_Latin1_General_CP1_CS_AS UNIQUE NOT NULL,  -- mã voucher (case-sensitive)
                         name NVARCHAR(255) NOT NULL,
                         description NVARCHAR(500) NULL,

                         discount_type VARCHAR(20) NOT NULL
                             CHECK (discount_type IN ('PERCENTAGE', 'FIXED_AMOUNT')),
                         discount_value DECIMAL(10,2) NOT NULL,          -- 20.00 = 20% hoặc 50000 = 50k

                         min_order_amount DECIMAL(12,2) DEFAULT 0,       -- đơn tối thiểu (tính trên tiền sân)
                         max_discount_amount DECIMAL(12,2) NULL,         -- giới hạn giảm tối đa (chỉ dùng cho %)

                         valid_from DATETIME NOT NULL,                   -- thời gian bắt đầu áp dụng
                         valid_to DATETIME NOT NULL,                     -- thời gian kết thúc

                         usage_limit INT NULL,                           -- tổng số lần dùng toàn hệ thống (NULL = vô hạn)
                         per_user_limit INT DEFAULT 1,                   -- mỗi user dùng tối đa bao nhiêu lần

                         applicable_booking_type VARCHAR(20) NOT NULL
                                                        DEFAULT 'SINGLE'
                             CHECK (applicable_booking_type IN ('SINGLE', 'RECURRING', 'BOTH')),

                         is_active BIT DEFAULT 1,
                         created_at DATETIME DEFAULT GETDATE(),
                         updated_at DATETIME NULL
);
GO

-- Index quan trọng
CREATE UNIQUE INDEX UX_Voucher_Code ON Voucher(code);
CREATE INDEX IX_Voucher_ValidPeriod ON Voucher(valid_from, valid_to);

-- VoucherFacility nếu voucher_id không có record nào có thì áp dụng toàn bộ facility
CREATE TABLE VoucherFacility (
                                 voucher_id INT NOT NULL,
                                 facility_id INT NOT NULL,
                                 PRIMARY KEY (voucher_id, facility_id),
                                 FOREIGN KEY (voucher_id) REFERENCES Voucher(voucher_id) ON DELETE CASCADE,
                                 FOREIGN KEY (facility_id) REFERENCES Facility(facility_id) ON DELETE CASCADE
);
GO

-- VoucherAccount nếu voucher_id không có record nào thì áp dụng toàn bộ user
CREATE TABLE VoucherAccount (
                                voucher_id INT NOT NULL,
                                account_id INT NOT NULL,
                                PRIMARY KEY (voucher_id, account_id),
                                FOREIGN KEY (voucher_id) REFERENCES Voucher(voucher_id) ON DELETE CASCADE,
                                FOREIGN KEY (account_id) REFERENCES Account(account_id) ON DELETE CASCADE
);
GO

-- Invoice
CREATE TABLE Invoice (
                         invoice_id INT IDENTITY PRIMARY KEY,
                         booking_id INT UNIQUE NOT NULL,

                         total_amount DECIMAL(12,2) NOT NULL,
                         paid_amount DECIMAL(12,2) DEFAULT 0,
                         deposit_percent INT DEFAULT 100,


                         payment_status VARCHAR(20)
                             CHECK (payment_status IN ('UNPAID','PARTIAL','PAID'))
                                                   DEFAULT 'UNPAID',

                         created_at DATETIME DEFAULT GETDATE(),
                         voucher_id INT NULL, -- voucher applied (nếu có)
                         discount_amount DECIMAL(12,2) DEFAULT 0.00, -- so tien giam gia

                         FOREIGN KEY (booking_id) REFERENCES Booking(booking_id),
                         FOREIGN KEY (voucher_id) REFERENCES Voucher(voucher_id),
                         CHECK (deposit_percent BETWEEN 0 AND 100)

);
GO


-- Booking racket
CREATE TABLE RacketRental (
                              racket_rental_id INT IDENTITY PRIMARY KEY,
                              --booking_id INT NOT NULL,
                              booking_slot_id INT NOT NULL,  --  GẮN THEO SLOT
                              inventory_id INT NOT NULL,

                              quantity INT NOT NULL CHECK (quantity > 0),
                              unit_price DECIMAL(10,2) NOT NULL,
                              added_by VARCHAR(10)
                                  CHECK (added_by IN ('CUSTOMER','STAFF')),

                              created_at DATETIME DEFAULT GETDATE(),

                              FOREIGN KEY (booking_slot_id) REFERENCES BookingSlot(booking_slot_id) ON DELETE CASCADE,
                              FOREIGN KEY (inventory_id) REFERENCES Inventory(inventory_id),

                              UNIQUE (booking_slot_id, inventory_id)
);
GO

-- RACKET RENTAL LOG
CREATE TABLE RacketRentalLog (
                                 rental_id INT IDENTITY PRIMARY KEY,

                                 booking_slot_id INT NOT NULL,
                                 facility_inventory_id INT NOT NULL,
                                 quantity INT NOT NULL,

                                 staff_id INT NOT NULL,
                                 rented_at DATETIME DEFAULT GETDATE(),
                                 returned_at DATETIME NULL,

                                 FOREIGN KEY (booking_slot_id) REFERENCES BookingSlot(booking_slot_id),
                                 FOREIGN KEY (facility_inventory_id) REFERENCES FacilityInventory(facility_inventory_id),
                                 FOREIGN KEY (staff_id) REFERENCES Staff(staff_id)
);
GO


-- Payment
CREATE TABLE Payment (
                         payment_id INT IDENTITY PRIMARY KEY,
                         invoice_id INT NOT NULL,

                         vnpay_txn_no NVARCHAR(100) NULL,          -- VNPay transaction number (trả về)
                         vnpay_response_code VARCHAR(10) NULL,       -- VNPay response code
                         expire_at DATETIME NULL,                    -- thời hạn thanh toán
                         created_at DATETIME DEFAULT GETDATE(),      -- thời điểm tạo paymen

                         gateway VARCHAR(20) DEFAULT 'VNPAY',
                         transaction_code NVARCHAR(100),
                         paid_amount DECIMAL(12,2),
                         payment_time DATETIME,

                         payment_type VARCHAR(20)
                             CHECK (payment_type IN ('DEPOSIT','REMAINING','FULL')),

                         method VARCHAR(20)
                             CHECK (method IN ('VNPAY','CASH','BANK_TRANSFER')),

                         payment_status VARCHAR(20)
                             CHECK (payment_status IN ('SUCCESS','FAILED','PENDING')),

                         staff_confirm_id INT NULL,
                         confirm_time DATETIME NULL,

                         FOREIGN KEY (invoice_id) REFERENCES Invoice(invoice_id),
                         FOREIGN KEY (staff_confirm_id) REFERENCES Staff(staff_id)
);
GO

--
CREATE TABLE CourtScheduleException (
                                        exception_id INT IDENTITY(1,1) PRIMARY KEY,
                                        court_id     INT NOT NULL,
                                        facility_id  INT NOT NULL,

                                        start_date   DATE NOT NULL,
                                        end_date     DATE NOT NULL,

                                        start_time   TIME NULL,               -- nếu NULL thì đóng cả ngày
                                        end_time     TIME NULL,

                                        exception_type VARCHAR(20) CHECK (exception_type IN ('MAINTENANCE','EVENT','PRIVATE_USE','OTHER')),
                                        reason       NVARCHAR(300) NULL,

                                        created_by   INT NULL,
                                        created_at   DATETIME DEFAULT GETDATE(),
                                        updated_at   DATETIME NULL,
                                        is_active    BIT DEFAULT 1,

                                        FOREIGN KEY (court_id)    REFERENCES Court(court_id) ON DELETE CASCADE,
                                        FOREIGN KEY (facility_id) REFERENCES Facility(facility_id),
                                        FOREIGN KEY (created_by)  REFERENCES Staff(staff_id),

                                        CONSTRAINT CK_Exception_Dates
                                            CHECK (end_date >= start_date),

                                        CONSTRAINT CK_Exception_Times
                                            CHECK (
                                                (start_time IS NULL AND end_time IS NULL)
                                                    OR
                                                (start_time IS NOT NULL AND end_time IS NOT NULL AND end_time > start_time)
                                                )
);
GO

-- Review
CREATE TABLE Review (
                        review_id INT IDENTITY PRIMARY KEY,
                        booking_id INT UNIQUE NOT NULL,
                        account_id INT NOT NULL,
                        rating INT CHECK (rating BETWEEN 1 AND 5),
                        comment NVARCHAR(500),
                        created_at DATETIME DEFAULT GETDATE(),
                        FOREIGN KEY (booking_id) REFERENCES Booking(booking_id),
                        FOREIGN KEY (account_id) REFERENCES Account(account_id)
);
GO

-- Notice
CREATE TABLE Notification (
                              notification_id INT IDENTITY PRIMARY KEY,
                              account_id INT NOT NULL,
                              title NVARCHAR(255),
                              content NVARCHAR(500),
                              type VARCHAR(10) CHECK (type IN ('EMAIL','SYSTEM')),
                              is_sent BIT DEFAULT 0,
                              created_at DATETIME DEFAULT GETDATE(),
                              FOREIGN KEY (account_id) REFERENCES Account(account_id)
);
GO


-- Favorite
CREATE TABLE CustomerFavoriteFacility (
                                          favorite_id INT IDENTITY PRIMARY KEY,

                                          account_id  INT NOT NULL,
                                          facility_id INT NOT NULL,

                                          FOREIGN KEY (account_id)  REFERENCES Account(account_id) ON DELETE CASCADE,
                                          FOREIGN KEY (facility_id) REFERENCES Facility(facility_id) ON DELETE CASCADE,

                                          UNIQUE (account_id, facility_id)
);
GO

-- VoucherUsage luu thong tin dung voucher
CREATE TABLE VoucherUsage (
                              usage_id INT IDENTITY(1,1) PRIMARY KEY,
                              voucher_id INT NOT NULL,
                              account_id INT NULL,
                              booking_id INT NOT NULL,
                              invoice_id INT NOT NULL,
                              discount_amount DECIMAL(12,2) NOT NULL,     -- số tiền thực tế đã giảm
                              used_at DATETIME DEFAULT GETDATE(),

                              FOREIGN KEY (voucher_id) REFERENCES Voucher(voucher_id),
                              FOREIGN KEY (account_id) REFERENCES Account(account_id),
                              FOREIGN KEY (booking_id) REFERENCES Booking(booking_id),
                              FOREIGN KEY (invoice_id) REFERENCES Invoice(invoice_id)
);
GO

CREATE INDEX IX_VoucherUsage_Voucher ON VoucherUsage(voucher_id);
CREATE INDEX IX_VoucherUsage_Account ON VoucherUsage(account_id);

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

-- Insert sample images (placeholders)
INSERT INTO FacilityImage (facility_id, image_path, is_thumbnail, created_at)
SELECT facility_id, 'facility/default-facility.jpg', 1, GETDATE()
FROM Facility;
GO

USE badminton_court_booking;
GO

-- Create test accounts for all roles
-- (Password for all: 123456)

-- Test CUSTOMER
IF NOT EXISTS (SELECT 1 FROM Account WHERE email = 'customer@test.com')
BEGIN
INSERT INTO Account (email, password_hash, full_name, phone, role, is_active, created_at)
VALUES (
           'customer@test.com',
           '$2a$10$l9VhV2BupVyxxagpB243S.AuynE7hf7bSEVjFwaRl9KEO/IPQYFrO',
           N'Nguyễn Văn Customer',
           '0901234567',
           'CUSTOMER',
           1,
           GETDATE()
       );
END

-- Test STAFF
IF NOT EXISTS (SELECT 1 FROM Account WHERE email = 'staff@test.com')
BEGIN
INSERT INTO Account (email, password_hash, full_name, phone, role, is_active, created_at)
VALUES (
           'staff@test.com',
           '$2a$10$l9VhV2BupVyxxagpB243S.AuynE7hf7bSEVjFwaRl9KEO/IPQYFrO',
           N'Trần Thị Staff',
           '0902234567',
           'STAFF',
           1,
           GETDATE()
       );
END

-- Test OWNER
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

-- Test ADMIN (already exists, but create if not)
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

-- ============================================================
-- SAMPLE DATA: Court (sân) cho 5 facility đầu tiên
-- ============================================================
-- Facility 1: CLB Cầu Lông Hà Đông Star (3 sân NORMAL, 1 sân VIP)
INSERT INTO Court (facility_id, court_type_id, court_name, description, is_active)
VALUES
(1, 1, N'Sân A1', N'Sân tiêu chuẩn', 1),
(1, 1, N'Sân A2', N'Sân tiêu chuẩn', 1),
(1, 1, N'Sân A3', N'Sân tiêu chuẩn', 1),
(1, 2, N'Sân VIP 1', N'Sân VIP chất lượng cao', 1);

-- Facility 2: CLB Cầu Lông Thanh Xuân Pro
INSERT INTO Court (facility_id, court_type_id, court_name, description, is_active)
VALUES
(2, 1, N'Sân B1', N'Sân tiêu chuẩn', 1),
(2, 1, N'Sân B2', N'Sân tiêu chuẩn', 1),
(2, 2, N'Sân VIP 1', N'Sân VIP', 1);

-- Facility 3: CLB Cầu Lông Văn Quán
INSERT INTO Court (facility_id, court_type_id, court_name, description, is_active)
VALUES
(3, 1, N'Sân C1', N'Sân tiêu chuẩn', 1),
(3, 1, N'Sân C2', N'Sân tiêu chuẩn', 1);

-- Facility 4: CLB Cầu Lông Yên Nghĩa
INSERT INTO Court (facility_id, court_type_id, court_name, description, is_active)
VALUES
(4, 1, N'Sân D1', N'Sân tiêu chuẩn', 1),
(4, 1, N'Sân D2', N'Sân tiêu chuẩn', 1),
(4, 1, N'Sân D3', N'Sân tiêu chuẩn', 1);

-- Facility 5: CLB Cầu Lông Kiến Hưng
INSERT INTO Court (facility_id, court_type_id, court_name, description, is_active)
VALUES
(5, 1, N'Sân E1', N'Sân tiêu chuẩn', 1),
(5, 1, N'Sân E2', N'Sân tiêu chuẩn', 1),
(5, 2, N'Sân VIP 1', N'Sân VIP', 1);
GO

-- ============================================================
-- SAMPLE DATA: FacilityPriceRule
-- Giá mỗi 30 phút, chia theo khung giờ sáng/chiều/tối và WEEKDAY/WEEKEND
-- ============================================================

-- Facility 1 – NORMAL courts (court_type_id=1)
INSERT INTO FacilityPriceRule (facility_id, court_type_id, day_type, start_time, end_time, price)
VALUES
(1, 1, 'WEEKDAY', '05:30', '11:00', 60000),   -- Sáng
(1, 1, 'WEEKDAY', '11:00', '17:00', 50000),   -- Chiều
(1, 1, 'WEEKDAY', '17:00', '22:00', 75000),   -- Tối
(1, 1, 'WEEKEND', '05:30', '11:00', 75000),
(1, 1, 'WEEKEND', '11:00', '17:00', 65000),
(1, 1, 'WEEKEND', '17:00', '22:00', 90000);

-- Facility 1 – VIP courts (court_type_id=2)
INSERT INTO FacilityPriceRule (facility_id, court_type_id, day_type, start_time, end_time, price)
VALUES
(1, 2, 'WEEKDAY', '05:30', '11:00', 100000),
(1, 2, 'WEEKDAY', '11:00', '17:00', 90000),
(1, 2, 'WEEKDAY', '17:00', '22:00', 120000),
(1, 2, 'WEEKEND', '05:30', '11:00', 120000),
(1, 2, 'WEEKEND', '11:00', '17:00', 110000),
(1, 2, 'WEEKEND', '17:00', '22:00', 140000);

-- Facility 2 – NORMAL
INSERT INTO FacilityPriceRule (facility_id, court_type_id, day_type, start_time, end_time, price)
VALUES
(2, 1, 'WEEKDAY', '06:00', '11:00', 55000),
(2, 1, 'WEEKDAY', '11:00', '17:00', 45000),
(2, 1, 'WEEKDAY', '17:00', '23:00', 70000),
(2, 1, 'WEEKEND', '06:00', '11:00', 70000),
(2, 1, 'WEEKEND', '11:00', '17:00', 60000),
(2, 1, 'WEEKEND', '17:00', '23:00', 85000);

-- Facility 2 – VIP
INSERT INTO FacilityPriceRule (facility_id, court_type_id, day_type, start_time, end_time, price)
VALUES
(2, 2, 'WEEKDAY', '06:00', '11:00', 90000),
(2, 2, 'WEEKDAY', '11:00', '17:00', 80000),
(2, 2, 'WEEKDAY', '17:00', '23:00', 110000),
(2, 2, 'WEEKEND', '06:00', '11:00', 110000),
(2, 2, 'WEEKEND', '11:00', '17:00', 100000),
(2, 2, 'WEEKEND', '17:00', '23:00', 130000);

-- Facility 3 – NORMAL
INSERT INTO FacilityPriceRule (facility_id, court_type_id, day_type, start_time, end_time, price)
VALUES
(3, 1, 'WEEKDAY', '06:00', '17:00', 50000),
(3, 1, 'WEEKDAY', '17:00', '22:00', 65000),
(3, 1, 'WEEKEND', '06:00', '17:00', 65000),
(3, 1, 'WEEKEND', '17:00', '22:00', 80000);

-- Facility 4 – NORMAL
INSERT INTO FacilityPriceRule (facility_id, court_type_id, day_type, start_time, end_time, price)
VALUES
(4, 1, 'WEEKDAY', '05:00', '11:00', 55000),
(4, 1, 'WEEKDAY', '11:00', '17:00', 45000),
(4, 1, 'WEEKDAY', '17:00', '22:00', 70000),
(4, 1, 'WEEKEND', '05:00', '11:00', 70000),
(4, 1, 'WEEKEND', '11:00', '17:00', 60000),
(4, 1, 'WEEKEND', '17:00', '22:00', 85000);

-- Facility 5 – NORMAL (open 06:00 – 22:30)
INSERT INTO FacilityPriceRule (facility_id, court_type_id, day_type, start_time, end_time, price)
VALUES
(5, 1, 'WEEKDAY', '06:00', '11:00', 50000),
(5, 1, 'WEEKDAY', '11:00', '17:00', 45000),
(5, 1, 'WEEKDAY', '17:00', '22:30', 70000),
(5, 1, 'WEEKEND', '06:00', '11:00', 65000),
(5, 1, 'WEEKEND', '11:00', '17:00', 55000),
(5, 1, 'WEEKEND', '17:00', '22:30', 85000);

-- Facility 5 – VIP
INSERT INTO FacilityPriceRule (facility_id, court_type_id, day_type, start_time, end_time, price)
VALUES
(5, 2, 'WEEKDAY', '06:00', '11:00', 90000),
(5, 2, 'WEEKDAY', '11:00', '17:00', 80000),
(5, 2, 'WEEKDAY', '17:00', '22:30', 110000),
(5, 2, 'WEEKEND', '06:00', '11:00', 110000),
(5, 2, 'WEEKEND', '11:00', '17:00', 100000),
(5, 2, 'WEEKEND', '17:00', '22:30', 130000);
GO

