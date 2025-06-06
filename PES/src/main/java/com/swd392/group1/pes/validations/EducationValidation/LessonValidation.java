package com.swd392.group1.pes.validations.EducationValidation;

import com.swd392.group1.pes.repositories.LessonRepo;
import com.swd392.group1.pes.requests.CreateLessonRequest;
import com.swd392.group1.pes.requests.UpdateLessonRequest;

public class LessonValidation {
    public static String validateCreate(CreateLessonRequest request) {
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            return "Lesson title is required";
        }
        if (request.getTitle().length() > 100) {
            return "Lesson title must not exceed 100 characters";
        }
        if (request.getDescription() != null && request.getDescription().length() > 500) {
            return "Lesson description must not exceed 500 characters";
        }
        return "";
    }

    public static String validateUpdate(String id, UpdateLessonRequest request, LessonRepo lessonRepo) {
        int lessonId;
        try {
            lessonId = Integer.parseInt(id);
        } catch (NumberFormatException e) {
            return "Invalid lesson ID";
        }
        if (!lessonRepo.existsById(lessonId)) {
            return "Lesson not found";
        }
        if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
            return "Lesson title is required";
        }
        if (request.getTitle().length() > 100) {
            return "Lesson title must not exceed 100 characters";
        }
        if (request.getDescription() != null && request.getDescription().length() > 500) {
            return "Lesson description must not exceed 500 characters";
        }
        return "";
    }
}
