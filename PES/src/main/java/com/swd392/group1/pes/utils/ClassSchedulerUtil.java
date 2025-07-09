package com.swd392.group1.pes.utils;

import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.models.Classes;
import com.swd392.group1.pes.repositories.ClassRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ClassSchedulerUtil {

    private final ClassRepo classRepo;

    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void autoUpdateClassStatus() {
        LocalDate today = LocalDate.now();

        List<Classes> toInProgress = classRepo.findByStatusAndStartDateLessThanEqual(Status.CLASS_ACTIVE.getValue(), today);
        for (Classes cls : toInProgress) {
            cls.setStatus(Status.CLASS_IN_PROGRESS.getValue());
        }

        List<Classes> toClose = classRepo.findByStatusAndEndDateLessThan(Status.CLASS_IN_PROGRESS.getValue(), today);
        for (Classes cls : toClose) {
            cls.setStatus(Status.CLASS_CLOSED.getValue());
        }
        classRepo.saveAll(toInProgress);
        classRepo.saveAll(toClose);
    }

}
