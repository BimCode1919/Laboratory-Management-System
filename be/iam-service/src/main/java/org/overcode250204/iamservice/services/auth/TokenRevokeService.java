package org.overcode250204.iamservice.services.auth;

public interface TokenRevokeService {
    boolean isRevoked(String refreshToken);
}
