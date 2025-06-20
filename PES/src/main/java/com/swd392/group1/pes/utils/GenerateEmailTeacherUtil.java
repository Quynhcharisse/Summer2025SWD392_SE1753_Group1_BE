package com.swd392.group1.pes.utils;

import com.swd392.group1.pes.enums.Role;
import com.swd392.group1.pes.repositories.AccountRepo;

public class GenerateEmailTeacherUtil {

    private static final String BASE_EMAIL = "lasystem.teacher@gmail.com";

    public static String generateTeacherEmail(AccountRepo accountRepo) {
        long count = accountRepo.countByRole(Role.TEACHER);
        long next = count + 1;

        String alias = String.format("gv%03d", next); // e.g., gv001
        return BASE_EMAIL.replace("@", "+" + alias + "@");
    }
}
