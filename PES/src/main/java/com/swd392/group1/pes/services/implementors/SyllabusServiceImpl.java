package com.swd392.group1.pes.services.implementors;

import com.swd392.group1.pes.dto.requests.AssignLessonsRequest;
import com.swd392.group1.pes.dto.requests.CreateSyllabusRequest;
import com.swd392.group1.pes.dto.requests.UpdateSyllabusRequest;
import com.swd392.group1.pes.dto.response.ResponseObject;
import com.swd392.group1.pes.enums.Grade;
import com.swd392.group1.pes.models.Lesson;
import com.swd392.group1.pes.models.Syllabus;
import com.swd392.group1.pes.models.SyllabusLesson;
import com.swd392.group1.pes.repositories.LessonRepo;
import com.swd392.group1.pes.repositories.SyllabusLessonRepo;
import com.swd392.group1.pes.repositories.SyllabusRepo;
import com.swd392.group1.pes.services.SyllabusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SyllabusServiceImpl implements SyllabusService {
    private final SyllabusRepo syllabusRepo;
    private final LessonRepo lessonRepo;
    private final SyllabusLessonRepo syllabusLessonRepo;

    @Override
    public ResponseEntity<ResponseObject> createSyllabus(CreateSyllabusRequest request) {
        // 1. Validation chung
        String error = createSyllabusValidation(request, syllabusRepo);

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

    public String createSyllabusValidation(CreateSyllabusRequest request, SyllabusRepo syllabusRepo) {

        if (syllabusRepo.existsBySubjectIgnoreCase(request.getSubject()))
            return "Syllabus already exists";


        if (request.getSubject().trim().isEmpty()) {
            return "Name cannot be empty";
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

    private String updateSyllabusValidation(String id, UpdateSyllabusRequest request) {

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

    private String assignLessonsValidation(List<String> lessonNames) {

        if (lessonNames == null || lessonNames.isEmpty()) {
            return "Please select at least one lesson.";}

        if(lessonNames.size() < 3) {
            return "Please select at least 3 lessons for the syllabus";
        }

        return "";
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



    private Grade getGradeFromName(String name) {
        for (Grade grade : Grade.values()) {
            if (grade.getName().equalsIgnoreCase(name)) {
                return grade;
            }
        }
        return null;
    }

    private String checkSyllabusId(String id){
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

}
