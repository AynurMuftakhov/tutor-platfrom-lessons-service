package com.mytutorplatform.lessonsservice.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

/**
 * DTO representing the score for a single grammar item.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemScoreDto {
    private UUID grammarItemId;
    private List<GapResultDto> gapResults;
    private boolean itemCorrect;
}