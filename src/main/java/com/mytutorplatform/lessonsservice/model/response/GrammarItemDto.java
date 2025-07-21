package com.mytutorplatform.lessonsservice.model.response;

import com.mytutorplatform.lessonsservice.model.GrammarItem;
import lombok.Data;

import java.util.UUID;

@Data
public class GrammarItemDto {
    private UUID id;

    private UUID materialId;

    private Integer sortOrder;
    private GrammarItem.Type type;

    private String text;

    private String metadata;

    private String answer;
}
