package com.swd392.group1.pes.dto.requests;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;


import java.time.LocalDateTime;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateEventRequest {
    String name;
    LocalDateTime startTime;
    LocalDateTime endTime;
    String location;
    String description;
    LocalDateTime registrationDeadline;
    String attachmentImg;
    String hostName;
}
