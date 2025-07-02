package com.swd392.group1.pes;

import com.swd392.group1.pes.enums.Grade;
import com.swd392.group1.pes.enums.Role;
import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.models.Account;
import com.swd392.group1.pes.models.AdmissionTerm;
import com.swd392.group1.pes.models.Parent;
import com.swd392.group1.pes.repositories.AccountRepo;
import com.swd392.group1.pes.repositories.AdmissionFormRepo;
import com.swd392.group1.pes.repositories.AdmissionTermRepo;
import com.swd392.group1.pes.repositories.ParentRepo;
import com.swd392.group1.pes.utils.RandomPasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@SpringBootApplication
@RequiredArgsConstructor
@EnableScheduling
public class PesApplication {

    private final AdmissionTermRepo admissionTermRepo;
    private final ParentRepo parentRepo;
    private final AccountRepo accountRepo;
    private final StudentRepo studentRepo;
    private final AdmissionFormRepo admissionFormRepo;
    private final TermItemRepo termItemRepo;

    private final Random random = new Random();

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
                    .password(RandomPasswordUtil.generateRandomString(8)) // Hoặc mật khẩu cố định đã hash
                    .name("Teacher")
                    .phone("0886122578")
                    .gender("male")
                    .identityNumber("060204004188")
                    .status(Status.ACCOUNT_ACTIVE.getValue())
                    .role(Role.TEACHER)
                    .createdAt(LocalDateTime.now())
                    .firstLogin(false) // Giáo viên không cần đổi mật khẩu lần đầu
                    .build();
            accountRepo.save(teacherAccount);
        }

        // Tạo sẵn HR Manager
        if (!accountRepo.existsByEmail("hrmanager@gmail.com")) {
            Account hrAccount = Account.builder()
                    .email("hrmanager@gmail.com")
                    .password("123456") // Nên hash mật khẩu này
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
                    .password("123456") // Nên hash mật khẩu này
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
                    .password("123456") // Nên hash mật khẩu này
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
                    .password("123456")
                    .name("Parent")
                    .gender("male")
                    .identityNumber("070404000033")
                    .phone("0705646041")
                    .address("66/11 Le Trong Tan, Binh Hung Hoa")
                    .firstLogin(true)
                    .role(Role.PARENT)
                    .status(Status.ACCOUNT_ACTIVE.getValue())
                    .createdAt(LocalDate.now())
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