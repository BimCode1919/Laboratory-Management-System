package org.overcode250204.iamservice.services.auth.impls;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.overcode250204.iamservice.configs.aesgcm.SecurityProperties;
import org.overcode250204.iamservice.configs.cognito.CognitoProperties;
import org.overcode250204.iamservice.dto.auth.FirstLoginDTO;
import org.overcode250204.iamservice.dto.auth.JwtTokenDTO;
import org.overcode250204.iamservice.dto.auth.LoginDTO;
import org.overcode250204.iamservice.dto.auth.RefreshTokenDTO;
import org.overcode250204.iamservice.entities.UserProfile;
import org.overcode250204.iamservice.exceptions.ErrorCode;
import org.overcode250204.iamservice.exceptions.IamServiceException;
import org.overcode250204.iamservice.repositories.UserRepository;
import org.overcode250204.iamservice.services.auth.CognitoService;
import org.overcode250204.iamservice.services.auth.RefreshTokenService;
import org.overcode250204.iamservice.services.auth.TokenCookieService;
import org.overcode250204.iamservice.services.crypto.AESEncryptionService;
import org.overcode250204.iamservice.utils.CognitoUtils;
import org.overcode250204.iamservice.utils.AuditLogUtils;
import org.overcode250204.iamservice.utils.HashUtil;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CognitoServiceImpl  implements CognitoService {
    private final CognitoProperties props;
    private final CognitoIdentityProviderClient cognitoClient;
    private final UserRepository userRepository;
    private final SecurityProperties securityProperties;
    private final AESEncryptionService aesService;
    private final CognitoUtils cognitoUtils;
    private final AuditLogUtils auditLogUtils;
    private final RefreshTokenService refreshTokenService;
    private final TokenCookieService tokenCookieService;

    @Override
    @Transactional
    public JwtTokenDTO login (LoginDTO dto, HttpServletResponse response){
        AuthenticationResultType token = null;
        String deviceId = null;
        try {
            String hashedIdentify = HashUtil.hmacSha256Base64(securityProperties.getBase64Pepper(), dto.getIdentifyNumber());
            UserProfile user = userRepository.findByIdentifyNumberHash(hashedIdentify)
                    .orElseThrow(() -> new RuntimeException(": " + dto.getIdentifyNumber()));
            String decryptedEmail = aesService.decrypt(user.getEmailEncrypt());
            AdminInitiateAuthResponse result = cognitoClient.adminInitiateAuth(
                    AdminInitiateAuthRequest.builder()
                            .userPoolId(props.getUserPoolId())
                            .clientId(props.getClientId())
                            .authFlow(AuthFlowType.ADMIN_USER_PASSWORD_AUTH)
                            .authParameters(Map.of(
                                    "USERNAME", decryptedEmail,
                                    "PASSWORD", dto.getPassword(),
                                    "SECRET_HASH", cognitoUtils.calculateSecretHash(decryptedEmail)
                            ))
                            .build()
            );

            if("NEW_PASSWORD_REQUIRED".equals(result.challengeNameAsString())){
                return JwtTokenDTO.builder()
                        .firstLogin(true)
                        .session(result.session())
                        .build();
            }

            token = result.authenticationResult();
            String refreshToken = token.refreshToken();
            deviceId = UUID.randomUUID().toString();

            if (refreshToken != null && !refreshToken.isBlank()) {
                UUID userId = user.getId();
                refreshTokenService.create(userId, deviceId, refreshToken);
                tokenCookieService.set(response, refreshToken);
            }

            auditLogUtils.createAuditOutboxEvent("IAM_USER_LOGIN", user.getId().toString(), "IAM_USER_LOGIN", user.getEmailEncrypt(), Map.of("ipAddress", dto.getIpAddress()));


        } catch (Exception e) {
            log.error("FAILED TO LOGIN At CognitoServiceImpl : {}", e.getMessage());
            auditLogUtils.createAuditOutboxEvent(
                    "USER_LOGIN_FAILED",
                    dto.getIdentifyNumber(),
                    "USER_LOGIN_FAILURE",
                    "anonymous",
                    Map.of(
                            "ipAddress", dto.getIpAddress(),
                            "exception", e.getMessage()
                    )
            );
            throw new IamServiceException(ErrorCode.FAIL_TO_LOGIN);
        }


        return JwtTokenDTO.builder()
                .accessToken(token.accessToken())
                .idToken(token.idToken())
                .refreshToken(token.refreshToken())
                .deviceId(deviceId)
                .expiresIn(token.expiresIn())
                .firstLogin(false)
                .build();
    }

    @Override
    @Transactional
    public JwtTokenDTO firstLogin (FirstLoginDTO dto, HttpServletResponse responses){
        String hashedIdentifyNumber = HashUtil.hmacSha256Base64(securityProperties.getBase64Pepper(), dto.getIdentifyNumber());

        UserProfile user = userRepository.findByIdentifyNumberHash(hashedIdentifyNumber)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String decryptEmail = aesService.decrypt(user.getEmailEncrypt());

        RespondToAuthChallengeResponse response = cognitoClient.respondToAuthChallenge(
                RespondToAuthChallengeRequest.builder()
                        .clientId(props.getClientId())
                        .challengeName(ChallengeNameType.NEW_PASSWORD_REQUIRED)
                        .session(dto.getSession())
                        .challengeResponses(Map.of(
                                "USERNAME", decryptEmail,
                                "NEW_PASSWORD", dto.getNewPassword(),
                                "SECRET_HASH", cognitoUtils.calculateSecretHash(decryptEmail)
                                )
                        )
                        .build()
        );

        AuthenticationResultType token = response.authenticationResult();
        String refreshToken = token.refreshToken();
        String deviceId = aesService.encrypt(UUID.randomUUID().toString());

        if (refreshToken != null && !refreshToken.isBlank()) {
            UUID userId = user.getId();
            refreshTokenService.create(userId, deviceId, refreshToken);
            tokenCookieService.set(responses, refreshToken);
        }
        auditLogUtils.createAuditOutboxEvent(
                "USER_FIRST_LOGIN",
                user.getId().toString(),
                "USER_FIRSTLOGIN_COMPLETED",
                decryptEmail,
                Map.of()
        );

        return JwtTokenDTO.builder()
                .accessToken(token.accessToken())
                .idToken(token.idToken())
                .refreshToken(null)
                .deviceId(deviceId)
                .expiresIn(token.expiresIn())
                .firstLogin(false)
                .build();
    }

    @Override
    public String forgotPassword(String email) {
        try {
            ForgotPasswordRequest request = ForgotPasswordRequest.builder()
                    .clientId(props.getClientId())
                    .username(email)
                    .secretHash(cognitoUtils.calculateSecretHash(email))
                    .build();

            cognitoClient.forgotPassword(request);
            auditLogUtils.createAuditOutboxEvent(
                    "USER_FORGOT_PASSWORD",
                    email,
                    "USER_PASSWORD_FORGOT_REQUESTED",
                    "anonymous",
                    Map.of("email", email)
            );
            return "Check your verify code in email";
        } catch (CognitoIdentityProviderException e) {
            auditLogUtils.createAuditOutboxEvent(
                    "USER_FORGOT_PASSWORD",
                    email,
                    "USER_PASSWORD_FORGOT_FAILED",
                    "anonymous",
                    Map.of("exception", e.getMessage())
            );
            throw new IamServiceException(ErrorCode.FAIL_TO_FORGOT_PASSWORD_FROM_COGNITO);
        } catch (Exception e) {
            auditLogUtils.createAuditOutboxEvent(
                    "USER_FORGOT_PASSWORD",
                    email,
                    "USER_PASSWORD_FORGOT_FAILED",
                    "anonymous",
                    Map.of("exception", e.getMessage())
            );
            throw new IamServiceException(ErrorCode.FORGOT_PASSWORD_FAILED);
        }
    }

    @Override
    public String confirmForgotPassword(String email, String confirmationCode, String newPassword) {
        try {
            ConfirmForgotPasswordRequest request = ConfirmForgotPasswordRequest.builder()
                    .clientId(props.getClientId())
                    .username(email)
                    .confirmationCode(confirmationCode)
                    .password(newPassword)
                    .secretHash(cognitoUtils.calculateSecretHash(email))
                    .build();

            cognitoClient.confirmForgotPassword(request);
            auditLogUtils.createAuditOutboxEvent(
                    "USER_CONFIRM_PASSWORD",
                    email,
                    "USER_PASSWORD_RESET_COMPLETED",
                    "anonymous",
                    Map.of("email", email)
            );
            return "Change password successfully";
        } catch (CognitoIdentityProviderException e) {
            auditLogUtils.createAuditOutboxEvent(
                    "USER_CONFIRM_PASSWORD",
                    email,
                    "USER_CONFIRM_PASSWORD_FAILED",
                    "anonymous",
                    Map.of("exception", e.getMessage())
            );
            throw new IamServiceException(ErrorCode.FAIL_TO_CONFIRM_PASSWORD_FROM_COGNITO);
        } catch (Exception e) {
            auditLogUtils.createAuditOutboxEvent(
                    "USER_CONFIRM_PASSWORD",
                    email,
                    "USER_CONFIRM_PASSWORD_FAILED",
                    "anonymous",
                    Map.of("exception", e.getMessage())
            );
            throw new IamServiceException(ErrorCode.CONFIRM_PASSWORD_FAILED);
        }
    }

    @Override
    public void disableUser(String email) {
        try {
            AdminDisableUserRequest request = AdminDisableUserRequest.builder()
                    .userPoolId(props.getUserPoolId())
                    .username(email)
                    .build();

            cognitoClient.adminDisableUser(request);
            log.info("Disabled user [{}] successfully", email);

        } catch (CognitoIdentityProviderException e) {
            log.error("Failed to disable user in Cognito: {}", e.awsErrorDetails().errorMessage());
            throw new IamServiceException(ErrorCode.FAILED_TO_DISABLE);
        }
    }

    @Override
    public void enableUser(String email) {
        try {
            AdminEnableUserRequest request = AdminEnableUserRequest.builder()
                    .userPoolId(props.getUserPoolId())
                    .username(email)
                    .build();

            cognitoClient.adminEnableUser(request);
            log.info("Enabled user [{}] successfully", email);

        } catch (CognitoIdentityProviderException e) {
            log.error("Failed to enable user in Cognito: {}", e.awsErrorDetails().errorMessage());
            throw new IamServiceException(ErrorCode.FAILED_TO_ENABLE);
        }
    }

    @Override
    @Transactional
    public JwtTokenDTO refresh(HttpServletRequest request, HttpServletResponse response) {
        String rawRefreshToken = tokenCookieService.get(request);
        if (rawRefreshToken == null) throw new IamServiceException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);

        if (!refreshTokenService.validate(rawRefreshToken)) throw new IamServiceException(ErrorCode.INVALID_REFRESH_TOKEN);

        UUID userId = refreshTokenService.getUserId(rawRefreshToken);
        UserProfile user = userRepository.findById(userId)
                .orElseThrow(() -> new IamServiceException(ErrorCode.USER_NOT_FOUND_WITH_ID));
        String deviceId = UUID.randomUUID().toString();

        try {
            GetTokensFromRefreshTokenResponse result = cognitoClient.getTokensFromRefreshToken(
                    GetTokensFromRefreshTokenRequest.builder()
                            .clientId(props.getClientId())
                            .clientSecret(props.getClientSecret())
                            .refreshToken(rawRefreshToken)
                            .build()
            );

            AuthenticationResultType token = result.authenticationResult();
            String newAccessToken = token.accessToken();
            String newIdToken = token.idToken();
            String newRefreshToken = token.refreshToken();


            if (newRefreshToken != null && !newRefreshToken.isBlank()) {
                refreshTokenService.revoke(rawRefreshToken);
                refreshTokenService.create(userId, "DEVICE", newRefreshToken);
                tokenCookieService.set(response, newRefreshToken);
            } else {
                System.out.println("Failed to initialize new refresh token");
            }

            auditLogUtils.createAuditOutboxEvent(
                    "USER_REFRESH_TOKEN",
                    userId.toString(),
                    "USER_TOKEN_REFRESHED",
                    user.getCognitoSub(),
                    Map.of("deviceId", deviceId)
            );

            return JwtTokenDTO.builder()
                    .accessToken(newAccessToken)
                    .idToken(newIdToken)
                    .refreshToken(null)
                    .expiresIn(token.expiresIn())
                    .firstLogin(false)
                    .build();
        } catch (Exception e){
            auditLogUtils.createAuditOutboxEvent(
                    "USER_REFRESH_TOKEN",
                    userId.toString(),
                    "USER_TOKEN_REFRESH_FAILED",
                    user.getCognitoSub(),
                    Map.of("exception", e.getMessage())
            );
            throw new IamServiceException(ErrorCode.FAIL_TO_REFRESH_TOKEN);

        }
    }

    @Override
    @Transactional
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String rawRefreshToken = tokenCookieService.get(request);
        if (rawRefreshToken != null) {
            refreshTokenService.revoke(rawRefreshToken);
            tokenCookieService.clear(response);
        }
    }

    @Override
    @Transactional
    public JwtTokenDTO adminLoginWithEmail(String email) {
        String emailHashed = HashUtil.hmacSha256Base64(securityProperties.getBase64Pepper(), email);
        UserProfile user = userRepository.findByEmailHash(emailHashed)
                .orElseThrow(() -> new IamServiceException(ErrorCode.USER_NOT_FOUND_WITH_ID));
        try {
            Map<String, String> authParams = new HashMap<>();
            authParams.put("USERNAME", email);
            authParams.put("PASSWORD", aesService.decrypt(user.getPassword()));
            authParams.put("SECRET_HASH", cognitoUtils.calculateSecretHash(email));
            AdminInitiateAuthRequest request = AdminInitiateAuthRequest.builder()
                    .userPoolId(props.getUserPoolId())
                    .clientId(props.getClientId())
                    .authFlow(AuthFlowType.ADMIN_USER_PASSWORD_AUTH)
                    .authParameters(authParams)
                    .build();

            AdminInitiateAuthResponse result = cognitoClient.adminInitiateAuth(request);

            AuthenticationResultType token = result.authenticationResult();

            return JwtTokenDTO.builder()
                    .accessToken(token.accessToken())
                    .idToken(token.idToken())
                    .refreshToken(token.refreshToken())
                    .expiresIn(token.expiresIn())
                    .firstLogin(false)
                    .build();

        } catch (CognitoIdentityProviderException e) {
            log.error("Failed login Cognito for user {}: {}", email, e.awsErrorDetails().errorMessage());
            throw new IamServiceException(ErrorCode.FAIL_TO_LOGIN);
        }
    }
}
