package com.swd392.group1.pes;

import com.swd392.group1.pes.enums.Grade;
import com.swd392.group1.pes.enums.Role;
import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.models.Account;
import com.swd392.group1.pes.models.AdmissionForm;
import com.swd392.group1.pes.models.AdmissionTerm;
import com.swd392.group1.pes.models.Parent;
import com.swd392.group1.pes.models.Student;
import com.swd392.group1.pes.models.TermItem;
import com.swd392.group1.pes.repositories.AccountRepo;
import com.swd392.group1.pes.repositories.AdmissionFormRepo;
import com.swd392.group1.pes.repositories.AdmissionTermRepo;
import com.swd392.group1.pes.repositories.ParentRepo;
import com.swd392.group1.pes.repositories.StudentRepo;
import com.swd392.group1.pes.repositories.TermItemRepo;
import com.swd392.group1.pes.utils.RandomPasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

            // 1. Tạo AdmissionTerm (LocalDateTime)
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime end = LocalDateTime.of(2025, 7, 4, 23, 59);
            AdmissionTerm term = AdmissionTerm.builder()
                    .name("Term 2025")
                    .startDate(now)
                    .endDate(end)
                    .status(Status.ACTIVE_TERM)
                    .build();
            admissionTermRepo.save(term);

            // 2. Tạo TermItem cho SEED, BUD, LEAF
            List<TermItem> termItems = new ArrayList<>();
            for (Grade g : List.of(Grade.SEED, Grade.BUD, Grade.LEAF)) {
                TermItem ti = TermItem.builder()
                        .admissionTerm(term)
                        .grade(g)
                        .maxNumberRegistration(50)
                        .studentsPerClass(20)
                        .expectedClasses((int) Math.ceil(50.0 / 20))
                        .currentRegisteredStudents(0)
                        .status(Status.ACTIVE_TERM_ITEM)
                        .build();
                termItemRepo.save(ti);
                termItems.add(ti);
            }

            LocalDate baseDate = LocalDate.now();
            // 3. Tạo 15 phụ huynh, mỗi người có 2-3 con, form ở trạng thái PENDING_APPROVAL
            for (int i = 1; i <= 15; i++) {
                String email = String.format("parent%02d@demo.com", i);
                if (accountRepo.existsByEmail(email)) continue;

                // Tạo Account cho phụ huynh
                Account acc = Account.builder()
                        .email(email)
                        .password(RandomPasswordUtil.generateRandomString(8))
                        .name("Parent " + i)
                        .role(Role.PARENT)
                        .status(Status.ACCOUNT_ACTIVE.getValue())
                        .createdAt(LocalDateTime.now())
                        .build();
                accountRepo.save(acc);

                // Tạo thông tin Parent
                Parent parent = Parent.builder()
                        .account(acc)
                        .relationshipToChild("self")
                        .job("N/A")
                        .build();
                parentRepo.save(parent);

                // Tạo Con và Admission Form tương ứng
                int childrenCount = 2 + random.nextInt(2); // Random 2 hoặc 3 con
                for (int j = 1; j <= childrenCount; j++) {
                    TermItem chosenTermItem = termItems.get(random.nextInt(termItems.size()));
                    int age = chosenTermItem.getGrade().getAge();
                    LocalDate dob = baseDate.minusYears(age).minusMonths(random.nextInt(12));

                    Student child = Student.builder()
                            .name(String.format("Child_%d_%d", i, j))
                            .dateOfBirth(dob)
                            .build();
                    studentRepo.save(child);

                    AdmissionForm form = AdmissionForm.builder()
                            .parent(parent)
                            .student(child)
                            .termItem(chosenTermItem)
                            .status(Status.PENDING_APPROVAL)
                            .submittedDate(LocalDateTime.now())
                            .build();
                    admissionFormRepo.save(form);
                }
            }
        };
    }

    /**
     * Helper method to create default system accounts if they don't exist.
     */
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
    }
}
