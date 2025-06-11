package com.swd392.group1.pes.validations.EducationValidation.SyllabusValidation;


import com.swd392.group1.pes.requests.AssignLessonsRequest;

import java.util.List;


public class AssignLessonsValidation {
    public static String validate(String id, AssignLessonsRequest request) {

        if(!CheckSyllabusId.validate(id).trim().isEmpty())
            return CheckSyllabusId.validate(id);

        // Danh sach lesson topics bi rong
        List<String> requestedNames = request.getLessonNames();
        if (requestedNames == null || requestedNames.isEmpty()) {
            return "Please select at least one lesson.";}

        return "";
    }
}
