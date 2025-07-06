package com.swd392.group1.pes.services.implementors;

import com.swd392.group1.pes.dto.requests.AssignLessonsRequest;
import com.swd392.group1.pes.dto.requests.CancelEventRequest;
import com.swd392.group1.pes.dto.requests.CreateEventRequest;
import com.swd392.group1.pes.dto.requests.CreateLessonRequest;
import com.swd392.group1.pes.dto.requests.CreateSyllabusRequest;
import com.swd392.group1.pes.dto.requests.GenerateClassesRequest;
import com.swd392.group1.pes.dto.requests.RegisterEventRequest;
import com.swd392.group1.pes.dto.requests.UpdateLessonRequest;
import com.swd392.group1.pes.dto.requests.UpdateSyllabusRequest;
import com.swd392.group1.pes.dto.response.ResponseObject;
import com.swd392.group1.pes.enums.Grade;
import com.swd392.group1.pes.enums.Role;
import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.models.Account;
import com.swd392.group1.pes.models.Activity;
import com.swd392.group1.pes.models.AdmissionForm;
import com.swd392.group1.pes.models.AdmissionTerm;
import com.swd392.group1.pes.models.Classes;
import com.swd392.group1.pes.models.Event;
import com.swd392.group1.pes.models.EventParticipate;
import com.swd392.group1.pes.models.Lesson;
import com.swd392.group1.pes.models.Parent;
import com.swd392.group1.pes.models.Schedule;
import com.swd392.group1.pes.models.Student;
import com.swd392.group1.pes.models.StudentClass;
import com.swd392.group1.pes.models.Syllabus;
import com.swd392.group1.pes.models.SyllabusLesson;
import com.swd392.group1.pes.models.TeacherEvent;
import com.swd392.group1.pes.repositories.AccountRepo;
import com.swd392.group1.pes.repositories.ActivityRepo;
import com.swd392.group1.pes.repositories.AdmissionFormRepo;
import com.swd392.group1.pes.repositories.AdmissionTermRepo;
import com.swd392.group1.pes.repositories.ClassRepo;
import com.swd392.group1.pes.repositories.EventParticipateRepo;
import com.swd392.group1.pes.repositories.EventRepo;
import com.swd392.group1.pes.repositories.LessonRepo;
import com.swd392.group1.pes.repositories.ScheduleRepo;
import com.swd392.group1.pes.repositories.StudentClassRepo;
import com.swd392.group1.pes.repositories.StudentRepo;
import com.swd392.group1.pes.repositories.SyllabusLessonRepo;
import com.swd392.group1.pes.repositories.SyllabusRepo;
import com.swd392.group1.pes.repositories.TeacherEventRepo;
import com.swd392.group1.pes.repositories.TermItemRepo;
import com.swd392.group1.pes.requests.AssignLessonsRequest;
import com.swd392.group1.pes.requests.CancelEventRequest;
import com.swd392.group1.pes.requests.CreateEventRequest;
import com.swd392.group1.pes.requests.CreateLessonRequest;
import com.swd392.group1.pes.requests.CreateSyllabusRequest;
import com.swd392.group1.pes.requests.GenerateClassesRequest;
import com.swd392.group1.pes.requests.UpdateLessonRequest;
import com.swd392.group1.pes.requests.UpdateSyllabusRequest;
import com.swd392.group1.pes.response.ResponseObject;
import com.swd392.group1.pes.services.EducationService;
import com.swd392.group1.pes.services.MailService;
import com.swd392.group1.pes.utils.email.Format;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
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
    private final EventParticipateRepo eventParticipateRepo;
    private final MailService mailService;
    private final AdmissionTermRepo admissionTermRepo;
    private final ScheduleRepo scheduleRepo;
    private final ActivityRepo activityRepo;
    private final StudentClassRepo studentClassRepo;
    private final TermItemRepo termItemRepo;
    private final StudentRepo studentRepo;


    @Override
    public ResponseEntity<ResponseObject> createSyllabus(CreateSyllabusRequest request) {

        // 1. Validation chung
        String error = createSyllabusValidation(request, syllabusRepo);
        if (!assignLessonsValidation(request.getLessonNames()).trim().isEmpty()) {
            error = assignLessonsValidation(request.getLessonNames());
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

        int totalDuration = validLessons.stream()
                .mapToInt(Lesson::getDuration)
                .sum();


        if (totalDuration != 30) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body(ResponseObject.builder()
                            .message("Cannot create syllabus.\n Total after assignment would be " + totalDuration + " hours per week, but syllabus '" + request.getSubject() + "' must have exactly 30 hours per week.\n Adjust your lessons accordingly.")
                            .success(false)
                            .data(null)
                            .build());
        }

        // 5. Xây dựng và gán SyllabusLesson
        Syllabus syllabus = Syllabus.builder()
                .subject(request.getSubject())
                .description(request.getDescription())
                .numberOfWeek(request.getNumberOfWeek())
                .hoursOfSyllabus(request.getNumberOfWeek() * 30)
                .grade(getGradeFromName(request.getGrade()))
                .createdAt(LocalDateTime.now())
                .assignedToClasses(false)
                .build();

        List<SyllabusLesson> joins = validLessons.stream()
                .map(lesson -> SyllabusLesson.builder()
                        .syllabus(syllabus)
                        .lesson(lesson)
                        .build())
                .toList();
        syllabus.setSyllabusLessonList(joins);
        syllabusRepo.save(syllabus);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ResponseObject.builder()
                        .message("Created Syllabus Successfully")
                        .success(true)
                        .data(null)
                        .build());
    }

    public static String createSyllabusValidation(CreateSyllabusRequest request, SyllabusRepo syllabusRepo) {

        if (syllabusRepo.existsBySubjectIgnoreCase(request.getSubject()))
            return "Syllabus already exists";


        if (request.getSubject().trim().isEmpty()) {
            return "Subject cannot be empty";
        }

        //  Description không điền
        if (request.getDescription().trim().isEmpty()) {
            return "Description should not be empty";
        }

        if (request.getNumberOfWeek() <= 0) {
            return "Number of weeks must be greater than 0";
        }

        if (request.getLessonNames().size() < 3) {
            return "Please select at least 3 lessons for the syllabus";
        }

        // Cần chọn Grade
        if (request.getGrade() == null || request.getGrade().trim().isEmpty()) {
            return "Grade is required";
        }

        // Grade được chọn không tồn tại
        boolean isExistGrade = Arrays.stream(Grade.values())
                .anyMatch(grade -> grade.getName().equalsIgnoreCase(request.getGrade()));
        if (!isExistGrade) {
            return "Selected grade does not exist.";
        }
        if (!assignLessonsValidation(request.getLessonNames()).isEmpty())
            return assignLessonsValidation(request.getLessonNames());

        return "";
    }

