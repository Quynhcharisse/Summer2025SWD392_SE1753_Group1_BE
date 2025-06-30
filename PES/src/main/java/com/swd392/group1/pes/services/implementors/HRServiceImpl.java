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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
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
                        .createdAt(LocalDate.now())
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


}
