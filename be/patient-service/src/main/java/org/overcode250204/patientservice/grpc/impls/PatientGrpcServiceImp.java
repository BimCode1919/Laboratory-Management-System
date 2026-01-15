package org.overcode250204.patientservice.grpc.impls;

import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;
import org.overcode250204.common.grpc.PatientIdentityInfo;
import org.overcode250204.common.grpc.PatientRecord;
import org.overcode250204.common.grpc.PatientServiceGrpc;
import org.overcode250204.patientservice.entities.MedicalRecord;
import org.overcode250204.patientservice.entities.Patient;
import org.overcode250204.patientservice.enums.Gender;
import org.overcode250204.patientservice.exceptions.ErrorCode;
import org.overcode250204.patientservice.exceptions.PatientException;
import org.overcode250204.patientservice.repositories.MedicalRecordRepository;
import org.overcode250204.patientservice.repositories.PatientRepository;
import org.overcode250204.patientservice.services.MedicalRecordSyncService;
import org.overcode250204.patientservice.utils.HashUtil;


import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;
import java.util.UUID;


@GrpcService
@RequiredArgsConstructor
public class PatientGrpcServiceImp extends PatientServiceGrpc.PatientServiceImplBase {

    private final PatientRepository patientRepository;

    private final MedicalRecordRepository medicalRecordRepository;

    private final MedicalRecordSyncService medicalRecordSyncService;

    private final HashUtil hashUtil;

    @Override
    public void ensurePatient(PatientIdentityInfo request, StreamObserver<PatientRecord> responseObserver) {
        LocalDate dob = LocalDate.parse(request.getDob());
        String emailHash = hashUtil.hmacSha256Base64(request.getEmail());
        String phoneHash = hashUtil.hmacSha256Base64(request.getPhone());
        Optional<Patient> patientExist = patientRepository.findByDateOfBirthAndPhoneOrEmail(dob, phoneHash, emailHash);
        boolean createdNew = false;
        Patient patient;
        if (patientExist.isPresent()) {

            MedicalRecord medicalRecord = medicalRecordRepository.findByPatientId(patientExist.get().getId())
                    .orElseThrow(() -> new PatientException(ErrorCode.PATIENT_MUST_MEDICAL_RECORD_EXISTS));
            PatientRecord response = PatientRecord.newBuilder()
                    .setPatientId(patientExist.get().getId().toString())
                    .setPatientCode(patientExist.get().getPatientCode().toString())
                    .setAddress(patientExist.get().getAddress())
                    .setAge(Integer.toString(patientExist.get().getAge()))
                    .setEmail(patientExist.get().getEmail())
                    .setGender(patientExist.get().getGender().name())
                    .setPhone(patientExist.get().getPhone())
                    .setMedicalRecordId(medicalRecord.getId().toString())
                    .setFullName(patientExist.get().getFullName())
                    .setDob(patientExist.get().getDateOfBirth().toString())
                    .setCreatedNew(createdNew)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } else {
            createdNew = true;
            patient = new Patient();
            patient.setPatientCode(UUID.randomUUID());
            patient.setFullName(request.getFullName());
            patient.setDateOfBirth(dob);
            patient.setPhone(request.getPhone());
            patient.setEmail(request.getEmail());
            patient.setGender(Gender.valueOf(request.getGender()));
            patient.setAddress(request.getAddress());
            patient.setCreatedAt(Instant.now());
            patient.setPhoneHash(phoneHash);
            patient.setEmailHash(emailHash);

            if (request.getDob() != null) {
                patient.setAge(Period.between(dob, LocalDate.now()).getYears());
            }
            patient = patientRepository.save(patient);

            MedicalRecord medicalRecord = new MedicalRecord();
            medicalRecord.setPatient(patient);
            medicalRecord.setLastTestDate(Instant.now());
            medicalRecord.setVisitDate(Instant.now());
            medicalRecord.setCreatedBy(UUID.fromString(request.getCreatedByLabUser()));
            medicalRecordRepository.save(medicalRecord);

            medicalRecordSyncService.indexMedicalRecord(medicalRecord);

            PatientRecord response = PatientRecord.newBuilder()
                    .setPatientId(patient.getId().toString())
                    .setPatientCode(patient.getPatientCode().toString())
                    .setFullName(patient.getFullName())
                    .setAddress(patient.getAddress())
                    .setDob(patient.getDateOfBirth().toString())
                    .setAge(Integer.toString(patient.getAge()))
                    .setEmail(patient.getEmail())
                    .setGender(patient.getGender().name())
                    .setPhone(patient.getPhone())
                    .setMedicalRecordId(medicalRecord.getId().toString())
                    .setCreatedNew(createdNew)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }


    }
}
