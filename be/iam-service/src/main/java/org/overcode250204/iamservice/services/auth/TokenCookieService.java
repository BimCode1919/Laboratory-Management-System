package org.overcode250204.iamservice.services.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface TokenCookieService {
    void set(HttpServletResponse response, String token);
    String get(HttpServletRequest request);
    void clear(HttpServletResponse response);
}
