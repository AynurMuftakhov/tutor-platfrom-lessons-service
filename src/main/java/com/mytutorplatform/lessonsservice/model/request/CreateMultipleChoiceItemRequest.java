package com.mytutorplatform.lessonsservice.model.request;

import com.mytutorplatform.lessonsservice.model.GrammarItem;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateMultipleChoiceItemRequest {
    @NotNull(message = "Sort order is required")
    private Integer sortOrder;
    
    @NotNull(message = "Type must be MULTIPLE_CHOICE")
    private GrammarItem.Type type = GrammarItem.Type.MULTIPLE_CHOICE;
    
    @NotBlank(message = "Question is required")
    private String question;
    
    @NotNull(message = "Options are required")
    @Size(min = 4, max = 4, message = "Exactly 4 options are required")
    private String[] options;
    
    @NotNull(message = "Correct index is required")
    @Min(value = 0, message = "Correct index must be between 0 and 3")
    @Max(value = 3, message = "Correct index must be between 0 and 3")
    private Short correctIndex;
}