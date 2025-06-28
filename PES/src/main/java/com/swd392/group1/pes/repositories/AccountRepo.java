package com.swd392.group1.pes.repositories;

import com.swd392.group1.pes.enums.Role;
import com.swd392.group1.pes.models.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepo extends JpaRepository<Account, Integer> {
    Optional<Account> findByEmail(String email);
    Optional<Account> findByEmailAndStatus(String email, String status);
    Optional<Account> findByEmailAndPassword(String email, String password);
    List<Account> findByRole(Role role);
    Optional<Account> findByRoleAndEmailAndStatusIn(Role role, String email, List<String> status);
    boolean existsByEmail(String email);
    long countByRole(Role role);
    Optional<Account> findByCode(String code);
}
