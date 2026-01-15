package org.overcode250204.iamservice.services.auth;

import jakarta.servlet.http.HttpServletResponse;
import org.overcode250204.iamservice.dto.auth.JwtTokenDTO;

import java.io.IOException;

public interface GoogleOtpService {
    String loginWithGoogleByCode(String code, HttpServletResponse response) throws IOException;
    JwtTokenDTO verifyOtp(String email, String otp, HttpServletResponse response);
}
