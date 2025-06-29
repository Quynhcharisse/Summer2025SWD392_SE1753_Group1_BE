package com.swd392.group1.pes.repositories;

import com.swd392.group1.pes.models.AdmissionTerm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdmissionTermRepo extends JpaRepository<AdmissionTerm, Integer> {
    List<AdmissionTerm> findAllByParentTerm_Id(int parentTermId);

    List<AdmissionTerm> findAllByParentTermIsNull();
}
