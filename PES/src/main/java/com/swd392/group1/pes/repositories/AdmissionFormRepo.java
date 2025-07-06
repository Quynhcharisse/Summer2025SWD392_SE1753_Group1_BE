package com.swd392.group1.pes.repositories;

import com.swd392.group1.pes.enums.Grade;
import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.models.AdmissionForm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AdmissionFormRepo extends JpaRepository<AdmissionForm, Integer> {
    List<AdmissionForm> findAllByStudent_IdAndTermItem_IdAndStatusNotIn(int studentId, int termItemId, List<Status> statuses);

    List<AdmissionForm> findByStatus(Status status);

    List<AdmissionForm> findByStatusAndPaymentExpiryDateLessThanEqual(Status status, LocalDateTime paymentExpiryDate);

    //    int countByAdmissionTerm_IdAndStatusAndTransaction_Status(Integer termId, String formStatus, String transactionStatus);
    List<AdmissionForm> findByTermItem_AdmissionTerm_YearAndStatusAndTermItem_Grade(Integer termYear, Status formStatus, Grade grade);

    Long countByStatus(Status status);
}
