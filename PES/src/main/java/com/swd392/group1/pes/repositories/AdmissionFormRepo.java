package com.swd392.group1.pes.repositories;

import com.swd392.group1.pes.enums.Grade;
import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.models.AdmissionForm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdmissionFormRepo extends JpaRepository<AdmissionForm, Integer> {

    List<AdmissionForm> findAllByStudent_IdAndTermItem_IdAndStatusNotIn(int studentId, int termItemId,List<Status> statuses);

    List<AdmissionForm> findAllByStudent_IdAndTermItem_IdAndStatusIn(int studentId, int termItemId, List<Status> statuses);

    List<AdmissionForm> findAllByStudentNotNullAndParent_IdAndStatusIn(int parentId, List<Status> statuses);

    List<AdmissionForm> findByTermItem_AdmissionTerm_YearAndStatusAndTransaction_StatusAndTermItem_Grade(
            Integer termYear,
            Status formStatus,
            Status transactionStatus,
            Grade grade
    );
}
