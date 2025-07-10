package com.swd392.group1.pes.repositories;

import com.swd392.group1.pes.enums.Grade;
import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.models.Classes;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface ClassRepo extends JpaRepository<Classes, Integer> {
    int countByAcademicYearAndGrade(int academicYear, Grade grade);

    List<Classes> findByAcademicYearAndGrade(int academicYear, Grade grade);

    List<Classes> findByStatusAndStartDateLessThanEqual(String status, LocalDate startDate);

    List<Classes> findByStatusAndEndDateLessThan(String status, LocalDate endDate);

    int countByAcademicYear(int year);

    boolean existsByName(String name);

}
