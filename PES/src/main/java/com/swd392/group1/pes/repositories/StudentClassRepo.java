package com.swd392.group1.pes.repositories;


import com.swd392.group1.pes.enums.Grade;
import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.models.StudentClass;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentClassRepo extends JpaRepository<StudentClass, Integer> {
 List<StudentClass> findByClasses_AcademicYearAndClasses_Grade(
         int classes_academicYear, Grade classes_grade
 );
 List<StudentClass> findByClasses_Id(int id);
}
