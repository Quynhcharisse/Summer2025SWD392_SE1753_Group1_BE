package com.swd392.group1.pes.services.implementors;


import com.swd392.group1.pes.enums.Grade;
import com.swd392.group1.pes.enums.Role;
import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.models.Account;
import com.swd392.group1.pes.models.Classes;
import com.swd392.group1.pes.models.Event;
import com.swd392.group1.pes.models.Lesson;
import com.swd392.group1.pes.models.Syllabus;
import com.swd392.group1.pes.models.SyllabusLesson;
import com.swd392.group1.pes.models.TeacherEvent;
import com.swd392.group1.pes.repositories.AccountRepo;
import com.swd392.group1.pes.repositories.AdmissionFormRepo;
import com.swd392.group1.pes.repositories.ClassRepo;
import com.swd392.group1.pes.repositories.EventRepo;
import com.swd392.group1.pes.repositories.LessonRepo;
import com.swd392.group1.pes.repositories.SyllabusLessonRepo;
import com.swd392.group1.pes.repositories.SyllabusRepo;
import com.swd392.group1.pes.repositories.TeacherEventRepo;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EducationServiceImpl implements EducationService {

    private final SyllabusRepo syllabusRepo;
    private final ClassRepo classRepo;
    private final LessonRepo lessonRepo;
    private final EventRepo eventRepo;
    private final SyllabusLessonRepo syllabusLessonRepo;
    private final AdmissionFormRepo admissionFormRepo;
    private final AccountRepo accountRepo;
    private final TeacherEventRepo teacherEventRepo;


    @Override
    public ResponseEntity<ResponseObject> createSyllabus(CreateSyllabusRequest request) {

        // 1. Validation chung
        String error = CreateSyllabusValidation.validate(request, syllabusRepo);
        if (!AssignLessonsValidation.validate(request.getLessonNames()).trim().isEmpty()) {
            error = AssignLessonsValidation.validate(request.getLessonNames());
        }
        if (!error.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build());
        }

        // 2. Lấy danh sách Lesson hợp lệ
        List<String> requestedNames = request.getLessonNames();
        List<Lesson> validLessons = new ArrayList<>();
        for (String name : requestedNames) {
            lessonRepo.findByTopicIgnoreCase(name)
                    .ifPresent(validLessons::add);
        }

        if (validLessons.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ResponseObject.builder()
                            .message("No lessons found matching the provided names: \n"
                                    + String.join(", ", requestedNames))
                            .success(false)
                            .data(null)
                            .build());
        }

        // 3. Tính tổng duration của các bài học được yêu cầu
        int totalDuration = validLessons.stream()
                .mapToInt(Lesson::getDuration)
                .sum() * request.getMaxNumberOfWeek();

        // 4. Tính maxAllowed từ số tuần
        int maxAllowed = request.getMaxNumberOfWeek() * 40;
        if (totalDuration > maxAllowed) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(ResponseObject.builder()
                            .message("Cannot create syllabus.\n Total lesson duration ("
                                    + totalDuration + "h) exceeds limit ("
                                    + maxAllowed + "h).")
                            .success(false)
                            .data(null)
                            .build());
        }

        // 5. Xây dựng và gán SyllabusLesson
        Syllabus syllabus = Syllabus.builder()
                .subject(request.getSubject())
                .description(request.getDescription())
                .maxNumberOfWeek(request.getMaxNumberOfWeek())
                .maxHoursOfSyllabus(maxAllowed)
                .grade(getGradeFromName(request.getGrade()))
                .createdAt(LocalDateTime.now())
                .build();

        List<SyllabusLesson> joins = validLessons.stream()
                .map(lesson -> SyllabusLesson.builder()
                        .syllabus(syllabus)
                        .lesson(lesson)
                        .build())
                        .toList();
        syllabus.setSyllabusLessonList(joins);

        // 6. Lưu xuống DB
        syllabusRepo.save(syllabus);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ResponseObject.builder()
                        .message("Created Syllabus Successfully")
                        .success(true)
                        .data(null)
                        .build());
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
                        .maxHoursOfSyllabus(request.getMaxNumberOfWeek() * 40)
                        .grade(getGradeFromName(request.getGrade()))
                        .createdAt(syllabusRepo.findById(Integer.parseInt(id)).get().getCreatedAt())
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

        List<Map<String,Object>> syllabusesDetail = syllabuses.stream()
                .sorted(Comparator.comparing(Syllabus::getCreatedAt).reversed())
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
        int maxStudents = admissionFormRepo.countByAdmissionTerm_IdAndStatusAndTransaction_Status(request.getTermId(), Status.APPROVED.getValue(), Status.TRANSACTION_SUCCESSFUL.getValue());
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

        String error = AssignLessonsValidation.validate(request.getLessonNames());

        if(!CheckSyllabusId.validate(id).trim().isEmpty())
            error = CheckSyllabusId.validate(id);

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
        if (validLessons.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ResponseObject.builder()
                            .message("No lessons found matching the provided names: "
                                    + String.join(", ", requestedNames))
                            .success(false)
                            .data(null)
                            .build());
        }
        //5. Tạo bản đồ bài học đã có trong syllabus
        Map<Integer, SyllabusLesson> existingLessonMap = syllabus.getSyllabusLessonList().stream()
                .collect(Collectors.toMap(sl -> sl.getLesson().getId(), sl -> sl));

        // 6. Tính tổng duration sau khi gán thêm
        int existingDuration = syllabus.getSyllabusLessonList().stream()
                .mapToInt(sl -> sl.getLesson().getDuration())
                .sum();

        int addedDuration = validLessons.stream()
                .filter(lesson -> !existingLessonMap.containsKey(lesson.getId()))
                .mapToInt(Lesson::getDuration)
                .sum();

        int totalAfterAssign = (existingDuration + addedDuration) * syllabus.getMaxNumberOfWeek();
        int maxAllowed = syllabus.getMaxHoursOfSyllabus();

        if (totalAfterAssign > maxAllowed) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    ResponseObject.builder()
                            .message("Cannot assign lessons.\n Total duration would be " + totalAfterAssign +
                                    " hours which exceeds the allowed limit (" + maxAllowed + " hours) for syllabus '" +
                                    syllabus.getSubject() + "'.")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

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

