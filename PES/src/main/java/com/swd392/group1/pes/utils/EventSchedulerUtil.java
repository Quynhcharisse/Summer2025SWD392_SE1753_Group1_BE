package com.swd392.group1.pes.utils;

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

        System.out.println("Scheduler at " + now + ", looking for events between "
                + reminderTime + " and " + windowEnd);

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
                try {
                    mailService.sendMail(
                            parentEmail,
                            "[PES] REMINDER EVENT",
                            String.format("Reminder Event \"%s\" starts in 15 minutes", event.getName()),
                            buildReminderBody(ep.getStudent().getParent().getAccount().getName(),
                                    childName,
                                    event.getName(),
                                    event.getStartTime())
                    );
                } catch (Exception ex) {
                    // Log lỗi nhưng không dừng scheduler
                    System.err.println("Failed to send reminder for event "
                            + event.getId() + " to " + parentEmail + ": " + ex.getMessage());
                }
            }
        }
    }

    private String buildReminderBody(String parentName,
                                     String childName,
                                     String eventName,
                                     LocalDateTime startTime) {
        return String.format(
                "Dear %s,\n\n" +
                        "This is a friendly reminder that your child %s is registered for \"%s\" which starts at %s.\n" +
                        "Please be ready 15 minutes ahead of time.\n\n" +
                        "Best regards,\nSunShine Preschool",
                parentName,
                childName,
                eventName,
                startTime
        );
    }

}

