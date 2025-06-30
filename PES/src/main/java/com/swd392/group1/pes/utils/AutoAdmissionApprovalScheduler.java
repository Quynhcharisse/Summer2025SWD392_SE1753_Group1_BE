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
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AutoAdmissionApprovalScheduler {

    private final AdmissionFormRepo admissionFormRepo;
    private final StudentRepo studentRepo;
    private final TermItemRepo termItemRepo;
    private final AdmissionTermRepo admissionTermRepo;
    private final MailService mailService;

    @Scheduled(fixedDelay = 10000)
    @Transactional(rollbackFor = Exception.class)
    public void autoApprovePendingForms() {
        log.info("Starting auto-approval process...");
        List<AdmissionForm> pendingForms = admissionFormRepo.findByStatus(Status.PENDING_APPROVAL);
        log.info("Found {} pending forms to process", pendingForms.size());
        LocalDate today = LocalDate.now();

        for (AdmissionForm form : pendingForms) {
            log.info("Processing Form ID: {} for Student: {}", form.getId(), form.getStudent().getName());
            TermItem termItem = form.getTermItem();
            log.info("Term Item details - ID: {}, Current Students: {}", termItem.getId(), termItem.getCurrentRegisteredStudents());

            // 1) Kiểm tra deadline
            if (termItem.getAdmissionTerm().getEndDate().isBefore(today.atStartOfDay())) {
                log.warn("Form {} rejected - Deadline passed. End date: {}", form.getId(), termItem.getAdmissionTerm().getEndDate());
                reject(form, "Hạn nộp hồ sơ đã kết thúc: " + termItem.getAdmissionTerm().getEndDate().toLocalDate());
                continue;
            }
            log.info("Form {} passed deadline check", form.getId());

            // 2) Kiểm tra tuổi (3-5 tuổi)
            LocalDate dob = form.getStudent().getDateOfBirth();
            int age = calculateAge(dob);
            log.info("Student age check - DOB: {}, Current Age: {}", dob, age);

            if (!isAgeValidForGrade(dob, form.getTermItem().getGrade())) {
                String reason = String.format(
                        "Tuổi không hợp lệ: %d tuổi. Yêu cầu cho lớp %s là %d tuổi.",
                        age, form.getTermItem().getGrade().name(), form.getTermItem().getGrade().getAge()
                );
                log.warn("Form {} rejected - Invalid age: {}", form.getId(), age);
                reject(form, reason);
                continue;
            }
            log.info("Form {} passed age validation", form.getId());

            // 3) Kiểm tra sức chứa của lớp
            if (!canAcceptMoreStudents(termItem)) {
                String reason = String.format(
                        "Lớp '%s' đã đủ số lượng học sinh (%d/%d).",
                        termItem.getGrade().getName(), termItem.getCurrentRegisteredStudents(), termItem.getMaxNumberRegistration()
                );
                log.warn("Form {} rejected - Class is full.", form.getId());
                reject(form, reason);
                continue;
            }
            log.info("Form {} passed class capacity check", form.getId());


            try {
                // 4) Duyệt và chuyển sang chờ thanh toán
                log.info("Starting approval process for Form {}", form.getId());

                // Save new student if needed (should be done before updating term item to ensure student ID exists)
                if (form.getStudent().getId() == null) {
                    log.info("Saving new student for Form {}", form.getId());
                    studentRepo.save(form.getStudent());
                    log.info("New student saved with ID: {}", form.getStudent().getId());
                }

                // Update form status to WAITING_PAYMENT
                form.setStatus(Status.WAITING_PAYMENT); // Changed to WAITING_PAYMENT
                form.setPaymentExpiryDate(LocalDateTime.now().plusDays(2));
                log.info("Set form {} status to WAITING_PAYMENT with payment expiry: {}", form.getId(), form.getPaymentExpiryDate());

                // Update term item (increment registered students)
                int oldCount = termItem.getCurrentRegisteredStudents();
                termItem.setCurrentRegisteredStudents(oldCount + 1);
                termItemRepo.save(termItem);
                log.info("Updated term item {} registered students count: {} -> {}",
                        termItem.getId(), oldCount, termItem.getCurrentRegisteredStudents());

                // Save form
                admissionFormRepo.save(form);
                log.info("Successfully saved form {} with updated status", form.getId());

                // Gửi mail thông báo chờ thanh toán
                try {
                    String subject = "[PES] Hồ sơ nhập học đã được duyệt - Chờ thanh toán";
                    String heading = "✅ Hồ sơ nhập học đã được duyệt - Chờ thanh toán";
                    String bodyHtml = Format.getAdmissionApprovedBody(form.getStudent().getName()); // Bạn có thể tùy chỉnh hàm này để phù hợp với trạng thái chờ thanh toán
                    mailService.sendMail(
                            form.getParent().getAccount().getEmail(),
                            subject,
                            heading,
                            bodyHtml
                    );
                    log.info("Successfully sent approval/waiting payment email for Form {}", form.getId());
                } catch (Exception e) {
                    log.error("Failed to send approval/waiting payment email for Form {}: {}", form.getId(), e.getMessage());
                    // Consider throwing a custom exception here if email sending is critical
                }
            } catch (Exception e) {
                log.error("Error processing Form {}: {}", form.getId(), e.getMessage(), e);
                throw e; // Re-throw to trigger transaction rollback
            }
        }
        log.info("Completed auto-approval process");
    }

    private void reject(AdmissionForm form, String reason) {
        try {
            log.info("Rejecting Form {} with reason: {}", form.getId(), reason);
            form.setStatus(Status.REJECTED);
            form.setCancelReason(reason);
            admissionFormRepo.save(form);
            log.info("Successfully saved rejected form {}", form.getId());

            String subject = "[PES] Hồ sơ nhập học bị từ chối";
            String heading = "❌ Hồ sơ nhập học bị từ chối";
            String bodyHtml = Format.getAdmissionRejectedBody(form.getStudent().getName(), reason);
            mailService.sendMail(
                    form.getParent().getAccount().getEmail(),
                    subject,
                    heading,
                    bodyHtml
            );
            log.info("Successfully sent rejection email for Form {}", form.getId());
        } catch (Exception e) {
            log.error("Error rejecting Form {}: {}", form.getId(), e.getMessage(), e);
            throw e; // Re-throw to trigger transaction rollback
        }
    }

    private static int calculateAge(LocalDate dob) {
        return (int) ChronoUnit.YEARS.between(dob, LocalDate.now());
    }

    // This method assumes Grade enum has a getAge() method returning the expected age for that grade.
    private static boolean isAgeValidForGrade(LocalDate dob, Grade grade) {
        int age = calculateAge(dob);
        // Example: Grade.KINDERGARTEN_3_YEARS.getAge() returns 3
        return age == grade.getAge(); // Simplified based on assumption
    }

    private boolean canAcceptMoreStudents(TermItem termItem) {
        return termItem.getCurrentRegisteredStudents() < termItem.getMaxNumberRegistration();
    }
}