// Không cần gọi syllabus.setSyllabusLessonList(updatedList) nữa
        syllabusRepo.save(syllabus);

        return ResponseEntity.ok().body(
                ResponseObject.builder()
                        .message("Unassigned " + removedCount + " lesson(s) from syllabus.")
                        .success(true)
                        .data(null)
                        .build());
    }

    @Override
    public ResponseEntity<ResponseObject> viewAssignedSyllabuses(String id) {
        String error = LessonValidation.checkLessonId(id);

        if(!error.isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build());
        }
        // Lsson không tồn tại hoặc bị xóa
        if(lessonRepo.findById(Integer.parseInt(id)).isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseObject.builder()
                            .message("Lesson with id " + id + " does not exist or be deleted")
                            .success(false)
                            .data(null)
                            .build()
            );
        List<SyllabusLesson> links = syllabusLessonRepo.findByLessonId(Integer.parseInt(id));

        List<Map<String, Object>> assignedSyllabuses = links.stream()
                .map(SyllabusLesson::getSyllabus)
                .map(this::buildSyllabusDetail)
                .toList();
        return  ResponseEntity.ok().body(
                ResponseObject.builder()
                        .message("Assigned Syllabuses list retrieved successfully")
                        .success(true)
                        .data(assignedSyllabuses)
                        .build()
        );
    }

    @Override
    public ResponseEntity<ResponseObject> viewLessonDetail(String id) {
        String error = CheckSyllabusId.validate(id);

        if (!error.isEmpty())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build()
            );

        // Lsson không tồn tại hoặc bị xóa
        if(lessonRepo.findById(Integer.parseInt(id)).isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseObject.builder()
                            .message("Lesson with id " + id + " does not exist or be deleted")
                            .success(false)
                            .data(null)
                            .build()
            );



        Lesson lesson = lessonRepo.findById(Integer.parseInt(id)).get();

        return ResponseEntity.ok().body(
                ResponseObject.builder()
                        .message("View lesson Detail Successfully")
                        .success(true)
                        .data(buildLessonDetail(lesson))
                        .build()
        );
    }


    private Map<String,Object> buildSyllabusDetail(Syllabus syllabus){
        Map<String,Object> data = new HashMap<>();
        data.put("id",syllabus.getId());
        data.put("subject",syllabus.getSubject());
        data.put("description",syllabus.getDescription());
        data.put("maxNumberOfWeek",syllabus.getMaxNumberOfWeek());
        data.put("maxHoursOfSyllabus", syllabus.getMaxHoursOfSyllabus());
        data.put("grade",syllabus.getGrade());
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

        if (request.getToolsRequired() == null || request.getToolsRequired().trim().isEmpty()) {
            request.setToolsRequired("N/A");
                    }
        Lesson lesson = Lesson.builder()
                    .topic(request.getTopic())
                    .description(request.getDescription())
                    .duration(request.getDuration())
                    .objective(request.getObjective())
                    .toolsRequired(request.getToolsRequired())
                    .createdAt(LocalDateTime.now())
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

        List<String> violatedSyllabuses = checkDurationExceedSyllabus(lesson, request.getDuration());
        if (!violatedSyllabuses.isEmpty())
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    ResponseObject.builder()
                            .message("Cannot update duration.\n The following syllabuses would exceed max hours:\n" +
                                    String.join("\n", violatedSyllabuses))
                            .success(false)
                            .data(null)
                            .build()
            );
        lesson.setTopic(request.getTopic());
        lesson.setDescription(request.getDescription());
        lesson.setObjective(request.getObjective());
        lesson.setDuration(request.getDuration());
        if (request.getToolsRequired() == null || request.getToolsRequired().trim().isEmpty()) {
            lesson.setToolsRequired("N/A");
        } else {
            lesson.setToolsRequired(request.getToolsRequired());
        }
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
        // Sắp xếp theo createdAt giảm dần
        lessons.sort(Comparator.comparing(Lesson::getCreatedAt).reversed());
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
    public ResponseEntity<ResponseObject> viewLessonNotAssignedOfSyllabus(String id, String searchQuery) {
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
        String q = (searchQuery == null ? "" : searchQuery.trim().toLowerCase());
        List<Map<String,Object>> unassignedLessons = allLessons.stream()
                .filter(l -> !assignedLessonIds.contains(l.getId()))
                .filter(l ->
                        // a) Nếu q.empty → trả về true cho tất cả
                        q.isEmpty()
                                // b) Hoặc topic chứa q
                                || l.getTopic().toLowerCase().contains(q))
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
    public ResponseEntity<ResponseObject> viewLessonAssignedOfSyllabus(String id, String searchQuery) {

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
        String q = (searchQuery == null ? "" : searchQuery.trim().toLowerCase());
        List<Map<String, Object>> assignedLessons = assignedLinks.stream()
                .map(SyllabusLesson::getLesson)
                .filter(l -> {
                    if (q.isEmpty()) return true;
                    String topic = l.getTopic().toLowerCase();
                    return topic.contains(q);
                })
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
        data.put("objective",lesson.getObjective());
        data.put("duration", lesson.getDuration());
        data.put("toolsRequired",lesson.getToolsRequired());
        return data;
    }


    @Override
    public ResponseEntity<ResponseObject> createEvent(CreateEventRequest request) {
        String error = EventValidation.validateCreate(request, eventRepo);
        if (!error.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        List<String> requestEmails = request.getEmails();
        List<Account> validTeachers = new ArrayList<>();

        List<String> statuses = List.of(Status.ACCOUNT_ACTIVE.getValue(), Status.ACCOUNT_UNBAN.getValue());

        for (String email : requestEmails) {
            accountRepo.findByRoleAndEmailAndStatusIn(Role.TEACHER , email, statuses)
                    .ifPresent(validTeachers::add);
        }

        if(validTeachers.isEmpty())
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ResponseObject.builder()
                            .message("No teachers found matching the provided emails: \n"
                                    + String.join(", ", requestEmails))
                            .success(false)
                            .data(null)
                            .build());

        LocalDateTime newStart = request.getStartTime();
        LocalDateTime newEnd = request.getEndTime();


        List<Integer> validTeacherIds = validTeachers.stream()
                .map(Account::getId)
                .toList();

        // Lấy tất cả TeacherEvent xung đột
        List<TeacherEvent> conflicts = teacherEventRepo
                .findByTeacherIdInAndEventStatusAndEventStartTimeLessThanEqualAndEventEndTimeGreaterThanEqual(
                        validTeacherIds, Status.EVENT_ACTIVE , newEnd, newStart);

        Map<Integer, List<Event>> conflictsByTeacher = new HashMap<>();
        for (TeacherEvent te : conflicts) {
            Integer tid = te.getTeacher().getId();
            Event e = te.getEvent();
            // Thêm event e vào list của tid
            conflictsByTeacher
                    .computeIfAbsent(tid, k -> new ArrayList<>())
                    .add(e);
        }


        // 3. Nếu có xung đột, build message trả về
        if (!conflictsByTeacher.isEmpty()) {
            // Chuẩn bị map id -> email trước để lấy nhanh
            Map<Integer, String> teacherEmailMap = validTeachers.stream()
                    .collect(Collectors.toMap(Account::getId, Account::getEmail));
            List<String> msgs = new ArrayList<>();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            for (Map.Entry<Integer, List<Event>> entry : conflictsByTeacher.entrySet()) {
                Integer tid = entry.getKey();
                String email = teacherEmailMap.get(tid);
                String details = entry.getValue().stream()
                        .map(ev -> {
                            String start = ev.getStartTime().format(formatter);
                            String end = ev.getEndTime().format(formatter);
                            return ev.getName() + " (" + start + " - " + end + ")";
                        })
                        .collect(Collectors.joining("; "));
                msgs.add("Teacher " + email + " has a scheduling conflict with existing event(s): [" + details + "]");
            }
            String fullMsg = "Cannot create event due to schedule conflicts:\n" + String.join("\n", msgs);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    ResponseObject.builder()
                            .message(fullMsg)
                            .success(false)
                            .data(null)
                            .build());
        }
        Event event = Event.builder()
                .name(request.getName())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .location(request.getLocation())
                .description(request.getDescription())
                .createdAt(LocalDateTime.now())
                .registrationDeadline(request.getRegistrationDeadline())
                .status(Status.EVENT_ACTIVE)
                .attachmentImg(request.getAttachmentImg())
                .hostName(request.getHostName())
                .build();

        List<TeacherEvent> joins = validTeachers.stream()
                .map(teacher -> TeacherEvent.builder()
                        .event(event)
                        .teacher(teacher)
                        .build())
                .toList();

        event.setTeacherEventList(joins);
        eventRepo.save(event);
        return ResponseEntity.ok(ResponseObject.builder()
                .message("Event created successfully")
                .success(true)
                .data(null)
                .build());
    }

    @Override
    public ResponseEntity<ResponseObject> updateEvent(String id, UpdateEventRequest request) {
        String error = EventValidation.validateUpdate(id, request);
        if (!error.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build()
            );
        }
        Event event = eventRepo.findById(Integer.parseInt(id)).orElse(null);
        if (event == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseObject.builder()
                            .message("Event not found")
                            .success(false)
                            .data(null)
                            .build()
            );
        }
        event.setName(request.getName());
        event.setStartTime(request.getStartTime());
        event.setEndTime(request.getEndTime());
        event.setLocation(request.getLocation());
        event.setDescription(request.getDescription());
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
                .sorted(Comparator.comparing(Event::getCreatedAt).reversed())
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

        String error = EventValidation.checkEventId(id);

        if(!error.isEmpty())
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        Event event = eventRepo.findById(Integer.parseInt(id)).orElse(null);
        if (event == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder()
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
        data.put("startTime", event.getStartTime());
        data.put("endTime", event.getEndTime());
        data.put("location", event.getLocation());
        data.put("description", event.getDescription());
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

    private List<String> checkDurationExceedSyllabus(Lesson lesson, int newDuration) {

        if (lesson.getDuration() == newDuration) return Collections.emptyList();

        List<SyllabusLesson> assignedLinks = syllabusLessonRepo.findByLessonId(lesson.getId());
        List<String> violatedSyllabuses = new ArrayList<>();

        for (SyllabusLesson sl : assignedLinks) {
            Syllabus syllabus = sl.getSyllabus();
            int maxAllowed = syllabus.getMaxHoursOfSyllabus();
            List<SyllabusLesson> allLessons = syllabus.getSyllabusLessonList();

            int totalDuration = 0;
            for (SyllabusLesson sll : allLessons) {
                Lesson l = sll.getLesson();
                totalDuration += (l.getId().equals(lesson.getId()) ? newDuration : l.getDuration());
            }

            if (totalDuration > maxAllowed) {
                violatedSyllabuses.add(syllabus.getSubject() + " (" + totalDuration + "h > " + maxAllowed + "h)");
            }
        }

        return violatedSyllabuses;
    }

}
