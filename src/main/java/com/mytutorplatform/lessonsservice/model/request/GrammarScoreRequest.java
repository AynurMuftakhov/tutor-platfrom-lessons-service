package com.mytutorplatform.lessonsservice.model.request;

import lombok.Data;

import java.util.List;

/**
 * Request DTO for scoring grammar items.
 * Contains a list of attempts, one for each grammar item in the material.
 */
@Data
public class GrammarScoreRequest {
    private List<AttemptDto> attempts;
}