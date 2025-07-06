package com.swd392.group1.pes.services;

import com.swd392.group1.pes.dto.requests.CreateLessonRequest;
import com.swd392.group1.pes.dto.requests.UpdateLessonRequest;
import com.swd392.group1.pes.dto.response.ResponseObject;
import org.springframework.http.ResponseEntity;

public interface LessonService {
    ResponseEntity<ResponseObject> viewLessonDetail(String id);
    ResponseEntity<ResponseObject> createLesson(CreateLessonRequest request);
    ResponseEntity<ResponseObject> updateLesson(String id, UpdateLessonRequest request);
    ResponseEntity<ResponseObject> viewLessonList(String searchQuery);
    ResponseEntity<ResponseObject> viewAssignedSyllabuses(String id);

}
