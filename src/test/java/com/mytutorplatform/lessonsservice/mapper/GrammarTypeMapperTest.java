package com.mytutorplatform.lessonsservice.mapper;

import com.mytutorplatform.lessonsservice.model.GrammarItem;
import com.mytutorplatform.lessonsservice.model.request.CreateGrammarItemRequest;
import com.mytutorplatform.lessonsservice.model.request.CreateMultipleChoiceItemRequest;
import com.mytutorplatform.lessonsservice.model.response.GrammarItemDto;
import com.mytutorplatform.lessonsservice.model.response.MultipleChoiceItemDto;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class GrammarTypeMapperTest {

    private final GrammarTypeMapper mapper = Mappers.getMapper(GrammarTypeMapper.class);

    @Test
    void shouldMapGrammarItemToDto() {
        // Given
        UUID id = UUID.randomUUID();
        UUID materialId = UUID.randomUUID();
        GrammarItem item = GrammarItem.builder()
                .id(id)
                .materialId(materialId)
                .sortOrder(1)
                .type(GrammarItem.Type.GAP_FILL)
                .text("This is a [gap] test.")
                .metadata("metadata")
                .answer("gap")
                .build();

        // When
        GrammarItemDto dto = mapper.map(item);

        // Then
        assertNotNull(dto);
        assertEquals(id, dto.getId());
        assertEquals(materialId, dto.getMaterialId());
        assertEquals(1, dto.getSortOrder());
        assertEquals(GrammarItem.Type.GAP_FILL, dto.getType());
        assertEquals("This is a [gap] test.", dto.getText());
        assertEquals("metadata", dto.getMetadata());
        assertEquals("gap", dto.getAnswer());
    }

    @Test
    void shouldMapMultipleChoiceItemToDto() {
        // Given
        UUID id = UUID.randomUUID();
        UUID materialId = UUID.randomUUID();
        String[] options = {"Option A", "Option B", "Option C", "Option D"};
        GrammarItem item = GrammarItem.builder()
                .id(id)
                .materialId(materialId)
                .sortOrder(1)
                .type(GrammarItem.Type.MULTIPLE_CHOICE)
                .question("What is the correct option?")
                .options(options)
                .correctIndex((short) 2)
                .build();

        // When
        MultipleChoiceItemDto dto = mapper.mapToMultipleChoice(item);

        // Then
        assertNotNull(dto);
        assertEquals(id, dto.getId());
        assertEquals(materialId, dto.getMaterialId());
        assertEquals(1, dto.getSortOrder());
        assertEquals(GrammarItem.Type.MULTIPLE_CHOICE, dto.getType());
        assertEquals("What is the correct option?", dto.getQuestion());
        assertArrayEquals(options, dto.getOptions());
        // correctIndex should not be included in the DTO
    }

    @Test
    void shouldMapCreateRequestToGrammarItem() {
        // Given
        UUID materialId = UUID.randomUUID();
        CreateGrammarItemRequest request = new CreateGrammarItemRequest();
        request.setSortOrder(1);
        request.setType(GrammarItem.Type.GAP_FILL);
        request.setText("This is a [gap] test.");
        request.setMetadata("metadata");
        request.setAnswer("gap");

        // When
        GrammarItem item = mapper.map(materialId, request);

        // Then
        assertNotNull(item);
        assertEquals(materialId, item.getMaterialId());
        assertEquals(1, item.getSortOrder());
        assertEquals(GrammarItem.Type.GAP_FILL, item.getType());
        assertEquals("This is a [gap] test.", item.getText());
        assertEquals("metadata", item.getMetadata());
        assertEquals("gap", item.getAnswer());
    }

    @Test
    void shouldMapCreateRequestToMultipleChoiceItem() {
        // Given
        UUID materialId = UUID.randomUUID();
        String[] options = {"Option A", "Option B", "Option C", "Option D"};
        CreateMultipleChoiceItemRequest request = new CreateMultipleChoiceItemRequest();
        request.setSortOrder(1);
        request.setType(GrammarItem.Type.MULTIPLE_CHOICE);
        request.setQuestion("What is the correct option?");
        request.setOptions(options);
        request.setCorrectIndex((short) 2);

        // When
        GrammarItem item = mapper.mapMultipleChoice(materialId, request);

        // Then
        assertNotNull(item);
        assertEquals(materialId, item.getMaterialId());
        assertEquals(1, item.getSortOrder());
        assertEquals(GrammarItem.Type.MULTIPLE_CHOICE, item.getType());
        assertEquals("What is the correct option?", item.getQuestion());
        assertArrayEquals(options, item.getOptions());
        assertEquals((short) 2, item.getCorrectIndex());
        // text, metadata, and answer should be null
        assertNull(item.getText());
        assertNull(item.getMetadata());
        assertNull(item.getAnswer());
    }

    @Test
    void shouldMapToAppropriateDto() {
        // Given
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        UUID materialId = UUID.randomUUID();
        
        GrammarItem gapFillItem = GrammarItem.builder()
                .id(id1)
                .materialId(materialId)
                .sortOrder(1)
                .type(GrammarItem.Type.GAP_FILL)
                .text("This is a [gap] test.")
                .metadata("metadata")
                .answer("gap")
                .build();
                
        String[] options = {"Option A", "Option B", "Option C", "Option D"};
        GrammarItem mcqItem = GrammarItem.builder()
                .id(id2)
                .materialId(materialId)
                .sortOrder(2)
                .type(GrammarItem.Type.MULTIPLE_CHOICE)
                .question("What is the correct option?")
                .options(options)
                .correctIndex((short) 2)
                .build();

        // When
        Object gapFillDto = mapper.mapToAppropriateDto(gapFillItem);
        Object mcqDto = mapper.mapToAppropriateDto(mcqItem);

        // Then
        assertTrue(gapFillDto instanceof GrammarItemDto);
        assertTrue(mcqDto instanceof MultipleChoiceItemDto);
        
        GrammarItemDto gapFillResult = (GrammarItemDto) gapFillDto;
        assertEquals(id1, gapFillResult.getId());
        assertEquals(GrammarItem.Type.GAP_FILL, gapFillResult.getType());
        
        MultipleChoiceItemDto mcqResult = (MultipleChoiceItemDto) mcqDto;
        assertEquals(id2, mcqResult.getId());
        assertEquals(GrammarItem.Type.MULTIPLE_CHOICE, mcqResult.getType());
    }
}