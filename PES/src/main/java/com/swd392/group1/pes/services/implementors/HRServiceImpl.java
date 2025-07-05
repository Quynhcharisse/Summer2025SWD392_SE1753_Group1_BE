package com.swd392.group1.pes.services.implementors;

import com.swd392.group1.pes.email.Format;
import com.swd392.group1.pes.enums.Role;
import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.models.Account;
import com.swd392.group1.pes.repositories.AccountRepo;
import com.swd392.group1.pes.requests.CreateTeacherRequest;
import com.swd392.group1.pes.requests.ProcessAccountRequest;
import com.swd392.group1.pes.response.ResponseObject;
import com.swd392.group1.pes.services.HRService;
import com.swd392.group1.pes.services.MailService;
import com.swd392.group1.pes.utils.GenerateEmailTeacherUtil;
import com.swd392.group1.pes.utils.RandomPasswordUtil;
import com.swd392.group1.pes.validations.HRValidation.CreateTeacherValidation;
import com.swd392.group1.pes.validations.HRValidation.ProcessAccountValidation;
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

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class HRServiceImpl implements HRService {

    private final AccountRepo accountRepo;
    private final MailService mailService;

    @Override
    public ResponseEntity<ResponseObject> processAccount(ProcessAccountRequest request, String action) {

        String error = ProcessAccountValidation.processAccountValidate(request, action, accountRepo);

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

    @Override
    public ResponseEntity<ResponseObject> createTeacherAcc(CreateTeacherRequest request) {

        String error = CreateTeacherValidation.validate(request, accountRepo);
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

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Teachers");
            Row header = sheet.createRow(0);
            String[] columns = {"Email", "Name", "AvatarUrl", "Gender", "Role", "Status"};
            for (int i = 0; i < columns.length; i++) {
                header.createCell(i).setCellValue(columns[i]);
            }
            int rowIdx = 1;
            for (Account teacher : teachers) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(teacher.getEmail() != null ? teacher.getEmail() : "");
                row.createCell(1).setCellValue(teacher.getName() != null ? teacher.getName() : "");
                row.createCell(2).setCellValue(teacher.getAvatarUrl() != null ? teacher.getAvatarUrl() : "");
                row.createCell(3).setCellValue(teacher.getGender() != null ? teacher.getGender() : "");
                row.createCell(4).setCellValue(teacher.getRole() != null ? teacher.getRole().toString() : "");
                row.createCell(5).setCellValue(teacher.getStatus() != null ? teacher.getStatus() : "");
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            ByteArrayResource resource = new ByteArrayResource(out.toByteArray());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=teachers_" + dateTimeStr + ".xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(resource);
        } catch (IOException e) {
            throw new RuntimeException("Excel export failed", e);
        }
    }

    public ResponseEntity<Resource> exportParentListToExcel() {
        List<Account> parents = accountRepo.findByRole(Role.PARENT);
        String dateTimeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Parents");
            Row header = sheet.createRow(0);
            String[] columns = {
                    "Email", "Name", "Phone", "Gender", "Identity Number", "Address", "Job", "Relationship To Child", "Role", "Status"
            };
            for (int i = 0; i < columns.length; i++) {
                header.createCell(i).setCellValue(columns[i]);
            }
            int rowIdx = 1;
            for (Account parentAcc : parents) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(parentAcc.getEmail() != null ? parentAcc.getEmail() : "");
                row.createCell(1).setCellValue(parentAcc.getName() != null ? parentAcc.getName() : "");
                row.createCell(2).setCellValue(parentAcc.getPhone() != null ? parentAcc.getPhone() : "");
                row.createCell(3).setCellValue(parentAcc.getGender() != null ? parentAcc.getGender() : "");
                row.createCell(4).setCellValue(parentAcc.getIdentityNumber() != null ? parentAcc.getIdentityNumber() : "");
                row.createCell(5).setCellValue(parentAcc.getAddress() != null ? parentAcc.getAddress() : "");
                row.createCell(6).setCellValue(
                    parentAcc.getParent() != null && parentAcc.getParent().getJob() != null
                        ? parentAcc.getParent().getJob() : ""
                );
                row.createCell(7).setCellValue(
                    parentAcc.getParent() != null && parentAcc.getParent().getRelationshipToChild() != null
                        ? parentAcc.getParent().getRelationshipToChild() : ""
                );
                row.createCell(8).setCellValue(parentAcc.getRole() != null ? parentAcc.getRole().toString() : "");
                row.createCell(9).setCellValue(parentAcc.getStatus() != null ? parentAcc.getStatus() : "");
            }
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
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

    public ResponseEntity<Resource> exportChildrenListToExcel() {
        List<Account> parentAccounts = accountRepo.findByRole(Role.PARENT);
        String dateTimeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Children");
            Row header = sheet.createRow(0);
            String[] columns = {
                    "Parent Email", "Parent Name",
                    "Child Name", "Child Gender", "Child Date of Birth", "Child Place of Birth"
            };
            for (int i = 0; i < columns.length; i++) {
                header.createCell(i).setCellValue(columns[i]);
            }
            int rowIdx = 1;
            for (Account parentAcc : parentAccounts) {
                String parentEmail = parentAcc.getEmail();
                String parentName = parentAcc.getName();
                if (parentAcc.getParent() != null && parentAcc.getParent().getStudentList() != null) {
                    for (com.swd392.group1.pes.models.Student child : parentAcc.getParent().getStudentList()) {
                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(parentEmail != null ? parentEmail : "");
                        row.createCell(1).setCellValue(parentName != null ? parentName : "");
                        row.createCell(2).setCellValue(child.getName() != null ? child.getName() : "");
                        row.createCell(3).setCellValue(child.getGender() != null ? child.getGender() : "");
                        row.createCell(4).setCellValue(child.getDateOfBirth() != null ? child.getDateOfBirth().toString() : "");
                        row.createCell(5).setCellValue(child.getPlaceOfBirth() != null ? child.getPlaceOfBirth() : "");
                    }
                }
            }
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            ByteArrayResource resource = new ByteArrayResource(out.toByteArray());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=children_" + dateTimeStr + ".xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(resource);
        } catch (Exception e) {
            throw new RuntimeException("Excel export failed", e);
        }
    }

    public ResponseEntity<Resource> exportParentAndChildrenToExcel() {
        List<Account> parentAccounts = accountRepo.findByRole(Role.PARENT);
        String dateTimeStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Parents & Children");
            Row header = sheet.createRow(0);
            String[] columns = {
                    "Parent Email", "Parent Name", "Parent Phone", "Parent Gender", "Parent Identity Number",
                    "Parent Address", "Parent Job", "Relationship To Child",
                    "Child Name", "Child Gender", "Child Date of Birth", "Child Place of Birth"
            };
            for (int i = 0; i < columns.length; i++) {
                header.createCell(i).setCellValue(columns[i]);
            }
            int rowIdx = 1;
            for (Account parentAcc : parentAccounts) {
                String parentEmail = parentAcc.getEmail();
                String parentName = parentAcc.getName();
                String parentPhone = parentAcc.getPhone();
                String parentGender = parentAcc.getGender();
                String parentIdentity = parentAcc.getIdentityNumber();
                String parentAddress = parentAcc.getAddress();
                String parentJob = "";
                String relationship = "";
                List<com.swd392.group1.pes.models.Student> children = List.of();

                if (parentAcc.getParent() != null) {
                    parentJob = parentAcc.getParent().getJob();
                    relationship = parentAcc.getParent().getRelationshipToChild();
                    if (parentAcc.getParent().getStudentList() != null) {
                        children = parentAcc.getParent().getStudentList();
                    }
                }

                if (children.isEmpty()) {
                    Row row = sheet.createRow(rowIdx++);
                    row.createCell(0).setCellValue(parentEmail != null ? parentEmail : "");
                    row.createCell(1).setCellValue(parentName != null ? parentName : "");
                    row.createCell(2).setCellValue(parentPhone != null ? parentPhone : "");
                    row.createCell(3).setCellValue(parentGender != null ? parentGender : "");
                    row.createCell(4).setCellValue(parentIdentity != null ? parentIdentity : "");
                    row.createCell(5).setCellValue(parentAddress != null ? parentAddress : "");
                    row.createCell(6).setCellValue(parentJob != null ? parentJob : "");
                    row.createCell(7).setCellValue(relationship != null ? relationship : "");
                } else {
                    for (com.swd392.group1.pes.models.Student child : children) {
                        Row row = sheet.createRow(rowIdx++);
                        row.createCell(0).setCellValue(parentEmail != null ? parentEmail : "");
                        row.createCell(1).setCellValue(parentName != null ? parentName : "");
                        row.createCell(2).setCellValue(parentPhone != null ? parentPhone : "");
                        row.createCell(3).setCellValue(parentGender != null ? parentGender : "");
                        row.createCell(4).setCellValue(parentIdentity != null ? parentIdentity : "");
                        row.createCell(5).setCellValue(parentAddress != null ? parentAddress : "");
                        row.createCell(6).setCellValue(parentJob != null ? parentJob : "");
                        row.createCell(7).setCellValue(relationship != null ? relationship : "");
                        row.createCell(8).setCellValue(child.getName() != null ? child.getName() : "");
                        row.createCell(9).setCellValue(child.getGender() != null ? child.getGender() : "");
                        row.createCell(10).setCellValue(child.getDateOfBirth() != null ? child.getDateOfBirth().toString() : "");
                        row.createCell(11).setCellValue(child.getPlaceOfBirth() != null ? child.getPlaceOfBirth() : "");
                    }
                }
            }
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            ByteArrayResource resource = new ByteArrayResource(out.toByteArray());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=parents_children_" + dateTimeStr + ".xlsx")
                    .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                    .body(resource);
        } catch (Exception e) {
            throw new RuntimeException("Excel export failed", e);
        }
    }
}
