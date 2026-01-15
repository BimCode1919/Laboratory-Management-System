package org.overcode250204.iamservice.dto.auth;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConfirmForgotPasswordDTO {
    private String email;
    private String confirmationCode;
    private String newPassword;
}
