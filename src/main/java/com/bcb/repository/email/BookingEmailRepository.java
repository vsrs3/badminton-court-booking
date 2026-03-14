package com.bcb.repository.email;

import com.bcb.dto.email.BookingEmailHeaderDTO;
import com.bcb.dto.email.BookingEmailSlotDTO;
import com.bcb.dto.email.BookingRecipientDTO;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface BookingEmailRepository {
    BookingRecipientDTO findRecipient(int bookingId) throws Exception;

    BookingEmailHeaderDTO findHeader(int bookingId) throws Exception;

    List<BookingEmailSlotDTO> findSlots(int bookingId) throws Exception;

    Map<Integer, String> findCourtNames(Set<Integer> courtIds) throws Exception;

    Map<Integer, LocalTime[]> findSlotTimes(Set<Integer> slotIds) throws Exception;
}
