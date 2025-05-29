package com.swd392.group1.pes.services.implementors;

import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.models.Account;
import com.swd392.group1.pes.repositories.AccountRepo;
import com.swd392.group1.pes.requests.ProcessAccountRequest;
import com.swd392.group1.pes.response.ResponseObject;
import com.swd392.group1.pes.services.HRService;
import com.swd392.group1.pes.validations.AccountValidation.ProcessAccountValidation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HRServiceImpl implements HRService {

    private final AccountRepo accountRepo;

    @Override
    public ResponseEntity<ResponseObject> processAccount(ProcessAccountRequest request, String action) {

        String error = ProcessAccountValidation.processAccountValidate(request, action, accountRepo);

        if (!error.isEmpty()) {
            return ResponseEntity.ok().body(
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
            return ResponseEntity.ok().body(
                    ResponseObject.builder()
                            .message("Invalid action")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        if (account == null) {
            return ResponseEntity.ok().body(
                    ResponseObject.builder()
                            .message("Account not found or in invalid state")
                            .success(false)
                            .data(null)
                            .build()
            );
        }

        account.setStatus(newStatus);
        accountRepo.save(account);

        String message = action.equalsIgnoreCase("ban") ?
                "Account banned successfully" : "Account unbanned successfully";

        return ResponseEntity.ok().body(
                ResponseObject.builder()
                        .message(message)
                        .success(true)
                        .data(null)
                        .build()
        );
    }
}
