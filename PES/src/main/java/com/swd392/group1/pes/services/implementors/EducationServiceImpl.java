package com.swd392.group1.pes.services.implementors;


import com.swd392.group1.pes.enums.Grade;
import com.swd392.group1.pes.models.Classes;
import com.swd392.group1.pes.models.Lesson;
import com.swd392.group1.pes.models.Syllabus;
import com.swd392.group1.pes.models.SyllabusLesson;
import com.swd392.group1.pes.repositories.ClassRepo;
import com.swd392.group1.pes.repositories.LessonRepo;
import com.swd392.group1.pes.repositories.StudentRepo;
import com.swd392.group1.pes.repositories.SyllabusRepo;
import com.swd392.group1.pes.requests.AssignLessonsRequest;
import com.swd392.group1.pes.requests.CreateSyllabusRequest;
import com.swd392.group1.pes.requests.GenerateClassesRequest;
import com.swd392.group1.pes.requests.UpdateSyllabusRequest;
import com.swd392.group1.pes.response.ResponseObject;
import com.swd392.group1.pes.services.EducationService;
import com.swd392.group1.pes.validations.SyllabusValidation.AssignLessonsValidation;
import com.swd392.group1.pes.validations.SyllabusValidation.CheckSyllabusExistence;
import com.swd392.group1.pes.validations.SyllabusValidation.CreateSyllabusValidation;
import com.swd392.group1.pes.validations.SyllabusValidation.UpdateSyllabusValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class EducationServiceImpl implements EducationService {

    private final SyllabusRepo syllabusRepo;
    private final StudentRepo studentRepo;
    private final ClassRepo classRepo;
    private final LessonRepo lessonRepo;


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

        String error = CheckSyllabusExistence.validate(id, syllabusRepo);

        if(!error.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        error = UpdateSyllabusValidation.validate(id, request, syllabusRepo);

        if(!error.isEmpty()) {
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
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
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

        if(syllabuses.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body(
                    ResponseObject.builder()
                            .message("No Syllabuses Found")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

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
        String error = CheckSyllabusExistence.validate(id, syllabusRepo);

        if (!error.isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build()
            );

        error = AssignLessonsValidation.validate(id, request, lessonRepo);

        if (!error.isEmpty())
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .message(error)
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

        List<SyllabusLesson> updatedList = new ArrayList<>(syllabus.getSyllabusLessonList());
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

    private Grade getGradeFromName(String name) {
        for (Grade grade : Grade.values()) {
            if (grade.getName().equalsIgnoreCase(name)) {
                return grade;
            }
        }
        return null;
    }

}
