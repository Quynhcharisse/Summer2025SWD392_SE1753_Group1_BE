package com.swd392.group1.pes.repositories;

import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.models.EventParticipate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface EventParticipateRepo extends JpaRepository<EventParticipate, Integer> {
    Optional<EventParticipate> findByStudentIdAndEventId(Integer studentId, Integer eventId);

    List<EventParticipate> findAllByStudentId(Integer studentId);

    List<EventParticipate> findByStudentParentIdOrderByRegisteredAtDesc(Integer parentId);

    List<EventParticipate> findAllByEventId(Integer eventId);

    List<EventParticipate> findByEvent_StatusAndEvent_StartTimeBetween(Status status, LocalDateTime startTime, LocalDateTime endTime);
}
