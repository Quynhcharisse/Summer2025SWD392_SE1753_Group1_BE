package com.swd392.group1.pes.requests;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;


import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateEventRequest {
    String name;
    LocalDateTime startTime;
    LocalDateTime endTime;
    String location;
    String description;
    LocalDateTime registrationDeadline;
    String attachmentImg;
    String hostName;
    List<String> emails;
}
