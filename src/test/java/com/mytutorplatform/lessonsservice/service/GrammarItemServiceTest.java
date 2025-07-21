package com.mytutorplatform.lessonsservice.service;

import com.mytutorplatform.lessonsservice.mapper.GrammarTypeMapper;
import com.mytutorplatform.lessonsservice.model.GrammarItem;
import com.mytutorplatform.lessonsservice.model.Material;
import com.mytutorplatform.lessonsservice.model.request.CreateGrammarItemRequest;
import com.mytutorplatform.lessonsservice.model.request.CreateMultipleChoiceItemRequest;
import com.mytutorplatform.lessonsservice.model.response.GrammarItemDto;
import com.mytutorplatform.lessonsservice.model.response.MultipleChoiceItemDto;
import com.mytutorplatform.lessonsservice.repository.GrammarItemRepository;
import com.mytutorplatform.lessonsservice.repository.MaterialRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GrammarItemServiceTest {

    @Mock
    private GrammarItemRepository repository;

    @Mock
    private GrammarTypeMapper grammarTypeMapper;

    @Mock
    private MaterialRepository materialRepository;

    @InjectMocks
    private GrammarItemService service;

    private UUID materialId;
    private UUID gapFillItemId;
    private UUID mcqItemId;
    private GrammarItem gapFillItem;
    private GrammarItem mcqItem;
    private CreateGrammarItemRequest gapFillRequest;
    private CreateMultipleChoiceItemRequest mcqRequest;
    private GrammarItemDto gapFillDto;
    private MultipleChoiceItemDto mcqDto;

    @BeforeEach
    void setUp() {
        materialId = UUID.randomUUID();
        gapFillItemId = UUID.randomUUID();
        mcqItemId = UUID.randomUUID();

        // Set up a gap fill item
        gapFillItem = GrammarItem.builder()
                .id(gapFillItemId)
                .materialId(materialId)
                .sortOrder(1)
                .type(GrammarItem.Type.GAP_FILL)
                .text("I [live] in London.")
                .answer("live|am living")
                .build();

        // Set up a multiple choice item
        String[] options = {"Option A", "Option B", "Option C", "Option D"};
        mcqItem = GrammarItem.builder()
                .id(mcqItemId)
                .materialId(materialId)
                .sortOrder(2)
                .type(GrammarItem.Type.MULTIPLE_CHOICE)
                .question("What is the correct option?")
                .options(options)
                .correctIndex((short) 2)
                .build();

        // Set up request objects
        gapFillRequest = new CreateGrammarItemRequest();
        gapFillRequest.setSortOrder(1);
        gapFillRequest.setType(GrammarItem.Type.GAP_FILL);
        gapFillRequest.setText("I [live] in London.");
        gapFillRequest.setAnswer("live|am living");

        mcqRequest = new CreateMultipleChoiceItemRequest();
        mcqRequest.setSortOrder(2);
        mcqRequest.setType(GrammarItem.Type.MULTIPLE_CHOICE);
        mcqRequest.setQuestion("What is the correct option?");
        mcqRequest.setOptions(options);
        mcqRequest.setCorrectIndex((short) 2);

        // Set up DTO objects
        gapFillDto = new GrammarItemDto();
        gapFillDto.setId(gapFillItemId);
        gapFillDto.setMaterialId(materialId);
        gapFillDto.setSortOrder(1);
        gapFillDto.setType(GrammarItem.Type.GAP_FILL);
        gapFillDto.setText("I [live] in London.");
        gapFillDto.setAnswer("live|am living");

        mcqDto = new MultipleChoiceItemDto();
        mcqDto.setId(mcqItemId);
        mcqDto.setMaterialId(materialId);
        mcqDto.setSortOrder(2);
        mcqDto.setType(GrammarItem.Type.MULTIPLE_CHOICE);
        mcqDto.setQuestion("What is the correct option?");
        mcqDto.setOptions(options);

        // Mock repository responses
        when(materialRepository.findById(materialId)).thenReturn(Optional.of(mock(Material.class)));
    }

    @Test
    void shouldCreateGapFillItem() {
        // Given
        when(grammarTypeMapper.map(materialId, gapFillRequest)).thenReturn(gapFillItem);
        when(repository.save(gapFillItem)).thenReturn(gapFillItem);
        when(grammarTypeMapper.map(gapFillItem)).thenReturn(gapFillDto);

        // When
        GrammarItemDto result = service.createItem(materialId, gapFillRequest);

        // Then
        assertNotNull(result);
        assertEquals(gapFillItemId, result.getId());
        assertEquals(GrammarItem.Type.GAP_FILL, result.getType());
        
        verify(materialRepository).findById(materialId);
        verify(grammarTypeMapper).map(materialId, gapFillRequest);
        verify(repository).save(gapFillItem);
        verify(grammarTypeMapper).map(gapFillItem);
    }

    @Test
    void shouldCreateMultipleChoiceItem() {
        // Given
        when(grammarTypeMapper.mapMultipleChoice(materialId, mcqRequest)).thenReturn(mcqItem);
        when(repository.save(mcqItem)).thenReturn(mcqItem);
        when(grammarTypeMapper.mapToMultipleChoice(mcqItem)).thenReturn(mcqDto);

        // When
        MultipleChoiceItemDto result = service.createMultipleChoiceItem(materialId, mcqRequest);

        // Then
        assertNotNull(result);
        assertEquals(mcqItemId, result.getId());
        assertEquals(GrammarItem.Type.MULTIPLE_CHOICE, result.getType());
        
        verify(materialRepository).findById(materialId);
        verify(grammarTypeMapper).mapMultipleChoice(materialId, mcqRequest);
        verify(repository).save(mcqItem);
        verify(grammarTypeMapper).mapToMultipleChoice(mcqItem);
    }

    @Test
    void shouldGetItemsByMaterialId() {
        // Given
        List<GrammarItem> items = Arrays.asList(gapFillItem, mcqItem);
        List<Object> dtos = Arrays.asList(gapFillDto, mcqDto);
        
        when(repository.findByMaterialId(materialId)).thenReturn(items);
        when(grammarTypeMapper.mapToAppropriateDtos(items)).thenReturn(dtos);

        // When
        List<Object> result = service.getItemsByMaterialId(materialId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.get(0) instanceof GrammarItemDto);
        assertTrue(result.get(1) instanceof MultipleChoiceItemDto);
        
        verify(repository).findByMaterialId(materialId);
        verify(grammarTypeMapper).mapToAppropriateDtos(items);
    }

    @Test
    void shouldGetGrammarItemDtosByMaterialId() {
        // Given
        List<GrammarItem> items = Arrays.asList(gapFillItem, mcqItem);
        List<GrammarItemDto> dtos = Arrays.asList(gapFillDto, gapFillDto); // Just using the same DTO twice for simplicity
        
        when(repository.findByMaterialId(materialId)).thenReturn(items);
        when(grammarTypeMapper.mapList(items)).thenReturn(dtos);

        // When
        List<GrammarItemDto> result = service.getGrammarItemDtosByMaterialId(materialId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        
        verify(repository).findByMaterialId(materialId);
        verify(grammarTypeMapper).mapList(items);
    }

    @Test
    void shouldThrowExceptionWhenMaterialNotFound() {
        // Given
        UUID nonExistentMaterialId = UUID.randomUUID();
        when(materialRepository.findById(nonExistentMaterialId)).thenReturn(Optional.empty());

        // When/Then
        assertThrows(EntityNotFoundException.class, () -> {
            service.createItem(nonExistentMaterialId, gapFillRequest);
        });
        
        assertThrows(EntityNotFoundException.class, () -> {
            service.createMultipleChoiceItem(nonExistentMaterialId, mcqRequest);
        });
    }

    @Test
    void shouldThrowExceptionWhenMaterialIdIsNull() {
        // When/Then
        assertThrows(IllegalArgumentException.class, () -> {
            service.createItem(null, gapFillRequest);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            service.createMultipleChoiceItem(null, mcqRequest);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            service.getItemsByMaterialId(null);
        });
        
        assertThrows(IllegalArgumentException.class, () -> {
            service.getGrammarItemDtosByMaterialId(null);
        });
    }
}