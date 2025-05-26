package com.swd392.group1.pes.configs;

import com.swd392.group1.pes.enums.Status;
import com.swd392.group1.pes.models.Account;
import com.swd392.group1.pes.services.JWTService;
import com.swd392.group1.pes.utils.CookieUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JWTAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTH_PATH_PREFIX = "/api/v1/auth";

    private static final String COOKIE_NAME = "access";

    private final JWTService jwtService;

    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        if (requestURI.startsWith(AUTH_PATH_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        Cookie accessToken = CookieUtil.getCookie(request, COOKIE_NAME);
        if(accessToken == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String email = jwtService.extractEmailFromJWT(accessToken.getValue());
        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            Account account = (Account) userDetailsService.loadUserByUsername(email);

            if(account == null || !account.getStatus().equalsIgnoreCase(Status.ACCOUNT_ACTIVE.getValue())) {
                filterChain.doFilter(request, response);
                return;
            }

            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    account, null, account.getAuthorities()
            );

            authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }
        //là lệnh chuyển tiếp request đến filter tiếp theo hoặc servlet
        filterChain.doFilter(request, response);
    }
}
