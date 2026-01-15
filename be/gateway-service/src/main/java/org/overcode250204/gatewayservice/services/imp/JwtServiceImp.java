package org.overcode250204.gatewayservice.services.imp;

import com.nimbusds.jwt.JWTClaimsSet;
import lombok.RequiredArgsConstructor;
import org.overcode250204.gatewayservice.services.JwtService;
import org.overcode250204.gatewayservice.utils.JwtUtils;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtServiceImp implements JwtService {

    private final JwtUtils jwtUtils;

    @Override
    public JWTClaimsSet verifyToken(String token) throws Exception {
        return jwtUtils.verifyToken(token);
    }
}
