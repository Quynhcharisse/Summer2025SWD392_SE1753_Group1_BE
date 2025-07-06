package com.swd392.group1.pes.repositories;

import com.swd392.group1.pes.models.SyllabusLesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SyllabusLessonRepo extends JpaRepository<SyllabusLesson, Integer> {
    List<SyllabusLesson> findBySyllabusId(int id);

    List<SyllabusLesson> findByLessonId(int id);
}
