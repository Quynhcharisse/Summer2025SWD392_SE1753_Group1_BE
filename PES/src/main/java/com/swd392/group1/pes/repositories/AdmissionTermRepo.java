package com.swd392.group1.pes.repositories;

import com.swd392.group1.pes.models.AdmissionTerm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdmissionTermRepo extends JpaRepository<AdmissionTerm, Integer> {
    Optional<AdmissionTerm> findByYear(int year);
    boolean existsByYear(int year);
}
