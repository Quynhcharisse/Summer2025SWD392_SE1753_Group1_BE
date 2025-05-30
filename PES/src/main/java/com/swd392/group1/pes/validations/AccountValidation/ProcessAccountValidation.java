package com.swd392.group1.pes.validations.AccountValidation;

import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.models.Account;
import com.swd392.group1.pes.repositories.AccountRepo;
import com.swd392.group1.pes.requests.ProcessAccountRequest;

public class ProcessAccountValidation {
    public static String processAccountValidate(ProcessAccountRequest request, String action, AccountRepo accountRepo) {

        Account acc = null;

        if (action.equalsIgnoreCase("ban")) {
            // Chỉ ban tài khoản đang hoạt động
            acc = accountRepo.findByEmailAndStatus(request.getEmail(), Status.ACCOUNT_ACTIVE.getValue()).orElse(null);
        } else if (action.equalsIgnoreCase("unban")) {
            // Chỉ unban tài khoản bị ban
            acc = accountRepo.findByEmailAndStatus(request.getEmail(), Status.ACCOUNT_BAN.getValue()).orElse(null);
        } else {
            return "Invalid action";
        }

        if (acc == null) {
            return "Account not found or in invalid state for action: " + action;
        }
        return "";
    }
}
