package com.swd392.group1.pes.services.implementors;

import com.swd392.group1.pes.dto.requests.CancelEventRequest;
import com.swd392.group1.pes.dto.requests.CreateEventRequest;
import com.swd392.group1.pes.dto.requests.RegisterEventRequest;
import com.swd392.group1.pes.dto.response.ResponseObject;
import com.swd392.group1.pes.enums.Role;
import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.models.Account;
import com.swd392.group1.pes.models.Event;
import com.swd392.group1.pes.models.EventParticipate;
import com.swd392.group1.pes.models.Parent;
import com.swd392.group1.pes.models.Student;
import com.swd392.group1.pes.models.TeacherEvent;
import com.swd392.group1.pes.repositories.AccountRepo;
import com.swd392.group1.pes.repositories.EventParticipateRepo;
import com.swd392.group1.pes.repositories.EventRepo;
import com.swd392.group1.pes.repositories.TeacherEventRepo;
import com.swd392.group1.pes.services.EventService;
import com.swd392.group1.pes.services.MailService;
import com.swd392.group1.pes.utils.email.Format;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepo eventRepo;
    private final AccountRepo accountRepo;
    private final TeacherEventRepo teacherEventRepo;
    private final EventParticipateRepo eventParticipateRepo;
    private final MailService mailService;


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

    public String validateCreateEvent(CreateEventRequest request, EventRepo eventRepo) {

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

        if (request.getLocation().length() > 100) {
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
    public ResponseEntity<ResponseObject> viewAssignedStudentsOfEvent(String id) {
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
        List<EventParticipate> ep = eventParticipateRepo.findAllByEventId(Integer.parseInt(id));

        List<Map<String, Object>> students = ep.stream()
                .map(e -> buildStudentDetail(e.getStudent()))
                .toList();

        return ResponseEntity.ok().body(
                ResponseObject.builder()
                        .message("View assigned students of event successfully")
                        .success(true)
                        .data(students)
                        .build()
        );
    }

    @Override
    public ResponseEntity<ByteArrayResource> exportEventParticipateOfEvent(String id) {

        String error = checkEventId(id);

        if (!error.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    null
            );
        }
        try (Workbook workbook = new XSSFWorkbook()) {

                // Lsson không tồn tại hoặc bị xóa
        if (eventRepo.findById(Integer.parseInt(id)).isEmpty())
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                   null
            );

        Event event = eventRepo.findById(Integer.parseInt(id)).get();

            List<EventParticipate> ep = eventParticipateRepo.findAllByEventId(Integer.parseInt(id));

        List<Student> students =  ep.stream()
                .map(EventParticipate::getStudent)
                .toList();

            Sheet sheet = workbook.createSheet("Students");

            String[] headers = {"ID", "Student Name", "Gender", "Age" , "Place Of Birth", "Parent Name", "Parent Phone number"};

            Row header = sheet.createRow(0);

            for (int i = 0; i < headers.length; i++) {
                header.createCell(i).setCellValue(headers[i]);
            }

            int rowIdx = 1;

            for(Student student : students){
                Row row = sheet.createRow(rowIdx++);
                Account parentAcc = (student.getParent() != null) ? student.getParent().getAccount() : null;
                row.createCell(0).setCellValue(Objects.toString(student.getId(), ""));
                row.createCell(1).setCellValue(Objects.toString(student.getName(), ""));
                row.createCell(2).setCellValue(Objects.toString(student.getGender(), ""));
                row.createCell(3).setCellValue(Objects.toString(Period.between(student.getDateOfBirth(), event.getRegistrationDeadline().toLocalDate()).getYears(), ""));
                row.createCell(4).setCellValue(Objects.toString(student.getPlaceOfBirth(), ""));
                row.createCell(5).setCellValue(Objects.toString(parentAcc != null ? parentAcc.getName() : null, ""));
                row.createCell(6).setCellValue(Objects.toString(parentAcc != null ? parentAcc.getPhone() : null, ""));
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            ByteArrayResource resource = new ByteArrayResource(out.toByteArray());
            String fileName = event.getName().replaceAll("\\s+", "_") + "-participants.xlsx";
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + fileName)
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(resource);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    null
            );
        }
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

    private Map<String, Object> buildStudentDetail(Student student) {
        Map<String, Object> studentDetail = new HashMap<>();
        studentDetail.put("id", student.getId());
        studentDetail.put("name", student.getName());
        studentDetail.put("parentName", student.getParent().getAccount().getName());
        studentDetail.put("parentPhoneNumber", student.getParent().getAccount().getPhone());
        studentDetail.put("gender", student.getGender());
        studentDetail.put("dateOfBirth", student.getDateOfBirth());
        studentDetail.put("placeOfBirth", student.getPlaceOfBirth());
        return studentDetail;
    }

}