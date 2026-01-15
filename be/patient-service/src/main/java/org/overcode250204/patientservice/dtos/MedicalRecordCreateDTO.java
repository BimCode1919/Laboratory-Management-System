package org.overcode250204.patientservice.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.*;
import org.overcode250204.patientservice.enums.Gender;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecordCreateDTO {

    @NotBlank(message = "Full name is mandatory")
    private String fullName;

    @NotNull(message = "Date of Birth is mandatory")
    @Past(message = "Date of Birth must be in the past")
    @DateTimeFormat(pattern = "MM/dd/yyyy")
    private LocalDate dateOfBirth;

    @NotNull(message = "Gender is mandatory")
    private Gender gender;

    @NotBlank(message = "Address is mandatory")
    private String address;

    @NotBlank(message = "Phone number is mandatory")
    private String phoneNumber;

    @Email(message = "Email must be a valid email format")
    private String email;
}