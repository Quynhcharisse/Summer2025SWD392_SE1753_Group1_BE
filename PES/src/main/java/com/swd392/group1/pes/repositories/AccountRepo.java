package com.swd392.group1.pes.repositories;

import com.swd392.group1.pes.models.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepo extends JpaRepository<Account, Integer> {
    Optional<Account> findByEmail(String email);
    Optional<Account> findByEmailAndStatus(String email, String status);
    Optional<Account> findByEmailAndPassword(String email, String password);
    boolean existsByEmail(String email);
}
