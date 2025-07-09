package com.swd392.group1.pes.repositories;

import com.swd392.group1.pes.models.Activity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.DayOfWeek;
import java.util.List;

public interface ActivityRepo extends JpaRepository<Activity, Integer> {
    List<Activity> findBySchedule_Id(Integer scheduleId);

    int deleteBySchedule_IdAndDayOfWeek(Integer scheduleId, DayOfWeek dayOfWeek);

}
