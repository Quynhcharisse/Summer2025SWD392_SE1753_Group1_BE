package com.swd392.group1.pes.enums;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

public enum Role {
    PARENT,
    TEACHER,
    HR,
    ADMISSION_MANAGER,
    EDUCATION_MANAGER;

    public List<SimpleGrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.name().toLowerCase()));
    }
}
