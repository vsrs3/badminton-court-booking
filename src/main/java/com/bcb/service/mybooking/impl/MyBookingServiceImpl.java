package com.bcb.service.mybooking.impl;

import com.bcb.dto.mybooking.BookingSlotDetailDTO;
import com.bcb.dto.mybooking.MyBookingDetailDTO;
import com.bcb.dto.mybooking.MyBookingListDTO;
import com.bcb.exception.BusinessException;
import com.bcb.model.Notification;
import com.bcb.repository.mybooking.MyBookingRepository;
import com.bcb.repository.mybooking.NotificationRepository;
import com.bcb.repository.mybooking.impl.MyBookingRepositoryImpl;
import com.bcb.repository.mybooking.impl.NotificationRepositoryImpl;
import com.bcb.service.mybooking.MyBookingService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Implementation of {@link MyBookingService}.
 */
public class MyBookingServiceImpl implements MyBookingService {

    private static final DateTimeFormatter TF = DateTimeFormatter.ofPattern("HH:mm");

    private final MyBookingRepository bookingRepo;
    private final NotificationRepository notificationRepo;

    public MyBookingServiceImpl() {
        this.bookingRepo = new MyBookingRepositoryImpl();
        this.notificationRepo = new NotificationRepositoryImpl();
    }

    /** {@inheritDoc} */
    @Override
    public List<MyBookingListDTO> getMyBookings(int accountId, String status,
                                                 LocalDate dateFrom, LocalDate dateTo) {
        return bookingRepo.findMyBookings(accountId, status, dateFrom, dateTo);
    }

    /** {@inheritDoc} */
    @Override
    public MyBookingDetailDTO getBookingDetail(int bookingId, int accountId) throws BusinessException {
        MyBookingDetailDTO detail = bookingRepo.findBookingDetail(bookingId, accountId)
                .orElseThrow(() -> new BusinessException("NOT_FOUND",
                        "Booking not found or you don't have permission to view it."));

        // Load slot details
        List<BookingSlotDetailDTO> slots = bookingRepo.findSlotsByBookingId(bookingId);
        detail.setSlots(slots);

        // Calculate total hours from slots
        double totalHours = 0;
        for (BookingSlotDetailDTO slot : slots) {
            try {
                LocalTime start = LocalTime.parse(slot.getStartTime(), TF);
                LocalTime end = LocalTime.parse(slot.getEndTime(), TF);
                long minutes = java.time.Duration.between(start, end).toMinutes();
                totalHours += minutes / 60.0;
            } catch (Exception ignored) {
                // skip invalid time parsing
            }
        }
        detail.setTotalHours(totalHours);

        return detail;
    }

    /** {@inheritDoc} */
    @Override
    public void cancelBooking(int bookingId, int accountId) throws BusinessException {
        // Validate cancellable
        if (!bookingRepo.isCancellable(bookingId, accountId)) {
            throw new BusinessException("CANCEL_NOT_ALLOWED",
                    "Booking cannot be cancelled. It may already be paid, cancelled, or does not belong to you.");
        }

        // Perform cancellation (transactional)
        bookingRepo.cancelBooking(bookingId);

        // Send in-app notification
        Notification notification = new Notification();
        notification.setAccountId(accountId);
        notification.setTitle("Đặt sân đã bị hủy");
        notification.setContent("Booking #" + bookingId + " đã được hủy thành công.");
        notification.setType("SYSTEM");
        notification.setIsSent(false);
        notificationRepo.insertNotification(notification);
    }
}

