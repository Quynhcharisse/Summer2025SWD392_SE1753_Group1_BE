package com.swd392.group1.pes.utils;

import com.swd392.group1.pes.email.Format;
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


    //@Scheduled(cron = "0 */30 * * * *")
    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void rejectExpiredPaymentForms() {
        log.info("Running scheduled task: Checking expired payment forms at {}", LocalDateTime.now());

        //Tìm tất cả các form có status APPROVED_WAITING_PAYMENT và đã hết hạn
        List<AdmissionForm> formsToProcess = admissionFormRepo.findByStatusAndPaymentExpiryDate(
                Status.APPROVED,
                LocalDateTime.now()
        );

        for (AdmissionForm form : formsToProcess) {
            //check giao dịch thành công nào cho form này không? ==> nhưng trạng thái form chưa được cập nhật (ví dụ do lỗi IPN, mạng...).
            boolean hasSuccessfulPayment = transactionRepo
                    .findByAdmissionFormAndStatus(form, Status.TRANSACTION_SUCCESSFUL)
                    .isPresent();

            if (!hasSuccessfulPayment) {
                //thanh toán failed, đặt form về trạng thái REJECTED
                form.setStatus(Status.REJECTED);
                form.setCancelReason("Payment window expired. No payment received."); // Ghi lý do từ chối
                admissionFormRepo.save(form);

                log.error("Admission Form ID: {} has been REJECTED due to payment timeout.", form.getId());

                //gửi email thông báo từ chối cho phụ huynh
                if (form.getParent() != null && form.getParent().getAccount() != null && form.getStudent() != null) {
                    String parentEmail = form.getParent().getAccount().getEmail();
                    String studentName = form.getStudent().getName();
                    String subject = "[PES] Admission Rejected - Payment Timeout";
                    String heading = "❌ Admission Rejected";
                    String bodyHtml = Format.getAdmissionRejectedBody(studentName, "Thời gian thanh toán cho đơn đăng ký của bạn đã hết hạn. Vui lòng liên hệ nhà trường để biết thêm chi tiết.");
                    mailService.sendMail(parentEmail, subject, heading, bodyHtml);
                }

            } else {
                //nếu thấy thanh toán success, form vẫn ở APPROVED_WAITING_PAYMENT --> cập nhật lại cho đúng trạng thái APPROVED_PAID
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
