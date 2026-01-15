package org.overcode250204.gatewayservice.services;

import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jwt.JWTClaimsSet;
import io.jsonwebtoken.Claims;

import java.util.List;
import java.util.function.Function;

public interface JwtService {
    JWTClaimsSet verifyToken(String token) throws Exception;

}
