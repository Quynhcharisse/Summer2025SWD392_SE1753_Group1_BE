package com.swd392.group1.pes.repositories;

import com.swd392.group1.pes.enums.Grade;
import com.swd392.group1.pes.enums.Role;
import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.models.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepo extends JpaRepository<Account, Integer> {
    Optional<Account> findByEmail(String email);

    Optional<Account> findByEmailAndStatus(String email, String status);

    Optional<Account> findByEmailAndPassword(String email, String password);

    List<Account> findByRole(Role role);

    List<Account> findByRoleAndStatus(Role role, String status);

    Optional<Account> findByRoleAndEmailAndStatus(Role role, String email, String status);

    boolean existsByEmail(String email);

    long countByRole(Role role);

    Account findByIdAndRole(int teacherId, Role role);

    Account findByClasses_Id(int classId);

    List<Account> findByRoleAndClassesIsEmpty(
            Role role
    );

    List<Account> findByRoleAndClasses_AcademicYearAndClasses_Grade(
            Role role,
            int academicYear,
            Grade grade
    );

}
