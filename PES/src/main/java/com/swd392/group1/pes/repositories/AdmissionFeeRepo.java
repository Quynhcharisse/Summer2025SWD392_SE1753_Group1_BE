package com.swd392.group1.pes.repositories;

import com.swd392.group1.pes.enums.Grade;
import com.swd392.group1.pes.models.AdmissionFee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AdmissionFeeRepo extends JpaRepository<AdmissionFee, Integer> {
    Optional<AdmissionFee> findByAdmissionTerm_Id(int termId);
    List<AdmissionFee> findByAdmissionTermIsNullAndGrade(Grade grade);
    Optional<AdmissionFee> findFirstByAdmissionTermIsNullAndGrade(Grade grade);
}
