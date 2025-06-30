package com.swd392.group1.pes.utils;

import com.swd392.group1.pes.email.Format;
import com.swd392.group1.pes.enums.Grade;
import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.models.AdmissionForm;
import com.swd392.group1.pes.models.TermItem;
import com.swd392.group1.pes.repositories.AdmissionFormRepo;
import com.swd392.group1.pes.repositories.AdmissionTermRepo;
import com.swd392.group1.pes.repositories.StudentRepo;
import com.swd392.group1.pes.repositories.TermItemRepo;
import com.swd392.group1.pes.services.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AutoProcessFormScheduler {

    private final AdmissionFormRepo admissionFormRepo;
    private final StudentRepo studentRepo;
    private final TermItemRepo termItemRepo;
    private final AdmissionTermRepo admissionTermRepo;
    private final MailService mailService;

    @Scheduled(fixedDelay = 10000)
    @Transactional(rollbackFor = Exception.class)
    public void autoApprovePendingForms() {
        List<AdmissionForm> pendingForms = admissionFormRepo.findByStatus(Status.PENDING_APPROVAL);
        LocalDateTime today = LocalDateTime.now();

        for (AdmissionForm form : pendingForms) {
            TermItem termItem = form.getTermItem();
            LocalDateTime termStartDate = termItem.getAdmissionTerm().getStartDate();
            LocalDate termStartDateLocal = termStartDate.toLocalDate(); //vì birth day trẻ chỉ là Local Date

            // 1) Kiểm tra deadline
            if (termItem.getAdmissionTerm().getEndDate().isBefore(today)) {
                reject(form, "Hạn nộp hồ sơ đã kết thúc: " + termItem.getAdmissionTerm().getEndDate().toLocalDate());
                continue;
            }

            // 2) Kiểm tra tuổi (tính theo ngày bắt đầu term)
            LocalDate dob = form.getStudent().getDateOfBirth();
            int ageAtStart = Period.between(dob, termStartDateLocal).getYears();

            if (!isAgeValidForGrade(dob, form.getTermItem().getGrade(), termStartDateLocal)) {
                String reason = String.format(
                        "Tuổi không hợp lệ: %d tuổi (tính đến ngày %s). Yêu cầu cho lớp %s là %d tuổi.",
                        ageAtStart,
                        termStartDate,
                        form.getTermItem().getGrade().name(),
                        form.getTermItem().getGrade().getAge()
                );
                reject(form, reason);
                continue;
            }

            // 3) Kiểm tra sức chứa của lớp
            if (!canAcceptMoreStudents(termItem)) {
                String reason = String.format(
                        "Lớp '%s' đã đủ số lượng học sinh (%d/%d).",
                        termItem.getGrade().getName(), termItem.getCurrentRegisteredStudents(), termItem.getMaxNumberRegistration()
                );
                reject(form, reason);
                continue;
            }

            try {
                // 4) Duyệt và chuyển sang chờ thanh toán

                if (form.getStudent().getId() == null) {
                    studentRepo.save(form.getStudent());
                }

                form.setStatus(Status.WAITING_PAYMENT);
                form.setPaymentExpiryDate(LocalDateTime.now().plusDays(2));

                int oldCount = termItem.getCurrentRegisteredStudents();
                termItem.setCurrentRegisteredStudents(oldCount + 1);
                termItemRepo.save(termItem);

                admissionFormRepo.save(form);

                try {
                    String subject = "[PES] Hồ sơ nhập học đã được duyệt - Chờ thanh toán";
                    String heading = "✅ Hồ sơ nhập học đã được duyệt - Chờ thanh toán";
                    String bodyHtml = Format.getAdmissionApprovedBody(form.getStudent().getName());
                    mailService.sendMail(
                            form.getParent().getAccount().getEmail(),
                            subject,
                            heading,
                            bodyHtml
                    );
                } catch (Exception e) {
                    log.error("Failed to send approval/waiting payment email for Form {}: {}", form.getId(), e.getMessage());
                }
            } catch (Exception e) {
                log.error("Error processing Form {}: {}", form.getId(), e.getMessage(), e);
                throw e;
            }
        }
    }

    private void reject(AdmissionForm form, String reason) {
        try {
            form.setStatus(Status.REJECTED);
            form.setCancelReason(reason);
            admissionFormRepo.save(form);

            String subject = "[PES] Hồ sơ nhập học bị từ chối";
            String heading = "❌ Hồ sơ nhập học bị từ chối";
            String bodyHtml = Format.getAdmissionRejectedBody(form.getStudent().getName(), reason);
            mailService.sendMail(
                    form.getParent().getAccount().getEmail(),
                    subject,
                    heading,
                    bodyHtml
            );
        } catch (Exception e) {
            log.error("Error rejecting Form {}: {}", form.getId(), e.getMessage(), e);
            throw e;
        }
    }

    private static boolean isAgeValidForGrade(LocalDate dob, Grade grade, LocalDate termStartDate) {
        int ageAtStart = Period.between(dob, termStartDate).getYears();
        return ageAtStart == grade.getAge();
    }

    private boolean canAcceptMoreStudents(TermItem termItem) {
        return termItem.getCurrentRegisteredStudents() < termItem.getMaxNumberRegistration();
    }
}
