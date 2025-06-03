package com.swd392.group1.pes.repositories;

import com.swd392.group1.pes.models.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonRepo extends JpaRepository<Lesson, Integer>
{
}
