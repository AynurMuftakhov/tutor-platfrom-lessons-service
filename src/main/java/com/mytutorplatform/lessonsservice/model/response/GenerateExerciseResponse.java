package com.mytutorplatform.lessonsservice.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Response object containing the generated exercise content.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GenerateExerciseResponse {
    
    /**
     * The HTML content of the exercise with placeholders.
     * Example: "Mount {{1:the}} Everest is ..."
     */
    private String html;
    
    /**
     * Map of placeholder indices to possible answers.
     * Example: {1: ["the"], 2: ["a", "an"], ...}
     */
    private Map<Integer, List<String>> answers;
    
    /**
     * Metadata about the AI model used for generation.
     */
    private Meta meta;
}