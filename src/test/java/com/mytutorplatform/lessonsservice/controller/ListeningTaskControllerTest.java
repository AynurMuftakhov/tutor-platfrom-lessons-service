package com.mytutorplatform.lessonsservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mytutorplatform.lessonsservice.model.ListeningTask;
import com.mytutorplatform.lessonsservice.model.request.CreateListeningTaskRequest;
import com.mytutorplatform.lessonsservice.service.LessonTaskService;
import com.mytutorplatform.lessonsservice.service.ListeningTaskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LessonTaskController.class)
public class ListeningTaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ListeningTaskService listeningTaskService;

    @MockBean
    private LessonTaskService lessonTaskService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void testUpdateListeningTask() throws Exception {
        // Prepare test data
        UUID taskId = UUID.randomUUID();
        UUID materialId = UUID.randomUUID();
        
        CreateListeningTaskRequest request = new CreateListeningTaskRequest();
        request.setTitle("Updated Task");
        request.setStartSec(10);
        request.setEndSec(60);
        request.setWordLimit(100);
        request.setTimeLimitSec(120);
        request.setMaterialId(materialId);
        
        ListeningTask updatedTask = new ListeningTask();
        updatedTask.setId(taskId);
        updatedTask.setTitle("Updated Task");
        updatedTask.setStartSec(10);
        updatedTask.setEndSec(60);
        updatedTask.setWordLimit(100);
        updatedTask.setTimeLimitSec(120);
        updatedTask.setMaterialId(materialId);
        
        // Mock service method
        when(listeningTaskService.updateListeningTask(eq(taskId), any(CreateListeningTaskRequest.class)))
                .thenReturn(updatedTask);
        
        // Perform request and verify response
        mockMvc.perform(patch("/api/listening-tasks/{taskId}", taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId.toString()))
                .andExpect(jsonPath("$.title").value("Updated Task"))
                .andExpect(jsonPath("$.startSec").value(10))
                .andExpect(jsonPath("$.endSec").value(60))
                .andExpect(jsonPath("$.wordLimit").value(100))
                .andExpect(jsonPath("$.timeLimitSec").value(120))
                .andExpect(jsonPath("$.materialId").value(materialId.toString()));
    }

    @Test
    public void testGetAllListeningTasks() throws Exception {
        // Prepare test data
        UUID materialId = UUID.randomUUID();
        
        ListeningTask task1 = new ListeningTask();
        task1.setId(UUID.randomUUID());
        task1.setTitle("Task 1");
        task1.setStartSec(0);
        task1.setEndSec(30);
        task1.setMaterialId(materialId);
        
        ListeningTask task2 = new ListeningTask();
        task2.setId(UUID.randomUUID());
        task2.setTitle("Task 2");
        task2.setStartSec(30);
        task2.setEndSec(60);
        task2.setMaterialId(materialId);
        
        List<ListeningTask> tasks = Arrays.asList(task1, task2);
        
        // Mock service method
        when(listeningTaskService.getAllListeningTasks(materialId)).thenReturn(tasks);
        
        // Perform request and verify response
        mockMvc.perform(get("/api/listening-tasks")
                .param("materialId", materialId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Task 1"))
                .andExpect(jsonPath("$[1].title").value("Task 2"));
    }

    @Test
    public void testCreateListeningTask() throws Exception {
        // Prepare test data
        UUID materialId = UUID.randomUUID();
        
        CreateListeningTaskRequest request = new CreateListeningTaskRequest();
        request.setTitle("New Task");
        request.setStartSec(0);
        request.setEndSec(60);
        request.setMaterialId(materialId);
        
        ListeningTask createdTask = new ListeningTask();
        createdTask.setId(UUID.randomUUID());
        createdTask.setTitle("New Task");
        createdTask.setStartSec(0);
        createdTask.setEndSec(60);
        createdTask.setMaterialId(materialId);
        
        // Mock service method
        when(listeningTaskService.createListeningTask(any(CreateListeningTaskRequest.class)))
                .thenReturn(createdTask);
        
        // Perform request and verify response
        mockMvc.perform(post("/api/listening-tasks")
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
    public void testDeleteListeningTask() throws Exception {
        // Prepare test data
        UUID taskId = UUID.randomUUID();
        
        // Perform request and verify response
        mockMvc.perform(delete("/api/listening-tasks/{taskId}", taskId))
                .andExpect(status().isNoContent());
    }
}