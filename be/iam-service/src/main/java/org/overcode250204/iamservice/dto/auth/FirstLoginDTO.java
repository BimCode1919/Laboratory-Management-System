package org.overcode250204.iamservice.dto.auth;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FirstLoginDTO {
    private String identifyNumber;
    private String newPassword;
    private String session;
}
