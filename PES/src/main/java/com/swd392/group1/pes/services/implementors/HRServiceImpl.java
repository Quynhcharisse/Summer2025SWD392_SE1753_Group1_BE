package com.swd392.group1.pes.services.implementors;

import com.swd392.group1.pes.dto.requests.CreateTeacherRequest;
import com.swd392.group1.pes.dto.requests.ProcessAccountRequest;
import com.swd392.group1.pes.dto.requests.UpdateTeacherRequest;
import com.swd392.group1.pes.dto.response.ResponseObject;
import com.swd392.group1.pes.enums.Role;
import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.models.Account;
import com.swd392.group1.pes.repositories.AccountRepo;
import com.swd392.group1.pes.services.HRService;
import com.swd392.group1.pes.services.MailService;
import com.swd392.group1.pes.utils.GenerateEmailTeacherUtil;
import com.swd392.group1.pes.utils.RandomPasswordUtil;
import com.swd392.group1.pes.utils.email.Format;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class HRServiceImpl implements HRService {

    private final AccountRepo accountRepo;
    private final MailService mailService;

    @Override
    public ResponseEntity<ResponseObject> processAccount(ProcessAccountRequest request, String action) {

        String error = processAccountValidate(request, action, accountRepo);

        if (!error.isEmpty()) {
            log.warn("Failed to {} account {}: {}", action, request.getEmail(), error);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        Account account = null;
        if (action.equalsIgnoreCase("ban")) {
            account = accountRepo.findByEmailAndStatus(request.getEmail(), Status.ACCOUNT_ACTIVE.getValue()).orElse(null);
        } else if (action.equalsIgnoreCase("unban")) {
            account = accountRepo.findByEmailAndStatus(request.getEmail(), Status.ACCOUNT_BAN.getValue()).orElse(null);
        }

        String newStatus;
        if (action.equalsIgnoreCase("ban")) {
            newStatus = Status.ACCOUNT_BAN.getValue();
        } else if (action.equalsIgnoreCase("unban")) {
            newStatus = Status.ACCOUNT_UNBAN.getValue();
        } else {
            log.error("Invalid action {} for account {}", action, request.getEmail());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .message("Invalid action")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        if (account == null) {
            log.warn("Account {} not found or in invalid state for action {}", request.getEmail(), action);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    ResponseObject.builder()
                            .message("Account not found or in invalid state")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        account.setStatus(newStatus);
        accountRepo.save(account);

        // 6. Chuẩn bị email notification
        String subject = action.equalsIgnoreCase("ban")
                ? "Your Account Has Been Suspended"
                : "Your Account Has Been Reactivated";

        String heading = action.equalsIgnoreCase("ban")
                ? "🚫 Account Suspended"
                : "✅ Account Reactivated";

        String bodyHtml = action.equalsIgnoreCase("ban")
                ? Format.getAccountBannedBody(account.getName())
                : Format.getAccountReactivatedBody(account.getName());

        try {
            mailService.sendMail(
                    account.getEmail(),
                    subject,
                    heading,
                    bodyHtml
            );
        } catch (Exception e) {
            log.error("Failed to send notification email to {} for action {}: {}",
                    account.getEmail(), action, e.getMessage());
        }

        String msg = action.equalsIgnoreCase("ban") ?
                "Account banned successfully" : "Account unbanned successfully";

        log.info("Successfully {} account {} ({})", action, account.getEmail(), account.getRole());

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseObject.builder()
                        .message(msg)
                        .success(true)
                        .data(null)
                        .build()
        );
    }

    public static String processAccountValidate(ProcessAccountRequest request, String action, AccountRepo accountRepo) {

        Account acc = null;

        if (action.equalsIgnoreCase("ban")) {

            acc = accountRepo.findByEmailAndStatus(request.getEmail(), Status.ACCOUNT_ACTIVE.getValue()).orElse(null);
        } else if (action.equalsIgnoreCase("unban")) {

            acc = accountRepo.findByEmailAndStatus(request.getEmail(), Status.ACCOUNT_BAN.getValue()).orElse(null);
        } else {
            return "Invalid action";
        }

        if (acc == null) {
            return "Account not found or in invalid state for action: " + action;
        }

        if (!acc.getRole().equals(Role.PARENT) && !acc.getRole().equals(Role.TEACHER)) {
            return "Can only process parent or teacher accounts";
        }

        return "";
    }

    @Override
    public ResponseEntity<ResponseObject> createTeacherAcc(CreateTeacherRequest request) {

        String error = createTeacherValidation(request, accountRepo);
        if (!error.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    ResponseObject.builder()
                            .message(error)
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        // Tạo mật khẩu ngẫu nhiên 8 ký tự
        String rawPassword = RandomPasswordUtil.generateRandomString(8);

        Account account = accountRepo.save(
                Account.builder()
                        .email(GenerateEmailTeacherUtil.generateTeacherEmail(accountRepo))
                        .password(rawPassword)
                        .name(request.getName())
                        .avatarUrl(null)
                        .gender(request.getGender())
                        .status(Status.ACCOUNT_ACTIVE.getValue())
                        .role(Role.TEACHER)
                        .createdAt(LocalDateTime.now())
                        .build()
        );

        // 3. Chuẩn bị email
        String subject = "[PES] New Teacher Account Created";
        String heading = "🎓 New Teacher Account Created";
        String bodyHtml = Format.getTeacherBody(
                account.getEmail(),
                rawPassword
        );

        // 4. Gửi mail (MailServiceImpl đã tự catch/log lỗi)
        mailService.sendMail(
                account.getEmail(),
                subject,
                heading,
                bodyHtml
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(
                ResponseObject.builder()
                        .message("Create Teacher Successfully")
                        .success(true)
                        .data(null)
                        .build()
        );
    }

    public static String createTeacherValidation(CreateTeacherRequest request, AccountRepo accountRepo) {

        if (request.getName().trim().isEmpty()) {
            return "Name is required";
        }

        if (!request.getName().trim().matches("^[a-zA-Z\\s'-]+$")) {
            return "Name can only contain letters, spaces, hyphens, and apostrophes";
        }

        if (request.getName().trim().length() < 2 || request.getName().trim().length() > 50) {
            return "Name must be between 2 and 50 characters";
        }

        if (request.getGender().trim().isEmpty()) {
            return "Gender is required";
        }

        if (!request.getGender().equals("male") &&
                !request.getGender().equals("female") &&
                !request.getGender().equals("other")) {
            return "Gender must be male, female, or other";
        }
        return "";
    }

    @Override
    public ResponseEntity<ResponseObject> updateTeacherAcc(String id, UpdateTeacherRequest request) {
        if (id == null || id.isBlank()) {
            return ResponseEntity.badRequest().body(
                ResponseObject.builder()
                    .message("Teacher id is required for update")
                    .success(false)
                    .data(null)
                    .build()
            );
        }
        int teacherId;
        try {
            teacherId = Integer.parseInt(id);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest().body(
                ResponseObject.builder()
                    .message("Invalid teacher id format")
                    .success(false)
                    .data(null)
                    .build()
            );
        }
        Account teacher = accountRepo.findByIdAndRole(teacherId, Role.TEACHER);
        if (teacher == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ResponseObject.builder()
                    .message("Teacher not found")
                    .success(false)
                    .data(null)
                    .build()
            );
        }
        String error = updateTeacherValidation(request);
        if (!error.isEmpty()) {
            return ResponseEntity.badRequest().body(
                ResponseObject.builder()
                    .message(error)
                    .success(false)
                    .data(null)
                    .build()
            );
        }
        if (request.getName() != null && !request.getName().isBlank()) {
            teacher.setName(request.getName().trim());
        }
        if (request.getPhone() != null && !request.getPhone().isBlank()) {
            teacher.setPhone(request.getPhone().trim());
        }
        if (request.getGender() != null && !request.getGender().isBlank()) {
            teacher.setGender(request.getGender().trim());
        }
        if (request.getAvatarUrl() != null && !request.getAvatarUrl().isBlank()) {
            teacher.setAvatarUrl(request.getAvatarUrl().trim());
        }
        accountRepo.save(teacher);
        return ResponseEntity.ok(
            ResponseObject.builder()
                .message("Teacher updated successfully")
                .success(true)
                .data(null)
                .build()
        );
    }

    public static String updateTeacherValidation(UpdateTeacherRequest request) {
        if (request.getName() != null) {
            String name = request.getName().trim();
            if (name.isEmpty()) {
                return "Name is required";
            }
            if (!name.matches("^[a-zA-Z\\s'-]+$")) {
                return "Name can only contain letters, spaces, hyphens, and apostrophes";
            }
            if (name.length() < 2 || name.length() > 50) {
                return "Name must be between 2 and 50 characters";
            }
        }
        if (request.getPhone() != null) {
            String phone = request.getPhone().trim();
            if (!phone.isEmpty() && !phone.matches("^(03|05|07|08|09)\\d{8}$")) {
                return "Phone number must start with a valid prefix and be 10 digits.";
            }
        }
        if (request.getGender() != null) {
            String gender = request.getGender().trim();
            if (gender.isEmpty()) {
                return "Gender is required";
            }
            if (!gender.equals("male") && !gender.equals("female") && !gender.equals("other")) {
                return "Gender must be male, female, or other";
            }
        }
        return "";
    }

    @Override
    public ResponseEntity<ResponseObject> viewTeacherList() {

        List<Map<String, Object>> teacherList = accountRepo.findByRole(Role.TEACHER).stream()
                .map(account ->
                        {
                            Map<String, Object> data = new HashMap<>();
                            data.put("email", account.getEmail());
                            data.put("name", account.getName());
                            data.put("avatarUrl", account.getAvatarUrl());
                            data.put("gender", account.getGender());
                            data.put("role", account.getRole());
                            data.put("status", account.getStatus());
                            return data;
                        }
                )
                .toList();

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseObject.builder()
                        .message("")
                        .success(true)
                        .data(teacherList)
                        .build()
        );
    }

    @Override
    public ResponseEntity<ResponseObject> viewParentList() {
        List<Map<String, Object>> parentList = accountRepo.findByRole(Role.PARENT).stream()
                .map(parentAcc -> {
                    Map<String, Object> data = new HashMap<>();
                    data.put("email", parentAcc.getEmail());
                    data.put("name", parentAcc.getName());
                    data.put("phone", parentAcc.getPhone());
                    data.put("gender", parentAcc.getGender());
                    data.put("identityNumber", parentAcc.getIdentityNumber());
                    data.put("avatarUrl", parentAcc.getAvatarUrl());
                    data.put("address", parentAcc.getAddress());

                    // xử lý an toàn khi parentAcc.getParent() có thể null
                    if (parentAcc.getParent() != null) {
                        data.put("job", parentAcc.getParent().getJob());
                        data.put("relationshipToChild", parentAcc.getParent().getRelationshipToChild());
                    } else {
                        data.put("job", null);
                        data.put("relationshipToChild", null);
                    }

                    data.put("role", parentAcc.getRole());
                    data.put("status", parentAcc.getStatus());
                    return data;
                })
                .toList();

        return ResponseEntity.status(HttpStatus.OK).body(
                ResponseObject.builder()
                        .message("")
                        .success(true)
                        .data(parentList)
                        .build()
        );
    }

    public ResponseEntity<Resource> exportTeacherListToExcel() {
        List<Account> teachers = accountRepo.findByRole(Role.TEACHER);
        String dateTimeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

        String[] columns = {"Email", "Name", "AvatarUrl", "Gender", "Role", "Status"};

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Teachers");
            Row header = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                header.createCell(i).setCellValue(columns[i]);
            }
            int rowIdx = 1;
            for (Account teacher : teachers) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(Objects.toString(teacher.getEmail(), ""));
                row.createCell(1).setCellValue(Objects.toString(teacher.getName(), ""));
                row.createCell(2).setCellValue(Objects.toString(teacher.getAvatarUrl(), ""));
                row.createCell(3).setCellValue(Objects.toString(teacher.getGender(), ""));
                row.createCell(4).setCellValue(teacher.getRole() != null ? teacher.getRole().toString() : "");
                row.createCell(5).setCellValue(Objects.toString(teacher.getStatus(), ""));
            }
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(out);
            ByteArrayResource resource = new ByteArrayResource(out.toByteArray());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=teachers_" + dateTimeStr + ".xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(resource);
        } catch (Exception e) {
            throw new RuntimeException("Excel export failed", e);
        }
    }

    public ResponseEntity<Resource> exportParentListToExcel() {
        List<Account> parents = accountRepo.findByRole(Role.PARENT);
        String dateTimeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

        String[] columns = {
            "Email", "Name", "Phone", "Gender", "Identity Number", "Address",
            "Job", "Relationship To Child", "Role", "Status"
        };

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Parents");
            Row header = sheet.createRow(0);
            for (int i = 0; i < columns.length; i++) {
                header.createCell(i).setCellValue(columns[i]);
            }
            int rowIdx = 1;
            for (Account parentAcc : parents) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(Objects.toString(parentAcc.getEmail(), ""));
                row.createCell(1).setCellValue(Objects.toString(parentAcc.getName(), ""));
                row.createCell(2).setCellValue(Objects.toString(parentAcc.getPhone(), ""));
                row.createCell(3).setCellValue(Objects.toString(parentAcc.getGender(), ""));
                row.createCell(4).setCellValue(Objects.toString(parentAcc.getIdentityNumber(), ""));
                row.createCell(5).setCellValue(Objects.toString(parentAcc.getAddress(), ""));
                row.createCell(6).setCellValue(
                    parentAcc.getParent() != null
                        ? Objects.toString(parentAcc.getParent().getJob(), "")
                        : ""
                );
                row.createCell(7).setCellValue(
                    parentAcc.getParent() != null
                        ? Objects.toString(parentAcc.getParent().getRelationshipToChild(), "")
                        : ""
                );
                row.createCell(8).setCellValue(parentAcc.getRole() != null ? parentAcc.getRole().toString() : "");
                row.createCell(9).setCellValue(Objects.toString(parentAcc.getStatus(), ""));
            }
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(out);
            ByteArrayResource resource = new ByteArrayResource(out.toByteArray());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=parents_" + dateTimeStr + ".xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(resource);
        } catch (Exception e) {
            throw new RuntimeException("Excel export failed", e);
        }
    }


}
