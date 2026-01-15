package org.overcode250204.iamservice.services.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.overcode250204.iamservice.dto.auth.FirstLoginDTO;
import org.overcode250204.iamservice.dto.auth.JwtTokenDTO;
import org.overcode250204.iamservice.dto.auth.LoginDTO;
import org.overcode250204.iamservice.dto.auth.RefreshTokenDTO;

import java.util.UUID;

public interface CognitoService {
    JwtTokenDTO login(LoginDTO dto, HttpServletResponse response);
    JwtTokenDTO firstLogin (FirstLoginDTO dto, HttpServletResponse response);
    String forgotPassword(String email);
    String confirmForgotPassword(String email, String confirmationCode, String newPassword);
    void disableUser(String email);
    void enableUser(String email);
    JwtTokenDTO refresh(HttpServletRequest request, HttpServletResponse response);
    void logout(HttpServletRequest request, HttpServletResponse response);

    JwtTokenDTO adminLoginWithEmail(String email);
}
