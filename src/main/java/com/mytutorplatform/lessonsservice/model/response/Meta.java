package com.mytutorplatform.lessonsservice.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Metadata about the AI model used to generate the exercise.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Meta {
    /**
     * The AI model used to generate the exercise.
     */
    private String model;
    
    /**
     * The temperature setting used for generation.
     */
    private double temp;
    
    /**
     * The number of tokens used in the generation.
     */
    private int tokensUsed;
}