package com.swd392.group1.pes.validations.EducationValidation.SyllabusValidation;

import com.swd392.group1.pes.models.Lesson;
import com.swd392.group1.pes.repositories.LessonRepo;
import com.swd392.group1.pes.requests.AssignLessonsRequest;
import com.swd392.group1.pes.requests.UnassignLessonsRequest;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class UnassignLessonsValidation {
    public static String validate(String id, UnassignLessonsRequest request) {

        if(!CheckSyllabusId.validate(id).trim().isEmpty())
            return CheckSyllabusId.validate(id);

        // Danh sach lesson topics bi rong
        List<String> requestedNames = request.getLessonNames();
        if (requestedNames == null || requestedNames.isEmpty()) {
            return "Please select at least one lesson.";}

        return "";

    }
}
