package com.mytutorplatform.lessonsservice.model.request;

import java.util.List;
import java.util.UUID;

public record ValidateCoverageRequest(String transcript, List<UUID> wordIds) {}
