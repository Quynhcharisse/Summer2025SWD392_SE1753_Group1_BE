package com.swd392.group1.pes.validations.EducationValidation;

import com.swd392.group1.pes.repositories.EventRepo;
import com.swd392.group1.pes.requests.CreateEventRequest;
import com.swd392.group1.pes.requests.UpdateEventRequest;

import java.time.Duration;


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

    public static String validateUpdate(String id, UpdateEventRequest request) {

        if(!checkEventId(id).isEmpty()){
           return checkEventId(id);
        }

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            return "Event name is required";
        }
        if (request.getName().length() > 100) {
            return "Event name must not exceed 100 characters";
        }

        if (request.getStartTime() == null) {
            return "Start time is required";
        }
        if (request.getEndTime() == null) {
            return "End time is required";
        }
        if (request.getStartTime().isAfter(request.getEndTime())) {
            return "Start time must be before end time";
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

        if (request.getAttachmentImg() == null || request.getAttachmentImg().trim().isEmpty()) {
            return "Event image is required";
        }

        if (request.getHostName() == null || request.getHostName().trim().isEmpty()) {
            return "Host Event Name is required";
        }

        return "";
    }

    public static String checkEventId(String id){
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
