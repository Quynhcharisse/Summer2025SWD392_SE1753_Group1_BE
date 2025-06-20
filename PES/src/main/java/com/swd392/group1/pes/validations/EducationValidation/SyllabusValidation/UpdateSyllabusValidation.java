package com.swd392.group1.pes.validations.EducationValidation.SyllabusValidation;

import com.swd392.group1.pes.enums.Grade;
import com.swd392.group1.pes.requests.UpdateSyllabusRequest;

import java.util.Arrays;

public class UpdateSyllabusValidation {
    public static String validate(String id, UpdateSyllabusRequest request) {

       if(!CheckSyllabusId.validate(id).trim().isEmpty())
           return CheckSyllabusId.validate(id);

        // Syllabus's subject không điền
        if(request.getSubject().trim().isEmpty()){
           return "Subject cannot be empty";
        }

        //  Description không điền
        if(request.getDescription().trim().isEmpty()){
            return "Description should not be empty";
        }

        // Number of week không điền
        if( request.getMaxNumberOfWeek() <= 0 ){
            return "Number of weeks must be greater than 0";
        }

        if (request.getMaxNumberOfWeek() >= 54) {
            return "Number of weeks must be less than 54 weeks";
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

        return "";
    }
}
