package com.swd392.group1.pes.validations.AccountValidation;

import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.models.Account;
import com.swd392.group1.pes.repositories.AccountRepo;
import com.swd392.group1.pes.requests.ProcessAccountRequest;

public class ProcessAccountValidation {
    public static String processAccountValidate(ProcessAccountRequest request, AccountRepo accountRepo) {

        Account acc = accountRepo.findByEmailAndStatus(request.getEmail(), Status.ACCOUNT_ACTIVE.getValue()).orElse(null);

        if (acc == null) {
            return "Account not found";
        }

        // Check if the action is either "ban" or "unban"
        if (!request.getAction().equalsIgnoreCase("ban") && !request.getAction().equalsIgnoreCase("unban")) {
            return "Invalid action";
        }

        return "";
    }
}
