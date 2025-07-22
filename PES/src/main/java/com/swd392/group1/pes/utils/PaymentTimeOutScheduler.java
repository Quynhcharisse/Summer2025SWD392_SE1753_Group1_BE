package com.swd392.group1.pes.utils;

import com.swd392.group1.pes.utils.email.Format;
import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.models.AdmissionForm;
import com.swd392.group1.pes.repositories.AdmissionFormRepo;
import com.swd392.group1.pes.repositories.TransactionRepo;
import com.swd392.group1.pes.services.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentTimeOutScheduler {

    private final AdmissionFormRepo admissionFormRepo;
    private final TransactionRepo transactionRepo;
    private final MailService mailService;

    @Scheduled(fixedDelay = 5000) //5 giây
    @Transactional
    public void rejectExpiredPaymentForms() {
        log.info("Running scheduled task: Checking expired payment forms at {}", LocalDateTime.now());
        List<AdmissionForm> formsToProcess = admissionFormRepo.findByStatusAndPaymentExpiryDateLessThanEqual(
                Status.WAITING_PAYMENT,
                LocalDateTime.now()
        );

        for (AdmissionForm form : formsToProcess) {
            boolean hasSuccessfulPayment = transactionRepo
                    .findByAdmissionFormAndStatus(form, Status.TRANSACTION_SUCCESSFUL)
                    .isPresent();

            if (!hasSuccessfulPayment) {
                form.setStatus(Status.REJECTED);
                form.setCancelReason("Payment window expired. No payment received.");
                admissionFormRepo.save(form);

                log.error("Admission Form ID: {} has been REJECTED due to payment timeout.", form.getId());

                if (form.getParent() != null && form.getParent().getAccount() != null && form.getStudent() != null) {
                    String parentEmail = form.getParent().getAccount().getEmail();
                    String studentName = form.getStudent().getName();
                    String subject = "[PES] Admission Rejected - Payment Timeout";
                    String heading = "Admission Rejected";
                    String bodyHtml = Format.getAdmissionRejectedBody(studentName, "The payment deadline for your application has expired. Please contact the school for further details.");
                    mailService.sendMail(parentEmail, subject, heading, bodyHtml);
                }

            } else {
                if (form.getStatus() != Status.APPROVED_PAID) {
                    form.setStatus(Status.APPROVED_PAID);
                    admissionFormRepo.save(form);
                    log.info("Admission Form ID: {} updated to APPROVED_PAID (successful payment found by scheduler).", form.getId());
                }
            }
        }
        log.info("Finished checking expired payment forms.");
    }
}
