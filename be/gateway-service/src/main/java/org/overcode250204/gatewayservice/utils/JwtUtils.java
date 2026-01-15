package org.overcode250204.gatewayservice.utils;

import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.overcode250204.gatewayservice.exceptions.JwtVerificationException;
import org.overcode250204.gatewayservice.properties.SecurityProperties;
import org.springframework.stereotype.Component;

import java.net.URL;
import java.text.ParseException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtils {

    private final SecurityProperties props;

    private JWKSet cachedJwkSet;

    private Instant lastJwtFetch = Instant.EPOCH;

    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private Timer verifyTimer;


    public JWTClaimsSet verifyToken(String token) {
        Timer.Sample sample = verifyTimer != null ? Timer.start() : null;
        try {
            SignedJWT jwt;
            try {
                jwt = SignedJWT.parse(token);
            } catch (ParseException e) {
                throw new JwtVerificationException("Invalid JWT Token", e);
            }

            String kid = jwt.getHeader().getKeyID();



            JWKSet jwkSet = getJwkSet();

            JWK jwk = (kid != null) ? jwkSet.getKeyByKeyId(kid) : null;

            if (jwk == null) {
                log.info("JWT kid={} not found in JWKS cache, forcing JWKS refetch", kid);
                forceFetchJwkSet();
                jwkSet = getJwkSet();
                jwk = (kid != null) ? jwkSet.getKeyByKeyId(kid) : null;
                if (jwk == null) {
                    throw new JwtVerificationException("No matching JWK found for kid: " + kid);
                }
            }

            if (!(jwk instanceof RSAKey)) {
                throw new JwtVerificationException("Unsupported JWK type: " + jwk.getClass().getName());
            }

            RSAKey rsa = (RSAKey) jwk;

            try {
                boolean sigok = jwt.verify(new RSASSAVerifier(rsa));
                if (!sigok) {
                    throw new JwtVerificationException("Invalid JWT signature");
                }



            } catch (Exception e) {
                throw new JwtVerificationException("Invalid JWT signature verification failed", e);
            }

            JWTClaimsSet claims;
            try {
                claims = jwt.getJWTClaimsSet();
            } catch (ParseException e) {
                throw new JwtVerificationException("Failed to parse JWT claims", e);
            }

            if (props.getIssuer() != null && !props.getIssuer().equals(claims.getIssuer())) {
                throw new JwtVerificationException("Invalid JWT issuer: " + claims.getIssuer());
            }


            String tokenType = (String) claims.getClaims().get("token_use");
            if (tokenType == null || (!tokenType.equals("access") && !tokenType.equals("id"))) {
                throw new JwtVerificationException("Invalid token_use: " + tokenType);
            }


            if (tokenType.equals("id")) {
                String expectedAudience = props.getAudience();
                List<String> aud = claims.getAudience();
                if (expectedAudience != null && (aud == null || !aud.contains(expectedAudience))) {
                    throw new JwtVerificationException("Invalid audience for ID token: " + aud);
                }
            }

            if (tokenType.equals("access")) {
                String expectedClientId = props.getClientId();
                Object clientIdObj = claims.getClaims().get("client_id");
                String clientId = clientIdObj != null ? clientIdObj.toString() : null;
                if (expectedClientId != null && !expectedClientId.equals(clientId)) {
                    throw new JwtVerificationException("Invalid client_id for Access token: " + clientId);

                }

            }

            Date now = new Date();
            Date exp = claims.getExpirationTime();
            if (exp == null || exp.before(now)) {
                throw new JwtVerificationException("Token expired");
            }

            Date notBefore = claims.getNotBeforeTime();
            if (notBefore != null && notBefore.after(now)) {
                throw new JwtVerificationException("Token not valid yet (nbf)");
            }


            return claims;

        } catch (Exception e) {
            throw new JwtVerificationException("Invalid JWT token", e);
        } finally {
            if (sample != null) {
                sample.stop(verifyTimer);
            }
        }


    }

    private JWKSet getJwkSet() {
        lock.readLock().lock();
        try {
            if (cachedJwkSet != null && Instant.now().isBefore(lastJwtFetch.plus(Duration.ofMinutes(props.getJwtRefreshIntervalMins())))) {
                return cachedJwkSet;
            }
        } catch (Exception e) {
            throw new JwtVerificationException("Could not get JWK Set", e);
        } finally {
            lock.readLock().unlock();
        }

        lock.writeLock().lock();
        try {
            if (cachedJwkSet == null || Instant.now().isAfter(lastJwtFetch.plus(Duration.ofMinutes(props.getJwtRefreshIntervalMins())))) {
                if (props.getJwksUrl() == null) {
                    throw new JwtVerificationException("JWKS URL is not configured (and no JWKSet injected)");
                }
                try {
                    log.info("Fetching JWKS from URL: {}", props.getJwksUrl());
                    JWKSet newSet = JWKSet.load(new URL(props.getJwksUrl()));
                    cachedJwkSet = newSet;
                    lastJwtFetch = Instant.now();
                } catch (Exception e) {
                    log.error("Error while fetching JWKS from URL: {}", props.getJwksUrl(), e);
                    if (cachedJwkSet == null) {
                        throw new JwtVerificationException("Unable to load JWKS and no cached keys available", e);
                    }
                }
            }
            return cachedJwkSet;
        } finally {
            lock.writeLock().unlock();
        }

    }

    private void forceFetchJwkSet() {
        lock.writeLock().lock();
        try {
            if (props.getJwksUrl() == null) return;
            try {
                log.info("Force fetching JWKS from {}", props.getJwksUrl());
                JWKSet newSet = JWKSet.load(new URL(props.getJwksUrl()));
                cachedJwkSet = newSet;
                lastJwtFetch = Instant.now();
            } catch (Exception e) {
                log.warn("Force fetch JWKS failed: {}", e.getMessage());
                if (cachedJwkSet == null) {
                    throw new JwtVerificationException("Unable to load JWKS and no cached keys available", e);
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }


}
