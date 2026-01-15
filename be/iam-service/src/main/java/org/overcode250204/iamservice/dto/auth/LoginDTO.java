package org.overcode250204.iamservice.dto.auth;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginDTO {
    private String identifyNumber;
    private String password;
    private String ipAddress;
}
