package com.swd392.group1.pes.controllers;

import com.swd392.group1.pes.dto.requests.CancelEventRequest;
import com.swd392.group1.pes.dto.requests.CreateEventRequest;
import com.swd392.group1.pes.dto.requests.ViewEventParticipantRequest;
import com.swd392.group1.pes.dto.response.ResponseObject;
import com.swd392.group1.pes.services.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/education")
public class EventController {

    private final EventService eventService;

    @PostMapping("/event")
    @PreAuthorize("hasRole('education')")
    public ResponseEntity<ResponseObject> createEvent(@RequestBody CreateEventRequest request) {
        return eventService.createEvent(request);
    }

    @GetMapping("/event/list")
    @PreAuthorize("hasRole('education')")
    public ResponseEntity<ResponseObject> viewEventList() {
        return eventService.viewEventList();
    }

    @GetMapping("/event/detail")
    @PreAuthorize("hasRole('education')")
    public ResponseEntity<ResponseObject> viewEventDetail(@RequestParam String id) {
        return eventService.viewEventDetail(id);
    }

    @PutMapping("/event/cancel")
    @PreAuthorize("hasRole('education')")
    public ResponseEntity<ResponseObject> cancelEvent(@RequestParam String id, @RequestBody CancelEventRequest cancelEventRequest) {
        return eventService.cancelEvent(id, cancelEventRequest);
    }

    @GetMapping("/event/assign/teachers")
    @PreAuthorize("hasRole('education')")
    public ResponseEntity<ResponseObject> viewAssignedTeachersOfEvent(@RequestParam String id) {
        return eventService.viewAssignedTeachersOfEvent(id);
    }

    @GetMapping("/event/assign/students")
    @PreAuthorize("hasRole('education')")
    public ResponseEntity<ResponseObject> viewAssignedStudentsOfEvent(@RequestParam String id) {
        return eventService.viewAssignedStudentsOfEvent(id);
    }

    @GetMapping("/event/participants/export")
    @PreAuthorize("hasRole('education')")
    public ResponseEntity<ByteArrayResource> exportEventParticipateOfEvent(@RequestParam String id) {
        return eventService.exportEventParticipateOfEvent(id);
    }

    @PutMapping("/event/numberOfParticipants/stats")
    @PreAuthorize("hasRole('education')")
    public ResponseEntity<ResponseObject> getEventParticipationStats(@RequestBody ViewEventParticipantRequest request) {
        return eventService.getEventParticipationStats(request);
    }

}
