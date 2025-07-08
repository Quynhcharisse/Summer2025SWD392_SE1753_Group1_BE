package com.swd392.group1.pes.services.implementors;

import com.swd392.group1.pes.dto.requests.CreateLessonRequest;
import com.swd392.group1.pes.dto.requests.UpdateLessonRequest;
import com.swd392.group1.pes.dto.response.ResponseObject;
import com.swd392.group1.pes.models.Lesson;
import com.swd392.group1.pes.models.Syllabus;
import com.swd392.group1.pes.models.SyllabusLesson;
import com.swd392.group1.pes.repositories.LessonRepo;
import com.swd392.group1.pes.repositories.SyllabusLessonRepo;
import com.swd392.group1.pes.services.LessonService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LessonServiceImpl implements LessonService {

    private final LessonRepo lessonRepo;
    private final SyllabusLessonRepo syllabusLessonRepo;

    @Override
    public ResponseEntity<ResponseObject> viewLessonDetail(String id) {
        String error = checkLessonIdValid(id);

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
        );    }

    @Override
    public ResponseEntity<ResponseObject> viewAssignedSyllabuses(String id) {
        String error = checkSyllabusId(id);

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
    private String checkLessonIdValid(String id) {
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

    private String lessonCreateValidation(CreateLessonRequest request, LessonRepo lessonRepo) {

        if (request.getTopic() == null || request.getTopic().trim().isEmpty()) {
            return "Lesson name is required";
        }

        if (!request.getTopic().matches("^[\\p{L}0-9 ]+$")) {
            return "Lesson name must not contain special characters.";
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

    private Map<String, Object> buildLessonDetail(Lesson lesson) {
        Map<String, Object> data = new HashMap<>();
        data.put("id", lesson.getId());
        data.put("topic", lesson.getTopic());
        data.put("description", lesson.getDescription());
        data.put("objective", lesson.getObjective());
        data.put("duration", lesson.getDuration());
        return data;
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

}
