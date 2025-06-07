package com.swd392.group1.pes.services;

import com.swd392.group1.pes.requests.RestPasswordRequest;
import com.swd392.group1.pes.requests.UpdateProfileRequest;
import com.swd392.group1.pes.response.ResponseObject;
import org.springframework.http.ResponseEntity;

public interface AccountService {
    ResponseEntity<ResponseObject> resetPassword(RestPasswordRequest request);

    ResponseEntity<ResponseObject> viewProfile(String email);

    ResponseEntity<ResponseObject> updateProfile(UpdateProfileRequest request);
}
