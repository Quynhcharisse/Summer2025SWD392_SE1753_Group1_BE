package com.swd392.group1.pes.validations.ParentValidation;

import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.models.AdmissionForm;
import com.swd392.group1.pes.models.Transaction;
import com.swd392.group1.pes.repositories.AdmissionFormRepo;
import com.swd392.group1.pes.repositories.TransactionRepo;
import com.swd392.group1.pes.requests.InitiateVNPayPaymentRequest;

public class PaymentValidation {
    public static String validate(InitiateVNPayPaymentRequest request, AdmissionFormRepo admissionFormRepo, TransactionRepo transactionRepo) {

        //Kiểm tra formId có được cung cấp không
        AdmissionForm form = admissionFormRepo.findById(request.getFormId()).orElse(null);
        if (form == null) {
            return "Missing formId in payment request";
        }

        Transaction existedTransaction = transactionRepo.findByAdmissionFormIdAndStatus(form.getId(), Status.WAITING_PAYMENT);
        if (existedTransaction != null) {
            return "Transaction already exists for this form";
        }

        return "";
    }
}
