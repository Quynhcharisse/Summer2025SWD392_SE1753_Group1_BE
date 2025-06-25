package com.swd392.group1.pes.validations.EducationValidation.SyllabusValidation;



import java.util.List;


public  class AssignLessonsValidation {
    public static String validate(List<String> lessonNames) {

        if (lessonNames == null || lessonNames.isEmpty()) {
            return "Please select at least one lesson.";}

        if(lessonNames.size() < 3) {
            return "Please select at least 3 lessons for the syllabus";
        }

        return "";
    }
}
