package com.swd392.group1.pes.repositories;

import com.swd392.group1.pes.enums.Grade;
import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.models.AdmissionForm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdmissionFormRepo extends JpaRepository<AdmissionForm, Integer> {
    //tìm form của một học sinh trong một TermItem cụ thể với các trạng thái cho phép
    List<AdmissionForm> findAllByStudent_IdAndTermItem_IdAndStatusNotIn(int studentId, int termItemId,List<Status> statuses);

    //tìm form REJECTED hoặc CANCELLED của một học sinh trong một TermItem cụ thể
    List<AdmissionForm> findAllByStudent_IdAndTermItem_IdAndStatusIn(int studentId, int termItemId, List<Status> statuses);

    List<AdmissionForm> findAllByStudentNotNullAndParent_IdAndStatusIn(int parentId, List<Status> statuses);

    List<AdmissionForm> findAllByParent_IdAndStudent_Id(int parent_id, int student_id);
    List<AdmissionForm> findByAdmissionTerm_YearAndStatusAndTransaction_StatusAndAdmissionTerm_Grade(
            Integer termYear,
            String formStatus,
            String transactionStatus,
            Grade grade
    );
}
