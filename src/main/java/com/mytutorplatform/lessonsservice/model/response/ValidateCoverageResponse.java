package com.mytutorplatform.lessonsservice.model.response;

import java.util.List;
import java.util.Map;

public record ValidateCoverageResponse(
        Map<String, Boolean> wordCoverage,
        List<String> missing
) {}