//    public static String assignLessonsValidation(List<String> lessonNames) {
//
//        if (lessonNames == null || lessonNames.isEmpty()) {
//            return "Please select at least one lesson.";
//        }
//
//        if (lessonNames.size() < 3) {
//            return "Please select at least 3 lessons for the syllabus";
//        }
//
//        return ""; xem kĩ hẵn bỏ
//    }

    @Override
    public ResponseEntity<ResponseObject> updateSyllabus(String id, UpdateSyllabusRequest request) {

        String error = updateSyllabusValidation(id, request);

        if (!error.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        // Syllabus không tồn tại hoặc bị xóa
        if (syllabusRepo.findById(Integer.parseInt(id)).isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseObject.builder()
                            .message("Syllabus with id " + id + " does not exist or be deleted")
                            .success(false)
                            .data(null)
                            .build()
            );

        if (syllabusRepo.findById(Integer.parseInt(id)).get().isAssignedToClasses()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    ResponseObject.builder()
                            .message("Cannot update syllabus that is already assigned to classes")
                            .success(false)
                            .data(null)
                            .build()
            );
        }


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

        List<SyllabusLesson> list = syllabusLessonRepo.findBySyllabusId(Integer.parseInt(id));
        syllabusRepo.save(
                Syllabus.builder()
                        .id(Integer.parseInt(id))
                        .subject(request.getSubject())
                        .description(request.getDescription())
                        .numberOfWeek(request.getNumberOfWeek())
                        .hoursOfSyllabus(request.getNumberOfWeek() * 30)
                        .grade(getGradeFromName(request.getGrade()))
                        .createdAt(syllabusRepo.findById(Integer.parseInt(id)).get().getCreatedAt())
                        .syllabusLessonList(list)
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

    public static String updateSyllabusValidation(String id, UpdateSyllabusRequest request) {

        if(!checkSyllabusId(id).trim().isEmpty())
            return checkSyllabusId(id);


        if(request.getSubject().trim().isEmpty()){
            return "Subject cannot be empty";
        }

        //  Description không điền
        if(request.getDescription().trim().isEmpty()){
            return "Description should not be empty";
        }

        // Number of week không điền
        if( request.getNumberOfWeek() <= 0 ){
            return "Number of weeks must be greater than 0";
        }

        // Cần chọn Grade
        if (request.getGrade() == null || request.getGrade().trim().isEmpty()) {
            return "Grade is required";
        }

        // Grade được chọn không tồn tại
        boolean isExistGrade = Arrays.stream(Grade.values())
                .anyMatch(grade -> grade.getName().equalsIgnoreCase(request.getGrade()));
        if (!isExistGrade) {
            return "Selected grade does not exist.";
        }

        return "";
    }

    @Override
    public ResponseEntity<ResponseObject> viewSyllabusDetail(String id) {

        String error = checkSyllabusId(id);

        if (!error.isEmpty())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build()
            );

        // Syllabus không tồn tại hoặc bị xóa
        if (syllabusRepo.findById(Integer.parseInt(id)).isEmpty())
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

    public static String checkSyllabusId(String id){
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


    @Override
    public ResponseEntity<ResponseObject> viewAllSyllabus() {

        List<Syllabus> syllabuses = syllabusRepo.findAll();

        List<Map<String, Object>> syllabusesDetail = syllabuses.stream()
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
    public ResponseEntity<ResponseObject> assignLessonsToSyllabus(String id, AssignLessonsRequest request) {
        // 1. Validate request.getLessonNames()
        List<String> requestedNames = request.getLessonNames();
        String error = assignLessonsValidation(requestedNames);
        // Nếu validation của ID cũng trả lỗi, ưu tiên thông báo ID lỗi
        String idError = checkSyllabusId(id);
        if (!idError.trim().isEmpty()) {
            error = idError;
        }
        if (!error.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build()
            );
        }


        Syllabus syllabus = syllabusRepo.findById(Integer.parseInt(id)).get();

        if (syllabus.isAssignedToClasses()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    ResponseObject.builder()
                            .message("Cannot assign lessons to syllabus because syllabus is already assigned to classes")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        // 3. Lấy danh sách Lesson hợp lệ theo topic (theo thứ tự request nếu cần):
        List<Lesson> validLessons = new ArrayList<>();
        for (String name : requestedNames) {
            lessonRepo.findByTopicIgnoreCase(name).ifPresent(validLessons::add);
        }
        // Nếu muốn báo lỗi khi một số tên không tồn tại, có thể so sánh requestedNames vs validLessons:
        if (validLessons.isEmpty() && !requestedNames.isEmpty()) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ResponseObject.builder()
                            .message("No lessons found matching the provided names: "
                                    + String.join(", ", requestedNames))
                            .success(false)
                            .data(null)
                            .build());
        }
        // Nếu requestedNames rỗng (UI gửi mảng rỗng, muốn clear hết), validLessons sẽ rỗng.

        // 4. Lấy map các lesson hiện có trong syllabus
        List<SyllabusLesson> existingList = syllabus.getSyllabusLessonList();
        Map<Integer, SyllabusLesson> existingLessonMap = existingList.stream()
                .collect(Collectors.toMap(sl -> sl.getLesson().getId(), sl -> sl));


        int totalAfterAssign = validLessons.stream()
                .mapToInt(Lesson::getDuration)
                .sum();


        if (totalAfterAssign != 30) {
            String msg = String.format(
                    "Assignment invalid: \n Total after assignment would be %d hours per week, but \n syllabus '%s' must have exactly 30 hours per week.\n Adjust your lessons accordingly.",
                    totalAfterAssign, syllabus.getSubject()
            );
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    ResponseObject.builder()
                            .message(msg)
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        // 7. Xác định lessons cần xóa (unassign) và cần thêm (assign)
        Set<Integer> requestedIds = validLessons.stream()
                .map(Lesson::getId)
                .collect(Collectors.toSet());
        Set<Integer> existingIds = existingLessonMap.keySet();

        // Những lesson cũ không nằm trong requestedIds => cần unassign
        List<SyllabusLesson> toRemoveLinks = existingList.stream()
                .filter(sl -> !requestedIds.contains(sl.getLesson().getId()))
                .toList();
        // Những lesson mới trong requestedIds nhưng không nằm trong existingIds => cần add
        List<Lesson> toAddLessons = validLessons.stream()
                .filter(l -> !existingIds.contains(l.getId()))
                .toList();
        // 8. Thực hiện cập nhật danh sách SyllabusLesson:
        // Xóa trước
        int removedCount = 0;
        if (!toRemoveLinks.isEmpty()) {
            for (SyllabusLesson slink : toRemoveLinks) {
                existingList.remove(slink);
                syllabusLessonRepo.delete(slink);
                removedCount++;
            }
        }
        // Thêm mới
        int addedCount = 0;
        for (Lesson lesson : toAddLessons) {
            SyllabusLesson newLink = SyllabusLesson.builder()
                    .syllabus(syllabus)
                    .lesson(lesson)
                    .build();
            existingList.add(newLink);
            addedCount++;
        }
        // Cập nhật lại list
        syllabus.setSyllabusLessonList(existingList);

        syllabusRepo.save(syllabus);

        // 10. Chuẩn bị thông điệp trả về
        if (addedCount == 0 && removedCount == 0) {
            return ResponseEntity.ok().body(
                    ResponseObject.builder()
                            .message("No change: the syllabus lessons remain unchanged.")
                            .success(true)
                            .data(null)
                            .build()
            );
        } else {
            String msg = String.format(
                    "Updated syllabus: %d lesson(s) added, %d lesson(s) removed.",
                    addedCount, removedCount
            );
            return ResponseEntity.ok().body(
                    ResponseObject.builder()
                            .message(msg)
                            .success(true)
                            .data(null)
                            .build()
            );
        }
    }

    public static String assignLessonsValidation(List<String> lessonNames) {

        if (lessonNames == null || lessonNames.isEmpty()) {
            return "Please select at least one lesson.";}

        if(lessonNames.size() < 3) {
            return "Please select at least 3 lessons for the syllabus";
        }

        return "";
    }

    @Override
    public ResponseEntity<ResponseObject> viewAssignedSyllabuses(String id) {
        String error = checkLessonIdValid(id);

        if (!error.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build());
        }
        // Lsson không tồn tại hoặc bị xóa
        if (lessonRepo.findById(Integer.parseInt(id)).isEmpty())
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
        return ResponseEntity.ok().body(
                ResponseObject.builder()
                        .message("Assigned Syllabuses list retrieved successfully")
                        .success(true)
                        .data(assignedSyllabuses)
                        .build()
        );
    }

    public static String checkLessonIdValid(String id) {
        // ID is empty
        if (id.isEmpty()) {
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

    @Override
    public ResponseEntity<ResponseObject> viewLessonDetail(String id) {
        String error = checkSyllabusId(id);

        if (!error.isEmpty())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build()
            );

        // Lsson không tồn tại hoặc bị xóa
        if (lessonRepo.findById(Integer.parseInt(id)).isEmpty())
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


    private Map<String, Object> buildSyllabusDetail(Syllabus syllabus) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", syllabus.getId());
        data.put("subject", syllabus.getSubject());
        data.put("description", syllabus.getDescription());
        data.put("numberOfWeek", syllabus.getNumberOfWeek());
        data.put("maxHoursOfSyllabus", syllabus.getHoursOfSyllabus());
        data.put("grade", syllabus.getGrade());
        return data;
    }

    @Override
    public ResponseEntity<ResponseObject> createLesson(CreateLessonRequest request) {
        String error = lessonCreateValidation(request, lessonRepo);
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

        boolean isLessonDuplicate = lessonRepo.existsByTopicIgnoreCase(request.getTopic());
        if (isLessonDuplicate) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    ResponseObject.builder()
                            .message("Lesson already exists")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        lessonRepo.save(lesson);
        return ResponseEntity.ok().body(
                ResponseObject.builder()
                        .message("Lesson created successfully")
                        .success(true)
                        .data(null)
                        .build()
        );
    }

    private String lessonCreateValidation(CreateLessonRequest request, LessonRepo lessonRepo) {

        if (request.getTopic() == null || request.getTopic().trim().isEmpty()) {
            return "Lesson topic is required";
        }

        if (!request.getTopic().matches("^[a-zA-Z0-9 ]+$")) {
            return "Lesson topic must not contain special characters.";
        }

        if (request.getDescription() == null) {
            return "Lesson description is required";
        }

        if (request.getObjective() == null || request.getObjective().trim().isEmpty()) {
            return "Lesson objective is required";
        }

        if (request.getDuration() <= 0) {
            return "Lesson duration hours of week must be greater than zero.";
        }

        if (request.getDuration() >= 31) {
            return "Lesson duration hours of week must be less than 31 hours.";
        }

        // Lesson topic da ton tai
        if (lessonRepo.findByTopicIgnoreCase(request.getTopic()).isPresent())
            return "Lesson topic already exists";

        return "";
    }

    @Override
    public ResponseEntity<ResponseObject> updateLesson(String id, UpdateLessonRequest request) {
        String error = lessonUpdateValidate(id, request);
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
        boolean assigned = lesson.getSyllabusLessonList().stream()
                .map(SyllabusLesson::getSyllabus)
                .anyMatch(Syllabus::isAssignedToClasses);
        if (assigned) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    ResponseObject.builder()
                            .message("Cannot update lesson that is already assigned to classes")
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

        List<String> violatedSyllabuses = checkDurationMatchSyllabus(lesson, request.getDuration());
        if (!violatedSyllabuses.isEmpty())
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    ResponseObject.builder()
                            .message("Cannot update duration.\n The following syllabuses would not meet required hours:\n" +
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

    private String lessonUpdateValidate(String id, UpdateLessonRequest request) {

        if (!checkLessonIdValid(id).trim().isEmpty())
            return checkLessonIdValid(id);

        if (request.getTopic() == null || request.getTopic().trim().isEmpty()) {
            return "Lesson topic is required";
        }

        if (!request.getTopic().matches("^[a-zA-Z0-9 ]+$")) {
            return "Lesson topic must not contain special characters.";
        }

        if (request.getDescription() == null) {
            return "Lesson description is required";
        }

        if (request.getObjective() == null || request.getObjective().trim().isEmpty()) {
            return "Lesson objective is required";
        }

        if (request.getDuration() <= 0) {
            return "Lesson duration hours of week must be greater than zero.";
        }

        if (request.getDuration() >= 41) {
            return "Lesson duration hours of week must be less than 41 hours.";
        }

        return "";
    }


    @Override
    public ResponseEntity<ResponseObject> viewLessonList(String searchQuery) {
        List<Lesson> lessons;
        if (searchQuery != null && !searchQuery.isBlank()) {
            lessons = lessonRepo.findByTopicContainingIgnoreCase(searchQuery.trim());
        } else {
            lessons = lessonRepo.findAll();
        }
        // Sắp xếp theo createdAt giảm dần
        lessons.sort(Comparator.comparing(Lesson::getCreatedAt).reversed());
        List<Map<String, Object>> lessonDetails = lessons.stream()
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
        String error = checkSyllabusId(id);
        if (!error.isEmpty())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build()
            );

        // Syllabus không tồn tại hoặc bị xóa
        if (syllabusRepo.findById(Integer.parseInt(id)).isEmpty())
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
        List<Map<String, Object>> unassignedLessons = allLessons.stream()
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

        String error = checkSyllabusId(id);

        if (!error.isEmpty())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build()
            );

        // Syllabus không tồn tại hoặc bị xóa
        if (syllabusRepo.findById(Integer.parseInt(id)).isEmpty())
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
        );
    }


    private Map<String, Object> buildLessonDetail(Lesson lesson) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", lesson.getId());
        data.put("topic", lesson.getTopic());
        data.put("description", lesson.getDescription());
        data.put("objective", lesson.getObjective());
        data.put("duration", lesson.getDuration());
        data.put("toolsRequired", lesson.getToolsRequired());
        return data;
    }


    @Override
    public ResponseEntity<ResponseObject> createEvent(CreateEventRequest request) {
        String error = validateCreateEvent(request, eventRepo);
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
            accountRepo.findByRoleAndEmailAndStatusIn(Role.TEACHER, email, statuses)
                    .ifPresent(validTeachers::add);
        }

        if (validTeachers.isEmpty())
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(ResponseObject.builder()
                            .message("No teachers found matching the provided emails: \n"
                                    + String.join(", ", requestEmails))
                            .success(false)
                            .data(null)
                            .build());

        LocalDateTime newStart = request.getStartTime().atZone(ZoneOffset.UTC).withZoneSameInstant(ZoneId.of("Asia/Ho_Chi_Minh")).toLocalDateTime();
        LocalDateTime newEnd = request.getEndTime().atZone(ZoneOffset.UTC).withZoneSameInstant(ZoneId.of("Asia/Ho_Chi_Minh")).toLocalDateTime();


        List<Integer> validTeacherIds = validTeachers.stream()
                .map(Account::getId)
                .toList();

        // Lấy tất cả TeacherEvent xung đột
        List<TeacherEvent> conflicts = teacherEventRepo
                .findByTeacherIdInAndEventStatusAndEventStartTimeLessThanAndEventEndTimeGreaterThan(
                        validTeacherIds, Status.EVENT_REGISTRATION_ACTIVE, newEnd, newStart);

        Map<Integer, List<Event>> conflictsByTeacher = new HashMap<>();
        for (TeacherEvent te : conflicts) {
            Integer tid = te.getTeacher().getId();
            Event e = te.getEvent();
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
                .startTime(newStart)
                .endTime(newEnd)
                .location(request.getLocation())
                .description(request.getDescription())
                .createdAt(LocalDateTime.now())
                .registrationDeadline(request.getRegistrationDeadline().atZone(ZoneOffset.UTC)
                        .withZoneSameInstant(ZoneId.of("Asia/Ho_Chi_Minh")).toLocalDateTime())
                .status(Status.EVENT_REGISTRATION_ACTIVE)
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

    public static String validateCreateEvent(CreateEventRequest request, EventRepo eventRepo) {

        if (request.getName() == null || request.getName().trim().isEmpty()) {
            return "Event name is required";
        }

        if (request.getName().length() > 100) {
            return "Event name must not exceed 100 characters";
        }

        if (eventRepo.existsByName(request.getName().trim())) {
            return "Event already exists";
        }

        if (request.getStartTime() == null) {
            return "Start time is required";
        }
        if (request.getEndTime() == null) {
            return "End time is required";
        }

        if (!request.getStartTime().atZone(ZoneOffset.UTC)
                .withZoneSameInstant(ZoneId.of("Asia/Ho_Chi_Minh")).toLocalDateTime()
                .isBefore(request.getEndTime().atZone(ZoneOffset.UTC)
                        .withZoneSameInstant(ZoneId.of("Asia/Ho_Chi_Minh")).toLocalDateTime())) {
            return "Start time must be before end time";
        }

        Duration duration = Duration.between(request.getStartTime().atZone(ZoneOffset.UTC)
                        .withZoneSameInstant(ZoneId.of("Asia/Ho_Chi_Minh")).toLocalDateTime(),
                request.getEndTime().atZone(ZoneOffset.UTC)
                        .withZoneSameInstant(ZoneId.of("Asia/Ho_Chi_Minh")).toLocalDateTime());
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
        if (!request.getRegistrationDeadline().atZone(ZoneOffset.UTC)
                .withZoneSameInstant(ZoneId.of("Asia/Ho_Chi_Minh")).toLocalDateTime()
                .isAfter(now.plusDays(1))) {
            return "Registration deadline must be at least one day in the future";
        }
        if (!request.getRegistrationDeadline().atZone(ZoneOffset.UTC)
                .withZoneSameInstant(ZoneId.of("Asia/Ho_Chi_Minh")).toLocalDateTime().
                isBefore(request.getStartTime().atZone(ZoneOffset.UTC)
                        .withZoneSameInstant(ZoneId.of("Asia/Ho_Chi_Minh")).toLocalDateTime())) {
            return "Registration deadline must be before the event start time";
        }
        LocalDateTime minAllowedDeadline = request.getStartTime().atZone(ZoneOffset.UTC)
                .withZoneSameInstant(ZoneId.of("Asia/Ho_Chi_Minh")).toLocalDateTime()
                .minusDays(1);
        if (request.getRegistrationDeadline().isAfter(minAllowedDeadline)) {
            return "Registration deadline must be at least one day before the event start time";
        }

        if (request.getAttachmentImg() == null || request.getAttachmentImg().trim().isEmpty()) {
            return "Event image is required";
        }

        if (request.getHostName() == null || request.getHostName().trim().isEmpty()) {
            return "Host Event Name is required";
        }

        if (request.getEmails() == null || request.getEmails().isEmpty()) {
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

    public static String checkStudentId(String id) {
        // ID is empty
        if (id.isEmpty()) {
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

    public static String checkEventId(String id) {
        // ID is empty
        if (id.isEmpty()) {
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


    @Override
    public ResponseEntity<ResponseObject> cancelEvent(String id, CancelEventRequest request) {
        String error = checkEventId(id);
        if (!error.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        if (request.getReason() == null || request.getReason().trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .message("Reason of cancel event is required")
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

        // 3. Chỉ cho phép hủy khi đang mở đăng ký
        if (!Status.EVENT_REGISTRATION_ACTIVE.equals(event.getStatus())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    ResponseObject.builder()
                            .message("Only events with status active can be canceled")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        event.setStatus(Status.EVENT_CANCELLED);
        eventRepo.save(event);

        List<EventParticipate> parts = eventParticipateRepo.findAllByEventId(Integer.parseInt(id));
        for (EventParticipate ep : parts) {
            Student stu = ep.getStudent();
            Parent parent = stu.getParent();
            if (parent != null && parent.getAccount() != null) {
                String parentEmail = parent.getAccount().getEmail();

                String subject = "[PES] Event Cancelled";
                String header = String.format("Event \"%s\" Cancelled", event.getName());
                String body = Format.getCancelEventForParentBody(parent.getAccount().getName(),
                        stu.getName(),
                        ep.getEvent().getName(), ep.getEvent().getStartTime(),
                        request.getReason());
                mailService.sendMail(parentEmail, subject, header, body);
            }
        }

        for (TeacherEvent te : event.getTeacherEventList()) {
            Account teacher = te.getTeacher();
            if (teacher != null && teacher.getEmail() != null) {
                String subj = "Event Cancelled";
                String heading = "Event \"" + event.getName() + "\" Cancelled";
                String body = Format.getCancelEventForTeacherBody(teacher.getName(),
                        te.getEvent().getName(),
                        te.getEvent().getStartTime(),
                        request.getReason());
                mailService.sendMail(teacher.getEmail(), subj, heading, body);
            }
        }

        return ResponseEntity.ok(ResponseObject.builder()
                .message("Cancel event successfully")
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

        String error = checkEventId(id);

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

    @Override
    public ResponseEntity<ResponseObject> viewAssignedTeachersOfEvent(String id) {
        String error = checkEventId(id);

        if (!error.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build());
        }
        // Lsson không tồn tại hoặc bị xóa
        if (eventRepo.findById(Integer.parseInt(id)).isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseObject.builder()
                            .message("Event with id " + id + " does not exist or be deleted")
                            .success(false)
                            .data(null)
                            .build()
            );
        Event event = eventRepo.findById(Integer.parseInt(id)).get();
        List<TeacherEvent> tes = Optional.ofNullable(event.getTeacherEventList()).orElse(List.of());

        // Build danh sách teacher detail
        List<Map<String, Object>> teacherDetails = tes.stream()
                .map(TeacherEvent::getTeacher)
                .filter(Objects::nonNull)
                .filter(account -> {
                    String status = account.getStatus();
                    return status != null && (
                            status.equalsIgnoreCase("active") ||
                                    status.equalsIgnoreCase("unban")
                    );
                })
                .map(account -> {
                    Map<String, Object> tMap = new HashMap<>();
                    tMap.put("id", account.getId());
                    tMap.put("email", account.getEmail());
                    tMap.put("name", account.getName());
                    tMap.put("phone", account.getPhone());
                    tMap.put("gender", account.getGender());
                    tMap.put("avatarUrl", account.getAvatarUrl());
                    tMap.put("role", account.getRole());
                    return tMap;
                })
                .toList();

        return ResponseEntity.ok(
                ResponseObject.builder()
                        .message("Assigned teachers of event retrieved successfully")
                        .success(true)
                        .data(teacherDetails)
                        .build()
        );
    }

    @Override
    public ResponseEntity<ResponseObject> viewActiveEvents() {
        List<Event> activeEvents = eventRepo.findByStatus(Status.EVENT_REGISTRATION_ACTIVE);
        List<Map<String, Object>> activeEventDetails = activeEvents.stream()
                .sorted(Comparator.comparing(Event::getCreatedAt).reversed())
                .map(this::buildEventDetail)
                .toList();
        return ResponseEntity.ok(ResponseObject.builder()
                .message("Active Event list retrieved successfully")
                .success(true)
                .data(activeEventDetails)
                .build());
    }

    @Override
    public ResponseEntity<ResponseObject> viewAllSyllabusesByGrade(String gradeName) {
        if (gradeName == null || gradeName.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .message("Grade is required")
                            .success(false)
                            .data(null)
                            .build()
            );
        }
        // Grade được chọn không tồn tại
        boolean isExistGrade = Arrays.stream(Grade.values())
                .anyMatch(grade -> grade.getName().equalsIgnoreCase(gradeName));
        if (!isExistGrade) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .message("Selected grade does not exist.")
                            .success(false)
                            .data(null)
                            .build()
            );
        }
        List<Syllabus> syllabusList = syllabusRepo.findAllByGrade(getGradeFromName(gradeName));
        List<Map<String, Object>> syllabusesDetail = syllabusList.stream()
                .sorted(Comparator.comparing(Syllabus::getCreatedAt).reversed())
                .map(this::buildSyllabusDetail)
                .toList();
        return ResponseEntity.ok().body(
                ResponseObject.builder()
                        .message("View All Syllabuses By Grade Successfully")
                        .success(true)
                        .data(syllabusesDetail)
                        .build()
        );
    }

    @Override
    public ResponseEntity<ResponseObject> viewNumberOfStudentsNotAssignToAnyClassByYearAdnGrade(String year, String grade) {
        List<Integer> years = admissionTermRepo.findAll()
                .stream()
                .map(AdmissionTerm::getYear)
                .distinct()
                .sorted()
                .toList();

        String error = checkAcademicYearAndGrade(year, grade, years);

        if (!error.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        List<AdmissionForm> approvedForms = admissionFormRepo.findByTermItem_AdmissionTerm_YearAndStatusAndTermItem_Grade(
                Integer.parseInt(year),
                Status.APPROVED_PAID,
                getGradeFromName(grade)
        );

        List<Student> students = approvedForms.stream()
                .map(AdmissionForm::getStudent)
                .toList();
        List<StudentClass> assignedStudentClasses = studentClassRepo.findByClasses_AcademicYearAndClasses_Grade(
                Integer.parseInt(year),
                getGradeFromName(grade)
        );
        Set<Integer> assignedStudentIds = assignedStudentClasses.stream()
                .map(sc -> sc.getStudent().getId())
                .collect(Collectors.toSet());

        List<Student> studentsToAssign = students.stream()
                .filter(st -> !assignedStudentIds.contains(st.getId()))
                .toList();

        return ResponseEntity.ok().body(
                ResponseObject.builder()
                        .message("View Number Of Students Not Assigned To Any Classes By Grade And Year Successfully")
                        .success(true)
                        .data(studentsToAssign.size())
                        .build()
        );
    }


    @Override
    public ResponseEntity<ResponseObject> viewAssignedStudentsOfClass(String classId) {

        String error = checkClassId(classId);

        if (!error.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build()
            );
        }
        List<StudentClass> assignedStudentOfClass = studentClassRepo.findByClasses_Id(
                Integer.parseInt(classId)
        );

        List<Student> students = assignedStudentOfClass.stream()
                .map(StudentClass::getStudent)
                .toList();

        List<Map<String, Object>> studentList = students.stream()
                .map(this::buildStudentDetail)
                .toList();

        return ResponseEntity.ok().body(
                ResponseObject.builder()
                        .message("View Assigned Students Of Class Successfully")
                        .success(true)
                        .data(studentList)
                        .build()
        );
    }

    private Map<String, Object> buildStudentDetail(Student student) {
        Map<String, Object> studentDetail = new HashMap<>();
        studentDetail.put("id", student.getId());
        studentDetail.put("name", student.getName());
        studentDetail.put("gender", student.getGender());
        studentDetail.put("dateOfBirth", student.getDateOfBirth());
        studentDetail.put("placeOfBirth", student.getPlaceOfBirth());
        return studentDetail;
    }

    @Override
    public ResponseEntity<ResponseObject> assignAvailableStudentsAuto() {
        return null;
    }

    @Override
    public ResponseEntity<ResponseObject> viewClassDetailOfChild(String childId) {

        return null;
    }

    private Map<String, Object> buildEventDetail(Event event) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        Map<String, Object> data = new HashMap<>();
        data.put("id", event.getId());
        data.put("name", event.getName());
        data.put("startTime", event.getStartTime().format(fmt));
        data.put("endTime", event.getEndTime().format(fmt));
        data.put("location", event.getLocation());
        data.put("description", event.getDescription());
        data.put("status", event.getStatus());
        data.put("registrationDeadline", event.getRegistrationDeadline().format(fmt));
        data.put("attachmentImg", event.getAttachmentImg());
        data.put("hostName", event.getHostName());
        return data;
    }

    @Override
    public ResponseEntity<ResponseObject> generateClassesAuto(GenerateClassesRequest request) {

        List<Integer> years = admissionTermRepo.findAll()
                .stream()
                .map(AdmissionTerm::getYear)
                .distinct()
                .sorted()
                .toList();

        String error = validateCreate(request, years);

        if (!error.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build());
        }

        Syllabus syllabus = syllabusRepo.findById(Integer.parseInt(request.getSyllabusId())).orElse(null);
        if (syllabus == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder()
                    .message("Syllabus not found")
                    .success(false)
                    .data(null)
                    .build());
        }
        Grade grade = syllabus.getGrade();
        LocalDate start = request.getStartDate();
        LocalDate end = start.plusWeeks(syllabus.getNumberOfWeek()).minusDays(1);
        if (start.getYear() != end.getYear()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseObject.builder()
                    .message("Start date and end date must be within the same calendar year")
                    .success(false)
                    .data(null)
                    .build());
        }
        List<Account> teachers = accountRepo.findByRoleAndStatus(Role.TEACHER, Status.ACCOUNT_ACTIVE.getValue());
        Set<String> validLessonNames = syllabus.getSyllabusLessonList().stream()
                .map(sl -> sl.getLesson().getTopic())
                .collect(Collectors.toSet());
        List<String> raws = request.getActivitiesNameByDay();
        Map<DayOfWeek, List<String[]>> entriesByDay = raws.stream()
                .map(raw -> raw.split("-", 4))
                .filter(parts -> parts.length == 4)
                .collect(Collectors.groupingBy(
                        parts -> DayOfWeek.valueOf(parts[0].toUpperCase())
                ));
        for (var e : entriesByDay.entrySet()) {
            DayOfWeek dow = e.getKey();
            List<String[]> activities = e.getValue();
            int count = e.getValue().size();
            if (count != 10) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(
                        ResponseObject.builder()
                                .message(String.format(
                                        "On %s there must be exactly 10 activities (lessons+extras), but found %d",
                                        dow, count))
                                .success(false)
                                .data(null)
                                .build()
                );
            }
            Set<String> slotSet = new HashSet<>();
            for (String[] parts : activities) {
                // parts[0] = dayOfWeek, parts[2] = startTime, parts[3] = endTime
                String slotKey = parts[0] + "-" + parts[2] + "-" + parts[3];
                if (!slotSet.add(slotKey)) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(
                            ResponseObject.builder()
                                    .message(String.format(
                                            "Duplicate slot on %s: start %s - end %s",
                                            parts[0], parts[2], parts[3]))
                                    .success(false)
                                    .data(null)
                                    .build()
                    );
                }
            }
        }
        Map<DayOfWeek, List<String>> lessonsByDay = raws.stream()
                .map(raw -> raw.split("-", 4))
                .filter(p -> p.length == 4 && p[1].startsWith("LE_"))
                .collect(Collectors.groupingBy(
                        p -> DayOfWeek.valueOf(p[0].toUpperCase()),
                        Collectors.mapping(p -> p[1].substring(3), Collectors.toList())
                ));
        for (var e : lessonsByDay.entrySet()) {
            DayOfWeek dow = e.getKey();
            List<String> lessons = e.getValue();
            Set<String> distinct = new HashSet<>(lessons);
            int totalWeeklyHours = raws.stream()
                    .map(raw -> raw.split("-", 4))
                    .filter(parts -> parts.length == 4 && parts[1].startsWith("LE_"))
                    .mapToInt(parts -> {
                        LocalTime startSlot = LocalTime.parse(parts[2]);
                        LocalTime endSlot = LocalTime.parse(parts[3]);
                        return (int) Duration.between(startSlot, endSlot).toHours();
                    })
                    .sum();
            if (totalWeeklyHours != 30)
                return ResponseEntity.status(HttpStatus.CONFLICT).body(ResponseObject.builder()
                        .message("Total lesson hours per week must be 30, but found " + totalWeeklyHours)
                        .success(false)
                        .data(null)
                        .build());

            if (lessons.size() != 6) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body(ResponseObject.builder()
                        .message(String.format("On %s there must be exactly 6 lessons, but found %d",
                                dow, lessons.size()))
                        .success(false)
                        .data(null)
                        .build());
            }
            for (String lesson : distinct) {
                if (!validLessonNames.contains(lesson)) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(ResponseObject.builder()
                            .message(String.format("Lesson '%s' is not part of the syllabus", lesson))
                            .success(false)
                            .data(null)
                            .build());
                }
            }
        }
        List<AdmissionForm> approvedForms = admissionFormRepo.findByTermItem_AdmissionTerm_YearAndStatusAndTermItem_Grade(
                Integer.parseInt(request.getYear()),
                Status.APPROVED_PAID,
                grade
        );

        List<Student> students = approvedForms.stream()
                .map(AdmissionForm::getStudent)
                .toList();
        List<StudentClass> assignedStudentClasses = studentClassRepo.findByClasses_AcademicYearAndClasses_Grade(
                Integer.parseInt(request.getYear()),
                grade
        );
        Set<Integer> assignedStudentIds = assignedStudentClasses.stream()
                .map(sc -> sc.getStudent().getId())
                .collect(Collectors.toSet());

        List<Student> studentsToAssign = students.stream()
                .filter(st -> !assignedStudentIds.contains(st.getId()))
                .toList();
        if (studentsToAssign.isEmpty()) {
            return ResponseEntity.ok(ResponseObject.builder()
                    .message("No new students to assign.").success(true).data(null).build());
        }
        // Đếm số lớp hiện có của năm đó
        int existing = classRepo.countByAcademicYearAndGrade(Integer.parseInt(request.getYear()), grade);

        // Tính số lớp mới cần tạo
        int numberOfStudentsPerClass = 20;
        int numberOfNeededClasses = (studentsToAssign.size() + numberOfStudentsPerClass - 1) / numberOfStudentsPerClass;
        int totalClassIfCreate = existing + numberOfNeededClasses;
        int expectedClass = termItemRepo.findByAdmissionTerm_YearAndGrade(Integer.parseInt(request.getYear()), syllabus.getGrade()).getExpectedClasses();
        if (totalClassIfCreate > expectedClass) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ResponseObject.builder()
                    .message(String.format("Number of classes after assignment (%d) exceeds expected classes (%d). Please update your plan or increase expected classes.",
                            totalClassIfCreate, expectedClass))
                    .success(false).data(null).build());
        }
        if (numberOfNeededClasses + existing > expectedClass) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ResponseObject.builder()
                    .message(String.format("Number of needed classes (%d) exceeds expected classes (%d). Please update your plan or increase expected classes.",
                            numberOfNeededClasses, expectedClass))
                    .success(false)
                    .data(null)
                    .build());
        }
        List<Account> availableTeachers = teachers.stream()
                .filter(teacher -> teacher.getClasses().stream()
                        .noneMatch(cls ->
                                cls.getAcademicYear() == Integer.parseInt(request.getYear())
                        )
                )
                .toList();
        if (availableTeachers.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder()
                    .message("No teachers available")
                    .success(true)
                    .data(null)
                    .build());
        }
        if (availableTeachers.size() < numberOfNeededClasses) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ResponseObject.builder()
                    .message(String.format("Only %d teachers available but %d new classes required",
                            availableTeachers.size(), numberOfNeededClasses))
                    .success(false)
                    .data(null)
                    .build());
        }
        List<Classes> toSave = new ArrayList<>();
        int studentIdx = 0;
        for (int i = 0; i < numberOfNeededClasses; i++) {
            Account assignedTeacher = availableTeachers.get(i);
            Classes cls = Classes.builder()
                    .name(grade + "-" + (existing + i + 1) + "-" + request.getYear())
                    .startDate(start)
                    .endDate(end)
                    .academicYear(Integer.parseInt(request.getYear()))
                    .grade(grade)
                    .status(Status.CLASS_ACTIVE.getValue())
                    .teacher(assignedTeacher)
                    .syllabus(syllabus)
                    .build();
            List<StudentClass> scList = new ArrayList<>();
            for (int j = 0; j < numberOfStudentsPerClass && studentIdx < studentsToAssign.size(); j++, studentIdx++) {
                scList.add(StudentClass.builder().classes(cls).student(studentsToAssign.get(studentIdx)).build());
            }
            cls.setStudentClassList(scList);
            cls.setNumberStudent(scList.size());
            List<Schedule> scheduleList = new ArrayList<>();
            for (int week = 1; week <= syllabus.getNumberOfWeek(); week++) {
                LocalDate mondayOfWeek = start.plusWeeks(week - 1);
                Schedule sch = Schedule.builder()
                        .weekName("Week - " + week)
                        .classes(cls)
                        .build();
                List<Activity> activities = raws.stream()
                        .map(raw -> raw.split("-", 4))
                        .filter(parts -> parts.length == 4)
                        .map(parts -> {
                            DayOfWeek dow = DayOfWeek.valueOf(parts[0].toUpperCase());
                            String token = parts[1];
                            String lessonTopic = token.startsWith("LE_")
                                    ? token.substring(3)
                                    : null;
                            String activityName = token.startsWith("LE_")
                                    ? null
                                    : token;
                            LocalTime startTime = LocalTime.parse(parts[2]);
                            LocalTime endTime = LocalTime.parse(parts[3]);
                            LocalDate activityDate = mondayOfWeek.plusDays(dow.getValue() - 1);
                            return Activity.builder()
                                    .name(activityName != null ? activityName : lessonTopic)
                                    .syllabusName(activityName != null ? "N/A" : syllabus.getSubject())
                                    .dayOfWeek(dow)
                                    .date(activityDate)
                                    .startTime(startTime)
                                    .endTime(endTime)
                                    .schedule(sch)
                                    .build();
                        })
                        .toList();
                sch.setActivityList(activities);
                scheduleList.add(sch);
            }
            cls.setScheduleList(scheduleList);
            toSave.add(cls);
        }
        Syllabus prev = syllabusRepo.findByAssignedToClassesAndGrade(true, syllabus.getGrade());
        if (prev != null) {
            prev.setAssignedToClasses(false);
            syllabusRepo.save(prev);
        }
        syllabus.setAssignedToClasses(true);
        syllabusRepo.save(syllabus);
        classRepo.saveAll(toSave);
        for (Classes createdClass : toSave) {
            String className = createdClass.getName();
            String teacherName = createdClass.getTeacher().getName();
            String startDateStr = createdClass.getStartDate().toString();
            // Duyệt từng học sinh trong lớp
            for (StudentClass sc : createdClass.getStudentClassList()) {
                Student student = sc.getStudent();
                Parent parent = student.getParent();
                if (parent != null && parent.getAccount() != null) {
                    String parentName = parent.getAccount().getName();
                    String parentEmail = parent.getAccount().getEmail();
                    String studentName = student.getName();
                    String subject = "Thông báo xếp lớp cho học sinh " + studentName;
                    String header = "Class Assignment Notification";
                    String body = Format.getAssignClassSuccessfulForParentBody(
                            parentName, studentName, className, teacherName, startDateStr
                    );
                    mailService.sendMail(parentEmail, subject, header, body);
                }
            }
            String teacherEmail = createdClass.getTeacher().getEmail();
            String subject = "Class Homeroom Assignment Notification";
            String header = "Class Assignment Notification";
            String body = Format.getAssignClassSuccessfulForTeacherBody(
                    teacherName, className, startDateStr
            );
            mailService.sendMail(teacherEmail, subject, header, body);

        }
        return ResponseEntity.ok(
                ResponseObject.builder()
                        .message("Classes generated successfully")
                        .success(true)
                        .data(null)
                        .build()
        );
    }

    private Map<String, Object> buildClassDetail(Classes classes) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", classes.getId());
        data.put("name", classes.getName());
        data.put("startDate", classes.getStartDate());
        data.put("endDate", classes.getEndDate());
        data.put("year", classes.getAcademicYear());
        data.put("teacherName", classes.getTeacher().getName());
        data.put("numberStudents", classes.getNumberStudent());
        data.put("status", classes.getStatus());
        data.put("grade", classes.getGrade());
        return data;
    }


    @Transactional
    public ResponseEntity<ResponseObject> deleteClassById(String classId) {
        Classes cls = classRepo.findById(Integer.parseInt(classId)).get();
        classRepo.delete(cls);
        return ResponseEntity.ok(ResponseObject.builder()
                .success(true)
                .message("Delete class successfully")
                .data(null)
                .build());
    }

    @Override
    public ResponseEntity<ResponseObject> viewAllClassesByYearAndGrade(String year, String grade) {

        List<Integer> years = admissionTermRepo.findAll()
                .stream()
                .map(AdmissionTerm::getYear)
                .distinct()
                .sorted()
                .toList();

        String error = checkAcademicYearAndGrade(year, grade, years);

        if (!error.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build());
        }

        List<Classes> classes = classRepo.findByAcademicYearAndGrade(Integer.parseInt(year), getGradeFromName(grade));

        List<Map<String, Object>> classesDetail = classes.stream()
                .map(this::buildClassDetail)
                .toList();
        return ResponseEntity.ok().body(
                ResponseObject.builder()
                        .message("View All Classes By Grade And Year Successfully")
                        .success(true)
                        .data(classesDetail)
                        .build()
        );

    }

    private Map<String, Object> buildScheduleDetail(Schedule schedule) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", schedule.getId());
        data.put("name", schedule.getWeekName());
        return data;
    }

    private Map<String, Object> buildActivityDetail(Activity activity) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", activity.getId());
        data.put("name", activity.getName());
        data.put("syllabusName", activity.getSyllabusName());
        data.put("dayOfWeek", activity.getDayOfWeek());
        data.put("startTime", activity.getStartTime());
        data.put("endTime", activity.getEndTime());
        data.put("date", activity.getDate());
        return data;
    }

    @Override
    public ResponseEntity<ResponseObject> getSchedulesByClassId(String classId) {

        String error = checkClassId(classId);

        if (!error.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build());
        }

        List<Schedule> schedules = scheduleRepo.findByClasses_Id(Integer.parseInt(classId));

        List<Map<String, Object>> scheduleList = schedules.stream()
                .map(this::buildScheduleDetail)
                .toList();

        return ResponseEntity.ok().body(
                ResponseObject.builder()
                        .message("View All Schedules Of Class Successfully")
                        .success(true)
                        .data(scheduleList)
                        .build()
        );
    }

    public static String validateCreate(GenerateClassesRequest request, List<Integer> validYears) {

        if (request.getYear().isEmpty()) {
            return "Year cannot be empty";
        }

        int year;
        try {
            year = Integer.parseInt(request.getYear());
        } catch (NumberFormatException ex) {
            return "Year must be a number";
        }

        // ✅ Kiểm tra year có tồn tại trong AdmissionTerm DB
        if (!validYears.contains(year)) {
            return "Year must belong to an existing Admission Term";
        }

        // ✅ Kiểm tra year có phải là năm hiện tại
        int currentYear = LocalDate.now().getYear();
        if (year != currentYear) {
            return String.format("Year must be the current year (%d)", currentYear);
        }

        if (request.getSyllabusId().isEmpty()) {
            return "Syllabus Id be empty";
        }

        try {
            Integer.parseInt(request.getSyllabusId());
        } catch (IllegalArgumentException ex) {
            return "Syllabus Id must be a number";
        }

        LocalDate startDate = request.getStartDate();
        if (!startDate.isAfter(LocalDate.now())) {
            return "Start date must be after today: " + LocalDate.now();
        }

        if (startDate.getYear() != Integer.parseInt(request.getYear())) {
            return String.format("Start date must be within the year %s, but was %d", request.getYear(), startDate.getYear());
        }

        if (startDate.getDayOfWeek() != DayOfWeek.MONDAY) {
            return "Start date must be a Monday (the first day of the week).";
        }

        return "";
    }

    public static String checkAcademicYearAndGrade(String year, String grade, List<Integer> validYears) {
        if (year.isEmpty()) {
            return "Year cannot be empty";
        }
        try {
            int numberYear = Integer.parseInt(year);

            if (validYears == null || !validYears.contains(numberYear)) {
                return "The academic year you selected does not exist in any admission term. Please verify your selection and try again.";
            }

        } catch (IllegalArgumentException ex) {
            return "Year must be a number";
        }

        if (grade == null || grade.trim().isEmpty()) {
            return "Grade is required";
        }


        boolean isExistGrade = Arrays.stream(Grade.values())
                .anyMatch(gra -> gra.getName().equalsIgnoreCase(grade));
        if (!isExistGrade) {
            return "Selected grade does not exist.";
        }
        return "";
    }

    public static String checkClassId(String classId) {

        if (classId.isEmpty()) {
            return "Class Id cannot be empty";
        }

        try {
            Integer.parseInt(classId);
        } catch (IllegalArgumentException ex) {
            return "Class Id must be a number";
        }


        return "";
    }

    @Override
    public ResponseEntity<ResponseObject> getActivitiesByScheduleId(String scheduleId) {

        String error = checkScheduleIdValidation(scheduleId);

        if (!error.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build());
        }

        List<Activity> activities = activityRepo.findBySchedule_Id(Integer.parseInt(scheduleId));

        List<Map<String, Object>> activityList = activities.stream()
                .map(this::buildActivityDetail)
                .toList();

        return ResponseEntity.ok().body(
                ResponseObject.builder()
                        .message("View All Activities Of Schedule Successfully")
                        .success(true)
                        .data(activityList)
                        .build()
        );
    }

    private String checkScheduleIdValidation(String scheduleId) {

        if (scheduleId.isEmpty()) {
            return "Schedule Id cannot be empty";
        }

        try {
            Integer.parseInt(scheduleId);
        } catch (IllegalArgumentException ex) {
            return "Schedule Id must be a number";
        }


        return "";
    }

    private Grade getGradeFromName(String name) {
        for (Grade grade : Grade.values()) {
            if (grade.getName().equalsIgnoreCase(name)) {
                return grade;
            }
        }
        return null;
    }

    private List<String> checkDurationMatchSyllabus(Lesson lesson, int newDuration) {

        if (lesson.getDuration() == newDuration) return Collections.emptyList();

        List<SyllabusLesson> assignedLinks = syllabusLessonRepo.findByLessonId(lesson.getId());
        List<String> violatedSyllabuses = new ArrayList<>();

        for (SyllabusLesson sl : assignedLinks) {
            Syllabus syllabus = sl.getSyllabus();
            int allowed = syllabus.getHoursOfSyllabus();
            List<SyllabusLesson> allLessons = syllabus.getSyllabusLessonList();

            int totalDuration = 0;
            for (SyllabusLesson sll : allLessons) {
                Lesson l = sll.getLesson();
                totalDuration += (l.getId().equals(lesson.getId()) ? newDuration : l.getDuration());
            }

            if (totalDuration != allowed) {
                violatedSyllabuses.add(syllabus.getSubject() + " (" + totalDuration + "h > " + allowed + "h)");
            }
        }

        return violatedSyllabuses;
    }

    public ResponseEntity<Resource> exportStudentListToExcel() {
        List<Student> students = studentRepo.findAllByIsStudentTrue();
        String dateTimeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

        String[] columns = {
                "Parent Email", "Parent Name",
                "Student Name", "Student Gender", "Student Date of Birth", "Student Place of Birth"
        };

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Students");
            Row header = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                header.createCell(i).setCellValue(columns[i]);
            }

            int rowIdx = 1;
            for (Student student : students) {
                Row row = sheet.createRow(rowIdx++);
                Account parentAcc = (student.getParent() != null) ? student.getParent().getAccount() : null;
                row.createCell(0).setCellValue(Objects.toString(parentAcc != null ? parentAcc.getEmail() : null, ""));
                row.createCell(1).setCellValue(Objects.toString(parentAcc != null ? parentAcc.getName() : null, ""));
                row.createCell(2).setCellValue(Objects.toString(student.getName(), ""));
                row.createCell(3).setCellValue(Objects.toString(student.getGender(), ""));
                row.createCell(4).setCellValue(student.getDateOfBirth() != null ? student.getDateOfBirth().toString() : "");
                row.createCell(5).setCellValue(Objects.toString(student.getPlaceOfBirth(), ""));
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            ByteArrayResource resource = new ByteArrayResource(out.toByteArray());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=students_" + dateTimeStr + ".xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(resource);
        } catch (Exception e) {
            throw new RuntimeException("Excel export failed", e);
        }
    }
}