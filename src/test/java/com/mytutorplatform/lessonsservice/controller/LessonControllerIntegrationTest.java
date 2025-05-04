package com.mytutorplatform.lessonsservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mytutorplatform.lessonsservice.model.Lesson;
import com.mytutorplatform.lessonsservice.model.LessonStatus;
import com.mytutorplatform.lessonsservice.repository.LessonRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class LessonControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID tutorId1;
    private UUID studentId1;
    private UUID studentId2;

    @BeforeEach
    void setup() {
        // Clear repository before each test
        lessonRepository.deleteAll();

        // Define IDs for tutors and students
        tutorId1 = UUID.randomUUID();
        studentId1 = UUID.randomUUID();
        studentId2 = UUID.randomUUID();

        // Create and save lessons for testing
        Lesson lesson1 = new Lesson();
        lesson1.setTutorId(tutorId1);
        lesson1.setStatus(LessonStatus.COMPLETED);
        lesson1.setStudentId(studentId1);
        lesson1.setDateTime(OffsetDateTime.now().minusDays(2)); // Past date

        Lesson lesson2 = new Lesson();
        lesson2.setTutorId(tutorId1);
        lesson2.setStatus(LessonStatus.COMPLETED);
        lesson2.setStudentId(studentId2);
        lesson2.setDateTime(OffsetDateTime.now().minusDays(1)); // Past date

        Lesson lesson3 = new Lesson();
        lesson3.setTutorId(tutorId1);
        lesson3.setStudentId(studentId1);
        lesson3.setDateTime(OffsetDateTime.now().plusDays(1)); // Future date

        // Save lessons to the repository
        lessonRepository.save(lesson1);
        lessonRepository.save(lesson2);
        lessonRepository.save(lesson2);
    }

    @Test
    public void testGetTutorStatistics() throws Exception {
        // Send GET request to the "getTutorStatistics" endpoint
        mockMvc.perform(get("/api/lessons/tutor/{tutorId}/statistics", tutorId1)
                        .contentType(MediaType.APPLICATION_JSON))
                // Expect HTTP 200 status
                .andExpect(status().isOk())
                // Validate response content type
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                // Validate the "taughtStudents" field
                .andExpect(jsonPath("$.taughtStudents", is(2))); // Two unique students for COMPLETED lessons
    }

    @Test
    public void testGetLessonsByCurrentDate() throws Exception {
        lessonRepository.deleteAll();

        Lesson lesson1 = new Lesson();
        lesson1.setStudentId(studentId1);
        lesson1.setDateTime(OffsetDateTime.now());
        lesson1.setTutorId(tutorId1);

        lessonRepository.save(lesson1);

        Lesson lesson2 = new Lesson();
        lesson2.setStudentId(studentId1);
        lesson2.setDateTime(OffsetDateTime.now());
        lesson2.setTutorId(tutorId1);

        lessonRepository.save(lesson2);

        Lesson lesson3 = new Lesson();
        lesson3.setStudentId(studentId1);
        lesson3.setDateTime(OffsetDateTime.now().minusDays(1));
        lesson3.setTutorId(tutorId1);

        lessonRepository.save(lesson3);

        mockMvc.perform(get("/api/lessons")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("date", OffsetDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE))
                        .param("tutorId", tutorId1.toString())
                )
                // Expect HTTP 200 status
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements", is(2)));
    }

    @Test
    public void testGetTutorStatistics_NoCompletedLessons() throws Exception {
        // Clear repository and add a lesson with a status other than COMPLETED
        lessonRepository.deleteAll();

        Lesson lesson = new Lesson();
        lesson.setStudentId(studentId1);
        lesson.setDateTime(OffsetDateTime.now());
        lesson.setTutorId(tutorId1);

        lessonRepository.save(lesson);

        // Send GET request to the "getTutorStatistics" endpoint
        mockMvc.perform(get("/api/lessons/tutor/{tutorId}/statistics", tutorId1)
                        .contentType(MediaType.APPLICATION_JSON))
                // Expect HTTP 200 status
                .andExpect(status().isOk())
                // Validate "taughtStudents" as 0 since no COMPLETED lessons exist
                .andExpect(jsonPath("$.taughtStudents", is(0)));
    }

    @Test
    public void testCountLessonsByTutorAndThisMonth() {
        UUID tutorId = UUID.randomUUID();

        // Create test lessons
        Lesson lesson1 = new Lesson();
        lesson1.setTutorId(tutorId);
        lesson1.setStudentId(UUID.randomUUID());
        lesson1.setStatus(LessonStatus.COMPLETED);
        lesson1.setDateTime(OffsetDateTime.now()); // This month
        lessonRepository.save(lesson1);

        Lesson lesson2 = new Lesson();
        lesson2.setTutorId(tutorId);
        lesson2.setStudentId(UUID.randomUUID());
        lesson2.setStatus(LessonStatus.COMPLETED);
        lesson2.setDateTime(OffsetDateTime.now()); // This month
        lessonRepository.save(lesson2);

        Lesson lesson3 = new Lesson();
        lesson3.setTutorId(tutorId);
        lesson3.setStudentId(UUID.randomUUID());
        lesson3.setStatus(LessonStatus.COMPLETED);
        lesson3.setDateTime(OffsetDateTime.now().minusMonths(1)); // Last month
        lessonRepository.save(lesson3);

        // Fetch statistics
        long count = lessonRepository.countByTutorIdAndStatusAndDateTimeBetween(
                tutorId,
                LessonStatus.COMPLETED,
                YearMonth.now().atDay(1).atStartOfDay().atOffset(ZoneOffset.UTC),
                YearMonth.now().atEndOfMonth().atTime(23, 59, 59).atOffset(ZoneOffset.UTC)
        );

        assertEquals(2, count);
    }

    @Test
    public void testGetLessonCountsByMonth() throws Exception {
        lessonRepository.deleteAll();

        UUID tutorId = UUID.randomUUID();
        UUID studentId = UUID.randomUUID();

        int year = YearMonth.now().getYear();
        int month = YearMonth.now().getMonthValue();

        OffsetDateTime day1 = YearMonth.of(year, month).atDay(10).atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime day2 = YearMonth.of(year, month).atDay(15).atStartOfDay().atOffset(ZoneOffset.UTC);

        Lesson lesson1 = new Lesson();
        lesson1.setTutorId(tutorId);
        lesson1.setStudentId(studentId);
        lesson1.setDateTime(day1);
        lesson1.setDuration(60); 
        lessonRepository.save(lesson1);

        Lesson lesson2 = new Lesson();
        lesson2.setTutorId(tutorId);
        lesson2.setStudentId(studentId);
        lesson2.setDateTime(day1);
        lesson2.setDuration(60); 
        lessonRepository.save(lesson2);

        Lesson lesson3 = new Lesson();
        lesson3.setTutorId(tutorId);
        lesson3.setStudentId(studentId);
        lesson3.setDateTime(day2);
        lesson3.setDuration(60);
        lessonRepository.save(lesson3);

        Lesson lesson4 = new Lesson();
        lesson4.setTutorId(UUID.randomUUID());
        lesson4.setStudentId(studentId);
        lesson4.setDateTime(day1);
        lesson4.setDuration(60);
        lessonRepository.save(lesson4);

        String day1Str = day1.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String day2Str = day2.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // Test with tutorId filter
        String response = mockMvc.perform(get("/api/lessons/month-counts")
                .contentType(MediaType.APPLICATION_JSON)
                .param("year", String.valueOf(year))
                .param("month", String.valueOf(month))
                .param("tutorId", tutorId.toString()))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        System.out.println("[DEBUG_LOG] Response: " + response);
        System.out.println("[DEBUG_LOG] Expected day1: " + day1Str);
        System.out.println("[DEBUG_LOG] Expected day2: " + day2Str);

        // Now add assertions to verify the response
        mockMvc.perform(get("/api/lessons/month-counts")
                .contentType(MediaType.APPLICATION_JSON)
                .param("year", String.valueOf(year))
                .param("month", String.valueOf(month))
                .param("tutorId", tutorId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$." + day1Str).value(2))
                .andExpect(jsonPath("$." + day2Str).value(1));

        // Test with studentId filter
        mockMvc.perform(get("/api/lessons/month-counts")
                .contentType(MediaType.APPLICATION_JSON)
                .param("year", String.valueOf(year))
                .param("month", String.valueOf(month))
                .param("studentId", studentId.toString()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$." + day1Str).value(3))
                .andExpect(jsonPath("$." + day2Str).value(1));
    }
}
