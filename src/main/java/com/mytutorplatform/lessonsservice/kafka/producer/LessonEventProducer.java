package com.mytutorplatform.lessonsservice.kafka.producer;

import com.mytutorplatform.lessonsservice.model.kafka.Event;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LessonEventProducer {
    private static final Logger logger = LoggerFactory.getLogger(LessonEventProducer.class);

    private static final String TOPIC = "lesson-events";

    private final KafkaTemplate<String, Event> kafkaTemplate;

    public void sendLessonUpdateEvent(Event event) {
        logger.info("Sending event {} to topic: {}", event, TOPIC);

        kafkaTemplate.send(TOPIC, event);
    }
}