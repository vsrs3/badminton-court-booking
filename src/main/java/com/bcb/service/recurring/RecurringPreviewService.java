package com.bcb.service.recurring;

import com.bcb.dto.recurring.RecurringPreviewRequestDTO;
import com.bcb.dto.recurring.RecurringPreviewResponseDTO;

/**
 * Service interface for recurring preview flow.
 *
 * @author AnhTN
 */
public interface RecurringPreviewService {

    RecurringPreviewResponseDTO preview(RecurringPreviewRequestDTO request);
}

