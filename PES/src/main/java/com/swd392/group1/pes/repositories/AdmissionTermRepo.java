package com.swd392.group1.pes.repositories;

import com.swd392.group1.pes.enums.Grade;
import com.swd392.group1.pes.models.AdmissionTerm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdmissionTermRepo extends JpaRepository<AdmissionTerm, Integer> {
    List<AdmissionTerm> findByGrade(Grade grade);

    long countByYearAndGrade(int year, Grade grade);

    List<AdmissionTerm> findAllByParentTerm_Id(int parentTermId);

    List<AdmissionTerm> findAllByParentTermIsNull();
}
