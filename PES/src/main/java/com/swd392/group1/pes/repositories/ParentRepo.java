package com.swd392.group1.pes.repositories;

import com.swd392.group1.pes.models.Parent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ParentRepo extends JpaRepository<Parent, Integer> {
    Optional<Parent> findByAccount_Id(int id);
}