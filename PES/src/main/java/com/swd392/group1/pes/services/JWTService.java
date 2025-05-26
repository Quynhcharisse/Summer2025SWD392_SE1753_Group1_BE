package com.swd392.group1.pes.services;

import com.swd392.group1.pes.models.Account;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.userdetails.UserDetails;

public interface JWTService {

    String extractEmailFromJWT(String jwt);

    Account extractAccountFromCookie(HttpServletRequest request);

    String generateAccessToken(UserDetails user);

    String generateRefreshToken(UserDetails user);

    boolean checkIfNotExpired(String jwt);

}
