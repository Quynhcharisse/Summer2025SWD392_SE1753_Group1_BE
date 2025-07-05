package com.swd392.group1.pes.repositories;

import com.swd392.group1.pes.models.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScheduleRepo extends JpaRepository<Schedule, Integer> {
    List<Schedule> findByClasses_Id(Integer classId);
}
