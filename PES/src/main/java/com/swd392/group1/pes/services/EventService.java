package com.swd392.group1.pes.services;

import com.swd392.group1.pes.dto.requests.CancelEventRequest;
import com.swd392.group1.pes.dto.requests.CreateEventRequest;
import com.swd392.group1.pes.dto.response.ResponseObject;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;

public interface EventService {

    ResponseEntity<ResponseObject> createEvent(CreateEventRequest request);
    ResponseEntity<ResponseObject> cancelEvent(String id, CancelEventRequest request);
    ResponseEntity<ResponseObject> viewEventList();
    ResponseEntity<ResponseObject> viewEventDetail(String id);
    ResponseEntity<ResponseObject> viewAssignedStudentsOfEvent(String id);
    ResponseEntity<ByteArrayResource> exportEventParticipateOfEvent(String id);
    ResponseEntity<ResponseObject> viewAssignedTeachersOfEvent(String id);
    ResponseEntity<ResponseObject> viewActiveEvents();
}
