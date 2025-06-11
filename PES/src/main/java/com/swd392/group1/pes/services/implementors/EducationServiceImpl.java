package com.swd392.group1.pes.services.implementors;


import com.swd392.group1.pes.enums.Grade;
import com.swd392.group1.pes.enums.Role;
import com.swd392.group1.pes.models.Classes;
import com.swd392.group1.pes.models.Event;
import com.swd392.group1.pes.models.Lesson;
import com.swd392.group1.pes.models.Syllabus;
import com.swd392.group1.pes.models.SyllabusLesson;
import com.swd392.group1.pes.repositories.ClassRepo;
import com.swd392.group1.pes.repositories.EventRepo;
import com.swd392.group1.pes.repositories.LessonRepo;
import com.swd392.group1.pes.repositories.StudentRepo;
import com.swd392.group1.pes.repositories.SyllabusLessonRepo;
import com.swd392.group1.pes.repositories.SyllabusRepo;
import com.swd392.group1.pes.requests.CreateLessonRequest;
import com.swd392.group1.pes.requests.AssignLessonsRequest;
import com.swd392.group1.pes.requests.CreateSyllabusRequest;
import com.swd392.group1.pes.requests.GenerateClassesRequest;
import com.swd392.group1.pes.requests.UnassignLessonsRequest;
import com.swd392.group1.pes.requests.UpdateEventRequest;
import com.swd392.group1.pes.requests.UpdateLessonRequest;
import com.swd392.group1.pes.requests.UpdateSyllabusRequest;
import com.swd392.group1.pes.response.ResponseObject;
import com.swd392.group1.pes.services.EducationService;
import com.swd392.group1.pes.requests.CreateEventRequest;
import com.swd392.group1.pes.validations.EducationValidation.EventValidation;
import com.swd392.group1.pes.validations.EducationValidation.LessonValidation;
import com.swd392.group1.pes.validations.EducationValidation.SyllabusValidation.AssignLessonsValidation;
import com.swd392.group1.pes.validations.EducationValidation.SyllabusValidation.CheckSyllabusId;
import com.swd392.group1.pes.validations.EducationValidation.SyllabusValidation.CreateSyllabusValidation;
import com.swd392.group1.pes.validations.EducationValidation.SyllabusValidation.UnassignLessonsValidation;
import com.swd392.group1.pes.validations.EducationValidation.SyllabusValidation.UpdateSyllabusValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class EducationServiceImpl implements EducationService {

    private final SyllabusRepo syllabusRepo;
    private final StudentRepo studentRepo;
    private final ClassRepo classRepo;
    private final LessonRepo lessonRepo;
    private final EventRepo eventRepo;
    private final SyllabusLessonRepo syllabusLessonRepo;


    @Override
    public ResponseEntity<ResponseObject> createSyllabus(CreateSyllabusRequest request) {

        String error = CreateSyllabusValidation.validate(request, syllabusRepo);

        if (!error.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
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

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ResponseObject.builder()
                        .message("Created Syllabus Successfully")
                        .success(true)
                        .data(null)
                        .build()
        );
    }

    @Override
    public ResponseEntity<ResponseObject> updateSyllabus(String id, UpdateSyllabusRequest request) {

        String error = UpdateSyllabusValidation.validate(id, request);

        if(!error.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        // Syllabus không tồn tại hoặc bị xóa
        if(syllabusRepo.findById(Integer.parseInt(id)).isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseObject.builder()
                            .message("Syllabus with id " + id + " does not exist or be deleted")
                            .success(false)
                            .data(null)
                            .build()
            );


        // ✅ Check subject trùng với syllabus khác
        boolean isSubjectDuplicate = syllabusRepo.existsBySubjectIgnoreCaseAndIdNot(request.getSubject(), Integer.parseInt(id));
        if (isSubjectDuplicate) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    ResponseObject.builder()
                            .message("Syllabus already exists")
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

        String error = CheckSyllabusId.validate(id);

        if (!error.isEmpty())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build()
            );

        // Syllabus không tồn tại hoặc bị xóa
        if(syllabusRepo.findById(Integer.parseInt(id)).isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseObject.builder()
                            .message("Syllabus with id " + id + " does not exist or be deleted")
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

        // Chia syllabus thành 2 nhóm
        List<Syllabus> notAssigned = new ArrayList<>();
        List<Syllabus> assigned = new ArrayList<>();

        for (Syllabus s : syllabuses) {
            if (!s.isAssigned()) {
                notAssigned.add(s);
            } else {
                assigned.add(s);
            }
        }

        List<Map<String, Object>> syllabusesDetail = Stream.concat(
                notAssigned.stream().map(this::buildSyllabusDetail),
                assigned.stream().map(this::buildSyllabusDetail)
        ).toList();

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
//                      .name(request.getGrade()+"_"+String.format("%02d", i+1) + "_" + request.getAcademicYear())
                      .numberStudent(request.getNumberStudentsOfEachClass())
//                      .academicYear(request.getAcademicYear())
                      .startDate(request.getStartDate())
                      .endDate(request.getEndDate())
                      .status("NOT VERIFIED")
//                      .grade(getGradeFromName(request.getGrade()))
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

    @Override
    public ResponseEntity<ResponseObject> assignLessonsToSyllabus(String id, AssignLessonsRequest request) {

        String error = AssignLessonsValidation.validate(id, request, lessonRepo);

        if (!error.isEmpty())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build()
            );

        // Syllabus không tồn tại hoặc bị xóa
        if(syllabusRepo.findById(Integer.parseInt(id)).isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseObject.builder()
                            .message("Syllabus with id " + id + " does not exist or be deleted")
                            .success(false)
                            .data(null)
                            .build()
            );



        Syllabus syllabus = syllabusRepo.findById(Integer.parseInt(id)).get();

        // 4. Lấy danh sách bài học theo tên
        List<String> requestedNames = request.getLessonNames();
        List<Lesson> validLessons = new ArrayList<>();

        for (String name : requestedNames) {
            lessonRepo.findByTopicIgnoreCase(name)
                    .ifPresent(validLessons::add); // đảm bảo đúng thứ tự nếu cần
        }

        //5. Tạo bản đồ bài học đã có trong syllabus
        Map<Integer, SyllabusLesson> existingLessonMap = syllabus.getSyllabusLessonList().stream()
                .collect(Collectors.toMap(sl -> sl.getLesson().getId(), sl -> sl));

        List<SyllabusLesson> updatedList = syllabus.getSyllabusLessonList();
        int newlyAddedCount = 0;

        for (Lesson lesson : validLessons) {
            if (!existingLessonMap.containsKey(lesson.getId())) {
                SyllabusLesson newLink = SyllabusLesson.builder()
                        .syllabus(syllabus)
                        .lesson(lesson)
                        .build();
                updatedList.add(newLink);
                newlyAddedCount++; // đếm số lesson mới được add
            }
        }

        if (newlyAddedCount == 0) {
            return ResponseEntity.ok().body(
                    ResponseObject.builder()
                            .message("No new lessons were added.")
                            .success(true)
                            .data(null)
                            .build());
        }

        syllabus.setSyllabusLessonList(updatedList);
        syllabus.setAssigned(true);
        syllabusRepo.save(syllabus);



        return ResponseEntity.ok().body(
                ResponseObject.builder()
                        .message("Added " + newlyAddedCount + " new lesson(s) to syllabus.")
                        .success(true)
                        .data(null)
                        .build()
        );
    }

    @Override
    public ResponseEntity<ResponseObject> unassignLessonsFromSyllabus(String id, UnassignLessonsRequest request) {

        String error = UnassignLessonsValidation.validate(id, request);

        if (!error.isEmpty())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build()
            );

        // Syllabus không tồn tại hoặc bị xóa
        if(syllabusRepo.findById(Integer.parseInt(id)).isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseObject.builder()
                            .message("Syllabus with id " + id + " does not exist or be deleted")
                            .success(false)
                            .data(null)
                            .build()
            );

        Optional<Syllabus> optionalSyllabus = syllabusRepo.findById(Integer.parseInt(id));
        if (optionalSyllabus.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseObject.builder()
                            .message("Syllabus with id " + id + " does not exist or has been deleted")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        Syllabus syllabus = optionalSyllabus.get();

        List<String> namesToRemove = request.getLessonNames().stream()
                .map(name -> name.trim().toLowerCase().replaceAll("\\s+", " "))
                .toList();

        List<SyllabusLesson> currentList = syllabus.getSyllabusLessonList();

// Lưu lại số lượng ban đầu
        int beforeSize = currentList.size();

// Xóa theo điều kiện
        currentList.removeIf(sl -> namesToRemove.contains(
                sl.getLesson().getTopic().trim().toLowerCase().replaceAll("\\s+", " ")
        ));

        int removedCount = beforeSize - currentList.size();

        if (removedCount == 0) {
            return ResponseEntity.ok().body(
                    ResponseObject.builder()
                            .message("No lessons were unassigned.")
                            .success(true)
                            .data(null)
                            .build()
            );
        }

        if (currentList.isEmpty()) syllabus.setAssigned(false);

// Không cần gọi syllabus.setSyllabusLessonList(updatedList) nữa
        syllabusRepo.save(syllabus);

        return ResponseEntity.ok().body(
                ResponseObject.builder()
                        .message("Unassigned " + removedCount + " lesson(s) from syllabus.")
                        .success(true)
                        .data(null)
                        .build());
    }


    private Map<String,Object> buildSyllabusDetail(Syllabus syllabus){
        Map<String,Object> data = new HashMap<>();
        data.put("id",syllabus.getId());
        data.put("subject",syllabus.getSubject());
        data.put("description",syllabus.getDescription());
        data.put("maxNumberOfWeek",syllabus.getMaxNumberOfWeek());
        data.put("grade",syllabus.getGrade());
        data.put("isAssigned",syllabus.isAssigned());
        return data;
    }

    @Override
    public ResponseEntity<ResponseObject> createLesson(CreateLessonRequest request) {
        String error = LessonValidation.validateCreate(request, lessonRepo);
        if (!error.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build()
            );
        }
        Lesson lesson = Lesson.builder()
                .topic(request.getTopic())
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
        String error = LessonValidation.validateUpdate(id, request);
        if (!error.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        Lesson lesson = lessonRepo.findById(Integer.parseInt(id)).orElse(null);
        if (lesson == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseObject.builder()
                            .message("Lesson with id " + id + " not found")
                            .success(false)
                            .data(null)
                            .build()
            );
        }
        boolean isLessonDuplicate = lessonRepo.existsByTopicIgnoreCaseAndIdNot(request.getTopic(), Integer.parseInt(id));
        if (isLessonDuplicate) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    ResponseObject.builder()
                            .message("Lesson already exists")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        lesson.setTopic(request.getTopic());
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

        return ResponseEntity.ok().body(
                ResponseObject.builder()
                        .message("Lesson list retrieved successfully")
                        .success(true)
                        .data(lessonDetails)
                        .build()
        );
    }

    @Override
    public ResponseEntity<ResponseObject> viewLessonNotAssignedOfSyllabus(String id) {
        String error = CheckSyllabusId.validate(id);

        if (!error.isEmpty())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build()
            );

        // Syllabus không tồn tại hoặc bị xóa
        if(syllabusRepo.findById(Integer.parseInt(id)).isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseObject.builder()
                            .message("Syllabus with id " + id + " does not exist or be deleted")
                            .success(false)
                            .data(null)
                            .build()
            );

        List<SyllabusLesson> assigned = syllabusLessonRepo.findBySyllabusId(Integer.parseInt(id));
        Set<Integer> assignedLessonIds = assigned.stream()
                .map(sl -> sl.getLesson().getId())
                .collect(Collectors.toSet());

        List<Lesson> allLessons = lessonRepo.findAll();
        List<Map<String,Object>> unassignedLessons = allLessons.stream()
                .filter(l -> !assignedLessonIds.contains(l.getId()))
                .map(this::buildLessonDetail)
                .toList();

        return ResponseEntity.ok().body(
                ResponseObject.builder()
                        .message("Unassigned Lessons list retrieved successfully")
                        .success(true)
                        .data(unassignedLessons)
                        .build()
        );
    }

    @Override
    public ResponseEntity<ResponseObject> viewLessonAssignedOfSyllabus(String id) {

        String error = CheckSyllabusId.validate(id);

        if (!error.isEmpty())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build()
            );

        // Syllabus không tồn tại hoặc bị xóa
        if(syllabusRepo.findById(Integer.parseInt(id)).isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseObject.builder()
                            .message("Syllabus with id " + id + " does not exist or be deleted")
                            .success(false)
                            .data(null)
                            .build()
            );

        // Lấy danh sách bài học đã gán
        List<SyllabusLesson> assignedLinks = syllabusLessonRepo.findBySyllabusId(Integer.parseInt(id));
        List<Map<String, Object>> assignedLessons = assignedLinks.stream()
                .map(SyllabusLesson::getLesson)
                .map(this::buildLessonDetail)
                .toList();

        return ResponseEntity.ok().body(
                ResponseObject.builder()
                        .message("Assigned Lessons list retrieved successfully")
                        .success(true)
                        .data(assignedLessons)
                        .build()
        );    }


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
