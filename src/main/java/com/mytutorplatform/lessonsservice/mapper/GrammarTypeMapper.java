package com.mytutorplatform.lessonsservice.mapper;

import com.mytutorplatform.lessonsservice.model.GrammarItem;
import com.mytutorplatform.lessonsservice.model.request.CreateGrammarItemRequest;
import com.mytutorplatform.lessonsservice.model.response.GrammarItemDto;
import org.mapstruct.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE , componentModel = "spring")
public interface GrammarTypeMapper {

    GrammarItemDto map(GrammarItem grammarItem);

    List<GrammarItemDto> mapList(List<GrammarItem> grammarItems);

    @Mapping(target = "materialId", source = "materialId")
    GrammarItem map(UUID materialId, CreateGrammarItemRequest createGrammarItemRequest);

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
}
