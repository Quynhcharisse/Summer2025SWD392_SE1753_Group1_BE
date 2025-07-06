package com.swd392.group1.pes.controllers;

import com.swd392.group1.pes.dto.requests.CreateTeacherRequest;
import com.swd392.group1.pes.dto.requests.ProcessAccountRequest;
import com.swd392.group1.pes.dto.requests.UpdateTeacherRequest;
import com.swd392.group1.pes.dto.response.ResponseObject;
import com.swd392.group1.pes.services.HRService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/hr")
public class HRController {

    private final HRService hrService;

    @PutMapping("/ban")
    @PreAuthorize("hasRole('hr')")
    public ResponseEntity<ResponseObject> banAccount(@RequestBody ProcessAccountRequest request) {
        return hrService.processAccount(request, "ban");
    }

    @PutMapping("/unban")
    @PreAuthorize("hasRole('hr')")
    public ResponseEntity<ResponseObject> unbanAccount(@RequestBody ProcessAccountRequest request) {
        return hrService.processAccount(request, "unban");
    }

    @PostMapping("/teacher")
    @PreAuthorize("hasRole('hr')")
    public ResponseEntity<ResponseObject> createTeacher(@RequestBody CreateTeacherRequest request) {
        return hrService.createTeacherAcc(request);
    }

    @PutMapping("/teacher")
    @PreAuthorize("hasRole('hr')")
    public ResponseEntity<ResponseObject> updateTeacher(@RequestParam String id, @RequestBody UpdateTeacherRequest request) {
        return hrService.updateTeacherAcc(id, request);
    }

    @PostMapping("/teacher/remove")
    @PreAuthorize("hasRole('hr')")
    public ResponseEntity<ResponseObject> removeTeacher(@RequestParam String id) {
        return hrService.removeTeacherAcc(id);
    }

    @GetMapping("/teacher")
    @PreAuthorize("hasAnyRole('hr', 'education')")
    public ResponseEntity<ResponseObject> viewTeacherList() {
        return hrService.viewTeacherList();
    }

    @GetMapping("/teacher/export")
    @PreAuthorize("hasRole('hr')")
    public ResponseEntity<Resource> exportTeacherListToExcel() { return hrService.exportTeacherListToExcel(); }

    @GetMapping("/parent")
    @PreAuthorize("hasRole('hr')")
    public ResponseEntity<ResponseObject> viewParentList() {
        return hrService.viewParentList();
    }

    @GetMapping("/parent/export")
    @PreAuthorize("hasRole('hr')")
    public ResponseEntity<Resource> exportParentListToExcel() { return hrService.exportParentListToExcel(); }
}
