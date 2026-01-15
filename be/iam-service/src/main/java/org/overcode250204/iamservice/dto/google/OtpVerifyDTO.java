package org.overcode250204.iamservice.dto.google;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpVerifyDTO {
    private String email;
    private String otp;
}
