package com.swd392.group1.pes.validations.EducationValidation;

import com.swd392.group1.pes.requests.GenerateClassesRequest;

public class ClassValidation {
    public static String validateCreate(GenerateClassesRequest request) {

        if(request.getYear().isEmpty()){
            return "Year cannot be empty";
        }

        try {
            Integer.parseInt(request.getYear());
        } catch (IllegalArgumentException ex) {
            return "Year must be a number";
        }


        return "";
    }

}
