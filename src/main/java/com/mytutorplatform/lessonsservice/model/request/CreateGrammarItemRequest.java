package com.mytutorplatform.lessonsservice.model.request;

import com.mytutorplatform.lessonsservice.model.GrammarItem;
import lombok.Data;

@Data
public class CreateGrammarItemRequest {
    private Integer sortOrder;
    private GrammarItem.Type type;

    private String text;

    private String metadata;

    private String answer;
}
