package com.mytutorplatform.lessonsservice.service;

import com.mytutorplatform.lessonsservice.kafka.producer.LessonEventProducer;
import com.mytutorplatform.lessonsservice.model.Lesson;
import com.mytutorplatform.lessonsservice.model.kafka.Event;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class LessonEventService {

    private final LessonEventProducer lessonEventProducer;

    public LessonEventService(LessonEventProducer lessonEventProducer) {
        this.lessonEventProducer = lessonEventProducer;
    }

    public void handleLessonRescheduled(Lesson lesson, OffsetDateTime newDateTime) {
        Event event = buildEvent(lesson, newDateTime);

        lessonEventProducer.sendLessonUpdateEvent(event);
    }

    private Event buildEvent(Lesson lesson, OffsetDateTime newDateTime) {
        Event event = new Event();
        event.setEventType("LESSON_RESCHEDULED");
        event.setLessonId(lesson.getId());
        event.setStartTime(newDateTime);
        event.setStudentIds(List.of(lesson.getStudentId()).toArray(new UUID[0]));
        event.setTutorId(lesson.getTutorId());
        event.setTimestamp(new Date());
        event.setOldStartTime(lesson.getDateTime());

        return event;
    }
}
