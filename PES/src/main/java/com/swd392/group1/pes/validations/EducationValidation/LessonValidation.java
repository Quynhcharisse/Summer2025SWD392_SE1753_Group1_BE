package com.swd392.group1.pes.validations.EducationValidation;

import com.swd392.group1.pes.repositories.LessonRepo;
import com.swd392.group1.pes.requests.CreateLessonRequest;
import com.swd392.group1.pes.requests.UpdateLessonRequest;

public class LessonValidation {
    public static String validateCreate(CreateLessonRequest request, LessonRepo lessonRepo) {

        if (request.getTitle() == null || request.getTitle().trim().isEmpty())
        {
            return "Lesson topic is required";
        }

        if (request.getDescription() == null ) {
            return "Lesson description is required";
        }

        // Lesson topic da ton tai
        if(lessonRepo.findByTopicIgnoreCase(request.getTitle()).isPresent())
            return "Lesson topic already exists";

        return "";
    }

    public static String validateUpdate(String id, UpdateLessonRequest request, LessonRepo lessonRepo) {

        if(!checkLessonId(id).trim().isEmpty())
            return checkLessonId(id);

        boolean isLessonDuplicate = lessonRepo.existsByTopicIgnoreCaseAndIdNot(request.getTitle(), Integer.parseInt(id));
        if (isLessonDuplicate) {
            return "Lesson already exists";
        }

        if (request.getTitle() == null || request.getTitle().trim().isEmpty())
        {
            return "Lesson topic is required";
        }

        if (request.getDescription() == null ) {
            return "Lesson description is required";
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

