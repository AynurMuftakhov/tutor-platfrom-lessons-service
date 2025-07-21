package com.mytutorplatform.lessonsservice.validation;

import com.mytutorplatform.lessonsservice.model.request.CreateListeningTaskRequest;
import org.springframework.stereotype.Component;

import java.net.MalformedURLException;
import java.net.URL;

@Component
public class ListeningTaskValidator {

    public void validateCreate(CreateListeningTaskRequest request) {
        validateTimeRange(request);
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
}