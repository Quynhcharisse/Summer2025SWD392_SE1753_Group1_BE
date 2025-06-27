package com.swd392.group1.pes.utils;

import com.swd392.group1.pes.email.Format;
import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.models.Event;
import com.swd392.group1.pes.models.EventParticipate;
import com.swd392.group1.pes.repositories.EventParticipateRepo;
import com.swd392.group1.pes.repositories.EventRepo;
import com.swd392.group1.pes.services.MailService;
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
    private final EventParticipateRepo eventParticipateRepo;
    private final MailService mailService;

    @Scheduled(cron = "0 * * * * *")
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

    @Scheduled(cron = "0 * * * * *")
    @Transactional(readOnly = true)
    public void sendEventReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reminderTime = now.plusMinutes(15).withSecond(0).withNano(0);
        LocalDateTime windowEnd   = reminderTime.plusSeconds(59);

        List<Event> upcomingEvents = eventRepo
                .findByStartTimeBetween(reminderTime, windowEnd);

        for (Event event : upcomingEvents) {
            List<EventParticipate> studentParticipateList = eventParticipateRepo.findAllByEventId(event.getId());
            for (EventParticipate ep : studentParticipateList) {
                String parentEmail = ep.getStudent()
                        .getParent()
                        .getAccount()
                        .getEmail();
                String childName = ep.getStudent().getName();
                    mailService.sendMail(
                            parentEmail,
                            "[PES] Reminder Event",
                            String.format("Reminder Event \"%s\" starts in 15 minutes", event.getName()),
                            Format.getReminderBody(ep.getStudent().getParent().getAccount().getName(),
                                    childName,
                                    event.getName(),
                                    event.getStartTime())
                    );
            }
        }
    }

}

