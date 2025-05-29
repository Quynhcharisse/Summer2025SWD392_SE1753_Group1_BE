package com.swd392.group1.pes.services.implementors;


import com.swd392.group1.pes.enums.Grade;
import com.swd392.group1.pes.models.Syllabus;
import com.swd392.group1.pes.repositories.SyllabusRepo;
import com.swd392.group1.pes.requests.CreateSyllabusRequest;
import com.swd392.group1.pes.response.ResponseObject;
import com.swd392.group1.pes.services.EducationManagerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EducationManagerServiceImpl implements EducationManagerService {

    private final SyllabusRepo syllabusRepo;


    @Override
    public ResponseEntity<ResponseObject> createSyllabus(CreateSyllabusRequest createSyllabusRequest) {

        syllabusRepo.save(
                Syllabus.builder()
                        .subject(createSyllabusRequest.getSubject())
                        .description(createSyllabusRequest.getDescription())
//                        .grade(Grade.valueOf(createSyllabusRequest.getGrade()))
                        .build()
        );

        return ResponseEntity.ok().body(
                ResponseObject.builder()
                        .message("Created Syllabus Successfully")
                        .success(true)
                        .data(null)
                        .build()
        );
    }
}
