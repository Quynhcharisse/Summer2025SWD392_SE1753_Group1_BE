package com.swd392.group1.pes.services.implementors;

import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.models.Account;
import com.swd392.group1.pes.repositories.AccountRepo;
import com.swd392.group1.pes.services.JWTService;
import com.swd392.group1.pes.utils.CookieUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class JWTServiceImpl implements JWTService {

    private final AccountRepo accountRepo;

    @Value("${security-secret-key}")
    private String secretKey;

    @Value("${security-access-expiration}")
    private long accessExpiration;

    @Value("${security-refresh-expiration}")
    private long refreshExpiration;

    @Override
    public String extractEmailFromJWT(String jwt) {
        return getClaim(jwt, Claims::getSubject);
    }

    @Override
    public Account extractAccountFromCookie(HttpServletRequest request) {
        Cookie cookie = CookieUtil.getCookie(request, "refresh");
        if (cookie == null) {
            return null;
        }

        String refreshToken  = cookie.getValue();
        String email = extractEmailFromJWT(refreshToken);

        return accountRepo.findByEmailAndStatus(email, Status.ACCOUNT_ACTIVE.getValue()).orElse(null);
    }

    private <T> T getClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaimsFromToken(token);
        if (claims == null) {
            return null;
        }
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaimsFromToken(String token) {
        if (token.split("\\.").length != 3) {
            return null;
        }
        try {
            return Jwts
                    .parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            return null;
        }
    }

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secretKey));
    }
    @Override
    public String generateAccessToken(UserDetails user) {
        Account account = (Account) user;
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", account.getRole());
        return generateToken(claims, user, accessExpiration);
    }



    @Override
    public String generateRefreshToken(UserDetails user) {
        Account account = (Account) user;
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", account.getRole());
        return generateToken(claims, user, refreshExpiration);
    }

    private String generateToken(Map<String, Object> extractClaims, UserDetails user, long expiredTime) {
        return Jwts.builder()
                .setClaims(extractClaims)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiredTime))
                .signWith(getSigningKey())
                .compact();
    }

    @Override
    public boolean checkIfNotExpired(String jwt) {
        Date expiration = getClaim(jwt, Claims::getExpiration);
        return expiration != null && !Objects.requireNonNull(expiration).before(new Date());
    }

    @Override
    public String generateResetToken(UserDetails user) {
        Map<String, Object> claims = new HashMap<>();
        long expiredTime = 5 * 60 * 1000; // 5 phút
        return generateTokenCode(claims, user, expiredTime);
    }

    private String generateTokenCode(Map<String, Object> extractClaims, UserDetails user, long expiredTime) {
        return Jwts.builder()
                .setClaims(extractClaims)
                .setSubject(user.getUsername())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiredTime))
                .signWith(getSigningKey())
                .compact();
    }
}
