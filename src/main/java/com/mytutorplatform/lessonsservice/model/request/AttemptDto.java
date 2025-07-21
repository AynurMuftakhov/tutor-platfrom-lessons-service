package com.mytutorplatform.lessonsservice.model.request;

import lombok.Data;

import java.util.List;
import java.util.UUID;

/**
 * DTO representing a student's attempt at a grammar item.
 * Contains the grammar item ID and either:
 * - For GAP_FILL: the student's answers for each gap
 * - For MULTIPLE_CHOICE: the index of the chosen option (0-3)
 */
@Data
public class AttemptDto {
    private UUID grammarItemId;

    // For GAP_FILL items
    private List<String> gapAnswers;

    // For MULTIPLE_CHOICE items
    private Short chosenIndex;
}
