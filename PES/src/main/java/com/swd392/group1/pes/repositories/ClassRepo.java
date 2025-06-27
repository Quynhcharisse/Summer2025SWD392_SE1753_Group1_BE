package com.swd392.group1.pes.repositories;

import com.swd392.group1.pes.enums.Grade;
import com.swd392.group1.pes.models.Classes;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassRepo extends JpaRepository<Classes, Integer> {
int countByAcademicYearAndGrade(
        int academicYear, Grade grade
);
}
