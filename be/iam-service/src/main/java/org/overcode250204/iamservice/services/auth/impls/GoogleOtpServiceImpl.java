package org.overcode250204.iamservice.services.auth.impls;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang.RandomStringUtils;
import org.overcode250204.iamservice.configs.aesgcm.SecurityProperties;
import org.overcode250204.iamservice.dto.auth.JwtTokenDTO;
import org.overcode250204.iamservice.exceptions.ErrorCode;
import org.overcode250204.iamservice.exceptions.IamServiceException;
import org.overcode250204.iamservice.repositories.UserRepository;
import org.overcode250204.iamservice.services.auth.CognitoService;
import org.overcode250204.iamservice.services.auth.GoogleOtpService;
import org.overcode250204.iamservice.services.auth.MailService;
import org.overcode250204.iamservice.services.auth.TokenCookieService;
import org.overcode250204.iamservice.utils.HashUtil;
import org.overcode250204.iamservice.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class GoogleOtpServiceImpl implements GoogleOtpService {

    @Value("${google.client.id}")
    private String clientId;
    @Value("${google.client.secret}")
    private String clientSecret;
    @Value("${google.redirect.uri}")
    private String redirectUri;
    @Value("${google.token.uri}")
    private String tokenUri;

    private static final int OTP_TTL_MINUTES = 5;
    private static final int OTP_MAX_ATTEMPT = 5;
    private final RestTemplate restTemplate;
    private final RedisTemplate<String, String> redisTemplate;
    private final CognitoService cognitoService;
    private final UserRepository userRepository;
    private final SecurityProperties props;
    private final TokenCookieService tokenCookieService;
    private final MailService mailService;

    @Value("${link-verify}")
    private String linkVerify;

    @Value("${google-failed-callback-url}")
    private String googleFailedCallbackUrl;

    public GoogleOtpServiceImpl(
            RestTemplate restTemplate,
            @Qualifier("customRedisTemplate") RedisTemplate<String, String> redisTemplate,
            CognitoService cognitoService,
            UserRepository userRepository,
            SecurityProperties props,
            TokenCookieService tokenCookieService,
            MailService mailService
    ) {
        this.restTemplate = restTemplate;
        this.redisTemplate = redisTemplate;
        this.cognitoService = cognitoService;
        this.userRepository = userRepository;
        this.props = props;
        this.tokenCookieService = tokenCookieService;
        this.mailService = mailService;
    }

    @Override
    public String loginWithGoogleByCode(String code, HttpServletResponse response) throws IOException {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("code", code);
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("redirect_uri", redirectUri);
        params.add("grant_type", "authorization_code");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        Map<String, Object> googleTokenResponse = restTemplate.postForObject(tokenUri, request, Map.class);
        if (googleTokenResponse == null || !googleTokenResponse.containsKey("id_token")) {
            throw new RuntimeException("Failed to get id_token from Google");
        }

        String idToken = (String) googleTokenResponse.get("id_token");

        Map<String, Object> payload = JwtUtils.decodeIdToken(idToken);
        String email = (String) payload.get("email");

        String emailHash = HashUtil.hmacSha256Base64(props.getBase64Pepper(), email);
        if (!userRepository.existsByEmailHash(emailHash)) {
            response.sendRedirect(googleFailedCallbackUrl);
            throw new IamServiceException(ErrorCode.USER_NOT_FOUND_WITH_ID);
        }

        String otpKey = "OTP:" + email;
        String attemptsKey = "OTP_ATTEMPTS:" + email;

        Integer attempts = redisTemplate.opsForValue().get(attemptsKey) != null ?
                Integer.parseInt(redisTemplate.opsForValue().get(attemptsKey)) : 0;

        if (attempts >= OTP_MAX_ATTEMPT) {
            throw new RuntimeException("Max OTP attempts exceeded, try later");
        }

        String otp = RandomStringUtils.randomNumeric(6);
        redisTemplate.opsForValue().set(otpKey, otp, OTP_TTL_MINUTES, TimeUnit.MINUTES);
        redisTemplate.opsForValue().set(attemptsKey, String.valueOf(attempts + 1), OTP_TTL_MINUTES, TimeUnit.MINUTES);

        String loginLink = linkVerify
                + "?otp=" + otp
                + "&email=" + email;
        mailService.sendLoginLink(email, loginLink);

        return "Login link sent, please check your email.";
    }

    @Override
    public JwtTokenDTO verifyOtp(String email, String otp, HttpServletResponse response) {
        String otpKey = "OTP:" + email;
        String cachedOtp = redisTemplate.opsForValue().get(otpKey);

        if (cachedOtp == null) {
            throw new RuntimeException("OTP expired or not found");
        }

        if (!cachedOtp.equals(otp)) {
            throw new RuntimeException("OTP invalid");
        }

        redisTemplate.delete(otpKey);
        redisTemplate.delete("OTP_ATTEMPTS:" + email);

        JwtTokenDTO tokenDTO = cognitoService.adminLoginWithEmail(email);

        tokenCookieService.set(response, tokenDTO.getRefreshToken());

        return tokenDTO;
    }
}
