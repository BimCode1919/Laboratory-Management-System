package org.overcode250204.testorderservice.services.impls;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.overcode250204.testorderservice.exceptions.ErrorCode;
import org.overcode250204.testorderservice.exceptions.TestOrderException;
import org.overcode250204.testorderservice.models.entites.PatientReference;
import org.overcode250204.testorderservice.repositories.PatientReferenceRepository;
import org.overcode250204.testorderservice.services.PatientReferenceService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PatientReferenceServiceImpl implements PatientReferenceService {

    private final PatientReferenceRepository patientReferenceRepository;

    @Override
    @Transactional
    public void updatePatientReference(Map<String, Object> payload) {
        try {
            String patientCode = (String) payload.get("patientCode");

            PatientReference patientReference  = patientReferenceRepository.findByPatientCode(UUID.fromString(patientCode));
            if (patientReference == null) {
                throw new TestOrderException(ErrorCode.PATIENT_CODE_DOES_NOT_EXIST);
            }
            String address = (String) payload.get("address");
            String email = (String) payload.get("email");
            String phone = (String) payload.get("phone");
            String fullName = (String) payload.get("fullName");
            String dateOfBirth = (String) payload.get("dateOfBirth");
            String gender = (String) payload.get("gender");

            patientReference.setAddress(address);
            patientReference.setEmail(email);
            patientReference.setPhoneNumber(phone);
            patientReference.setFullName(fullName);
            patientReference.setDateOfBirth(LocalDate.parse(dateOfBirth));
            patientReference.setGender(gender);

            patientReferenceRepository.save(patientReference);


        } catch (Exception e) {
            log.warn("Fail to sync updated patient information: {}" , e.getMessage());
            throw new TestOrderException(ErrorCode.FAIL_TO_SYNC_UPDATED_PATIENT_INFORMATION);
        }




    }
}
