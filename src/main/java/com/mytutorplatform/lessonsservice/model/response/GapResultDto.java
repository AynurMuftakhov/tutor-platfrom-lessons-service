package com.mytutorplatform.lessonsservice.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO representing the result of a student's answer for a single gap.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GapResultDto {
    private int index;
    private String student;
    private String correctAnswer;
    private boolean isCorrect;
}