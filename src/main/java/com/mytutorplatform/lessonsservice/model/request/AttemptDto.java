package com.mytutorplatform.lessonsservice.model.request;

import lombok.Data;

import java.util.List;
import java.util.UUID;

/**
 * DTO representing a student's attempt at a grammar item.
 * Contains the grammar item ID and the student's answers for each gap.
 */
@Data
public class AttemptDto {
    private UUID grammarItemId;
    private List<String> gapAnswers;
}