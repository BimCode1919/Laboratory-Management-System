package org.overcode250204.iamservice.dto.user;

import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.*;
import org.overcode250204.iamservice.dto.role.RoleDTO;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileDTO {
    private UUID id;

    @NotBlank(message = "Identify number is required.")
    @Pattern(regexp = "^\\d{9}(\\d{3})?$", message = "Identify number must be 9 or 12 digits.")
    private String identifyNumber;

    @NotBlank(message = "Email is required.")
    @Email(message = "Email must be in valid format.")
    private String email;

    @NotBlank(message = "Password is required.")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,12}$",
            message = "Password must be 8–12 characters long and include uppercase, lowercase, and a number."
    )
    private String password;

    @NotBlank(message = "Full name is required.")
    @Pattern(regexp = "^[\\p{L} ]+$", message = "Full name must not contain special characters or numbers.")
    private String fullName;

    @NotBlank(message = "Age is required.")
    private int age;

    @NotNull(message = "Date of birth is required.")
    @PastOrPresent(message = "Date of birth cannot be in the future.")
    private LocalDate dob;

    @NotBlank(message = "Phone number is required.")
    @Pattern(regexp = "^\\+?\\d{10,15}$", message = "Phone number must be valid (10–15 digits, optional +).")
    private String phoneNumber;

    @NotBlank(message = "Gender is required.")
    @Pattern(regexp = "^(?i)(male|female)$", message = "Gender must be either Male or Female.")
    private String gender;

    @NotBlank(message = "Address is required.")
    @Size(max = 255, message = "Address must not exceed 255 characters.")
    private String address;

    @NotBlank(message = "Status is required.")
    private String status;

    private List<String> roles;

    public UserProfileDTO(UUID id,
                          String identifyNumber,
                          String email,
                          String fullName,
                          LocalDate dob,
                          int age,
                          String phoneNumber,
                          String gender,
                          String address,
                          String status,
                          List<String> roles) {
        this.id = id;
        this.identifyNumber = identifyNumber;
        this.email = email;
        this.fullName = fullName;
        this.age = age;
        this.dob = dob;
        this.phoneNumber = phoneNumber;
        this.gender = gender;
        this.address = address;
        this.status = status;
        this.roles = roles;
    }
}
