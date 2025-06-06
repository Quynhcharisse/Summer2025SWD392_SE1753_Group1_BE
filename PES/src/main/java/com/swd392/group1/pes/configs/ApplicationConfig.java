package com.swd392.group1.pes.configs;

import com.swd392.group1.pes.enums.Role;
import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.models.Account;
import com.swd392.group1.pes.repositories.AccountRepo;
import com.swd392.group1.pes.services.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;

@Configuration
@RequiredArgsConstructor
public class ApplicationConfig {

    private final AccountRepo accountRepo;

    @Bean
    public UserDetailsService userDetailsService() {
        return email -> accountRepo.findByEmail(email)
                .orElse(null);
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;

    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder () {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CommandLineRunner initData(AccountRepo accountRepo) {
        return args -> {

            // Tạo sẵn HR Manager
            if (!accountRepo.existsByEmail("hrmanager@gmail.com")) {
                Account hrAccount = Account.builder()
                        .email("hrmanager@gmail.com")
                        .password("123456")
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