package com.mytutorplatform.lessonsservice.repository;

import com.mytutorplatform.lessonsservice.model.RecurringLessonSeries;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface RecurringLessonSeriesRepository extends JpaRepository<RecurringLessonSeries, UUID> {
}