package com.swd392.group1.pes;

import com.swd392.group1.pes.enums.Grade;
import com.swd392.group1.pes.enums.Role;
import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.models.Account;
import com.swd392.group1.pes.models.AdmissionTerm;
import com.swd392.group1.pes.models.Parent;
import com.swd392.group1.pes.models.Student;
import com.swd392.group1.pes.repositories.AccountRepo;
import com.swd392.group1.pes.repositories.AdmissionTermRepo;
import com.swd392.group1.pes.repositories.ParentRepo;
import com.swd392.group1.pes.repositories.StudentRepo;
import com.swd392.group1.pes.utils.RandomPasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@SpringBootApplication
@RequiredArgsConstructor
public class PesApplication {

    private final AdmissionTermRepo admissionTermRepo;
    private final ParentRepo parentRepo;

    public static void main(String[] args) {
        SpringApplication.run(PesApplication.class, args);
    }

    @Bean
    public CommandLineRunner initData(AccountRepo accountRepo) {
        return args -> {

            if (!accountRepo.existsByEmail("teacher@gmail.com")) {
                Account teacherAccount = accountRepo.save(
                        Account.builder()
                                .email("teacher@gmail.com")
                                .password(RandomPasswordUtil.generateRandomPassword())
                                .name("Teacher")
                                .phone("0886122578")
                                .gender("male")
                                .identityNumber("060204004188")
                                .status(Status.ACCOUNT_ACTIVE.getValue())
                                .role(Role.TEACHER)
                                .createdAt(LocalDate.now())
                                .build()
                );
                accountRepo.save(teacherAccount);
            }
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

            Random random = new Random();
            List<Status> statuses = List.of(Status.INACTIVE_TERM, Status.ACTIVE_TERM, Status.LOCKED_TERM);

            for (int year = 2015; year <= 2024; year++) {
                for (Grade grade : Grade.values()) {
                    // Kiểm tra nếu đã tồn tại Term cùng năm + grade thì bỏ qua
                    if (admissionTermRepo.countByYearAndGrade(year, grade) > 0) continue;

                    int expectedClasses = random.nextInt(3) + 2; // từ 2 đến 4 lớp
                    int studentsPerClass = 20;
                    int maxNumberRegistration = expectedClasses * studentsPerClass;
                    Status status = statuses.get(random.nextInt(statuses.size()));

                    AdmissionTerm term = AdmissionTerm.builder()
                            .name("Admission Term " + grade.getName() + " " + year)
                            .grade(grade)
                            .startDate(LocalDateTime.of(year, 3, 1, 8, 0))   // bắt đầu từ 1/3
                            .endDate(LocalDateTime.of(year, 4, 30, 17, 0))   // kết thúc 30/4
                            .year(year)
                            .studentsPerClass(studentsPerClass)
                            .expectedClasses(expectedClasses)
                            .maxNumberRegistration(maxNumberRegistration)
                            .status(status.getValue())
                            .build();

                    admissionTermRepo.save(term);
                }
            }
        };
    }
}
