package com.swd392.group1.pes.repositories;

import com.swd392.group1.pes.models.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepo extends JpaRepository<Event, Integer> {
}
