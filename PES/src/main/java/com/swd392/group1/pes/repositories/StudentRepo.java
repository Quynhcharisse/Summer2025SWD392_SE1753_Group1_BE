package com.swd392.group1.pes.repositories;

import com.swd392.group1.pes.models.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentRepo extends JpaRepository<Student, Integer> {
    List<Student> findAllByParent_Id(int id);
}
