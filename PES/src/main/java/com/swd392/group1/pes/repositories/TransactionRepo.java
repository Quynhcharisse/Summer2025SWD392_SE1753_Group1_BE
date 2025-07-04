package com.swd392.group1.pes.repositories;

import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.models.AdmissionForm;
import com.swd392.group1.pes.models.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransactionRepo extends JpaRepository<Transaction, Integer> {
    Optional<Transaction> findByTxnRef(String txnRef);//truy vấn Transaction theo txnRef

    Optional<Transaction> findByAdmissionFormAndStatus(AdmissionForm admissionForm, Status status);

    Transaction findByAdmissionFormIdAndStatus(Integer id, Status status);
}
