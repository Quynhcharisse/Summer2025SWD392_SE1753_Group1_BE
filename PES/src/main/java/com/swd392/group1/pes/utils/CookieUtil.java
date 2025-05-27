package com.swd392.group1.pes.utils;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class CookieUtil {
    public static Cookie getCookie (HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equalsIgnoreCase(name)) {
                    return cookie;
                }
            }
        }
        return null;
    }

    public static void createCookie (HttpServletResponse response, String accessValue, String refreshValue, long accessExp, long refreshExp) {
        Cookie access = new Cookie("access", accessValue);
        access.setPath("/");
        access.setMaxAge((int) (accessExp / 1000));
        response.addCookie(access);

        Cookie refresh = new Cookie("refresh", refreshValue);
        refresh.setPath("/");
        refresh.setMaxAge((int) (refreshExp / 1000));
        refresh.setHttpOnly(true);
        response.addCookie(refresh);

        Cookie checkCookie = new Cookie("check", "true");
        checkCookie.setPath("/");
        checkCookie.setMaxAge((int) (refreshExp / 1000));
        response.addCookie(checkCookie);
    }

    public static void removeCookie (HttpServletResponse response) {
        Cookie access = new Cookie("access", null);
        access.setPath("/");
        access.setMaxAge(0);
        response.addCookie(access);

        Cookie refresh = new Cookie("refresh", null);
        refresh.setPath("/");
        refresh.setMaxAge(0);
        response.addCookie(refresh);

        Cookie check = new Cookie("check", "true");
        refresh.setPath("/");
        refresh.setMaxAge(0);
        response.addCookie(check);
    }
}
