-- Drop database if exists
--IF EXISTS (SELECT name FROM sys.databases WHERE name = 'badminton_court_booking')
--BEGIN
--    ALTER DATABASE badminton_court_booking SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
--    DROP DATABASE badminton_court_booking;
--END
--GO

-- Create new database
CREATE DATABASE badminton_court_booking;
GO

-- Use the newly created database
USE badminton_court_booking;
GO

/* =========================
   1. ACCOUNT
   ========================= */
CREATE TABLE Account (
    account_id INT IDENTITY PRIMARY KEY,
    email NVARCHAR(255) UNIQUE NULL,
    password_hash NVARCHAR(255) NULL,
    google_id NVARCHAR(255) NULL,
    full_name NVARCHAR(255) NOT NULL,
    phone NVARCHAR(20) UNIQUE NULL,
    avatar_path NVARCHAR(500) NULL,
    role VARCHAR(10) CHECK (role IN ('OWNER','STAFF','USER')) NOT NULL,
    is_active BIT DEFAULT 1,
    created_at DATETIME DEFAULT GETDATE()
);
GO

/* =========================
   2. FACILITY
   ========================= */
CREATE TABLE Facility (
    facility_id INT IDENTITY PRIMARY KEY,
    name NVARCHAR(255) NOT NULL,

    province NVARCHAR(100),
    district NVARCHAR(100),
    ward NVARCHAR(100),
    address NVARCHAR(255) NOT NULL,

    description NVARCHAR(MAX),
    open_time TIME NOT NULL,
    close_time TIME NOT NULL,
    is_active BIT DEFAULT 1
);
GO

/* =========================
   3. FACILITY IMAGE
   ========================= */
CREATE TABLE FacilityImage (
    image_id INT IDENTITY PRIMARY KEY,
    facility_id INT NOT NULL,
    image_path NVARCHAR(500) NOT NULL,
    is_thumbnail BIT DEFAULT 0,
    created_at DATETIME DEFAULT GETDATE(),
    FOREIGN KEY (facility_id) REFERENCES Facility(facility_id) ON DELETE CASCADE
);
GO

/* =========================
   4. COURT
   ========================= */
CREATE TABLE Court (
    court_id INT IDENTITY PRIMARY KEY,
    facility_id INT NOT NULL,
    court_name NVARCHAR(100) NOT NULL,
    description NVARCHAR(MAX),
    is_active BIT DEFAULT 1,
    FOREIGN KEY (facility_id) REFERENCES Facility(facility_id) ON DELETE CASCADE
);
GO

/* =========================
   5. COURT PRICE (TIME RANGE)
   ========================= */
CREATE TABLE CourtPrice (
    price_id INT IDENTITY PRIMARY KEY,
    court_id INT NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    price_per_hour DECIMAL(10,2) NOT NULL,
    FOREIGN KEY (court_id) REFERENCES Court(court_id) ON DELETE CASCADE
);
GO

/* =========================
   6. STAFF
   ========================= */
CREATE TABLE Staff (
    staff_id INT IDENTITY PRIMARY KEY,
    account_id INT UNIQUE NOT NULL,
    is_active BIT DEFAULT 1,
    FOREIGN KEY (account_id) REFERENCES Account(account_id)
);
GO

/* =========================
   7. STAFF - COURT (ASSIGNMENT)
   ========================= */
CREATE TABLE StaffCourt (
    staff_id INT NOT NULL,
    court_id INT NOT NULL,
    PRIMARY KEY (staff_id, court_id),
    FOREIGN KEY (staff_id) REFERENCES Staff(staff_id) ON DELETE CASCADE,
    FOREIGN KEY (court_id) REFERENCES Court(court_id) ON DELETE CASCADE
);
GO

/* =========================
   8. GUEST
   ========================= */
CREATE TABLE Guest (
    guest_id INT IDENTITY PRIMARY KEY,
    guest_name NVARCHAR(255) NOT NULL,
    phone NVARCHAR(20) NOT NULL
);
GO

