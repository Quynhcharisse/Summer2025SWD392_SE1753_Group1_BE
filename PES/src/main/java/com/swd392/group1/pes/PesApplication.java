package com.swd392.group1.pes;

import com.swd392.group1.pes.enums.Grade;
import com.swd392.group1.pes.enums.Role;
import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.models.Account;
import com.swd392.group1.pes.models.AdmissionFee;
import com.swd392.group1.pes.repositories.AccountRepo;
import com.swd392.group1.pes.repositories.AdmissionFeeRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;

@SpringBootApplication
@RequiredArgsConstructor
public class PesApplication {

    private final AdmissionFeeRepo admissionFeeRepo;

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

            //Set phí mặc định
            seedAdmissionFeeIfMissing(Grade.SEED, 800_000, 80_000, 100_000, 100_000, 100_000);
            seedAdmissionFeeIfMissing(Grade.BUD, 1_000_000, 100_000, 110_000, 110_000, 110_000);
            seedAdmissionFeeIfMissing(Grade.LEAF, 1_200_000, 120_000, 120_000, 120_000, 120_000);

        };
    }

    private void seedAdmissionFeeIfMissing(Grade grade,
                                           double reservationFee, double serviceFee,
                                           double uniformFee, double materialFee, double facilityFee) {
        if (admissionFeeRepo.findByAdmissionTermIsNullAndGrade(grade).isEmpty()) {
            admissionFeeRepo.save(AdmissionFee.builder()
                    .grade(grade)
                    .reservationFee(reservationFee)
                    .serviceFee(serviceFee)
                    .uniformFee(uniformFee)
                    .learningMaterialFee(materialFee)
                    .facilityFee(facilityFee)
                    .build());
        }
    }
}