package com.swd392.group1.pes.validations.EducationValidation;

public class ScheduleValidation {

    public static String checkScheduleId(String scheduleId){

        if(scheduleId.isEmpty()){
            return "Schedule Id cannot be empty";
        }

        try {
            Integer.parseInt(scheduleId);
        } catch (IllegalArgumentException ex) {
            return "Schedule Id must be a number";
        }


        return "";
    }

}
