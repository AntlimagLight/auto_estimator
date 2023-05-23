package by.kaminsky.service;

import by.kaminsky.dto.EstimateDataDto;

public interface EstimateCreationService {

    String createEstimate(EstimateDataDto estimateData);

    EstimateDataDto createTest();
}
