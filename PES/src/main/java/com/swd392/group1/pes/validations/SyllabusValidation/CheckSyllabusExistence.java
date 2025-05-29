package com.swd392.group1.pes.validations.SyllabusValidation;

import com.swd392.group1.pes.repositories.SyllabusRepo;

public class CheckSyllabusExistence {
    public static String validate(String id, SyllabusRepo syllabusRepo){
        // ID is empty
        if(id.isEmpty()){
            return "Id cannot be empty";
        }

        // ID wrong format
        try {
            int identityNumber = Integer.parseInt(id);

            // Syllabus không tồn tại hoặc bị xóa
            if(syllabusRepo.findById(identityNumber).isEmpty())
            {
                return "Syllabus with id " + id + " does not exist or be deleted";
            }

        } catch (IllegalArgumentException ex) {
            return "Id must be a number";
        }
        return "";
    }
}
