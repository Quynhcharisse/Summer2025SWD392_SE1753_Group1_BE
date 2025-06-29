package com.swd392.group1.pes.repositories;

import com.swd392.group1.pes.models.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TransactionRepo extends JpaRepository<Transaction, Integer> {
    //cung cấp phương thức để truy vấn Transaction theo txnRef
    Optional<Transaction> findByTxnRef(String txnRef);
}
