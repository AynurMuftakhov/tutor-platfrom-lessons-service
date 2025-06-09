package com.mytutorplatform.lessonsservice.validation;

import com.mytutorplatform.lessonsservice.model.request.CreateListeningTaskRequest;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;

@Component
public class ListeningTaskValidator {

    public void validateCreate(CreateListeningTaskRequest request) {
        validateTimeRange(request);
        validateSourceUrl(request);
    }

    private void validateTimeRange(CreateListeningTaskRequest request) {
        if (request.getStartSec() == null || request.getEndSec() == null) {
           return;
        }
        
        if (request.getStartSec() >= request.getEndSec()) {
            throw new IllegalArgumentException("Start time must be less than end time");
        }
        
        if (request.getStartSec() < 0) {
            throw new IllegalArgumentException("Start time cannot be negative");
        }
    }

    private void validateSourceUrl(CreateListeningTaskRequest request) {
        if (request.getSourceUrl() == null || request.getSourceUrl().trim().isEmpty()) {
           return;
        }
        
        try {
            URL url = new URL(request.getSourceUrl());
            String protocol = url.getProtocol();
            if (!protocol.equals("https")) {
                throw new IllegalArgumentException("Source URL must use HTTPS protocol");
            }
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid source URL: " + e.getMessage());
        }
    }
}