package com.bcb.service.owner.impl;

import com.bcb.dto.owner.OwnerRentalDeactivateResultDTO;
import com.bcb.dto.owner.OwnerRentalDetailsDTO;
import com.bcb.dto.owner.OwnerRentalFacilityOptionDTO;
import com.bcb.dto.owner.OwnerRentalPointDTO;
import com.bcb.dto.owner.OwnerRentalPurgeResultDTO;
import com.bcb.dto.owner.OwnerRentalReportSummaryDTO;
import com.bcb.repository.owner.OwnerRentalReportRepository;
import com.bcb.repository.owner.impl.OwnerRentalReportRepositoryImpl;
import com.bcb.service.owner.OwnerRentalReportService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class OwnerRentalReportServiceImpl implements OwnerRentalReportService {

    private final OwnerRentalReportRepository repository = new OwnerRentalReportRepositoryImpl();

    @Override
    public List<OwnerRentalFacilityOptionDTO> getFacilityOptions(String keyword) throws Exception {
        return repository.findFacilityOptions(keyword);
    }

    @Override
    public OwnerRentalReportSummaryDTO getSummary(
            Integer facilityId,
            Integer year,
            Integer month,
            Integer day,
            Integer inactiveMonth,
            String detailScope
    ) throws Exception {
        List<OwnerRentalFacilityOptionDTO> facilities = repository.findFacilityOptions(null);
        if (facilities.isEmpty()) {
            return new OwnerRentalReportSummaryDTO();
        }

        int resolvedFacilityId = resolveFacilityId(facilityId, facilities);
        int resolvedYear = resolveYear(resolvedFacilityId, year);
        int resolvedMonth = normalizeMonth(month);
        int resolvedInactiveMonth = normalizeMonth(inactiveMonth);

        String facilityName = repository.findFacilityName(resolvedFacilityId);
        List<OwnerRentalPointDTO> monthlyRevenue = padMonthPoints(repository.findMonthlyRevenue(resolvedFacilityId, resolvedYear));
        List<OwnerRentalPointDTO> dailyRevenue = padDayPoints(
                repository.findDailyRevenue(resolvedFacilityId, resolvedYear, resolvedMonth),
                YearMonth.of(resolvedYear, resolvedMonth)
        );

        int resolvedDay = resolveDay(day, dailyRevenue, resolvedYear, resolvedMonth);
        List<String> slotLabels = repository.findSlotLabels(resolvedFacilityId);
        List<OwnerRentalPointDTO> hourlyRevenue = padSlotPoints(
                slotLabels,
                repository.findHourlyRevenue(resolvedFacilityId, LocalDate.of(resolvedYear, resolvedMonth, resolvedDay))
        );

        OwnerRentalReportSummaryDTO summary = new OwnerRentalReportSummaryDTO();
        summary.setFacilityId(resolvedFacilityId);
        summary.setFacilityName(facilityName == null ? "" : facilityName);
        summary.setSelectedYear(resolvedYear);
        summary.setSelectedMonth(resolvedMonth);
        summary.setSelectedDay(resolvedDay);
        summary.setSelectedInactiveMonth(resolvedInactiveMonth);
        summary.setMonthlyRevenue(monthlyRevenue);
        summary.setDailyRevenue(dailyRevenue);
        summary.setHourlyRevenue(hourlyRevenue);
        summary.setTopItems(repository.findTopItems(resolvedFacilityId, resolvedYear, resolvedMonth));
        summary.setInactiveItems(repository.findInactiveItems(resolvedFacilityId, resolvedYear, resolvedInactiveMonth, 10));
        summary.setDetails(buildDetails(
                resolvedFacilityId,
                resolvedYear,
                resolvedMonth,
                resolvedDay,
                null,
                "day".equalsIgnoreCase(detailScope) ? "day" : "month"
        ));

        return summary;
    }

    @Override
    public OwnerRentalDetailsDTO getDetails(
            Integer facilityId,
            Integer year,
            Integer month,
            Integer day,
            String slotTime,
            String scope
    ) throws Exception {
        List<OwnerRentalFacilityOptionDTO> facilities = repository.findFacilityOptions(null);
        if (facilities.isEmpty()) {
            return new OwnerRentalDetailsDTO();
        }

        int resolvedFacilityId = resolveFacilityId(facilityId, facilities);
        int resolvedYear = resolveYear(resolvedFacilityId, year);
        int resolvedMonth = normalizeMonth(month);
        int resolvedDay = normalizeDay(day, resolvedYear, resolvedMonth);

        return buildDetails(resolvedFacilityId, resolvedYear, resolvedMonth, resolvedDay, slotTime, normalizeScope(scope));
    }

    @Override
    public OwnerRentalDeactivateResultDTO deactivateInactiveItems(Integer facilityId, Integer year, Integer month)
            throws Exception {
        List<OwnerRentalFacilityOptionDTO> facilities = repository.findFacilityOptions(null);
        if (facilities.isEmpty()) {
            return new OwnerRentalDeactivateResultDTO();
        }

        int resolvedFacilityId = resolveFacilityId(facilityId, facilities);
        int resolvedYear = resolveYear(resolvedFacilityId, year);
        int resolvedMonth = normalizeMonth(month);

        OwnerRentalDeactivateResultDTO result = new OwnerRentalDeactivateResultDTO();
        result.setFacilityId(resolvedFacilityId);
        result.setYear(resolvedYear);
        result.setMonth(resolvedMonth);
        result.setDeactivatedCount(repository.deactivateInactiveItems(resolvedFacilityId, resolvedYear, resolvedMonth, 10));
        return result;
    }

    @Override
    public OwnerRentalPurgeResultDTO purgeRentalData(java.time.LocalDateTime start, java.time.LocalDateTime end)
            throws Exception {
        return repository.purgeRentalData(start, end);
    }

    private OwnerRentalDetailsDTO buildDetails(
            int facilityId,
            int year,
            int month,
            int day,
            String slotTime,
            String scope
    ) throws Exception {
        OwnerRentalDetailsDTO details = new OwnerRentalDetailsDTO();
        details.setScope(scope);

        if ("hour".equals(scope)) {
            details.setTitle("Chi tiết doanh thu khung giờ " + slotTime + " ngày " + day + "/" + month + "/" + year);
            details.setRows(repository.findDetailRows(facilityId, year, month, day, slotTime));
            return details;
        }

        if ("day".equals(scope)) {
            details.setTitle("Chi tiết doanh thu ngày " + day + "/" + month + "/" + year);
            details.setRows(repository.findDetailRows(facilityId, year, month, day, null));
            return details;
        }

        details.setTitle("Chi tiết doanh thu tháng " + month + "/" + year);
        details.setRows(repository.findDetailRows(facilityId, year, month, null, null));
        return details;
    }

    private List<OwnerRentalPointDTO> padMonthPoints(List<OwnerRentalPointDTO> rawPoints) {
        Map<Integer, BigDecimal> rawMap = new LinkedHashMap<>();
        for (OwnerRentalPointDTO rawPoint : rawPoints) {
            rawMap.put(rawPoint.getIndex(), safe(rawPoint.getRevenue()));
        }

        List<OwnerRentalPointDTO> points = new ArrayList<>();
        for (int month = 1; month <= 12; month++) {
            OwnerRentalPointDTO point = new OwnerRentalPointDTO();
            point.setIndex(month);
            point.setKey(String.valueOf(month));
            point.setLabel("Tháng " + month);
            point.setRevenue(rawMap.getOrDefault(month, BigDecimal.ZERO));
            points.add(point);
        }
        return points;
    }

    private List<OwnerRentalPointDTO> padDayPoints(List<OwnerRentalPointDTO> rawPoints, YearMonth yearMonth) {
        Map<Integer, BigDecimal> rawMap = new LinkedHashMap<>();
        for (OwnerRentalPointDTO rawPoint : rawPoints) {
            rawMap.put(rawPoint.getIndex(), safe(rawPoint.getRevenue()));
        }

        List<OwnerRentalPointDTO> points = new ArrayList<>();
        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
            OwnerRentalPointDTO point = new OwnerRentalPointDTO();
            point.setIndex(day);
            point.setKey(String.valueOf(day));
            point.setLabel(String.format("%02d", day));
            point.setRevenue(rawMap.getOrDefault(day, BigDecimal.ZERO));
            points.add(point);
        }
        return points;
    }

    private List<OwnerRentalPointDTO> padSlotPoints(List<String> slotLabels, List<OwnerRentalPointDTO> rawPoints) {
        Map<String, BigDecimal> rawMap = new LinkedHashMap<>();
        for (OwnerRentalPointDTO rawPoint : rawPoints) {
            rawMap.put(rawPoint.getKey(), safe(rawPoint.getRevenue()));
        }

        List<OwnerRentalPointDTO> points = new ArrayList<>();
        int index = 1;
        for (String slotLabel : slotLabels) {
            OwnerRentalPointDTO point = new OwnerRentalPointDTO();
            point.setIndex(index++);
            point.setKey(slotLabel);
            point.setLabel(slotLabel);
            point.setRevenue(rawMap.getOrDefault(slotLabel, BigDecimal.ZERO));
            points.add(point);
        }
        return points;
    }

    private BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private int resolveFacilityId(Integer facilityId, List<OwnerRentalFacilityOptionDTO> facilities) {
        if (facilityId != null) {
            for (OwnerRentalFacilityOptionDTO facility : facilities) {
                if (facility.getFacilityId() == facilityId) {
                    return facilityId;
                }
            }
        }
        return facilities.get(0).getFacilityId();
    }

    private int resolveYear(int facilityId, Integer year) throws Exception {
        if (year != null && year > 0) {
            return year;
        }

        Integer latestRentalYear = repository.findLatestRentalYear(facilityId);
        if (latestRentalYear != null && latestRentalYear > 0) {
            return latestRentalYear;
        }

        return Year.now().getValue();
    }

    private int normalizeMonth(Integer month) {
        if (month == null || month < 1 || month > 12) {
            return 1;
        }
        return month;
    }

    private int resolveDay(Integer day, List<OwnerRentalPointDTO> dailyRevenue, int year, int month) {
        if (day != null && day >= 1 && day <= YearMonth.of(year, month).lengthOfMonth()) {
            return day;
        }

        for (OwnerRentalPointDTO point : dailyRevenue) {
            if (point.getRevenue() != null && point.getRevenue().compareTo(BigDecimal.ZERO) > 0) {
                return point.getIndex();
            }
        }

        return 1;
    }

    private int normalizeDay(Integer day, int year, int month) {
        if (day == null) {
            return 1;
        }

        int maxDay = YearMonth.of(year, month).lengthOfMonth();
        if (day < 1) {
            return 1;
        }
        if (day > maxDay) {
            return maxDay;
        }
        return day;
    }

    private String normalizeScope(String scope) {
        if ("hour".equalsIgnoreCase(scope)) {
            return "hour";
        }
        if ("day".equalsIgnoreCase(scope)) {
            return "day";
        }
        return "month";
    }
}
