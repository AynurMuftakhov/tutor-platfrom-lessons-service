package com.mytutorplatform.lessonsservice.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * Response DTO for the grammar scoring endpoint.
 * Contains overall scores and detailed results for each grammar item.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrammarScoreResponse {
    private UUID materialId;
    private int totalItems;
    private int correctItems;
    private int totalGaps;
    private int correctGaps;
    private List<ItemScoreDto> details;
}