package org.overcode250204.iamservice.dto.auth;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JwtTokenDTO {
    private String accessToken;
    private String idToken;
    private String refreshToken;
    private String deviceId;
    private Integer expiresIn;
    private boolean firstLogin;
    private String session;

    public JwtTokenDTO(String accessToken, String idToken, String newRefreshToken, Integer expiresIn) {
        this.accessToken = accessToken;
        this.idToken = idToken;
        this.refreshToken = newRefreshToken;
        this.expiresIn = expiresIn;
    }
}
