package com.mytutorplatform.lessonsservice.model.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request object for generating AI-powered grammar exercises.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerateExerciseRequest {
    
    /**
     * The grammar focus for the exercise (e.g., 'articles', 'prepositions', 'tenses').
     */
    @NotBlank(message = "Grammar focus is required")
    private String grammarFocus;
    
    /**
     * The topic or lexical field for the exercise content.
     */
    @NotBlank(message = "Topic is required")
    private String topic;
    
    /**
     * The difficulty level of the exercise.
     */
    @NotBlank(message = "Level is required")
    private String level;
    
    /**
     * The number of sentences to generate for the exercise.
     */
    @Min(value = 1, message = "Sentences must be at least 1")
    @Max(value = 50, message = "Sentences must be at most 50")
    private int sentences;
    
    /**
     * The language for the exercise content (default: 'en').
     */
    private String language = "en";
    
    /**
     * The style of tokens to use in the exercise (default: 'doubleBraces').
     */
    private String tokenStyle = "doubleBraces";
}