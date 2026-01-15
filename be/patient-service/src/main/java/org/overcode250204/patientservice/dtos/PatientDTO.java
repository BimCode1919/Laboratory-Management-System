package org.overcode250204.patientservice.dtos;


import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import org.overcode250204.patientservice.enums.Gender;

import java.time.LocalDate;


@Getter
@Setter
public class PatientDTO {
    private String patientCode;
    private String fullName;

    private Gender gender;

    @Pattern(
            regexp = "^(\\+84|0)(3[2-9]|5[2689]|7[06-9]|8[1-9]|9[0-9])[0-9]{7}$",
            message = "Phone number must be a valid Vietnamese number"
    )
    private String phoneNumber;
    @Email(message = "Email must be a valid email format")
    private String email;
    private String address;
    private LocalDate dateOfBirth;

}
