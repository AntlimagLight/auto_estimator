package by.kaminsky.service;

import by.kaminsky.dto.EstimateDataDto;

public interface EstimateCreationService {

    String createPrometheusEstimate(EstimateDataDto estimateData);

    EstimateDataDto createTest();
}
