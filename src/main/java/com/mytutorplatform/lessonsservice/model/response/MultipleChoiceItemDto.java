package com.mytutorplatform.lessonsservice.model.response;

import com.mytutorplatform.lessonsservice.model.GrammarItem;
import lombok.Data;

import java.util.UUID;

@Data
public class MultipleChoiceItemDto {
    private UUID id;
    private UUID materialId;
    private Integer sortOrder;
    private GrammarItem.Type type = GrammarItem.Type.MULTIPLE_CHOICE;
    private String question;
    private String[] options;
    // Note: correctIndex is not included in the response DTO
    // to avoid revealing the correct answer to the client
}