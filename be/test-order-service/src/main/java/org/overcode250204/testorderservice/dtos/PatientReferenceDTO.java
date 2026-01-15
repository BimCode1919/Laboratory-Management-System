package org.overcode250204.testorderservice.dtos;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PatientReferenceDTO {
    private UUID patientId;
    private UUID patientCode;
    private String fullName;
    private LocalDate dateOfBirth;
    private Integer age;
    private String gender;
    private String address;
    private String phoneNumber;
    private String email;
    private LocalDateTime lastSyncedAt;
    private Boolean isActive;
}