package com.swd392.group1.pes.services.implementors;


import com.swd392.group1.pes.models.Syllabus;
import com.swd392.group1.pes.repositories.SyllabusRepo;
import com.swd392.group1.pes.requests.CreateSyllabusRequest;
import com.swd392.group1.pes.response.ResponseObject;
import com.swd392.group1.pes.services.EducationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EducationServiceImpl implements EducationService {

    private final SyllabusRepo syllabusRepo;

    @Override
    public ResponseEntity<ResponseObject> createSyllabus(CreateSyllabusRequest request) {

        syllabusRepo.save(
                Syllabus.builder()
                        .subject(request.getSubject())
                        .description(request.getDescription())
//                        .grade(Grade.valueOf(request.getGrade()))
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
