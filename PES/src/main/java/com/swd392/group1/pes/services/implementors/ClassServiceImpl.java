package com.swd392.group1.pes.services.implementors;

import com.swd392.group1.pes.dto.requests.AssignStudentsToClassRequest;
import com.swd392.group1.pes.dto.requests.GenerateClassesRequest;
import com.swd392.group1.pes.dto.requests.UnassignStudentsFromClassRequest;
import com.swd392.group1.pes.dto.requests.ViewCurrentScheduleRequest;

import com.swd392.group1.pes.dto.response.ResponseObject;
import com.swd392.group1.pes.enums.Grade;
import com.swd392.group1.pes.enums.Role;
import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.models.Account;
import com.swd392.group1.pes.models.Activity;
import com.swd392.group1.pes.models.AdmissionForm;
import com.swd392.group1.pes.models.AdmissionTerm;
import com.swd392.group1.pes.models.Classes;

import com.swd392.group1.pes.models.Lesson;
import com.swd392.group1.pes.models.Parent;
import com.swd392.group1.pes.models.Schedule;
import com.swd392.group1.pes.models.Student;
import com.swd392.group1.pes.models.StudentClass;
import com.swd392.group1.pes.models.Syllabus;
import com.swd392.group1.pes.models.SyllabusLesson;
import com.swd392.group1.pes.repositories.AccountRepo;
import com.swd392.group1.pes.repositories.ActivityRepo;
import com.swd392.group1.pes.repositories.AdmissionFormRepo;
import com.swd392.group1.pes.repositories.AdmissionTermRepo;
import com.swd392.group1.pes.repositories.ClassRepo;
import com.swd392.group1.pes.repositories.EventParticipateRepo;
import com.swd392.group1.pes.repositories.EventRepo;
import com.swd392.group1.pes.repositories.ScheduleRepo;
import com.swd392.group1.pes.repositories.StudentClassRepo;
import com.swd392.group1.pes.repositories.StudentRepo;
import com.swd392.group1.pes.repositories.SyllabusRepo;
import com.swd392.group1.pes.repositories.TermItemRepo;
import com.swd392.group1.pes.services.ClassService;
import com.swd392.group1.pes.services.MailService;
import com.swd392.group1.pes.utils.email.Format;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.poi.ss.usermodel.Cell;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClassServiceImpl implements ClassService {

    private final ClassRepo classRepo;
    private final SyllabusRepo syllabusRepo;
    private final TermItemRepo termItemRepo;
    private final AdmissionFormRepo admissionFormRepo;
    private final StudentClassRepo studentClassRepo;
    private final AccountRepo accountRepo;
    private final AdmissionTermRepo admissionTermRepo;
    private final MailService mailService;
    private final ScheduleRepo scheduleRepo;
    private final ActivityRepo activityRepo;
    private final StudentRepo studentRepo;
    private final EventParticipateRepo eventParticipateRepo;
    private final EventRepo eventRepo;

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

        List<String> raws = request.getActivitiesNameByDay();

        Pattern validActivityNamePattern = Pattern.compile("^[\\p{L}0-9 ]+$");

        for (String raw : raws) {
            String[] parts = raw.split("-", 4);
            if (parts.length == 4) {
                String token = parts[1];
                if (!token.startsWith("LE_") && !validActivityNamePattern.matcher(token).matches()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(ResponseObject.builder()
                                    .message("Activity name '" + token + "' contains special characters. Only letters, numbers and spaces are allowed.")
                                    .success(false)
                                    .data(null)
                                    .build());
                }
                // Kiểm tra độ dài tên activity (chỉ với activity thường, không phải lesson)
                if (!token.startsWith("LE_") && token.length() > 50) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body(ResponseObject.builder()
                                    .message("Activity name '" + token + "' must not exceed 50 characters.")
                                    .success(false)
                                    .data(null)
                                    .build());
                }
            }
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
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ResponseObject.builder()
                    .message("Start date and end date must be within the same calendar year")
                    .success(false)
                    .data(null)
                    .build());
        }

        Optional<Syllabus> existingOpt = syllabusRepo
                .findFirstByAssignedToClassesTrueAndGradeAndClassesList_AcademicYear(grade, Integer.parseInt(request.getYear()));

        if (existingOpt.isPresent() && !existingOpt.get().getId().equals(syllabus.getId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    ResponseObject.builder()
                            .message("Syllabus '" + existingOpt.get().getSubject() +
                                    "' has already been assigned to this grade in year " + request.getYear() +
                                    ". \n Please use the same syllabus or choose a different grade or year.")                            .success(false)
                            .data(null)
                            .build()
            );
        }

        List<Account> teachers = accountRepo.findByRoleAndStatus(Role.TEACHER, Status.ACCOUNT_ACTIVE.getValue());
        Set<String> validLessonNames = syllabus.getSyllabusLessonList().stream()
                .map(sl -> sl.getLesson().getTopic())
                .collect(Collectors.toSet());

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
        Map<String, Integer> lessonDurationMap = syllabus.getSyllabusLessonList().stream()
                .collect(Collectors.toMap(
                        sl -> sl.getLesson().getTopic(),
                        sl -> sl.getLesson().getDuration()
                ));
        Map<String, Integer> lessonActualHours = new HashMap<>();
        for (var e : lessonsByDay.entrySet()) {
            DayOfWeek dow = e.getKey();
            List<String> lessons = e.getValue();
            Set<String> distinct = new HashSet<>(lessons);
            int totalWeeklyHours = raws.stream()
                    .map(raw -> raw.split("-", 4))
                    .filter(parts -> parts.length == 4 && parts[1].startsWith("LE_"))
                    .mapToInt(parts -> {
                        String topic = parts[1].substring(3);
                        LocalTime startSlot = LocalTime.parse(parts[2]);
                        LocalTime endSlot = LocalTime.parse(parts[3]);
                        int hours = (int) Duration.between(start, end).toHours();
                        lessonActualHours.put(topic, lessonActualHours.getOrDefault(topic, 0) + hours);
                        return (int) Duration.between(startSlot, endSlot).toHours();
                    })
                    .sum();


            for (Map.Entry<String, Integer> entry : lessonDurationMap.entrySet()) {
                String topic = entry.getKey();
                int required = entry.getValue();
                int actual = lessonActualHours.getOrDefault(topic, 0);
                if (actual != required) {
                    return ResponseEntity.status(HttpStatus.CONFLICT).body(
                            ResponseObject.builder()
                                    .message(String.format("Lesson '%s' requires exactly %d hours, but scheduled %d hours.", topic, required, actual))
                                    .success(false)
                                    .data(null)
                                    .build()
                    );
                }
            }

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
            return ResponseEntity.badRequest().body(ResponseObject.builder()
                    .message("No new students to assign.").success(false).data(null).build());
        }
        int existing = classRepo.countByAcademicYearAndGrade(Integer.parseInt(request.getYear()), grade);

        int expectedClass = termItemRepo.findByAdmissionTerm_YearAndGrade(
                Integer.parseInt(request.getYear()), syllabus.getGrade()).getExpectedClasses();

        int numberOfStudentsPerClass = 20;
        int maxNewClassesCanCreate = expectedClass - existing;
        int numberOfNeededClasses = (studentsToAssign.size() + numberOfStudentsPerClass - 1) / numberOfStudentsPerClass;

        int numberOfClassesToCreate = Math.min(numberOfNeededClasses, maxNewClassesCanCreate);

        if (numberOfClassesToCreate <= 0) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    ResponseObject.builder()
                            .message("All planned classes have already been created. No additional classes can be generated.")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        int maxStudentsCanAssign = numberOfClassesToCreate * numberOfStudentsPerClass;
        if (studentsToAssign.size() > maxStudentsCanAssign) {
            studentsToAssign = studentsToAssign.subList(0, maxStudentsCanAssign);
        }
        List<Account> availableTeachers = teachers.stream()
                .filter(teacher -> teacher.getClasses().stream()
                        .noneMatch(cls ->
                                cls.getAcademicYear() == Integer.parseInt(request.getYear())
                                        || Status.CLASS_IN_PROGRESS.getValue().equalsIgnoreCase(cls.getStatus())
                        )
                )
                .toList();
        if (availableTeachers.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseObject.builder()
                    .message("No teachers available")
                    .success(false)
                    .data(null)
                    .build());
        }
        if (availableTeachers.size() < numberOfClassesToCreate) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(ResponseObject.builder()
                    .message(String.format("Only %d teachers available but %d new classes required",
                            availableTeachers.size(), numberOfClassesToCreate))
                    .success(false)
                    .data(null)
                    .build());
        }
        List<Classes> toSave = new ArrayList<>();
        int studentIdx = 0;
        for (int i = 0; i < numberOfClassesToCreate; i++) {
            Account assignedTeacher = availableTeachers.get(i);
            String className = grade + "-" + (existing + i + 1) + "-" + request.getYear();
            boolean isClassNameUsed = classRepo.existsByName(className);
            if (isClassNameUsed) {
                continue; // hoặc tăng chỉ số i để tránh trùng
            }
            Classes cls = Classes.builder()
                    .name(className)
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
                Student student = studentsToAssign.get(studentIdx);

                if (studentClassRepo.existsByStudent_IdAndClasses_AcademicYearAndClasses_Grade(student.getId(), Integer.parseInt(request.getYear()), grade)) {
                    studentIdx++;
                    continue;
                }

                scList.add(StudentClass.builder()
                        .classes(cls)
                        .student(student)
                        .build());
                            }
            if (scList.isEmpty()) {
                continue;
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
                        .message( numberOfClassesToCreate + " classes generated successfully")
                        .success(true)
                        .data(null)
                        .build()
        );
    }

    private String validateCreate(GenerateClassesRequest request, List<Integer> validYears) {

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

    @Override
    @Transactional
    public ResponseEntity<ResponseObject> deleteClassById(String classId) {
        String error = checkClassId(classId);
        if(!error.isEmpty()){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build());
        }
        if(classRepo.findById(Integer.parseInt(classId)).isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseObject.builder()
                            .message("Class with id " + classId + " does not exist or be deleted")
                            .success(false)
                            .data(null)
                            .build()
            );
        }
        Classes cls = classRepo.findById(Integer.parseInt(classId)).get();
        if (!Status.CLASS_ACTIVE.getValue().equalsIgnoreCase(cls.getStatus())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResponseObject.builder()
                            .message("Only classes with status 'ACTIVE' can be deleted")
                            .success(false)
                            .data(null)
                            .build());
        }
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

        if(scheduleRepo.findByClasses_Id(Integer.parseInt(classId)).isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseObject.builder()
                            .message("Class with id " + classId + " does not exist or be deleted")
                            .success(false)
                            .data(null)
                            .build()
            );
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

        if (scheduleRepo.findById(Integer.parseInt(scheduleId)).isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseObject.builder()
                            .message("Schedule with id " + scheduleId + " does not exist or be deleted")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        List<Activity> activities = activityRepo.findBySchedule_Id(Integer.parseInt(scheduleId));

        activities.sort(Comparator.comparing(Activity::getDayOfWeek)
                .thenComparing(Activity::getStartTime));

        Map<String, List<Map<String, Object>>> grouped = activities.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getDayOfWeek().toString(),
                        LinkedHashMap::new,
                        Collectors.mapping(this::buildActivityDetail, Collectors.toList())
                ));

        List<Map<String, Object>> groupedList = grouped.entrySet().stream()
                .map(e -> {
                    Map<String, Object> group = new HashMap<>();
                    group.put("dayOfWeek", e.getKey());
                    List<Map<String, Object>> activitiesOfDay = e.getValue();
                    // Lấy ngày của activity đầu tiên trong group
                    String date = "";
                    if (!activitiesOfDay.isEmpty() && activitiesOfDay.get(0).get("date") != null) {
                        date = activitiesOfDay.get(0).get("date").toString();
                    }
                    group.put("date", date);
                    group.put("activities", activitiesOfDay);
                    return group;
                })
                .toList();

        return ResponseEntity.ok().body(
                ResponseObject.builder()
                        .message("View All Activities Of Schedule Successfully")
                        .success(true)
                        .data(groupedList)
                        .build()
        );
    }



    @Override
    public ResponseEntity<ResponseObject> viewClassDetail(String classId) {

        String error = checkClassId(classId);

        if(!error.isEmpty())
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build());
        }

        if (classRepo.findById(Integer.parseInt(classId)).isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseObject.builder()
                            .message("Class with id " + classId + " does not exist or be deleted")
                            .success(false)
                            .data(null)
                            .build()
            );        }

        Classes classes = classRepo.findById(Integer.parseInt(classId)).get();

        List<SyllabusLesson> lessons = classes.getSyllabus().getSyllabusLessonList();

        List<Map<String, Object>> lessonList = lessons.stream()
                .map(SyllabusLesson::getLesson)
                .map(this::buildLessonDetail)
                .toList();

        Map<String, Object> data = new HashMap<>();
        data.put("id", classes.getId());
        data.put("name", classes.getName());
        data.put("startDate", classes.getStartDate());
        data.put("endDate", classes.getEndDate());
        data.put("year", classes.getAcademicYear());
        data.put("teacherName", classes.getTeacher().getName());
        data.put("teacherPhoneNumber", classes.getTeacher().getPhone());
        data.put("teacherEmail", classes.getTeacher().getEmail());
        data.put("numberStudents", classes.getNumberStudent());
        data.put("status", classes.getStatus());
        data.put("grade", classes.getGrade());
        data.put("syllabusName", classes.getSyllabus().getSubject());
        data.put("lessonList",  lessonList);
        return ResponseEntity.ok().body(
                ResponseObject.builder()
                        .message("View Detail Class Successfully")
                        .success(true)
                        .data(data)
                        .build()
        );    }

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

        int currentYear = LocalDate.now().getYear();
        if (Integer.parseInt(year) != currentYear) {
            return ResponseEntity.ok().body(
                    ResponseObject.builder()
                            .message("View Number Of Students Not Assigned To Any Classes By Grade And Year Successfully")
                            .success(true)
                            .data(0)
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

    @Override
    public ResponseEntity<ResponseObject> unassignStudentsFromClass(UnassignStudentsFromClassRequest request) {
        Optional<Classes> optionalClass = classRepo.findById(request.getClassId());

        if (classRepo.findById(request.getClassId()).isEmpty())

            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ResponseObject.builder()
                            .success(false)
                            .message("Class with id " + request.getClassId() + " doesn't found or be deleted")
                            .data(null)
                            .build());

        Classes cls = optionalClass.get();

        if (cls.getStatus().equalsIgnoreCase(Status.CLASS_IN_PROGRESS.getValue())
                || cls.getStatus().equalsIgnoreCase(Status.CLASS_CLOSED.getValue())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ResponseObject.builder()
                            .success(false)
                            .message("Cannot unassign students because the class is already in progress or closed.")
                            .data(null)
                            .build());
        }

        List<Integer> studentIds = request.getStudentIds();

        List<Student> existingStudents = studentRepo.findAllByIdIn(request.getStudentIds());
        Set<Integer> existingIds = existingStudents.stream()
                .map(Student::getId)
                .collect(Collectors.toSet());

        List<Integer> notExistIds = request.getStudentIds().stream()
                .filter(id -> !existingIds.contains(id))
                .toList();

        // Tìm những student thực sự đang học lớp này
        List<StudentClass> toRemove = studentClassRepo.findByClasses_IdAndStudent_IdIn(
                request.getClassId(), studentIds);

        Set<Integer> assignedIds = toRemove.stream()
                .map(sc -> sc.getStudent().getId())
                .collect(Collectors.toSet());

        // 4. Xác định student tồn tại nhưng không được assign vào lớp
        List<Integer> existButNotAssigned = existingIds.stream()
                .filter(id -> !assignedIds.contains(id))
                .toList();

        if (toRemove.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ResponseObject.builder()
                            .success(false)
                            .message("No valid students were found to unassign from this class.")
                            .data(null)
                            .build());
        }

        // Kiểm tra còn đủ số lượng sau khi gỡ
        int currentCount = studentClassRepo.countByClasses_Id(request.getClassId());
        int remaining = currentCount - toRemove.size();
        int minAllowed = 10;
        if (remaining < minAllowed) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ResponseObject.builder()
                            .success(false)
                            .message("Cannot unassign. Minimum allowed students in a class is " + minAllowed +
                                    ". Current: " + currentCount + ", Trying to unassign: " + toRemove.size())
                            .data(null)
                            .build());
        }

        studentClassRepo.deleteAll(toRemove);
        cls.setNumberStudent(remaining);
        classRepo.save(cls);

        // 8. Tạo thông báo
        StringBuilder message = new StringBuilder("Unassigned " + toRemove.size() + " students from class successfully.");
        if (!notExistIds.isEmpty()) {
            message.append(" The following student IDs do not exist in the system: ").append(notExistIds).append(".");
        }
        if (!existButNotAssigned.isEmpty()) {
            message.append(" The following students are not assigned to this class: ").append(existButNotAssigned).append(".");
        }

        return ResponseEntity.ok(
                ResponseObject.builder()
                        .success(true)
                        .message(message.toString())
                        .data(null)
                        .build());
    }

    @Transactional
    @Override
    public ResponseEntity<ResponseObject> deleteActivitiesByDates(String scheduleId, LocalDate dateStr) {
        Integer scheduleIdInt;
        try {
            scheduleIdInt = Integer.parseInt(scheduleId);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(
                    ResponseObject.builder().success(false).message("Invalid schedule ID").data(null).build());
        }

        Optional<Schedule> optionalSchedule = scheduleRepo.findById(scheduleIdInt);
        if (optionalSchedule.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseObject.builder().success(false).message("Schedule not found").data(null).build());
        }

        LocalDate today = LocalDate.now();
        if (dateStr.isBefore(today)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .success(false)
                            .message("Activities in the past cannot be deleted")
                            .data(null)
                            .build()
            );
        }

        Schedule currentSchedule = optionalSchedule.get();
        Classes cls = currentSchedule.getClasses();
        List<Schedule> allSchedules = cls.getScheduleList();

        if (cls.getStatus().equalsIgnoreCase(Status.CLASS_IN_PROGRESS.getValue())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .success(false)
                            .message("Cannot delete activities for a class that is IN_PROGRESS")
                            .data(null)
                            .build()
            );
        }

        // 1. Lấy các activity theo ngày cần xóa
        List<Activity> toReassign = activityRepo.findBySchedule_Id(scheduleIdInt).stream()
                .filter(act -> act.getDate().equals(dateStr))
                .toList();

        if (toReassign.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseObject.builder().success(false).message("No activities found for provided dates").data(null).build());
        }

        // 2. Gỡ activity khỏi activityList của tất cả các schedule liên quan
        for (Schedule sch : allSchedules) {
            if (sch.getActivityList() != null) {
                sch.getActivityList().removeIf(toReassign::contains);
            }
        }

        // 3. Xóa activity khỏi DB
        activityRepo.deleteAllInBatch(toReassign);

        // 4. Luôn tìm tuần cuối của class (schedule có id lớn nhất, còn activity)
        List<Schedule> notEmptySchedules = allSchedules.stream()
                .filter(sch -> sch.getActivityList() != null && !sch.getActivityList().isEmpty())
                .sorted(Comparator.comparing(Schedule::getId))
                .toList();

        int weekCounter = allSchedules.size();
        List<Activity> reassigned = new ArrayList<>();
        boolean assigned = false;

        if (!notEmptySchedules.isEmpty()) {
            Schedule lastSchedule = notEmptySchedules.get(notEmptySchedules.size() - 1);
            List<LocalDate> datesInLastWeek = lastSchedule.getActivityList().stream()
                    .map(Activity::getDate).toList();

            // Lấy Monday của tuần cuối cùng
            LocalDate lastWeekStart = datesInLastWeek.stream().min(LocalDate::compareTo).orElse(null);
            if (lastWeekStart != null) {
                // Duyệt các ngày từ Monday đến Friday
                List<LocalDate> lastPossibleDays = new ArrayList<>();
                LocalDate temp = lastWeekStart;
                for (int i = 0; lastPossibleDays.size() < 5; temp = temp.plusDays(1)) {
                    if (temp.getDayOfWeek().getValue() >= 1 && temp.getDayOfWeek().getValue() <= 5) {
                        lastPossibleDays.add(temp);
                    }
                }
                // Chỉ lấy slot còn trống nhỏ nhất
                Optional<LocalDate> nextSlot = lastPossibleDays.stream()
                        .filter(d -> !datesInLastWeek.contains(d))
                        .findFirst();
                if (nextSlot.isPresent()) {
                    LocalDate reassignDate = nextSlot.get();
                    for (Activity old : toReassign) {
                        Activity newAct = Activity.builder()
                                .name(old.getName())
                                .syllabusName(old.getSyllabusName())
                                .startTime(old.getStartTime())
                                .endTime(old.getEndTime())
                                .date(reassignDate)
                                .dayOfWeek(reassignDate.getDayOfWeek())
                                .schedule(lastSchedule)
                                .build();
                        lastSchedule.getActivityList().add(newAct);
                        reassigned.add(newAct);
                    }
                    assigned = true;
                }
            }
        }

        // Nếu tuần cuối không còn slot, tạo tuần mới (MONDAY tuần kế)
        if (!assigned) {
            weekCounter++;
            // Xác định Monday tiếp theo sau endDate của lớp
            LocalDate baseDate = cls.getEndDate().plusDays(1);
            while (baseDate.getDayOfWeek() != DayOfWeek.MONDAY) {
                baseDate = baseDate.plusDays(1);
            }
            LocalDate reassignDate = baseDate;

            Schedule newSchedule = Schedule.builder()
                    .weekName("Week - " + weekCounter)
                    .classes(cls)
                    .activityList(new ArrayList<>())
                    .build();
            scheduleRepo.save(newSchedule);
            allSchedules.add(newSchedule);

            for (Activity old : toReassign) {
                Activity newAct = Activity.builder()
                        .name(old.getName())
                        .syllabusName(old.getSyllabusName())
                        .startTime(old.getStartTime())
                        .endTime(old.getEndTime())
                        .date(reassignDate)
                        .dayOfWeek(reassignDate.getDayOfWeek())
                        .schedule(newSchedule)
                        .build();
                newSchedule.getActivityList().add(newAct);
                reassigned.add(newAct);
            }
        }

        // Lưu lại các activity vừa reassign
        activityRepo.saveAll(reassigned);

        // Cập nhật lại endDate cho lớp (nếu có activity mới)
        LocalDate currentEndDate = cls.getEndDate();
        LocalDate newEndDate = reassigned.stream()
                .map(Activity::getDate)
                .max(Comparator.naturalOrder())
                .orElse(currentEndDate);
        cls.setEndDate(newEndDate);
        classRepo.save(cls);

        return ResponseEntity.ok(
                ResponseObject.builder()
                        .success(true)
                        .message("Deleted " + toReassign.size() + " activities and reassigned them from " + dateStr + " to " + newEndDate)
                        .data(null)
                        .build()
        );
    }

    @Override
    public ResponseEntity<Resource> exportStudentListOfClassToExcel(String classId) {
        int classIdInt;
        try {
            classIdInt = Integer.parseInt(classId);
        } catch (NumberFormatException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }

        Classes cls = classRepo.findById(classIdInt).orElse(null);
        if (cls == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        List<StudentClass> studentClasses = studentClassRepo.findByClasses_Id(cls.getId());
        String[] columns = {"ID", "Name", "Gender", "Date of Birth", "Place of Birth", "Class Name", "Grade", "Academic Year", "Teacher Name", "Teacher Phone"};
        String fileNameTimestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd HHmmss"));

        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Students");

            Row header = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(columns[i]);
            }

            int rowIdx = 1;
            for (StudentClass sc : studentClasses) {
                Student student = sc.getStudent();
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(student.getId());
                row.createCell(1).setCellValue(student.getName());
                row.createCell(2).setCellValue(student.getGender());
                row.createCell(3).setCellValue(student.getDateOfBirth() != null
                        ? student.getDateOfBirth().toString() : "");
                row.createCell(4).setCellValue(student.getPlaceOfBirth());
                row.createCell(5).setCellValue(cls.getName());
                row.createCell(6).setCellValue(cls.getGrade() != null ? cls.getGrade().name() : "");
                int year = cls.getAcademicYear();
                row.createCell(7).setCellValue(year + "-" + (year + 1));
                row.createCell(8).setCellValue(cls.getTeacher() != null ? cls.getTeacher().getName() : "");
                row.createCell(9).setCellValue(cls.getTeacher() != null ? cls.getTeacher().getPhone() : "");
            }

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            ByteArrayResource resource = new ByteArrayResource(out.toByteArray());

            String filename = "students_class_" + classId + "_" + fileNameTimestamp + ".xlsx";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @Override
    public ResponseEntity<ResponseObject> assignAvailableStudentsAuto(String year, String grade) {

        List<Integer> years = admissionTermRepo.findAll()
                .stream()
                .map(AdmissionTerm::getYear)
                .distinct()
                .sorted()
                .toList();

        String error = checkAcademicYearAndGrade(year, grade, years);

        if (!error.isEmpty())
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        int currentYear = LocalDate.now().getYear();
        if (Integer.parseInt(year) != currentYear) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .message(String.format("Year must be the current year (%d)", currentYear))
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
        if (studentsToAssign.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseObject.builder()
                    .message("No new students to assign.").success(false).data(null).build());
        }

        int maxStudentPerClass = 20; // có thể set động

        // Lấy danh sách lớp hiện có của năm và khối đó
        List<Classes> availableClasses = classRepo.findByAcademicYearAndGrade(Integer.parseInt(year), getGradeFromName(grade));

        if (availableClasses.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseObject.builder()
                            .message("No classes exist for this academic year and grade. Please create classes first.")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        // Filter classes that still have empty slots
        List<Classes> classesWithSlots = availableClasses.stream()
                .filter(cls -> cls.getStatus().equalsIgnoreCase(Status.CLASS_ACTIVE.getValue()) && cls.getNumberStudent() < maxStudentPerClass)
                .toList();

        // If there are no classes with available slots, return an error
        if (classesWithSlots.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    ResponseObject.builder()
                            .message("No active classes with available slots.")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        List<StudentClass> newAssignments = new ArrayList<>();
        int idx = 0;
        for (Classes cls : classesWithSlots) {
            int slots = maxStudentPerClass - cls.getNumberStudent();
            for (int i = 0; i < slots && idx < studentsToAssign.size(); i++, idx++) {
                Student student = studentsToAssign.get(idx);
                StudentClass sc = StudentClass.builder()
                        .classes(cls)
                        .student(student)
                        .build();
                newAssignments.add(sc);
                cls.setNumberStudent(cls.getNumberStudent() + 1);
            }
            if (idx >= studentsToAssign.size()) break;
        }

        studentClassRepo.saveAll(newAssignments);
        classRepo.saveAll(classesWithSlots);

        for (StudentClass sc : newAssignments) {
            Student student = sc.getStudent();
            Classes cls = sc.getClasses();
            Parent parent = student.getParent();
            Account parentAccount = parent != null ? parent.getAccount() : null;

            if (parentAccount != null) {
                String parentName = parentAccount.getName();
                String parentEmail = parentAccount.getEmail();
                String studentName = student.getName();
                String className = cls.getName();
                String teacherName = cls.getTeacher() != null ? cls.getTeacher().getName() : "N/A";
                String startDateStr = cls.getStartDate().toString();

                String subject = "Thông báo xếp lớp cho học sinh " + studentName;
                String header = "Class Assignment Notification";
                String body = Format.getAssignClassSuccessfulForParentBody(
                        parentName, studentName, className, teacherName, startDateStr
                );

                mailService.sendMail(parentEmail, subject, header, body);
            }
        }


        return ResponseEntity.ok(ResponseObject.builder()
                .message(String.format("Successfully assigned %d students to active classes.", newAssignments.size()))
                .success(true)
                .data(null)
                .build());
    }

    @Override
    public ResponseEntity<ResponseObject> viewListOfStudentsNotAssignedToAnyClassByYearAndGrade(String year, String grade, int page, int size) {
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

        // Tính toán phân trang
        int total = studentsToAssign.size();
        int fromIndex = page * size;
        int toIndex = Math.min(fromIndex + size, total);

        List<Student> pagedList = new ArrayList<>();
        if (fromIndex < total) {
            pagedList = studentsToAssign.subList(fromIndex, toIndex);
        }
        List<Map<String, Object>> dataList = pagedList.stream()
                .map(this::buildStudentDetail)
                .toList();
        // Trả về cả tổng số lượng và trang hiện tại cho FE
        Map<String, Object> response = new HashMap<>();
        response.put("page", page);
        response.put("size", size);
        response.put("totalElements", total);
        response.put("totalPages", (int) Math.ceil((double) total / size));
        response.put("data", dataList);

        return ResponseEntity.ok().body(
                ResponseObject.builder()
                        .message("View List Of Students Not Assigned To Any Classes By Grade And Year Successfully")
                        .success(true)
                        .data(response)
                        .build()
        );
    }

    @Override
    public ResponseEntity<ResponseObject> assignAvailableStudents(AssignStudentsToClassRequest request) {
        List<Integer> studentIds = request.getStudentIds();

        // Tìm student theo ID
        List<Student> found = studentRepo.findAllByIdIn(studentIds);

        Set<Integer> foundIds = found.stream()
                .map(Student::getId)
                .collect(Collectors.toSet());

        // ID không tồn tại trong DB
        List<Integer> missingIds = studentIds.stream()
                .filter(id -> !foundIds.contains(id))
                .toList();

        Optional<Classes> optionalClass = classRepo.findById(request.getClassId());
        if (optionalClass.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ResponseObject.builder()
                            .success(false)
                            .message("Class with id " + request.getClassId() + " doesn't exist or has been deleted.")
                            .data(null)
                            .build());
        }

        Classes cls = optionalClass.get();

        if (cls.getStatus().equalsIgnoreCase(Status.CLASS_IN_PROGRESS.getValue())
                || cls.getStatus().equalsIgnoreCase(Status.CLASS_CLOSED.getValue())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ResponseObject.builder()
                            .success(false)
                            .message("Cannot assign students because class is already in progress or closed or deleted.")
                            .data(null)
                            .build());
        }

        // Danh sách ID đã gán trước đó
        Set<Integer> alreadyAssignedIds = cls.getStudentClassList().stream()
                .map(sc -> sc.getStudent().getId())
                .collect(Collectors.toSet());

        List<Integer> duplicateIds = studentIds.stream()
                .filter(alreadyAssignedIds::contains)
                .toList();

        // Nếu tất cả ID đều lỗi (đã gán hoặc không tồn tại)
        int validNewAssignments = found.size() - duplicateIds.size();
        if (validNewAssignments == 0) {
            StringBuilder errorMsg = new StringBuilder("No students were assigned.");
            if (!duplicateIds.isEmpty()) {
                errorMsg.append(" The following student IDs were already assigned: ").append(duplicateIds).append(".");
            }
            if (!missingIds.isEmpty()) {
                errorMsg.append(" The following student IDs do not exist or have been deleted: ").append(missingIds).append(".");
            }

            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ResponseObject.builder()
                            .success(false)
                            .message(errorMsg.toString())
                            .data(null)
                            .build());
        }

        // Tiến hành gán các học sinh hợp lệ
        List<StudentClass> scList = new ArrayList<>();
        for (Student student : found) {
            if (!alreadyAssignedIds.contains(student.getId())) {
                scList.add(StudentClass.builder()
                        .classes(cls)
                        .student(student)
                        .build());
            }
        }

        cls.getStudentClassList().addAll(scList);
        cls.setNumberStudent(cls.getStudentClassList().size());
        classRepo.save(cls);

        for (StudentClass sc : scList) {
            Student student = sc.getStudent();
            Parent parent = student.getParent();
            Account parentAccount = parent != null ? parent.getAccount() : null;

            if (parentAccount != null) {
                String parentName = parentAccount.getName();
                String parentEmail = parentAccount.getEmail();
                String studentName = student.getName();
                String className = cls.getName();
                String teacherName = cls.getTeacher() != null ? cls.getTeacher().getName() : "N/A";
                String startDateStr = cls.getStartDate().toString();

                String subject = "Thông báo xếp lớp cho học sinh " + studentName;
                String header = "Class Assignment Notification";
                String body = Format.getAssignClassSuccessfulForParentBody(
                        parentName, studentName, className, teacherName, startDateStr
                );

                mailService.sendMail(parentEmail, subject, header, body);
            }
        }

        // Tạo message thành công kèm cảnh báo nếu có
        StringBuilder message = new StringBuilder("Assigned " + scList.size() + " students to class successfully.");
        if (!duplicateIds.isEmpty()) {
            message.append(" The following student IDs were already assigned: ").append(duplicateIds).append(".");
        }
        if (!missingIds.isEmpty()) {
            message.append(" The following student IDs do not exist or have been deleted: ").append(missingIds).append(".");
        }

        return ResponseEntity.ok(
                ResponseObject.builder()
                        .success(true)
                        .message(message.toString())
                        .data(null)
                        .build()
        );
    }

    @Override
    public ResponseEntity<ResponseObject> viewListClassesOfChild(String childId) {

        String error = checkStudentId(childId);

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

        if (studentRepo.findById(Integer.parseInt(childId)).isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseObject.builder()
                            .message("Children with id " + childId + " does not exist or be deleted")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        List<StudentClass> studentClassList = studentClassRepo.findByStudent_Id(Integer.parseInt(childId));

        List<Classes> classesList = studentClassList.stream()
                .map(StudentClass::getClasses)
                .toList();

        List<Map<String, Object>> classes = classesList.stream()
                .sorted(Comparator.comparing(Classes::getAcademicYear).reversed())
                .map(this::buildClassDetail)
                .toList();

        return ResponseEntity.ok().body(
                ResponseObject.builder()
                        .message("View Assigned Classes Of Student Successfully")
                        .success(true)
                        .data(classes)
                        .build()
        );
    }

    @Override
    public ResponseEntity<ResponseObject> viewCurrentSchedule(String classId, ViewCurrentScheduleRequest request) {
        Optional<Classes> optionalClass = classRepo.findById(Integer.parseInt(classId));
        Classes cls = optionalClass.get();

        LocalDate monday = request.getDate();

        while (monday.getDayOfWeek().getValue() != 1) { // 1 = Monday
            monday = monday.minusDays(1);
        }
        LocalDate sunday = monday.plusDays(6); // Chủ nhật

        // 4. Tìm schedule chứa ít nhất 1 activity nằm trong tuần đó
        for (Schedule schedule : cls.getScheduleList()) {
            if (schedule.getActivityList() == null || schedule.getActivityList().isEmpty()) continue;

            LocalDate finalMonday = monday;
            boolean hasActivityInWeek = schedule.getActivityList().stream()
                    .anyMatch(act -> {
                        LocalDate actDate = act.getDate();
                        return !actDate.isBefore(finalMonday) && !actDate.isAfter(sunday);
                    });

            if (hasActivityInWeek) {
                Map<String, Object> response = new LinkedHashMap<>();
                response.put("scheduleId", schedule.getId());
                response.put("scheduleName", schedule.getWeekName());

                return ResponseEntity.ok(
                        ResponseObject.builder()
                                .success(true)
                                .message("Schedule found for week: " + monday + " to " + sunday)
                                .data(response)
                                .build()
                );
            }
        }

        // 5. Không có schedule nào chứa activity trong tuần đó
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ResponseObject.builder()
                        .success(false)
                        .message("No schedule found for week: " + monday + " to " + sunday)
                        .data(null)
                        .build()
        );

    }


    @Override
    public ResponseEntity<ResponseObject> reportNumberOfClassesByTermYear(String year) {

        List<Integer> years = admissionTermRepo.findAll()
                .stream()
                .map(AdmissionTerm::getYear)
                .distinct()
                .sorted()
                .toList();

        if (year.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .message("Year cannot be empty")
                            .success(false)
                            .data(null)
                            .build()
            );
        }
        try {
            int numberYear = Integer.parseInt(year);

            if (!years.contains(numberYear)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        ResponseObject.builder()
                                .message("The academic year you selected does not exist in any admission term. Please verify your selection and try again.")
                                .success(false)
                                .data(null)
                                .build()
                );
            }

        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .message("Year must be number")
                            .success(false)
                            .data(null)
                            .build()
            );        }
        int total = classRepo.countByAcademicYear(Integer.parseInt(year));

        Map<String, Integer> gradeCounts = new LinkedHashMap<>();
        for (Grade grade : Grade.values()) {
            int count = classRepo.countByAcademicYearAndGrade(Integer.parseInt(year), grade);
            gradeCounts.put(grade.name(), count);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("totalClasses", total);
        result.put("byGrade", gradeCounts);

        return ResponseEntity.ok(
                ResponseObject.builder()
                        .success(true)
                        .message("Class statistics for academic year " + year)
                        .data(result)
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

    private String checkClassId(String classId) {

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

    private String checkStudentId(String studentId) {

        if (studentId.isEmpty()) {
            return "Student Id cannot be empty";
        }

        try {
            Integer.parseInt(studentId);
        } catch (IllegalArgumentException ex) {
            return "Student Id must be a number";
        }
        return "";
    }

    private String checkAcademicYearAndGrade(String year, String grade, List<Integer> validYears) {
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

    private Map<String, Object> buildStudentDetail(Student student) {
        Map<String, Object> studentDetail = new HashMap<>();
        studentDetail.put("id", student.getId());
        studentDetail.put("name", student.getName());
        studentDetail.put("gender", student.getGender());
        studentDetail.put("dateOfBirth", student.getDateOfBirth());
        studentDetail.put("placeOfBirth", student.getPlaceOfBirth());
        return studentDetail;
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
        data.put("startTime", activity.getStartTime());
        data.put("endTime", activity.getEndTime());
        data.put("date", activity.getDate());
        return data;
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

}
