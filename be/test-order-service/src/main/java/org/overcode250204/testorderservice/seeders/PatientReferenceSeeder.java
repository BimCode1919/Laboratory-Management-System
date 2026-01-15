package org.overcode250204.testorderservice.seeders;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.overcode250204.testorderservice.models.entites.PatientReference;
import org.overcode250204.testorderservice.repositories.PatientReferenceRepository;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
@Order(1)
public class PatientReferenceSeeder {
//    private final PatientReferenceRepository patientReferenceReposit
//
//        patientReferenceRepository.saveAll(patients);
//        log.info("Seeded {} PatientReferences successfully.", patients.size());
//    }
//
//    private PatientReference buildPatient(int index) {
//        LocalDate dob = faker.timeAndDate().birthday(18, 80);
//
//        return PatientReference.builder()
//                .patientCode(UUID.randomUUID()) // unique code
//                .fullName(faker.name().fullName()) // e.g., "Nguyễn Văn A"
//                .dateOfBirth(dob)
//                // age tự calc ở PrePersist
//                .gender(faker.options().option("Nam", "Nữ", "Khác")) // VN genderory;
//        private final Faker faker = new Faker(new Locale("vi"));
//
//        @PostConstruct
//        @Transactional
//        public void seedPatientReferences() {
//            if (patientReferenceRepository.count() > 0) {
//                log.info("PatientReferences already exist. Skipping...");
//                return;
//            }
//
//            List<PatientReference> patients = new ArrayList<>();
//            for (int i = 0; i < 20; i++) { // seed 20, tăng nếu cần
//                patients.add(buildPatient(i + 1));
//            }
//                .address(faker.address().fullAddress()) // e.g., "123 Đường Trần Phú, Quận 1, TP.HCM"
//                .phoneNumber(faker.phoneNumber().cellPhone()) // VN format
//                .email(faker.internet().emailAddress())
//                .isActive(true)
//                // lastSyncedAt tự set ở PrePersist
//                .build();
//    }
}