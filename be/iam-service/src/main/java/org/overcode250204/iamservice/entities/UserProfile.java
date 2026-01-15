package org.overcode250204.iamservice.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.overcode250204.iamservice.enums.Status;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "user_profile")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "cognito_sub", nullable = false, unique = true, length = 255)
    private String cognitoSub;

    @Column(name = "identify_number", nullable = false, unique = true, length = 255)
    private String identifyNumberEncrypt;

    @Column(name = "identify_number_hash", nullable = false, unique = true, length = 255)
    private String identifyNumberHash;

    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String emailEncrypt;

    @Column(name = "email_hash", nullable = false, unique = true, length = 255)
    private String emailHash;

    @Column(name = "phone_number", length = 255)
    private String phoneNumber;

    @Column(name = "password", length = 255, nullable = true)
    private String password;

    @Column(nullable = false, length = 255)
    private String fullname;

    @Column(nullable = false)
    private LocalDate dob;

    @Column(name = "age")
    private int age;

    @Column(length = 255)
    private String gender;

    @Column(columnDefinition = "text", length = 255)
    private String address;

    @Column(name = "created_by_admin", nullable = false)
    private boolean createdByAdmin = true;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private Status status = Status.ACTIVE;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "updated_by")
    private UUID updatedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @OneToMany(mappedBy = "userProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<UserRole> userRoles = new ArrayList<>();

}
