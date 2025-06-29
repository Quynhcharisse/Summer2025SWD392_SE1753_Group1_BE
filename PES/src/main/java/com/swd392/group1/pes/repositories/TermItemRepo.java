package com.swd392.group1.pes.repositories;

import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.models.TermItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TermItemRepo extends JpaRepository<TermItem, Integer> {
    Optional<TermItem> findByStatusAndAdmissionTerm_Status(Status status, Status admissionTerm_Status);
}
