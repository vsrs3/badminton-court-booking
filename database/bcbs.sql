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
        CHECK (role IN ('ADMIN','OWNER','STAFF','USER')) NOT NULL,
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
(N'CLB CẦU LÔNG TPT SPORT - LÀNG ĐẠI HỌC', N'Bình Dương', N'Dĩ An', N'Đông Hòa', N'Đường Tôn Thất Tùng', 10.8742, 106.8015, 
 N'Sân cầu lông chuyên nghiệp với đầy đủ tiện nghi', '06:00', '22:00', 1),

(N'CÂU LẠC BỘ CẦU LÔNG NGÔI SAO - QUẬN CẦU GIẤY', N'Hà Nội', N'Cầu Giấy', N'Dịch Vọng Hậu', N'Số 15 Duy Tân', 21.0321, 105.7834,
 N'Sân cầu lông tiêu chuẩn quốc tế', '06:00', '22:00', 1),

(N'SÂN CẦU LÔNG CHUYÊN NGHIỆP ĐỐNG ĐA', N'Hà Nội', N'Đống Đa', N'Cát Linh', N'Khu tập thể Giảng Võ', 21.0256, 105.8182,
 N'Sân tập luyện chất lượng cao', '06:00', '23:00', 1),

(N'HỌC VIỆN CẦU LÔNG QUẦN NGỰA BA ĐÌNH', N'Hà Nội', N'Ba Đình', N'Ngọc Hà', N'Cung Thể thao Quần Ngựa', 21.0401, 105.8189,
 N'Học viện đào tạo cầu lông chuyên nghiệp', '05:00', '22:30', 1),

(N'HÀ NỘI BADMINTON CLUB - CHI NHÁNH TÂY HỒ', N'Hà Nội', N'Tây Hồ', N'Quảng An', N'Số 221 Đường Trích Sài', 21.0583, 105.8112,
 N'CLB cầu lông hiện đại nhất Hà Nội', '06:00', '22:00', 1),

(N'SÂN CẦU LÔNG TIÊU CHUẨN THI ĐẤU XUÂN LA', N'Hà Nội', N'Tây Hồ', N'Xuân La', N'Phường Xuân La', 21.0665, 105.8051,
 N'Sân thi đấu đạt chuẩn quốc gia', '05:30', '21:30', 1);
GO

INSERT INTO Facility (name, province, district, ward, address, latitude, longitude, description, open_time, close_time, is_active)
VALUES 
(N'CLB CẦU LÔNG DQV1 - LÀNG ĐẠI HỌC', N'Hà Nội', N'Ba Đình', N'Kim Mã', N'Kim Mã', 20.9715285, 105.9067182, 
 N'Sân cầu lông chuyên nghiệp với đầy đủ tiện nghi', '06:00', '22:00', 1),

(N'CLB CẦU LÔNG DQV2 - LÀNG ĐẠI HỌC', N'Hà Nội', N'Ba Đình', N'Kim Mã', N'Kim Mã', 21.036825, 105.830958, 
 N'Sân cầu lông chuyên nghiệp với đầy đủ tiện nghi', '06:00', '22:00', 1),

(N'CLB CẦU LÔNG DQV3 - LÀNG ĐẠI HỌC', N'Hà Nội', N'Ba Đình', N'Kim Mã', N'Kim Mã', 21.0388916, 105.8209511, 
 N'Sân cầu lông chuyên nghiệp với đầy đủ tiện nghi', '06:00', '22:00', 1),

(N'CLB CẦU LÔNG DQV4 - LÀNG ĐẠI HỌC', N'Hà Nội', N'Ba Đình', N'Kim Mã', N' Kim Mã', 21.0199024, 105.8448429, 
 N'Sân cầu lông chuyên nghiệp với đầy đủ tiện nghi', '06:00', '22:00', 1)
GO

-- Insert sample images (placeholders)
INSERT INTO FacilityImage (facility_id, image_path, is_thumbnail, created_at)
SELECT facility_id, 'assets/images/facilities/default-facility.jpg', 1, GETDATE()
FROM Facility;
GO



