package org.overcode250204.iamservice.controllers;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.overcode250204.base.BaseResponse;
import org.overcode250204.iamservice.dto.auth.*;
import org.overcode250204.iamservice.services.auth.CognitoService;
import org.overcode250204.iamservice.services.auth.GoogleOtpService;
import org.overcode250204.iamservice.utils.IpAddressUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class AuthController {
    private final CognitoService cognitoService;
    private final GoogleOtpService googleOtpService;

    @Value("${one-time-login-redirect-url}")
    private String oneTimeLoginRedirectUrl;

    @Value("${google-success-callback-url}")
    private String googleSuccessCallbackUrl;

    @Value("${spring.application.name}")
    private String serviceName;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginDTO dto, HttpServletRequest request, HttpServletResponse response) {
        String ipAddress = IpAddressUtils.getClientIp(request);
        dto.setIpAddress(ipAddress);
        return ResponseEntity.ok(BaseResponse.success(serviceName, cognitoService.login(dto, response)));
    }

    @PostMapping("/first-login/change-password")
    public ResponseEntity<?> confirmFirstLogin(@RequestBody FirstLoginDTO dto, HttpServletResponse response) {
        return ResponseEntity.ok(BaseResponse.success(serviceName, cognitoService.firstLogin(dto, response)));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordDTO dto) {
        return ResponseEntity.ok(BaseResponse.success(serviceName, cognitoService.forgotPassword(dto.getEmail())));
    }

    @PostMapping("/forgot-password-confirm")
    public ResponseEntity<?> confirmForgot(@RequestBody ConfirmForgotPasswordDTO dto) {
        return ResponseEntity.ok(BaseResponse.success(serviceName, cognitoService.confirmForgotPassword(dto.getEmail(), dto.getConfirmationCode(), dto.getNewPassword())));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refresh(HttpServletRequest request, HttpServletResponse response) {
        return ResponseEntity.ok(BaseResponse.success(
                serviceName,
                cognitoService.refresh(request, response)
        ));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        cognitoService.logout(request, response);
        return ResponseEntity.ok(BaseResponse.success(serviceName, "LOGOUT_SUCCESS"));
    }

    @GetMapping("/callback/google")
    public void googleCallback(@RequestParam String code, HttpServletResponse response) throws IOException {
        ResponseEntity.ok(BaseResponse.success(serviceName, googleOtpService.loginWithGoogleByCode(code, response)));
        response.sendRedirect(googleSuccessCallbackUrl);
    }

    @GetMapping("/verify")
    public void verifyOtp(@RequestParam("otp") String otp, @RequestParam("email") String email, HttpServletResponse response) throws IOException {
        JwtTokenDTO tokenDTO = googleOtpService.verifyOtp(email, otp, response);
        String redirectUrl = oneTimeLoginRedirectUrl + tokenDTO.getAccessToken();
        response.sendRedirect(redirectUrl);
    }
}