/* =========================
   9. BOOKING
   ========================= */
CREATE TABLE Booking (
    booking_id INT IDENTITY PRIMARY KEY,
    court_id INT NOT NULL,

    booking_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,

    booking_type VARCHAR(10)
        CHECK (booking_type IN ('SINGLE','RECURRING')) NOT NULL,

    account_id INT NULL,     -- user online
    guest_id INT NULL,       -- walk-in / phone
    staff_id INT NULL,       -- staff created

    booking_status VARCHAR(20)
        CHECK (booking_status IN ('PENDING','CONFIRMED','CANCELLED','EXPIRED','COMPLETED'))
        DEFAULT 'PENDING',

    payment_status VARCHAR(20)
        CHECK (payment_status IN ('UNPAID','DEPOSIT','PAID'))
        DEFAULT 'UNPAID',

    hold_expired_at DATETIME NULL,   -- +10 minutes hold
    checkin_time DATETIME NULL,
    checkout_time DATETIME NULL,
    created_at DATETIME DEFAULT GETDATE(),

    FOREIGN KEY (court_id) REFERENCES Court(court_id),
    FOREIGN KEY (account_id) REFERENCES Account(account_id),
    FOREIGN KEY (guest_id) REFERENCES Guest(guest_id),
    FOREIGN KEY (staff_id) REFERENCES Staff(staff_id),

    CHECK (
        (account_id IS NOT NULL AND guest_id IS NULL)
     OR (account_id IS NULL AND guest_id IS NOT NULL)
    )
);
GO

/* =========================
   10. RECURRING BOOKING
   ========================= */
CREATE TABLE RecurringBooking (
    recurring_id INT IDENTITY PRIMARY KEY,
    booking_id INT UNIQUE NOT NULL,
    day_of_week INT CHECK (day_of_week BETWEEN 1 AND 7),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    FOREIGN KEY (booking_id) REFERENCES Booking(booking_id) ON DELETE CASCADE
);
GO

/* =========================
   11. INVOICE
   ========================= */
CREATE TABLE Invoice (
    invoice_id INT IDENTITY PRIMARY KEY,
    booking_id INT UNIQUE NOT NULL,

    total_amount DECIMAL(12,2) NOT NULL,
    paid_amount DECIMAL(12,2) DEFAULT 0,
    remaining_amount AS (total_amount - paid_amount),

    payment_method VARCHAR(10)
        CHECK (payment_method IN ('CASH','QR','BANK')),

    issued_by_staff_id INT,
    issued_at DATETIME DEFAULT GETDATE(),

    FOREIGN KEY (booking_id) REFERENCES Booking(booking_id),
    FOREIGN KEY (issued_by_staff_id) REFERENCES Staff(staff_id)
);
GO

/* =========================
   12. REVIEW
   ========================= */
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

/* =========================
   13. NOTIFICATION
   ========================= */
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

/* =========================
   CREATE INDEXES FOR PERFORMANCE
   ========================= */

-- Account indexes
CREATE INDEX idx_account_email ON Account(email);
CREATE INDEX idx_account_phone ON Account(phone);
CREATE INDEX idx_account_role ON Account(role);
GO

-- Booking indexes
CREATE INDEX idx_booking_date ON Booking(booking_date);
CREATE INDEX idx_booking_court_date ON Booking(court_id, booking_date);
CREATE INDEX idx_booking_status ON Booking(booking_status);
CREATE INDEX idx_booking_account ON Booking(account_id);
GO

-- Court indexes
CREATE INDEX idx_court_facility ON Court(facility_id);
GO

-- Facility indexes
CREATE INDEX idx_facility_active ON Facility(is_active);
GO

-- Review indexes
CREATE INDEX idx_review_booking ON Review(booking_id);
GO

-- Notification indexes
CREATE INDEX idx_notification_account ON Notification(account_id);
CREATE INDEX idx_notification_sent ON Notification(is_sent);
GO
