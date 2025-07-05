package com.swd392.group1.pes.validations.EducationValidation;

import com.swd392.group1.pes.repositories.LessonRepo;
import com.swd392.group1.pes.requests.CreateLessonRequest;
import com.swd392.group1.pes.requests.UpdateLessonRequest;

public class LessonValidation {
    public static String validateCreate(CreateLessonRequest request, LessonRepo lessonRepo) {

        if (request.getTopic() == null || request.getTopic().trim().isEmpty())
        {
            return "Lesson topic is required";
        }

        if (!request.getTopic().matches("^[a-zA-Z0-9 ]+$")) {
            return "Lesson topic must not contain special characters.";
        }

        if (request.getDescription() == null ) {
            return "Lesson description is required";
        }

        if (request.getObjective() == null || request.getObjective().trim().isEmpty()) {
            return "Lesson objective is required";
        }

        if (request.getDuration() <= 0) {
            return "Lesson duration hours of week must be greater than zero.";
        }

        if (request.getDuration() >= 31) {
            return "Lesson duration hours of week must be less than 31 hours.";
        }

        // Lesson topic da ton tai
        if(lessonRepo.findByTopicIgnoreCase(request.getTopic()).isPresent())
            return "Lesson topic already exists";

        return "";
    }

    public static String validateUpdate(String id, UpdateLessonRequest request) {

        if(!checkLessonId(id).trim().isEmpty())
            return checkLessonId(id);

        if (request.getTopic() == null || request.getTopic().trim().isEmpty())
        {
            return "Lesson topic is required";
        }

        if (!request.getTopic().matches("^[a-zA-Z0-9 ]+$")) {
            return "Lesson topic must not contain special characters.";
        }

        if (request.getDescription() == null ) {
            return "Lesson description is required";
        }

        if (request.getObjective() == null || request.getObjective().trim().isEmpty()) {
            return "Lesson objective is required";
        }

        if (request.getDuration() <= 0) {
            return "Lesson duration hours of week must be greater than zero.";
        }

        if (request.getDuration() >= 41) {
            return "Lesson duration hours of week must be less than 41 hours.";
        }

        return "";
    }

    public static String checkLessonId(String id){
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

