package com.swd392.group1.pes.validations.EducationValidation.SyllabusValidation;

import com.swd392.group1.pes.enums.Grade;
import com.swd392.group1.pes.repositories.SyllabusRepo;
import com.swd392.group1.pes.requests.CreateSyllabusRequest;

import java.util.Arrays;

public class CreateSyllabusValidation {
    public static String validate(CreateSyllabusRequest request, SyllabusRepo syllabusRepo) {

        if (syllabusRepo.existsBySubjectIgnoreCase(request.getSubject()))
            return "Syllabus already exists";


        if (request.getSubject().trim().isEmpty()) {
            return "Subject cannot be empty";
        }

        //  Description không điền
        if (request.getDescription().trim().isEmpty()) {
            return "Description should not be empty";
        }

        if (request.getNumberOfWeek() <= 0) {
            return "Number of weeks must be greater than 0";
        }

        if(request.getLessonNames().size() < 3) {
            return "Please select at least 3 lessons for the syllabus";
        }

            // Cần chọn Grade
            if (request.getGrade() == null || request.getGrade().trim().isEmpty()) {
                return "Grade is required";
            }

            // Grade được chọn không tồn tại
            boolean isExistGrade = Arrays.stream(Grade.values())
                    .anyMatch(grade -> grade.getName().equalsIgnoreCase(request.getGrade()));
            if (!isExistGrade) {
                return "Selected grade does not exist.";
            }
            if (!AssignLessonsValidation.validate(request.getLessonNames()).isEmpty())
                return AssignLessonsValidation.validate(request.getLessonNames());

            return "";
        }
    }
