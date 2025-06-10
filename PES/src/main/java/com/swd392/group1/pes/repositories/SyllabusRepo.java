package com.swd392.group1.pes.repositories;

import com.swd392.group1.pes.models.Syllabus;
import org.springframework.data.jpa.repository.JpaRepository;
import javax.security.auth.Subject;

public interface SyllabusRepo extends JpaRepository<Syllabus, Integer> {
    boolean existsBySubjectIgnoreCase(String subject);
    boolean existsBySubjectIgnoreCaseAndIdNot(String subject, Integer id);
}
