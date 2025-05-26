package com.swd392.group1.pes.utils;

import com.swd392.group1.pes.response.ResponseObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public class ResponseUtil {
    public static ResponseEntity<ResponseObject> build(
            HttpStatus status,
            String message,
            boolean isSuccess,
            Object data
    ) {
        return ResponseEntity
                .status(status)
                .body(
                        ResponseObject.builder()
                                .message(message)
                                .success(isSuccess)
                                .data(data)
                                .build()
                );
    }
}
