package com.swd392.group1.pes.validations.EducationValidation.SyllabusValidation;

public class CheckSyllabusId {
    public static String validate(String id){
        // ID is empty
        if(id.isEmpty()){
            return "Id cannot be empty";
        }
        // ID wrong format
        try {
         Integer.parseInt(id);
        } catch (IllegalArgumentException ex) {
            return "Id must be a number";
        }
        return "";
    }
}
