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


        // 1. Chuyển lớp từ ACTIVE → IN_PROGRESS nếu ngày bắt đầu <= hôm nay
        List<Classes> toInProgress = classRepo.findByStatusAndStartDateLessThanEqual(
                Status.CLASS_ACTIVE.getValue(), today);
        if (!toInProgress.isEmpty()) {
            toInProgress.forEach(cls -> cls.setStatus(Status.CLASS_IN_PROGRESS.getValue()));
            classRepo.saveAll(toInProgress);
        }

        // 2. Chuyển lớp từ IN_PROGRESS → CLOSED nếu ngày kết thúc < hôm nay
        List<Classes> toClose = classRepo.findByStatusAndEndDateLessThan(
                Status.CLASS_IN_PROGRESS.getValue(), today);
        if (!toClose.isEmpty()) {
            toClose.forEach(cls -> cls.setStatus(Status.CLASS_CLOSED.getValue()));
            classRepo.saveAll(toClose);
        }
    }

}
