package com.swd392.group1.pes.services.implementors;

import com.swd392.group1.pes.dto.requests.GenerateClassesRequest;
import com.swd392.group1.pes.dto.response.ResponseObject;
import com.swd392.group1.pes.enums.Grade;
import com.swd392.group1.pes.enums.Role;
import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.models.Account;
import com.swd392.group1.pes.models.Activity;
import com.swd392.group1.pes.models.AdmissionForm;
import com.swd392.group1.pes.models.AdmissionTerm;
import com.swd392.group1.pes.models.Classes;
import com.swd392.group1.pes.models.Parent;
import com.swd392.group1.pes.models.Schedule;
import com.swd392.group1.pes.models.Student;
import com.swd392.group1.pes.models.StudentClass;
import com.swd392.group1.pes.models.Syllabus;
import com.swd392.group1.pes.repositories.AccountRepo;
import com.swd392.group1.pes.repositories.ActivityRepo;
import com.swd392.group1.pes.repositories.AdmissionFormRepo;
import com.swd392.group1.pes.repositories.AdmissionTermRepo;
import com.swd392.group1.pes.repositories.ClassRepo;
import com.swd392.group1.pes.repositories.ScheduleRepo;
import com.swd392.group1.pes.repositories.StudentClassRepo;
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
            }
        }

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
                    .message("No new students to assign.").success(false).data(null).build());
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
                    .success(false)
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
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
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

        if (activityRepo.findBySchedule_Id(Integer.parseInt(scheduleId)).isEmpty()){
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseObject.builder()
                            .message("Schedule with id " + scheduleId + " does not exist or be deleted")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        List<Activity> activities = activityRepo.findBySchedule_Id(Integer.parseInt(scheduleId));

        Map<String, List<Map<String, Object>>> grouped = activities.stream()
                .collect(Collectors.groupingBy(
                        a -> a.getDayOfWeek().toString(), // key: "MONDAY", "TUESDAY",...
                        LinkedHashMap::new,                // giữ thứ tự ngày nếu muốn
                        Collectors.mapping(this::buildActivityDetail, Collectors.toList())
                ));

        List<Map<String, Object>> groupedList = grouped.entrySet().stream()
                .map(e -> {
                    Map<String, Object> group = new HashMap<>();
                    group.put("dayOfWeek", e.getKey());
                    group.put("activities", e.getValue());
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
        String fileNameTimestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

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
                if (student == null) continue;

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
            return ResponseEntity.ok(ResponseObject.builder()
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
                .filter(cls -> cls.getNumberStudent() < maxStudentPerClass)
                .toList();
        // If there are no classes with available slots, return an error
        if (classesWithSlots.isEmpty()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(
                    ResponseObject.builder()
                            .message("All classes are full. There are no available classes to assign new students.")
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

        return ResponseEntity.ok(ResponseObject.builder()
                .message(String.format("Successfully assigned %d students to classes.", studentsToAssign.size()))
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

}
