package com.swd392.group1.pes.repositories;

import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.models.AdmissionForm;
import com.swd392.group1.pes.models.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TransactionRepo extends JpaRepository<Transaction, Integer> {
    Optional<Transaction> findByAdmissionFormAndStatus(AdmissionForm admissionForm, Status status);

    Transaction findByAdmissionFormIdAndStatus(Integer id, Status status);

    List<Transaction> findAllByPaymentDateAndStatus(LocalDate paymentDate, Status status);

    int countByStatusAndPaymentDateBetween(Status status, LocalDate startDate, LocalDate endDate);
}
