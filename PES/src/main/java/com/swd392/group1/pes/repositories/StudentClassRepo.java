package com.swd392.group1.pes.repositories;


import com.swd392.group1.pes.enums.Grade;
import com.swd392.group1.pes.models.StudentClass;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentClassRepo extends JpaRepository<StudentClass, Integer> {
    List<StudentClass> findByClasses_AcademicYearAndClasses_Grade(int classes_academicYear, Grade classes_grade);

    List<StudentClass> findByClasses_Id(int id);

    List<StudentClass> findByStudent_Id(int id);

    List<StudentClass> findByClasses_IdAndStudent_IdIn(int classId, List<Integer> studentIds);

    int countByClasses_Id(int classId);

    boolean existsByStudent_IdAndClasses_AcademicYearAndClasses_Grade(Integer student_id, int classes_academicYear, Grade classes_grade);
}
