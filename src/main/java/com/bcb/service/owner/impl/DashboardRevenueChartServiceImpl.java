package com.bcb.service.owner.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.bcb.dto.owner.OwnerRevenueChartDTO;
import com.bcb.repository.owner.DashboardRevenueChartRepository;
import com.bcb.repository.owner.impl.DashboardRevenueChartRepositoryImpl;
import com.bcb.service.owner.DashboardRevenueChartService;
import com.google.gson.Gson;

public class DashboardRevenueChartServiceImpl implements DashboardRevenueChartService {

    private final DashboardRevenueChartRepository repo = new DashboardRevenueChartRepositoryImpl();
    private final Gson gson = new Gson();

    // Label chuẩn 
    private static final List<String> WEEK_LABELS  =
        Arrays.asList("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun");

    private static final List<String> MONTH_LABELS =
        Arrays.asList("Jan", "Feb", "Mar", "Apr", "May", "Jun",
                      "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");

    //Pad đủ labels, thiếu label nào thì data = 0 
    private OwnerRevenueChartDTO pad(OwnerRevenueChartDTO raw, List<String> fullLabels) {
        Map<String, BigDecimal> map = new LinkedHashMap<>();

        // Khởi tạo tất cả = 0
        for (String label : fullLabels) {
            map.put(label, BigDecimal.ZERO);
        }

        // Ghi đè bằng data thực tế từ DB
        List<String>     rawLabels = raw.getLabels();
        List<BigDecimal> rawData   = raw.getData();
        for (int i = 0; i < rawLabels.size(); i++) {
            String key = rawLabels.get(i);
            if (map.containsKey(key)) {
                map.put(key, rawData.get(i));
            }
        }

        return new OwnerRevenueChartDTO(
            new ArrayList<>(map.keySet()),
            new ArrayList<>(map.values())
        );
    }
    
    
    // Pad 5 năm trước (currentYear-4 to currentYear)
    private OwnerRevenueChartDTO padYearlyPast(OwnerRevenueChartDTO raw) {
        int currentYear = java.time.Year.now().getValue();
        List<String> yearLabels = new ArrayList<>();
        for (int y = currentYear - 4; y <= currentYear; y++) {
            yearLabels.add(String.valueOf(y));
        }
        return pad(raw, yearLabels);
    }

    // Pad 5 năm tới (currentYear to currentYear+4)
    private OwnerRevenueChartDTO padYearlyFuture(OwnerRevenueChartDTO raw) {
        int currentYear = java.time.Year.now().getValue();
        List<String> yearLabels = new ArrayList<>();
        for (int y = currentYear; y <= currentYear + 4; y++) {
            yearLabels.add(String.valueOf(y));
        }
        return pad(raw, yearLabels);
    }

    // Pad yearly trend — labels động theo 5 năm
	/*
	 * private OwnerRevenueChartDTO padYearly(OwnerRevenueChartDTO raw) { int
	 * currentYear = java.time.Year.now().getValue(); List<String> yearLabels = new
	 * ArrayList<>(); for (int y = currentYear; y <= currentYear + 4; y++) {
	 * yearLabels.add(String.valueOf(y)); } return pad(raw, yearLabels); }
	 */
    
    

    //  Pad monthly trend — 12 tháng 
	/*
	 * private OwnerRevenueChartDTO padTrendMonthly(OwnerRevenueChartDTO raw) {
	 * return pad(raw, MONTH_LABELS); }
	 */

    @Override
    public String getRevenueChartJson() {

        // Weekly — pad đủ 7 ngày
        Map<String, OwnerRevenueChartDTO> weekly = new LinkedHashMap<>();
        weekly.put("This Week",     pad(repo.getDailyRevenueThisWeek(),     WEEK_LABELS));
        weekly.put("Previous Week", pad(repo.getDailyRevenuePreviousWeek(), WEEK_LABELS));

        // Yearly bar — pad đủ 12 tháng
        Map<String, OwnerRevenueChartDTO> yearly = new LinkedHashMap<>();
        yearly.put("This Year",     pad(repo.getMonthlyRevenueThisYear(),     MONTH_LABELS));
        yearly.put("Previous Year", pad(repo.getMonthlyRevenuePreviousYear(), MONTH_LABELS));

        // Trend
        Map<String, OwnerRevenueChartDTO> trend = new LinkedHashMap<>();
        trend.put("Past 5 Years",   padYearlyPast(repo.getRevenueTrendYearlyPast()));
        trend.put("Next 5 Years",   padYearlyFuture(repo.getRevenueTrendYearlyFuture()));
        
        //trend.put("Monthly", padTrendMonthly(repo.getRevenueTrendMonthly())); // 12 tháng gần nhất
        //trend.put("Yearly",  padYearly(repo.getRevenueTrendYearly()));        // 5 năm gần nhất

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("weekly", weekly);
        result.put("yearly", yearly);
        result.put("trend",  trend);

        return gson.toJson(result);
    }
}