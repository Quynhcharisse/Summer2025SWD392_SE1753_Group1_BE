package com.swd392.group1.pes.validations.EducationValidation;

import com.swd392.group1.pes.enums.Grade;
import com.swd392.group1.pes.requests.GenerateClassesRequest;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class ClassValidation {
    public static String validateCreate(GenerateClassesRequest request, List<Integer> validYears) {

        if (request.getYear().isEmpty()) {
            return "Year cannot be empty";
        }

        int year;
        try {
            year = Integer.parseInt(request.getYear());
        } catch (NumberFormatException ex) {
            return "Year must be a number";
        }

        // ✅ Kiểm tra year có tồn tại trong AdmissionTerm DB
        if (!validYears.contains(year)) {
            return "Year must belong to an existing Admission Term";
        }

        // ✅ Kiểm tra year có phải là năm hiện tại
        int currentYear = LocalDate.now().getYear();
        if (year != currentYear) {
            return String.format("Year must be the current year (%d)", currentYear);
        }

        if(request.getSyllabusId().isEmpty()){
            return "Syllabus Id be empty";
        }

        try {
            Integer.parseInt(request.getSyllabusId());
        } catch (IllegalArgumentException ex) {
            return "Syllabus Id must be a number";
        }

        LocalDate startDate = request.getStartDate();
        if (!startDate.isAfter(LocalDate.now())) {
            return "Start date must be after today: " + LocalDate.now();
        }

        if (startDate.getYear() != Integer.parseInt(request.getYear())) {
            return String.format("Start date must be within the year %s, but was %d", request.getYear(), startDate.getYear());
        }

        if (startDate.getDayOfWeek() != DayOfWeek.MONDAY) {
            return "Start date must be a Monday (the first day of the week).";
        }

        return "";
    }

    public static String checkAcademicYearAndGrade(String year, String grade, List<Integer> validYears) {
        if(year.isEmpty()){
            return "Year cannot be empty";
        }
        try {
            int numberYear = Integer.parseInt(year);

            if (validYears == null || !validYears.contains(numberYear)) {
                return "The academic year you selected does not exist in any admission term. Please verify your selection and try again.";
            }

        } catch (IllegalArgumentException ex) {
            return "Year must be a number";
        }

        if (grade == null || grade.trim().isEmpty()) {
            return "Grade is required";
        }


        boolean isExistGrade = Arrays.stream(Grade.values())
                .anyMatch(gra -> gra.getName().equalsIgnoreCase(grade));
        if (!isExistGrade) {
            return "Selected grade does not exist.";
        }
        return "";
    }

    public static String checkClassId(String classId){

        if(classId.isEmpty()){
            return "Class Id cannot be empty";
        }

        try {
            Integer.parseInt(classId);
        } catch (IllegalArgumentException ex) {
            return "Class Id must be a number";
        }


        return "";
        }}
