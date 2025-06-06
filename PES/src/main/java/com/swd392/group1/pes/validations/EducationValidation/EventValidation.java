package com.swd392.group1.pes.validations.EducationValidation;

import com.swd392.group1.pes.repositories.EventRepo;
import com.swd392.group1.pes.requests.CreateEventRequest;
import com.swd392.group1.pes.requests.UpdateEventRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class EventValidation {
    public static String validateCreate(CreateEventRequest request) {
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            return "Event name is required";
        }
        if (request.getName().length() > 100) {
            return "Event name must not exceed 100 characters";
        }
        if (request.getDate() == null) {
            return "Event date is required";
        }
        if (request.getDate().isBefore(LocalDate.now())) {
            return "Event date cannot be in the past";
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
        if (request.getRegistrationDeadline() != null) {
            LocalDateTime deadline = LocalDateTime.parse(request.getRegistrationDeadline());
            if (deadline.isAfter(request.getStartTime())) {
                return "Registration deadline must be before the event start time";
            }
        }
        if (request.getLocation() == null || request.getLocation().trim().isEmpty()) {
            return "Location is required";
        }
        if (request.getLocation().length() > 200) {
            return "Location must not exceed 200 characters";
        }
        if (request.getCreatedBy() == null || request.getCreatedBy().trim().isEmpty()) {
            return "CreatedBy is required";
        }
        return "";
    }

    public static String validateUpdate(String id, UpdateEventRequest request, EventRepo eventRepo) {
        int eventId;
        try {
            eventId = Integer.parseInt(id);
        } catch (NumberFormatException e) {
            return "Invalid event ID";
        }
        if (!eventRepo.existsById(eventId)) {
            return "Event not found";
        }
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            return "Event name is required";
        }
        if (request.getName().length() > 100) {
            return "Event name must not exceed 100 characters";
        }
        if (request.getDate() == null) {
            return "Event date is required";
        }
        if (request.getDate().isBefore(LocalDate.now())) {
            return "Event date cannot be in the past";
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
        if (request.getRegistrationDeadline() != null) {
            LocalDateTime deadline = LocalDateTime.parse(request.getRegistrationDeadline());
            if (deadline.isAfter(request.getStartTime())) {
                return "Registration deadline must be before the event start time";
            }
        }
        if (request.getLocation() == null || request.getLocation().trim().isEmpty()) {
            return "Location is required";
        }
        if (request.getLocation().length() > 200) {
            return "Location must not exceed 200 characters";
        }
        return "";
    }
}
