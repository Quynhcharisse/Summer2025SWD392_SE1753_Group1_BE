package com.swd392.group1.pes.validations.SyllabusValidation;

import com.swd392.group1.pes.enums.Grade;
import com.swd392.group1.pes.repositories.SyllabusRepo;
import com.swd392.group1.pes.requests.UpdateSyllabusRequest;

public class UpdateSyllabusValidation {

    public static String validate(String id, UpdateSyllabusRequest request, SyllabusRepo syllabusRepo) {

       CheckSyllabusExistence.validate(id, syllabusRepo);

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

        // Grade khong ton tai
        if(fromName(request.getGrade()) == null)
            return "Grade does not exist";

        return "";

    }

    private static Grade fromName(String name) {
        for (Grade grade : Grade.values()) {
            if (grade.getName().equalsIgnoreCase(name)) {
                return grade;
            }
        }
        return null;
    }

    }
