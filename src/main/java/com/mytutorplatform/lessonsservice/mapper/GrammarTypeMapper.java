package com.mytutorplatform.lessonsservice.mapper;

import com.mytutorplatform.lessonsservice.model.GrammarItem;
import com.mytutorplatform.lessonsservice.model.request.CreateGrammarItemRequest;
import com.mytutorplatform.lessonsservice.model.request.CreateMultipleChoiceItemRequest;
import com.mytutorplatform.lessonsservice.model.response.GrammarItemDto;
import com.mytutorplatform.lessonsservice.model.response.MultipleChoiceItemDto;
import org.mapstruct.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE , componentModel = "spring")
public interface GrammarTypeMapper {

    GrammarItemDto map(GrammarItem grammarItem);

    @Mapping(target = "type", constant = "MULTIPLE_CHOICE")
    MultipleChoiceItemDto mapToMultipleChoice(GrammarItem grammarItem);

    List<GrammarItemDto> mapList(List<GrammarItem> grammarItems);

    @Mapping(target = "materialId", source = "materialId")
    GrammarItem map(UUID materialId, CreateGrammarItemRequest createGrammarItemRequest);

    @Mapping(target = "materialId", source = "materialId")
    @Mapping(target = "text", ignore = true)
    @Mapping(target = "metadata", ignore = true)
    @Mapping(target = "answer", ignore = true)
    GrammarItem mapMultipleChoice(UUID materialId, CreateMultipleChoiceItemRequest request);

    /**
     * Maps a list of CreateGrammarItemRequest to a list of GrammarItem.
     * This is a default method implementation because MapStruct has issues with mapping
     * collections when additional parameters are involved.
     */
    default ArrayList<GrammarItem> mapList(UUID materialId, List<CreateGrammarItemRequest> grammarItemRequests) {
        if (grammarItemRequests == null) {
            return null;
        }

        ArrayList<GrammarItem> grammarItems = new ArrayList<>(grammarItemRequests.size());
        for (CreateGrammarItemRequest request : grammarItemRequests) {
            grammarItems.add(map(materialId, request));
        }
        return grammarItems;
    }

    /**
     * Determines the appropriate DTO type based on the GrammarItem type.
     * For MULTIPLE_CHOICE items, returns a MultipleChoiceItemDto.
     * For other types, returns a GrammarItemDto.
     */
    default Object mapToAppropriateDto(GrammarItem grammarItem) {
        if (grammarItem == null) {
            return null;
        }

        if (grammarItem.getType() == GrammarItem.Type.MULTIPLE_CHOICE) {
            return mapToMultipleChoice(grammarItem);
        } else {
            return map(grammarItem);
        }
    }

    /**
     * Maps a list of GrammarItems to their appropriate DTOs based on type.
     */
    default List<Object> mapToAppropriateDtos(List<GrammarItem> grammarItems) {
        if (grammarItems == null) {
            return null;
        }

        List<Object> dtos = new ArrayList<>(grammarItems.size());
        for (GrammarItem item : grammarItems) {
            dtos.add(mapToAppropriateDto(item));
        }
        return dtos;
    }
}
