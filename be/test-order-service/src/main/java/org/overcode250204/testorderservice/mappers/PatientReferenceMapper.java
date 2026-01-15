package org.overcode250204.testorderservice.mappers;

import org.overcode250204.testorderservice.dtos.PatientReferenceDTO;
import org.overcode250204.testorderservice.models.entites.PatientReference;
import org.springframework.stereotype.Component;

@Component
public class PatientReferenceMapper {
    public PatientReferenceDTO toDTO(PatientReference entity) {
        if (entity == null) return null;
        return new PatientReferenceDTO(
                entity.getPatientId(),
                entity.getPatientCode(),
                entity.getFullName(),
                entity.getDateOfBirth(),
                entity.getAge(),
                entity.getGender(),
                entity.getAddress(),
                entity.getPhoneNumber(),
                entity.getEmail(),
                entity.getLastSyncedAt(),
                entity.getIsActive()
        );
    }

    public void updateFromDto(PatientReferenceDTO dto, PatientReference entity) {
        if (dto == null || entity == null) return;
        if (dto.getFullName() != null) entity.setFullName(dto.getFullName());
        if (dto.getDateOfBirth() != null) entity.setDateOfBirth(dto.getDateOfBirth());
        if (dto.getAge() != null) entity.setAge(dto.getAge());
        if (dto.getGender() != null) entity.setGender(dto.getGender());
        if (dto.getAddress() != null) entity.setAddress(dto.getAddress());
        if (dto.getPhoneNumber() != null) entity.setPhoneNumber(dto.getPhoneNumber());
        if (dto.getEmail() != null) entity.setEmail(dto.getEmail());
        if (dto.getLastSyncedAt() != null) entity.setLastSyncedAt(dto.getLastSyncedAt());
        if (dto.getIsActive() != null) entity.setIsActive(dto.getIsActive());
    }
}