package com.swd392.group1.pes.validations.SyllabusValidation;


import com.swd392.group1.pes.models.Lesson;
import com.swd392.group1.pes.repositories.LessonRepo;
import com.swd392.group1.pes.requests.AssignLessonsRequest;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class AssignLessonsValidation {
    public static String validate(String id, AssignLessonsRequest request, LessonRepo lessonRepo) {
        // Danh sach lesson topics bi rong
        List<String> requestedNames = request.getLessonNames();
        if (requestedNames == null || requestedNames.isEmpty()) {
            return "Please select at least one lesson.";}

        // Tìm lesson theo tên topic (bỏ qua hoa thường)
        Set<String> foundTopicNamesLower = new HashSet<>();

        for (String name : requestedNames) {
            Optional<Lesson> lessonOpt = lessonRepo.findByTopicIgnoreCase(name);
            lessonOpt.ifPresent(lesson -> foundTopicNamesLower.add(lesson.getTopic().toLowerCase()));
        }

        // Tìm các tên topic không tồn tại
        List<String> invalidNames = requestedNames.stream()
                .filter(name -> !foundTopicNamesLower.contains(name.toLowerCase()))
                .toList();

        if (!invalidNames.isEmpty()) {
            return "The following lesson(s) could not be found: " + String.join(", ", invalidNames);
        }

        return "";
    }
}
