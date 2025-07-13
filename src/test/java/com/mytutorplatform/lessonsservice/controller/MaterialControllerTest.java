package com.mytutorplatform.lessonsservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mytutorplatform.lessonsservice.model.ListeningTask;
import com.mytutorplatform.lessonsservice.model.Material;
import com.mytutorplatform.lessonsservice.model.request.AttemptDto;
import com.mytutorplatform.lessonsservice.model.request.CreateListeningTaskRequest;
import com.mytutorplatform.lessonsservice.model.request.GrammarScoreRequest;
import com.mytutorplatform.lessonsservice.model.response.GapResultDto;
import com.mytutorplatform.lessonsservice.model.response.GrammarScoreResponse;
import com.mytutorplatform.lessonsservice.model.response.ItemScoreDto;
import com.mytutorplatform.lessonsservice.service.GrammarItemService;
import com.mytutorplatform.lessonsservice.service.GrammarScoringService;
import com.mytutorplatform.lessonsservice.service.ListeningTaskService;
import com.mytutorplatform.lessonsservice.service.MaterialService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MaterialController.class)
public class MaterialControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MaterialService materialService;

    @MockBean
    private ListeningTaskService listeningTaskService;

    @MockBean
    private GrammarItemService grammarItemService;

    @MockBean
    private GrammarScoringService grammarScoringService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testGetMaterials() throws Exception {
        // Prepare test data
        Material material1 = Material.builder()
                .id(UUID.randomUUID())
                .title("Test Material 1")
                .type(Material.AssetType.AUDIO)
                .sourceUrl("http://example.com/audio1.mp3")
                .build();

        Material material2 = Material.builder()
                .id(UUID.randomUUID())
                .title("Test Material 2")
                .type(Material.AssetType.VIDEO)
                .sourceUrl("http://example.com/video1.mp4")
                .build();

        List<Material> materials = Arrays.asList(material1, material2);
        PageImpl<Material> page = new PageImpl<>(materials);

        // Mock service method
        when(materialService.findMaterials(any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(page);

        // Perform request and verify response
        mockMvc.perform(get("/api/materials")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].title").value("Test Material 1"))
                .andExpect(jsonPath("$.content[1].title").value("Test Material 2"));
    }

    @Test
    public void testGetMaterialById() throws Exception {
        // Prepare test data
        UUID materialId = UUID.randomUUID();
        Material material = Material.builder()
                .id(materialId)
                .title("Test Material")
                .type(Material.AssetType.AUDIO)
                .sourceUrl("http://example.com/audio.mp3")
                .build();

        // Mock service method
        when(materialService.getMaterialById(materialId)).thenReturn(material);

        // Perform request and verify response
        mockMvc.perform(get("/api/materials/{id}", materialId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(materialId.toString()))
                .andExpect(jsonPath("$.title").value("Test Material"))
                .andExpect(jsonPath("$.type").value("AUDIO"))
                .andExpect(jsonPath("$.sourceUrl").value("http://example.com/audio.mp3"));
    }

    @Test
    public void testCreateMaterial() throws Exception {
        // Prepare test data
        Material material = Material.builder()
                .title("New Material")
                .type(Material.AssetType.VIDEO)
                .sourceUrl("http://example.com/video.mp4")
                .build();

        Material savedMaterial = Material.builder()
                .id(UUID.randomUUID())
                .title("New Material")
                .type(Material.AssetType.VIDEO)
                .sourceUrl("http://example.com/video.mp4")
                .build();

        // Mock service method
        when(materialService.createMaterial(any(Material.class))).thenReturn(savedMaterial);

        // Perform request and verify response
        mockMvc.perform(post("/api/materials")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(material)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("New Material"))
                .andExpect(jsonPath("$.type").value("VIDEO"))
                .andExpect(jsonPath("$.sourceUrl").value("http://example.com/video.mp4"));
    }

    @Test
    public void testUpdateMaterial() throws Exception {
        // Prepare test data
        UUID materialId = UUID.randomUUID();
        Material material = Material.builder()
                .title("Updated Material")
                .build();

        Material updatedMaterial = Material.builder()
                .id(materialId)
                .title("Updated Material")
                .type(Material.AssetType.AUDIO)
                .sourceUrl("http://example.com/audio.mp3")
                .build();

        // Mock service method
        when(materialService.updateMaterial(eq(materialId), any(Material.class))).thenReturn(updatedMaterial);

        // Perform request and verify response
        mockMvc.perform(patch("/api/materials/{id}", materialId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(material)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(materialId.toString()))
                .andExpect(jsonPath("$.title").value("Updated Material"));
    }

    @Test
    public void testDeleteMaterial() throws Exception {
        // Prepare test data
        UUID materialId = UUID.randomUUID();

        // Perform request and verify response
        mockMvc.perform(delete("/api/materials/{id}", materialId))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testGetTasksForMaterial() throws Exception {
        // Prepare test data
        UUID materialId = UUID.randomUUID();
        ListeningTask task1 = new ListeningTask();
        task1.setId(UUID.randomUUID());
        task1.setTitle("Task 1");
        task1.setMaterialId(materialId);

        ListeningTask task2 = new ListeningTask();
        task2.setId(UUID.randomUUID());
        task2.setTitle("Task 2");
        task2.setMaterialId(materialId);

        List<ListeningTask> tasks = Arrays.asList(task1, task2);

        // Mock service method
        when(materialService.getTasksForMaterial(materialId)).thenReturn(tasks);

        // Perform request and verify response
        mockMvc.perform(get("/api/materials/{id}/tasks", materialId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Task 1"))
                .andExpect(jsonPath("$[1].title").value("Task 2"));
    }

    @Test
    public void testCreateTaskForMaterial() throws Exception {
        // Prepare test data
        UUID materialId = UUID.randomUUID();
        CreateListeningTaskRequest request = new CreateListeningTaskRequest();
        request.setTitle("New Task");
        request.setStartSec(0);
        request.setEndSec(60);

        ListeningTask createdTask = new ListeningTask();
        createdTask.setId(UUID.randomUUID());
        createdTask.setTitle("New Task");
        createdTask.setStartSec(0);
        createdTask.setEndSec(60);
        createdTask.setMaterialId(materialId);

        // Mock service method
        when(listeningTaskService.createListeningTask(any(CreateListeningTaskRequest.class))).thenReturn(createdTask);

        // Perform request and verify response
        mockMvc.perform(post("/api/materials/{id}/tasks", materialId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("New Task"))
                .andExpect(jsonPath("$.startSec").value(0))
                .andExpect(jsonPath("$.endSec").value(60))
                .andExpect(jsonPath("$.materialId").value(materialId.toString()));
    }

    @Test
    public void testGetAllTags() throws Exception {
        // Prepare test data
        List<String> tags = Arrays.asList("grammar", "beginner", "advanced", "business");

        // Mock service method
        when(materialService.getAllTags()).thenReturn(tags);

        // Perform request and verify response
        mockMvc.perform(get("/api/materials/tags"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(4))
                .andExpect(jsonPath("$[0]").value("grammar"))
                .andExpect(jsonPath("$[1]").value("beginner"))
                .andExpect(jsonPath("$[2]").value("advanced"))
                .andExpect(jsonPath("$[3]").value("business"));
    }

    @Test
    public void testScoreGrammarItems() throws Exception {
        // Prepare test data
        UUID materialId = UUID.randomUUID();
        UUID grammarItemId1 = UUID.randomUUID();
        UUID grammarItemId2 = UUID.randomUUID();

        // Create request
        AttemptDto attempt1 = new AttemptDto();
        attempt1.setGrammarItemId(grammarItemId1);
        attempt1.setGapAnswers(Arrays.asList("the", "cat"));

        AttemptDto attempt2 = new AttemptDto();
        attempt2.setGrammarItemId(grammarItemId2);
        attempt2.setGapAnswers(Arrays.asList("a", "dog"));

        GrammarScoreRequest request = new GrammarScoreRequest();
        request.setAttempts(Arrays.asList(attempt1, attempt2));

        // Create response
        GapResultDto gap1 = GapResultDto.builder()
                .index(0)
                .student("the")
                .correct("the")
                .isCorrect(true)
                .build();

        GapResultDto gap2 = GapResultDto.builder()
                .index(1)
                .student("cat")
                .correct("cat")
                .isCorrect(true)
                .build();

        GapResultDto gap3 = GapResultDto.builder()
                .index(0)
                .student("a")
                .correct("the")
                .isCorrect(false)
                .build();

        GapResultDto gap4 = GapResultDto.builder()
                .index(1)
                .student("dog")
                .correct("dog")
                .isCorrect(true)
                .build();

        ItemScoreDto item1 = ItemScoreDto.builder()
                .grammarItemId(grammarItemId1)
                .gapResults(Arrays.asList(gap1, gap2))
                .itemCorrect(true)
                .build();

        ItemScoreDto item2 = ItemScoreDto.builder()
                .grammarItemId(grammarItemId2)
                .gapResults(Arrays.asList(gap3, gap4))
                .itemCorrect(false)
                .build();

        GrammarScoreResponse response = GrammarScoreResponse.builder()
                .materialId(materialId)
                .totalItems(2)
                .correctItems(1)
                .totalGaps(4)
                .correctGaps(3)
                .details(Arrays.asList(item1, item2))
                .build();

        // Mock service methods
        when(materialService.getMaterialById(materialId)).thenReturn(new Material());
        when(grammarScoringService.score(eq(materialId), anyList())).thenReturn(response);

        // Perform request and verify response
        mockMvc.perform(post("/api/materials/{materialId}/score", materialId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.materialId").value(materialId.toString()))
                .andExpect(jsonPath("$.totalItems").value(2))
                .andExpect(jsonPath("$.correctItems").value(1))
                .andExpect(jsonPath("$.totalGaps").value(4))
                .andExpect(jsonPath("$.correctGaps").value(3))
                .andExpect(jsonPath("$.details.length()").value(2))
                .andExpect(jsonPath("$.details[0].grammarItemId").value(grammarItemId1.toString()))
                .andExpect(jsonPath("$.details[0].itemCorrect").value(true))
                .andExpect(jsonPath("$.details[1].grammarItemId").value(grammarItemId2.toString()))
                .andExpect(jsonPath("$.details[1].itemCorrect").value(false));
    }
}
