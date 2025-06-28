package com.swd392.group1.pes.validations.EducationValidation;

import com.swd392.group1.pes.repositories.EventRepo;
import com.swd392.group1.pes.requests.CreateEventRequest;
import com.swd392.group1.pes.requests.RegisterEventRequest;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;


public class EventValidation {
    public static String validateCreate(CreateEventRequest request, EventRepo eventRepo) {

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            return "Event name is required";
        }

        if (request.getName().length() > 100) {
            return "Event name must not exceed 100 characters";
        }

        if(eventRepo.existsByName(request.getName().trim())){
            return "Event already exists";
        }

        if (request.getStartTime() == null) {
            return "Start time is required";
        }
        if (request.getEndTime() == null) {
            return "End time is required";
        }

        if(!request.getStartTime().isBefore(request.getEndTime())){
          return "Start time must be before end time";
        }

        Duration duration = Duration.between(request.getStartTime(), request.getEndTime());
        if (duration.isNegative() || duration.isZero() || duration.toMinutes() < 15) {
            return "Event duration must be at least 15 minutes";
        }

        if (request.getLocation() == null || request.getLocation().trim().isEmpty()) {
            return "Location is required";
        }

        if (request.getLocation().length() > 200) {
            return "Location must not exceed 200 characters";
        }

        if (request.getDescription() == null || request.getDescription().trim().isEmpty()) {
            return "Event description is required";
        }

        if (request.getRegistrationDeadline() == null) {
            return "Registration Deadline Time is required";
        }
        LocalDateTime now = LocalDateTime.now();
        if (!request.getRegistrationDeadline().isAfter(now.plusDays(1))) {
            return "Registration deadline must be at least one day in the future";
        }
        if (!request.getRegistrationDeadline().isBefore(request.getStartTime())) {
            return "Registration deadline must be before the event start time";
        }
        LocalDateTime minAllowedDeadline = request.getStartTime().minusDays(1);
        if (request.getRegistrationDeadline().isAfter(minAllowedDeadline)) {
            return "Registration deadline must be at least one day before the event start time";
        }

        if (request.getAttachmentImg() == null || request.getAttachmentImg().trim().isEmpty()) {
            return "Event image is required";
        }

        if (request.getHostName() == null || request.getHostName().trim().isEmpty()) {
            return "Host Event Name is required";
        }

        if(request.getEmails() == null || request.getEmails().isEmpty()) {
            return "Please select at least one teacher.";
        }

        return "";
    }
    public static String validateRegisterEvent(RegisterEventRequest request) {
        // 1. Kiểm tra eventId
        String err = checkEventId(request.getEventId());
        if (!err.isEmpty()) {
            return err;
        }

        // 2. Kiểm tra danh sách studentIds có tồn tại, không rỗng
        List<String> ids = request.getStudentIds();
        if (ids == null || ids.isEmpty()) {
            return "Student list must not be empty";
        }

        // 3. Với mỗi studentId, kiểm tra định dạng và giá trị
        for (String sid : ids) {
            String studentErr = checkStudentId(sid);
            if (!studentErr.isEmpty()) {
                return String.format("Invalid studentId '%s': %s", sid, studentErr);
            }
        }

        return "";
    }

    public static  String checkStudentId(String id)
    {
        // ID is empty
        if(id.isEmpty()){
            return "Student Id cannot be empty";
        }
        // ID wrong format
        try {
            Integer.parseInt(id);
        } catch (IllegalArgumentException ex) {
            return "Student Id must be a number";
        }
        return "";
    }

    public static String checkEventId(String id){
        // ID is empty
        if(id.isEmpty()){
            return "Event Id cannot be empty";
        }
        // ID wrong format
        try {
            Integer.parseInt(id);
        } catch (IllegalArgumentException ex) {
            return "Event Id must be a number";
        }
        return "";
    }

}
