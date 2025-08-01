package com.swd392.group1.pes;

import com.swd392.group1.pes.enums.Role;
import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.models.Account;
import com.swd392.group1.pes.models.Parent;
import com.swd392.group1.pes.repositories.AccountRepo;
import com.swd392.group1.pes.repositories.ParentRepo;
import com.swd392.group1.pes.utils.RandomPasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

@SpringBootApplication
@EnableAsync
@RequiredArgsConstructor
@EnableScheduling
public class PesApplication {

    private final AccountRepo accountRepo;

    private final ParentRepo parentRepo;

    private final PasswordEncoder passwordEncoder;

    public static void main(String[] args) {
        SpringApplication.run(PesApplication.class, args);
    }

    @Bean
    public CommandLineRunner initData() {
        return args -> {
            // =================================================================
            // TẠO CÁC TÀI KHOẢN MẶC ĐỊNH CHO HỆ THỐNG
            // =================================================================
            createDefaultAccounts();
        };
    }
    private void createDefaultAccounts() {
        // Tạo sẵn Teacher
        if (!accountRepo.existsByEmail("teacher@gmail.com")) {
            Account teacherAccount = Account.builder()
                    .email("teacher@gmail.com")
                    .password(passwordEncoder.encode(RandomPasswordUtil.generateRandomString(8)))
                    .name("Teacher")
                    .phone("0886122578")
                    .gender("male")
                    .identityNumber("060204004188")
                    .status(Status.ACCOUNT_ACTIVE.getValue())
                    .role(Role.TEACHER)
                    .createdAt(LocalDateTime.now())
                    .firstLogin(false)
                    .build();
            accountRepo.save(teacherAccount);
        }

        // Tạo sẵn HR Manager
        if (!accountRepo.existsByEmail("hrmanager@gmail.com")) {
            Account hrAccount = Account.builder()
                    .email("hrmanager@gmail.com")
                    .password(passwordEncoder.encode("123456"))
                    .name("HR Manager")
                    .gender("female")
                    .identityNumber("070425890001")
                    .phone("09123450009")
                    .firstLogin(true)
                    .role(Role.HR)
                    .status(Status.ACCOUNT_ACTIVE.getValue())
                    .createdAt(LocalDateTime.now())
                    .build();
            accountRepo.save(hrAccount);
        }

        // Tạo sẵn Educational Manager
        if (!accountRepo.existsByEmail("educationalmanager@gmail.com")) {
            Account educationalManagerAccount = Account.builder()
                    .email("educationalmanager@gmail.com")
                    .password(passwordEncoder.encode("123456"))
                    .name("Educational Manager")
                    .gender("male")
                    .identityNumber("070425890003")
                    .phone("09123456700")
                    .firstLogin(true)
                    .role(Role.EDUCATION)
                    .status(Status.ACCOUNT_ACTIVE.getValue())
                    .createdAt(LocalDateTime.now())
                    .build();
            accountRepo.save(educationalManagerAccount);
        }

        // Tạo sẵn Admission Manager
        if (!accountRepo.existsByEmail("admissionmanager@gmail.com")) {
            Account admissionAccount = Account.builder()
                    .email("admissionmanager@gmail.com")
                    .password(passwordEncoder.encode("123456"))
                    .name("Admission Manager")
                    .gender("female")
                    .identityNumber("070425890002")
                    .phone("09453456789")
                    .firstLogin(true)
                    .role(Role.ADMISSION)
                    .status(Status.ACCOUNT_ACTIVE.getValue())
                    .createdAt(LocalDateTime.now())
                    .build();
            accountRepo.save(admissionAccount);
        }

        if (!accountRepo.existsByEmail("parent1@gmail.com")) {
            Account parent = Account.builder()
                    .email("parent1@gmail.com")
                    .password(passwordEncoder.encode("123456"))
                    .name("Parent")
                    .gender("male")
                    .identityNumber("070404000033")
                    .phone("0705646041")
                    .address("66/11 Le Trong Tan, Binh Hung Hoa")
                    .firstLogin(true)
                    .role(Role.PARENT)
                    .status(Status.ACCOUNT_ACTIVE.getValue())
                    .createdAt(LocalDateTime.now())
                    .build();
            accountRepo.save(parent);

            Parent parent1 = Parent.builder()
                    .account(parent)
                    .job("IT")
                    .relationshipToChild("father")
                    .build();

            parentRepo.save(parent1);
        }
    }
}