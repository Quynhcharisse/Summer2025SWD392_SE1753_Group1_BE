package com.swd392.group1.pes.validations.SyllabusValidation;

import com.swd392.group1.pes.enums.Grade;
import com.swd392.group1.pes.repositories.SyllabusRepo;
import com.swd392.group1.pes.requests.CreateSyllabusRequest;

import java.util.Arrays;

public class CreateSyllabusValidation {



    public static String validate(CreateSyllabusRequest request, SyllabusRepo syllabusRepo){

        // Syllabus da ton tai
        if(syllabusRepo.existsBySubjectIgnoreCase(request.getSubject()))
            return "Syllabus already exists";

        // Syllabus's subject không điền
        if(request.getSubject().trim().isEmpty()){
            return "Subject cannot be empty";
        }

        //  Description không điền
        if(request.getDescription().trim().isEmpty()){
            return "Description should not be empty";
        }


        if( request.getMaxNumberOfWeek() <= 0 ){
            return "Number of weeks must be greater than 0";
        }

            // Grade khong ton tai
        boolean isExistGrade = Arrays.stream(Grade.values())
                .anyMatch(grade -> grade.getName().equalsIgnoreCase(request.getGrade()));
        if (!isExistGrade) {
            return "Grade must exist";
        }

        return "";
    }


}
