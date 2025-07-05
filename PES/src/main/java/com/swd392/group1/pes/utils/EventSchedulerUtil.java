package com.swd392.group1.pes.utils;

import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.models.Event;
import com.swd392.group1.pes.repositories.EventRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
public class EventSchedulerUtil {

    private final EventRepo eventRepo;

    @Scheduled(cron = "*/30 * * * * *")
    @Transactional
    public void updateEventStatusCloseRegistration() {
        LocalDateTime now = LocalDateTime.now();
        List<Event> toClose = eventRepo
                .findByStatusAndRegistrationDeadlineLessThanEqual(Status.EVENT_REGISTRATION_ACTIVE, now);
        toClose.forEach(e -> e.setStatus(Status.EVENT_REGISTRATION_CLOSED));
        if (!toClose.isEmpty()) {
            eventRepo.saveAll(toClose);
        }
    }
}

