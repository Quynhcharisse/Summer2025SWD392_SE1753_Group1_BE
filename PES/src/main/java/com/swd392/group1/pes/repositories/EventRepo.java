package com.swd392.group1.pes.repositories;

import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.models.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepo extends JpaRepository<Event, Integer> {
  boolean existsByName(String name);
  List<Event> findByStatusAndRegistrationDeadlineLessThanEqual(Status status, LocalDateTime time);
  List<Event> findByStatus(Status status);
  List<Event> findByStartTimeBetween(LocalDateTime from, LocalDateTime to);
}
