package com.swd392.group1.pes;

import com.swd392.group1.pes.enums.Role;
import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.models.Account;
import com.swd392.group1.pes.repositories.AccountRepo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;

@SpringBootApplication
public class PesApplication {

    public static void main(String[] args) {
        SpringApplication.run(PesApplication.class, args);
    }

    @Bean
    public CommandLineRunner initData(AccountRepo accountRepo) {
        return args -> {

            // Tạo sẵn HR Manager
            if (!accountRepo.existsByEmail("hrmanager@gmail.com")) {
                Account hrAccount = Account.builder()
                        .email("hrmanager@gmail.com")
                        .password("123456").name("HR Manager")
                        .gender("female")
                        .identityNumber("070425890001")
                        .phone("09123450009")
                        .name("HR Manager")
                        .gender("female")
                        .identityNumber("070425890001")
                        .phone("09123450009")
                        .firstLogin(true)
                        .role(Role.HR)
                        .status(Status.ACCOUNT_ACTIVE.getValue())
                        .createdAt(LocalDate.now())
                        .build();
                accountRepo.save(hrAccount);
            }
            // Tạo sẵn Educational Manager
            if (!accountRepo.existsByEmail("educationalmanager@gmail.com")) {
                Account educationalManagerAccount = Account.builder()
                        .email("educationalmanager@gmail.com")
                        .password("123456")
                        .name("Educational Manager")
                        .gender("male")
                        .identityNumber("070425890003")
                        .phone("09123456700")
                        .name("Educational Manager")
                        .gender("male")
                        .identityNumber("070425890003")
                        .phone("09123456700")
                        .firstLogin(true)
                        .role(Role.EDUCATION)
                        .status(Status.ACCOUNT_ACTIVE.getValue())
                        .createdAt(LocalDate.now())
                        .build();
                accountRepo.save(educationalManagerAccount);
            }
            // Tạo sẵn admission Manager
            if (!accountRepo.existsByEmail("admissionmanager@gmail.com")) {
                Account admissionAccount = Account.builder()
                        .email("admissionmanager@gmail.com")
                        .password("123456")
                        .name("Admission Manager")
                        .gender("female")
                        .identityNumber("070425890002")
                        .phone("09453456789")
                        .firstLogin(true)
                        .role(Role.ADMISSION)
                        .status(Status.ACCOUNT_ACTIVE.getValue())
                        .createdAt(LocalDate.now())
                        .build();
                accountRepo.save(admissionAccount);
            }

            //Tạo sẵn parent
            if (!accountRepo.existsByEmail("parent1@gmail.com")) {
                Account parent = Account.builder()
                        .email("parent1@gmail.com")
                        .password("123456")
                        .name("Parent")
                        .gender("male")
                        .identityNumber("070404000033")
                        .phone("0705646041")
                        .firstLogin(true)
                        .role(Role.PARENT)
                        .status(Status.ACCOUNT_ACTIVE.getValue())
                        .createdAt(LocalDate.now())
                        .build();
                accountRepo.save(parent);
            }
        };
    }
}
