package com.bcb.service.impl;

import com.bcb.model.CourtType;
import com.bcb.repository.CourtTypeRepository;
import com.bcb.repository.impl.CourtTypeRepositoryImpl;
import com.bcb.service.CourtTypeService;

import java.util.List;

public class CourtTypeServiceImpl implements CourtTypeService {

    private final CourtTypeRepository courtTypeRepository;

    public CourtTypeServiceImpl() {
        this.courtTypeRepository = new CourtTypeRepositoryImpl();
    }

    @Override
    public List<CourtType> getAllTypes() {
        return courtTypeRepository.findAll();
    }
}
