package com.swd392.group1.pes.utils;

import com.swd392.group1.pes.repositories.AccountRepo;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GenerateEmailTeacherUtil {

    public static String generateTeacherEmail(String fullName, AccountRepo accountRepo) {
        if (fullName == null || fullName.trim().isEmpty()) {
            throw new IllegalArgumentException("Full name cannot be null or empty");
        }

        String[] parts = fullName.trim().split("\\s+");
        String firstName = parts[0].toLowerCase();
        String lastName = parts.length > 1 ? parts[parts.length - 1].toLowerCase() : "";

        String uniqueEmail = firstName + (lastName.isEmpty() ? "" : "." + lastName) + "@sunshine.edu.vn";
        int counter = 1;
        while (accountRepo.findByEmail(uniqueEmail).isPresent()) {
            uniqueEmail = firstName + "." + lastName + counter + "@sunshine.edu.vn";
            counter++;
        }
        return uniqueEmail;
    }
}
