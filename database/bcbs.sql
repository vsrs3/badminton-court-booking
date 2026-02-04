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


-- Court
CREATE TABLE Court (
    court_id INT IDENTITY PRIMARY KEY,
    facility_id INT NOT NULL,
    court_type_id INT NOT NULL,
    court_name NVARCHAR(100),
    is_active BIT DEFAULT 1,

    FOREIGN KEY (facility_id) REFERENCES Facility(facility_id),
    FOREIGN KEY (court_type_id) REFERENCES CourtType(court_type_id)
);
GO


-- Timeslot
CREATE TABLE TimeSlot (
    slot_id INT IDENTITY PRIMARY KEY,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    CONSTRAINT CK_TimeSlot_1Hour
        CHECK (DATEDIFF(MINUTE, start_time, end_time) = 60)
);
GO


CREATE TABLE FacilityPriceRule (
    price_id INT IDENTITY PRIMARY KEY,

    facility_id INT NOT NULL,
    court_type_id INT NOT NULL,
    day_type VARCHAR(10)
        CHECK (day_type IN ('WEEKDAY','WEEKEND')) NOT NULL,
    slot_id INT NOT NULL,

    price DECIMAL(10,2) NOT NULL,

    FOREIGN KEY (facility_id) REFERENCES Facility(facility_id),
    FOREIGN KEY (court_type_id) REFERENCES CourtType(court_type_id),
    FOREIGN KEY (slot_id) REFERENCES TimeSlot(slot_id),

    UNIQUE (facility_id, court_type_id, day_type, slot_id)
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
    court_type_id INT NOT NULL,
    account_id INT NOT NULL,

    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20)
    CHECK (status IN ('ACTIVE','PAUSED','CANCELLED'))
    DEFAULT 'ACTIVE',
    created_at DATETIME DEFAULT GETDATE(),

    FOREIGN KEY (facility_id) REFERENCES Facility(facility_id),
    FOREIGN KEY (court_type_id) REFERENCES CourtType(court_type_id),
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
    court_id INT NOT NULL,
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
    FOREIGN KEY (court_id) REFERENCES Court(court_id),
    FOREIGN KEY (account_id) REFERENCES Account(account_id),
    FOREIGN KEY (guest_id) REFERENCES Guest(guest_id),
    FOREIGN KEY (staff_id) REFERENCES Staff(staff_id),

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
    slot_id INT NOT NULL,
    price DECIMAL(10,2) NOT NULL,

    FOREIGN KEY (booking_id) REFERENCES Booking(booking_id) ON DELETE CASCADE,
    FOREIGN KEY (slot_id) REFERENCES TimeSlot(slot_id),

    UNIQUE (booking_id, slot_id)
);
GO


-- CourtSlotBooking Avoid duplicate booking
CREATE TABLE CourtSlotBooking (
    court_id INT NOT NULL,
    booking_date DATE NOT NULL,
    slot_id INT NOT NULL,
    booking_id INT NOT NULL,

    PRIMARY KEY (court_id, booking_date, slot_id),

    FOREIGN KEY (court_id) REFERENCES Court(court_id),
    FOREIGN KEY (slot_id) REFERENCES TimeSlot(slot_id),
    FOREIGN KEY (booking_id) REFERENCES Booking(booking_id) ON DELETE CASCADE
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

    FOREIGN KEY (booking_id) REFERENCES Booking(booking_id),
    CHECK (deposit_percent BETWEEN 0 AND 100)

);
GO


-- Booking racket
CREATE TABLE RacketRental (
    racket_rental_id INT IDENTITY PRIMARY KEY,
    booking_id INT NOT NULL,
    inventory_id INT NOT NULL,

    quantity INT NOT NULL CHECK (quantity > 0),
    unit_price DECIMAL(10,2) NOT NULL,
    added_by VARCHAR(10)
        CHECK (added_by IN ('CUSTOMER','STAFF')),

    created_at DATETIME DEFAULT GETDATE(),

    FOREIGN KEY (booking_id) REFERENCES Booking(booking_id) ON DELETE CASCADE,
    FOREIGN KEY (inventory_id) REFERENCES Inventory(inventory_id),

    UNIQUE (booking_id, inventory_id)
);
GO

-- RACKET RENTAL LOG
CREATE TABLE RacketRentalLog (
    rental_id INT IDENTITY PRIMARY KEY,

    booking_id INT NOT NULL,
    facility_inventory_id INT NOT NULL,
    quantity INT NOT NULL,

    staff_id INT NOT NULL,
    rented_at DATETIME DEFAULT GETDATE(),
    returned_at DATETIME NULL,

    FOREIGN KEY (booking_id) REFERENCES Booking(booking_id),
    FOREIGN KEY (facility_inventory_id) REFERENCES FacilityInventory(facility_inventory_id),
    FOREIGN KEY (staff_id) REFERENCES Staff(staff_id)
);
GO


-- Payment
CREATE TABLE Payment (
    payment_id INT IDENTITY PRIMARY KEY,
    invoice_id INT NOT NULL,

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

-- Insert sample images (placeholders)
INSERT INTO FacilityImage (facility_id, image_path, is_thumbnail, created_at)
SELECT facility_id, 'assets/images/facilities/default-facility.jpg', 1, GETDATE()
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
        '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92',
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
        '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92',
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
        '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92',
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
        '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92',
        N'Phạm Thị Admin',
        '0904234567',
        'ADMIN',
        1,
        GETDATE()
    );
END

GO

SELECT email, full_name, role FROM Account ORDER BY role;



