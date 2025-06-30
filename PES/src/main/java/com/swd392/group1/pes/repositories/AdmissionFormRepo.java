package com.swd392.group1.pes.repositories;

import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.models.AdmissionForm;
import com.swd392.group1.pes.models.Student;
import com.swd392.group1.pes.models.TermItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdmissionFormRepo extends JpaRepository<AdmissionForm, Integer> {
    //tìm form của một học sinh trong một TermItem cụ thể với các trạng thái cho phép
    List<AdmissionForm> findAllByStudent_IdAndTermItem_IdAndStatusNotIn(int studentId, int termItemId, List<Status> statuses);

    //tìm form REJECTED hoặc CANCELLED của một học sinh trong một TermItem cụ thể
    List<AdmissionForm> findAllByStudent_IdAndTermItem_IdAndStatusIn(int studentId, int termItemId, List<Status> statuses);

    List<AdmissionForm> findAllByStudentNotNullAndParent_IdAndStatusIn(int parentId, List<Status> statuses);

    List<AdmissionForm> findByStatusAndPaymentExpiryDateBefore(Status status, LocalDateTime paymentExpiryDate);

    List<AdmissionForm> findByStatus(Status status);

    Optional<Object> findByStudentAndTermItemAndStatus(Student student, TermItem appropriateTermItem, Status status);

    List<AdmissionForm> findByStatusAndPaymentExpiryDate(Status status, LocalDateTime now);

//    int countByAdmissionTerm_IdAndStatusAndTransaction_Status(Integer termId, String formStatus, String transactionStatus);
}
