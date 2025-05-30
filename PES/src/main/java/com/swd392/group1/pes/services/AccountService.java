package com.swd392.group1.pes.services;

import com.swd392.group1.pes.requests.RenewPasswordRequest;
import com.swd392.group1.pes.requests.UpdateProfileRequest;
import com.swd392.group1.pes.response.ResponseObject;
import org.springframework.http.ResponseEntity;

public interface AccountService {
    ResponseEntity<ResponseObject> renewPassword(RenewPasswordRequest request);

    ResponseEntity<ResponseObject> renewFirstTimePassword(RenewPasswordRequest request);

    ResponseEntity<ResponseObject> viewProfile();

    ResponseEntity<ResponseObject> updateProfile(UpdateProfileRequest request);
}
