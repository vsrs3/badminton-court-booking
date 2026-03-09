package com.bcb.repository.impl;

import com.bcb.dto.staff.StaffFacilityHoursDTO;
import com.bcb.dto.staff.StaffPriceRuleDTO;
import com.bcb.dto.staff.StaffSlotPriceCourtDTO;
import com.bcb.dto.staff.StaffTimeSlotDTO;
import com.bcb.repository.staff.StaffSlotPriceRepository;
import com.bcb.utils.DBContext;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;

public class StaffSlotPriceRepositoryImpl implements StaffSlotPriceRepository {

    @Override
    public List<StaffSlotPriceCourtDTO> findActiveCourts(int facilityId) throws Exception {
        String sql = "SELECT court_id, court_type_id FROM Court WHERE facility_id = ? AND is_active = 1";
        List<StaffSlotPriceCourtDTO> courts = new ArrayList<>();

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facilityId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StaffSlotPriceCourtDTO court = new StaffSlotPriceCourtDTO();
                    court.setCourtId(rs.getInt("court_id"));
                    court.setCourtTypeId(rs.getInt("court_type_id"));
                    courts.add(court);
                }
            }
        }

        return courts;
    }

    @Override
    public StaffFacilityHoursDTO findFacilityHours(int facilityId) throws Exception {
        String sql = "SELECT open_time, close_time FROM Facility WHERE facility_id = ?";
        StaffFacilityHoursDTO hours = new StaffFacilityHoursDTO();

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facilityId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Time open = rs.getTime("open_time");
                    Time close = rs.getTime("close_time");
                    hours.setOpenTime(open != null ? open.toLocalTime().toString() : null);
                    hours.setCloseTime(close != null ? close.toLocalTime().toString() : null);
                }
            }
        }

        return hours;
    }

    @Override
    public List<StaffTimeSlotDTO> findTimeSlotsWithinHours(String openTime, String closeTime) throws Exception {
        String sql = "SELECT slot_id, start_time, end_time FROM TimeSlot " +
                "WHERE start_time >= CAST(? AS TIME) AND end_time <= CAST(? AS TIME) ORDER BY start_time";
        List<StaffTimeSlotDTO> slots = new ArrayList<>();

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, openTime);
            ps.setString(2, closeTime);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StaffTimeSlotDTO slot = new StaffTimeSlotDTO();
                    slot.setSlotId(rs.getInt("slot_id"));
                    slot.setStartTime(rs.getTime("start_time").toLocalTime());
                    slot.setEndTime(rs.getTime("end_time").toLocalTime());
                    slots.add(slot);
                }
            }
        }

        return slots;
    }

    @Override
    public List<StaffPriceRuleDTO> findPriceRules(int facilityId, String dayType) throws Exception {
        String sql = "SELECT court_type_id, start_time, end_time, price FROM FacilityPriceRule " +
                "WHERE facility_id = ? AND day_type = ? ORDER BY court_type_id, start_time";
        List<StaffPriceRuleDTO> rules = new ArrayList<>();

        try (Connection conn = DBContext.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, facilityId);
            ps.setString(2, dayType);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    StaffPriceRuleDTO rule = new StaffPriceRuleDTO();
                    rule.setCourtTypeId(rs.getInt("court_type_id"));
                    rule.setStartTime(rs.getTime("start_time").toLocalTime());
                    rule.setEndTime(rs.getTime("end_time").toLocalTime());
                    rule.setPrice(rs.getBigDecimal("price"));
                    rules.add(rule);
                }
            }
        }

        return rules;
    }
}

