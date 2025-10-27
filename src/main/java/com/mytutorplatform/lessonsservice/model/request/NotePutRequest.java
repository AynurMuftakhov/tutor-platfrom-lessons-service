package com.mytutorplatform.lessonsservice.model.request;

import jakarta.validation.constraints.NotNull;

public record NotePutRequest(
        @NotNull(message = "content is required") String content,
        String format
) {}
