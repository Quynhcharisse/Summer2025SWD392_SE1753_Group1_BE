package com.swd392.group1.pes.repositories;

import com.swd392.group1.pes.enums.Grade;
import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.models.TermItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TermItemRepo extends JpaRepository<TermItem, Integer> {
    List<TermItem> findAllByGradeAndStatusAndAdmissionTerm_Year(Grade grade, Status status, int year);
}
