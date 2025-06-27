package com.swd392.group1.pes.repositories;

import com.swd392.group1.pes.enums.Grade;
import com.swd392.group1.pes.models.AdmissionForm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdmissionFormRepo extends JpaRepository<AdmissionForm, Integer> {
    List<AdmissionForm> findAllByParent_IdAndStudent_Id(int parent_id, int student_id);
    List<AdmissionForm> findByAdmissionTerm_YearAndStatusAndTransaction_StatusAndAdmissionTerm_Grade(
            Integer termYear,
            String formStatus,
            String transactionStatus,
            Grade grade
    );
}
