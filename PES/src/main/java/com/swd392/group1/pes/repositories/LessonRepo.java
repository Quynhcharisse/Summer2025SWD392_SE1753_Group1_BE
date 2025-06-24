package com.swd392.group1.pes.repositories;

import com.swd392.group1.pes.models.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LessonRepo extends JpaRepository<Lesson, Integer> {
    Optional<Lesson> findByTopicIgnoreCase(String name);
    List<Lesson> findByTopicContainingIgnoreCase(String keyword);
    boolean existsByTopicIgnoreCaseAndIdNot(String name, int id);
    boolean existsByTopicIgnoreCase(String name);
}
