package com.swd392.group1.pes.repositories;

import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.models.TeacherEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface TeacherEventRepo extends JpaRepository<TeacherEvent, Integer> {
    // Với nhiều teacherId cùng lúc
    List<TeacherEvent> findByTeacherIdInAndEventStatusAndEventStartTimeLessThanAndEventEndTimeGreaterThan(Collection<Integer> teacher_id, Status event_status, LocalDateTime event_startTime, LocalDateTime event_endTime);
}
