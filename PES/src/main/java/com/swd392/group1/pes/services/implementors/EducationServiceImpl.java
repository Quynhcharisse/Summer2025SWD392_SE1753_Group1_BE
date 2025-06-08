package com.swd392.group1.pes.services.implementors;


import com.swd392.group1.pes.enums.Grade;
import com.swd392.group1.pes.enums.Role;
import com.swd392.group1.pes.models.Classes;
import com.swd392.group1.pes.models.Event;
import com.swd392.group1.pes.models.Lesson;
import com.swd392.group1.pes.models.Syllabus;
import com.swd392.group1.pes.repositories.ClassRepo;
import com.swd392.group1.pes.repositories.EventRepo;
import com.swd392.group1.pes.repositories.StudentRepo;
import com.swd392.group1.pes.repositories.LessonRepo;
import com.swd392.group1.pes.repositories.SyllabusRepo;
import com.swd392.group1.pes.requests.CreateLessonRequest;
import com.swd392.group1.pes.requests.CreateSyllabusRequest;
import com.swd392.group1.pes.requests.GenerateClassesRequest;
import com.swd392.group1.pes.requests.UpdateEventRequest;
import com.swd392.group1.pes.requests.UpdateLessonRequest;
import com.swd392.group1.pes.requests.UpdateSyllabusRequest;
import com.swd392.group1.pes.response.ResponseObject;
import com.swd392.group1.pes.services.EducationService;
import com.swd392.group1.pes.requests.CreateEventRequest;
import com.swd392.group1.pes.validations.EducationValidation.EventValidation;
import com.swd392.group1.pes.validations.EducationValidation.LessonValidation;
import com.swd392.group1.pes.validations.SyllabusValidation.CheckSyllabusExistence;
import com.swd392.group1.pes.validations.SyllabusValidation.CreateSyllabusValidation;
import com.swd392.group1.pes.validations.SyllabusValidation.UpdateSyllabusValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EducationServiceImpl implements EducationService {

    private final SyllabusRepo syllabusRepo;
    private final StudentRepo studentRepo;
    private final ClassRepo classRepo;

    private final LessonRepo lessonRepo;
    private final EventRepo eventRepo;


    @Override
    public ResponseEntity<ResponseObject> createSyllabus(CreateSyllabusRequest request) {

        String error = CreateSyllabusValidation.validate(request, syllabusRepo);

        if (!error.isEmpty()) {
            return ResponseEntity.ok().body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        syllabusRepo.save(
                Syllabus.builder()
                        .subject(request.getSubject())
                        .description(request.getDescription())
                        .maxNumberOfWeek(request.getMaxNumberOfWeek())
                        .grade(getGradeFromName(request.getGrade()))
                        .build()
        );

        return ResponseEntity.ok().body(
                ResponseObject.builder()
                        .message("Created Syllabus Successfully")
                        .success(true)
                        .data(null)
                        .build()
        );
    }

    @Override
    public ResponseEntity<ResponseObject> updateSyllabus(String id, UpdateSyllabusRequest request) {

        String error = UpdateSyllabusValidation.validate(id, request, syllabusRepo);

        if(!error.isEmpty()) {
            return ResponseEntity.ok().body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        syllabusRepo.save(
                Syllabus.builder()
                        .id(Integer.parseInt(id))
                        .subject(request.getSubject())
                        .description(request.getDescription())
                        .maxNumberOfWeek(request.getMaxNumberOfWeek())
                        .grade(getGradeFromName(request.getGrade()))
                        .build()
        );

        return ResponseEntity.ok().body(
                ResponseObject.builder()
                        .message("Updated Syllabus Successfully")
                        .success(true)
                        .data(null)
                        .build()
        );

    }

    @Override
    public ResponseEntity<ResponseObject> viewSyllabusDetail(String id) {

        String error = CheckSyllabusExistence.validate(id, syllabusRepo);

        if (!error.isEmpty())
            return ResponseEntity.ok().body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build()
            );

        Syllabus syllabus = syllabusRepo.findById(Integer.parseInt(id)).get();

        return ResponseEntity.ok().body(
                   ResponseObject.builder()
                           .message("View Syllabus Detail Successfully")
                           .success(true)
                           .data(buildSyllabusDetail(syllabus))
                           .build()
        );
    }

    @Override
    public ResponseEntity<ResponseObject> viewAllSyllabus() {
        List<Syllabus> syllabuses = syllabusRepo.findAll();
        List<Map<String,Object>> syllabusesDetail = syllabuses.stream()
                                                    .map(this::buildSyllabusDetail)
                                                    .toList();
        return ResponseEntity.ok().body(
                ResponseObject.builder()
                        .message("View All Syllabuses Successfully")
                        .success(true)
                        .data(syllabusesDetail)
                        .build()
        );
    }

    @Override
    public ResponseEntity<ResponseObject> generateClassesAuto(GenerateClassesRequest request) {
        int maxStudents = studentRepo.findAll().size();
        int maxClasses = (int) Math.ceil((double) maxStudents / request.getNumberStudentsOfEachClass());
        Syllabus syllabus = syllabusRepo.findById(request.getSyllabusId()).get();
        for (int i = 0; i < maxClasses; i++)
        {
            classRepo.save(
              Classes.builder()
                      .name(request.getGrade()+"_"+String.format("%02d", i+1) + "_" + request.getAcademicYear())
                      .numberStudent(request.getNumberStudentsOfEachClass())
                      .academicYear(request.getAcademicYear())
                      .startDate(request.getStartDate())
                      .endDate(request.getEndDate())
                      .status("NOT VERIFIED")
                      .grade(getGradeFromName(request.getGrade()))
                      .syllabus(syllabus)
                      .build()
            );
        }
        return ResponseEntity.ok(
                ResponseObject.builder()
                        .message("Generate Classes Successfully")
                        .success(true)
                        .data(null)
                        .build()
        );
    }


    private Map<String,Object> buildSyllabusDetail(Syllabus syllabus){
        Map<String,Object> data = new HashMap<>();
        data.put("subject",syllabus.getSubject());
        data.put("description",syllabus.getDescription());
        data.put("maxNumberOfWeek",syllabus.getMaxNumberOfWeek());
        data.put("grade",syllabus.getGrade());
        return data;
    }

    @Override
    public ResponseEntity<ResponseObject> createLesson(CreateLessonRequest request) {
        String error = LessonValidation.validateCreate(request);
        if (!error.isEmpty()) {
            return ResponseEntity.ok().body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build()
            );
        }
        Lesson lesson = Lesson.builder()
                .topic(request.getTitle())
                .description(request.getDescription())
                .build();
        lessonRepo.save(lesson);
        return ResponseEntity.ok().body(
                ResponseObject.builder()
                        .message("Lesson created successfully")
                        .success(true)
                        .data(null)
                        .build()
        );
    }

    @Override
    public ResponseEntity<ResponseObject> updateLesson(String id, UpdateLessonRequest request) {
        String error = LessonValidation.validateUpdate(id, request, lessonRepo);
        if (!error.isEmpty()) {
            return ResponseEntity.ok().body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        Lesson lesson = lessonRepo.findById(Integer.parseInt(id)).orElse(null);
        if (lesson == null) {
            return ResponseEntity.ok().body(
                    ResponseObject.builder()
                            .message("Lesson not found")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        lesson.setTopic(request.getTitle());
        lesson.setDescription(request.getDescription());
        lessonRepo.save(lesson);

        return ResponseEntity.ok().body(
                ResponseObject.builder()
                        .message("Lesson updated successfully")
                        .success(true)
                        .data(null)
                        .build()
        );
    }

    @Override
    public ResponseEntity<ResponseObject> viewLessonList() {
        List<Lesson> lessons = lessonRepo.findAll();
        List<Map<String,Object>> lessonDetails = lessons.stream()
                .map(this::buildLessonDetail)
                .toList();
        if (lessons.isEmpty()) {
            return ResponseEntity.ok().body(
                    ResponseObject.builder()
                            .message("No lessons found")
                            .success(false)
                            .data(null)
                            .build()
            );
        }
        return ResponseEntity.ok().body(
                ResponseObject.builder()
                        .message("Lesson list retrieved successfully")
                        .success(true)
                        .data(lessonDetails)
                        .build()
        );
    }

    @Override
    public ResponseEntity<ResponseObject> deleteLesson(String id) {
        int lessonId;
        try {
            lessonId = Integer.parseInt(id);
        } catch (NumberFormatException e) {
            return ResponseEntity.ok().body(
                    ResponseObject.builder()
                            .message("Invalid lesson ID")
                            .success(false)
                            .data(null)
                            .build()
            );
        }
        if (!lessonRepo.existsById(lessonId)) {
            return ResponseEntity.ok().body(
                    ResponseObject.builder()
                            .message("Lesson not found")
                            .success(false)
                            .data(null)
                            .build()
            );
        }
        lessonRepo.deleteById(lessonId);
        return ResponseEntity.ok().body(
                ResponseObject.builder()
                        .message("Lesson deleted successfully")
                        .success(true)
                        .data(null)
                        .build()
        );
    }

    private Map<String,Object> buildLessonDetail(Lesson lesson){
        Map<String,Object> data = new HashMap<>();
        data.put("id", lesson.getId());
        data.put("topic",lesson.getTopic());
        data.put("description",lesson.getDescription());
        return data;
    }

    @Override
    public ResponseEntity<ResponseObject> createEvent(CreateEventRequest request) {
        String error = EventValidation.validateCreate(request);
        if (!error.isEmpty()) {
            return ResponseEntity.ok().body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build()
            );
        }
        Event event = Event.builder()
                .name(request.getName())
                .date(request.getDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .location(request.getLocation())
                .description(request.getDescription())
                .createdBy(Role.EDUCATION.toString())
                .createdAt(LocalDate.now())
                .status(request.getStatus())
                .registrationDeadline(request.getRegistrationDeadline())
                .attachmentImg(request.getAttachmentImg())
                .hostName(request.getHostName())
                .build();
        eventRepo.save(event);
        return ResponseEntity.ok(ResponseObject.builder()
                .message("Event created successfully")
                .success(true)
                .data(null)
                .build());
    }

    @Override
    public ResponseEntity<ResponseObject> updateEvent(String id, UpdateEventRequest request) {
        String error = EventValidation.validateUpdate(id, request, eventRepo);
        if (!error.isEmpty()) {
            return ResponseEntity.ok().body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build()
            );
        }
        int eventId = Integer.parseInt(id);
        Event event = eventRepo.findById(eventId).orElse(null);
        if (event == null) {
            return ResponseEntity.ok().body(
                    ResponseObject.builder()
                            .message("Event not found")
                            .success(false)
                            .data(null)
                            .build()
            );
        }
        event.setName(request.getName());
        event.setDate(request.getDate());
        event.setStartTime(request.getStartTime());
        event.setEndTime(request.getEndTime());
        event.setLocation(request.getLocation());
        event.setDescription(request.getDescription());
        event.setStatus(request.getStatus());
        event.setRegistrationDeadline(request.getRegistrationDeadline());
        event.setAttachmentImg(request.getAttachmentImg());
        event.setHostName(request.getHostName());
        eventRepo.save(event);
        return ResponseEntity.ok(ResponseObject.builder()
                .message("Event updated successfully")
                .success(true)
                .data(null)
                .build());
    }

    @Override
    public ResponseEntity<ResponseObject> viewEventList() {
        List<Event> events = eventRepo.findAll();
        List<Map<String, Object>> eventDetails = events.stream()
                .map(this::buildEventDetail)
                .toList();
        return ResponseEntity.ok(ResponseObject.builder()
                .message("Event list retrieved successfully")
                .success(true)
                .data(eventDetails)
                .build());
    }

    @Override
    public ResponseEntity<ResponseObject> viewEventDetail(String id) {
        int eventId;
        try {
            eventId = Integer.parseInt(id);
        } catch (NumberFormatException e) {
            return ResponseEntity.ok(ResponseObject.builder()
                    .message("Invalid event ID")
                    .success(false)
                    .data(null)
                    .build());
        }
        Event event = eventRepo.findById(eventId).orElse(null);
        if (event == null) {
            return ResponseEntity.ok(ResponseObject.builder()
                    .message("Event not found")
                    .success(false)
                    .data(null)
                    .build());
        }
        return ResponseEntity.ok(ResponseObject.builder()
                .message("Event detail retrieved successfully")
                .success(true)
                .data(buildEventDetail(event))
                .build());
    }

    private Map<String, Object> buildEventDetail(Event event) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", event.getId());
        data.put("name", event.getName());
        data.put("date", event.getDate());
        data.put("startTime", event.getStartTime());
        data.put("endTime", event.getEndTime());
        data.put("location", event.getLocation());
        data.put("description", event.getDescription());
        data.put("createdBy", event.getCreatedBy());
        data.put("createdAt", event.getCreatedAt());
        data.put("status", event.getStatus());
        data.put("registrationDeadline", event.getRegistrationDeadline());
        data.put("attachmentImg", event.getAttachmentImg());
        data.put("hostName", event.getHostName());
        return data;
    }

    private Grade getGradeFromName(String name) {
        for (Grade grade : Grade.values()) {
            if (grade.getName().equalsIgnoreCase(name)) {
                return grade;
            }
        }
        return null;
    }

}
