package com.swd392.group1.pes.services;

import com.swd392.group1.pes.requests.RestPasswordRequest;
import com.swd392.group1.pes.requests.UpdateProfileRequest;
import com.swd392.group1.pes.response.ResponseObject;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

public interface AccountService {
    ResponseEntity<ResponseObject> resetPassword(RestPasswordRequest request);

    ResponseEntity<ResponseObject> viewProfile(HttpServletRequest request);

    ResponseEntity<ResponseObject> updateProfile(UpdateProfileRequest request, HttpServletRequest httpRequest);
}
