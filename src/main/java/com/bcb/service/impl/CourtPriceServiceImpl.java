package com.bcb.service.impl;

import com.bcb.exception.BusinessException;
import com.bcb.model.CourtPrice;
import com.bcb.repository.CourtPriceRepository;
import com.bcb.repository.impl.CourtPriceRepositoryImpl;
import com.bcb.service.CourtPriceService;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class CourtPriceServiceImpl implements CourtPriceService {

    private final CourtPriceRepository courtPriceRepository;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public CourtPriceServiceImpl() {
        this.courtPriceRepository = new CourtPriceRepositoryImpl();
    }

    @Override
    public List<CourtPrice> getPricesByCourt(int courtId) {
        List<CourtPrice> prices = courtPriceRepository.findByCourtId(courtId);
        prices.forEach(this::prepareFormattedTime);
        return prices;
    }

    @Override
    public CourtPrice getPriceById(int priceId) throws BusinessException {
        CourtPrice price = courtPriceRepository.findById(priceId)
                .orElseThrow(() -> new BusinessException("Court price not found"));
        prepareFormattedTime(price);
        return price;
    }

    @Override
    public int createPrice(CourtPrice price) throws BusinessException {
        validate(price);
        return courtPriceRepository.insert(price);
    }

    @Override
    public void updatePrice(CourtPrice price) throws BusinessException {
        validate(price);
        int rows = courtPriceRepository.update(price);
        if (rows == 0) {
            throw new BusinessException("Update failed. Court price not found.");
        }
    }

    @Override
    public void deletePrice(int priceId) throws BusinessException {
        int rows = courtPriceRepository.delete(priceId);
        if (rows == 0) {
            throw new BusinessException("Delete failed. Court price not found.");
        }
    }

    /* ================== helper ================== */

    private void prepareFormattedTime(CourtPrice price) {
        if (price.getStartTime() != null) {
            price.setStartTimeFormatted(price.getStartTime().format(TIME_FORMATTER));
        }
        if (price.getEndTime() != null) {
            price.setEndTimeFormatted(price.getEndTime().format(TIME_FORMATTER));
        }
    }

    private void validate(CourtPrice price) throws BusinessException {
        if (price.getStartTime() == null || price.getEndTime() == null) {
            throw new BusinessException("Start time and end time are required");
        }
        if (!price.getEndTime().isAfter(price.getStartTime())) {
            throw new BusinessException("End time must be after start time");
        }
        if (price.getPricePerHour() == null || price.getPricePerHour().doubleValue() <= 0) {
            throw new BusinessException("Price must be greater than 0");
        }
    }
}
