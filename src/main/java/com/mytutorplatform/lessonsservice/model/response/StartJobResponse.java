package com.mytutorplatform.lessonsservice.model.response;

import java.util.UUID;

public record StartJobResponse(UUID jobId, String status) {}
