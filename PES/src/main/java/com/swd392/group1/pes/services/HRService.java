package com.swd392.group1.pes.services;

import com.swd392.group1.pes.requests.CreateTeacherRequest;
import com.swd392.group1.pes.requests.ProcessAccountRequest;
import com.swd392.group1.pes.requests.UpdateTeacherRequest;
import com.swd392.group1.pes.response.ResponseObject;
import org.springframework.http.ResponseEntity;

public interface HRService {
    ResponseEntity<ResponseObject> processAccount (ProcessAccountRequest request, String action);

    ResponseEntity<ResponseObject> createTeacher (CreateTeacherRequest request);

    ResponseEntity<ResponseObject> viewTeacherList ();

    ResponseEntity<ResponseObject> updateTeacher (UpdateTeacherRequest request);

    ResponseEntity<ResponseObject> viewParentList ();
}
