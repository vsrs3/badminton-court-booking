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

-- Email Verification
CREATE TABLE [dbo].[EmailVerification](
    [id] [int] IDENTITY(1,1) NOT NULL,
    [email] [nvarchar](255) NOT NULL,
    [password_hash] [nvarchar](255) NOT NULL,
    [full_name] [nvarchar](255) NOT NULL,
    [phone] [nvarchar](20) NULL,
    [role] [varchar](10) NOT NULL,
    [token] [nvarchar](255) NOT NULL,
    [expire_at] [datetime] NOT NULL,
    [created_at] [datetime] NULL
);
GO

CREATE TABLE [dbo].[PasswordResetToken](
    [id] [int] IDENTITY(1,1) NOT NULL,
    [email] [nvarchar](255) NOT NULL,
    [token] [nvarchar](255) NOT NULL,
    [expire_at] [datetime] NOT NULL,
    [created_at] [datetime] NOT NULL CONSTRAINT [DF_PasswordResetToken_CreatedAt] DEFAULT (GETDATE())
);
GO

CREATE UNIQUE INDEX [UX_PasswordResetToken_Token]
    ON [dbo].[PasswordResetToken]([token]);
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

INSERT INTO CourtType (type_code, description)
VALUES
    ('NORMAL', N'Sân tiêu chuẩn'),
    ('VIP',    N'Sân VIP chất lượng cao');
GO

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
GO

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
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    status VARCHAR(20)
        CHECK (status IN ('ACTIVE','PAUSED','CANCELLED'))
        DEFAULT 'ACTIVE',
    created_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (facility_id) REFERENCES Facility(facility_id)
);
GO

-- Recurring Pattern
CREATE TABLE RecurringPattern (
    pattern_id INT IDENTITY PRIMARY KEY,
    recurring_id INT NOT NULL,
    court_id INT NOT NULL,
    day_of_week INT CHECK (day_of_week BETWEEN 1 AND 7),
    slot_id INT NOT NULL,
    FOREIGN KEY (recurring_id) REFERENCES RecurringBooking(recurring_id) ON DELETE CASCADE,
    FOREIGN KEY (slot_id) REFERENCES TimeSlot(slot_id),
    foreign key (court_id) references Court(court_id),
    UNIQUE (recurring_id, day_of_week, slot_id)
);
GO

-- Guest
CREATE TABLE Guest (
    guest_id INT IDENTITY PRIMARY KEY,
    guest_name NVARCHAR(255) NOT NULL,
    phone NVARCHAR(20) NOT NULL,
    email NVARCHAR(255) NULL
);
GO

-- Booking
CREATE TABLE Booking (
    booking_id INT IDENTITY PRIMARY KEY,

    recurring_id INT NULL,
    facility_id INT NOT NULL, -- them de nhat quan, giam quer
    booking_date DATE NULL,

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
        (staff_id IS NULL AND account_id IS NOT NULL AND guest_id IS NULL)
        OR (staff_id IS NOT NULL AND guest_id IS NOT NULL)
        OR (staff_id IS NOT NULL AND account_id IS NOT NULL)
    )
);
GO

