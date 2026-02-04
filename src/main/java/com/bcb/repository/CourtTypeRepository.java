package com.bcb.repository;

import com.bcb.model.CourtType;
import java.util.List;
import java.util.Optional;

public interface CourtTypeRepository {
    List<CourtType> findAll();
    Optional<CourtType> findById(int courtTypeId);
}