-- Booking slot (UPDATED to include is_released with DEFAULT 0 and NOT NULL)
CREATE TABLE BookingSlot (
    booking_slot_id INT IDENTITY PRIMARY KEY,
    booking_id INT NOT NULL,
    court_id INT NOT NULL,        -- FIX: gắn sân tại slot
    booking_date DATE NULL,
    slot_id INT NOT NULL,
    price DECIMAL(10,2) NOT NULL,

    is_released BIT NOT NULL CONSTRAINT DF_BookingSlot_IsReleased DEFAULT (0),

    slot_status VARCHAR(20)
        CHECK (slot_status IN ('PENDING','CHECKED_IN','CHECK_OUT','NO_SHOW','CANCELLED'))
        DEFAULT 'PENDING',
    checkin_time DATETIME NULL,
    checkout_time DATETIME NULL,

    FOREIGN KEY (booking_id) REFERENCES Booking(booking_id) ON DELETE CASCADE,
    FOREIGN KEY (court_id) REFERENCES Court(court_id),
    FOREIGN KEY (slot_id) REFERENCES TimeSlot(slot_id),

    UNIQUE (booking_id, booking_date,court_id, slot_id)
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

    actor_staff_id INT NULL,
    change_action VARCHAR(30) NULL,
    before_data NVARCHAR(MAX) NULL,
    after_data NVARCHAR(MAX) NULL,
    reason NVARCHAR(500) NULL,
    etag_before VARCHAR(64) NULL,
    etag_after VARCHAR(64) NULL,
    refund_due DECIMAL(12,2) NULL,

    FOREIGN KEY (booking_id) REFERENCES Booking(booking_id),
    FOREIGN KEY (actor_staff_id) REFERENCES Staff(staff_id)
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

    refund_due DECIMAL(12,2) NOT NULL CONSTRAINT DF_Invoice_RefundDue DEFAULT (0),
    refund_status VARCHAR(20) NOT NULL CONSTRAINT DF_Invoice_RefundStatus DEFAULT ('NONE')
        CONSTRAINT CK_Invoice_RefundStatus CHECK (refund_status IN ('NONE','PENDING_MANUAL','REFUNDED')),
    refund_note NVARCHAR(500) NULL,

    voucher_id INT NULL, -- voucher applied (nếu có)
    discount_amount DECIMAL(12,2) DEFAULT 0.00, -- so tien giam gia
    created_at DATETIME DEFAULT GETDATE(),

    FOREIGN KEY (booking_id) REFERENCES Booking(booking_id),
    FOREIGN KEY (voucher_id) REFERENCES Voucher(voucher_id),
    CHECK (deposit_percent BETWEEN 0 AND 100)

);
GO

-- Booking racket
CREATE TABLE RacketRental (
    racket_rental_id INT IDENTITY PRIMARY KEY,
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

CREATE TABLE InventoryRentalSchedule (
    id INT IDENTITY(1,1) PRIMARY KEY,
    facility_id INT NOT NULL,
    booking_date DATE NOT NULL,
    court_id INT NOT NULL,
    slot_id INT NOT NULL,
    inventory_id INT NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    availableItem INT NOT NULL CHECK (availableItem >= 0),
    status NVARCHAR(20) NOT NULL
        CHECK (status IN (N'RENTED', N'RENTING', N'RETURNED')),
    created_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (facility_id) REFERENCES Facility(facility_id),
    FOREIGN KEY (court_id) REFERENCES Court(court_id),
    FOREIGN KEY (slot_id) REFERENCES TimeSlot(slot_id),
    FOREIGN KEY (inventory_id) REFERENCES Inventory(inventory_id),
    UNIQUE (facility_id, booking_date, court_id, slot_id, inventory_id)
);
GO

-- Payment
CREATE TABLE Payment (
    payment_id INT IDENTITY PRIMARY KEY,
    invoice_id INT NOT NULL,

    vnpay_txn_no NVARCHAR(100) NULL,          -- VNPay transaction number (trả về)
    vnpay_response_code VARCHAR(10) NULL,     -- VNPay response code
    expire_at DATETIME NULL,                  -- thời hạn thanh toán
    created_at DATETIME DEFAULT GETDATE(),    -- thời điểm tạo payment

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

-- Email Queue (for async email sending)
CREATE TABLE EmailQueue (
    email_id INT IDENTITY PRIMARY KEY,
    email_type VARCHAR(20) NOT NULL
        CONSTRAINT CK_EmailQueue_EmailType CHECK (email_type IN (
            'CREATE','CREATE_RECURRING','UPDATE','CANCEL',
            'REMINDER_UPCOMING_24H','REMINDER_UPCOMING_2H','REMINDER_PAYMENT_12H'
        )),
    booking_id INT NOT NULL,
    to_email NVARCHAR(255) NOT NULL,
    payload_json NVARCHAR(MAX) NULL,
    reminder_at DATETIME NULL,
    status VARCHAR(20) NOT NULL
        CONSTRAINT CK_EmailQueue_Status CHECK (status IN ('PENDING','SENDING','SENT','FAILED'))
        DEFAULT 'PENDING',
    retry_count INT NOT NULL DEFAULT 0,
    next_attempt_at DATETIME NOT NULL DEFAULT GETDATE(),
    last_error NVARCHAR(500) NULL,
    created_at DATETIME NOT NULL DEFAULT GETDATE(),
    sent_at DATETIME NULL,
    FOREIGN KEY (booking_id) REFERENCES Booking(booking_id)
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
