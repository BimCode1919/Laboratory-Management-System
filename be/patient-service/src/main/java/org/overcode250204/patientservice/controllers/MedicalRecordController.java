package org.overcode250204.patientservice.controllers;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.overcode250204.base.BaseResponse;
import org.overcode250204.patientservice.dtos.MedicalRecordCreateDTO;
import org.overcode250204.patientservice.dtos.MedicalRecordDTO;
import org.overcode250204.patientservice.dtos.MedicalRecordUpdateDTO;
import org.overcode250204.patientservice.services.MedicalRecordService;
import org.overcode250204.patientservice.utils.AuthUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/medical")
@RequiredArgsConstructor
public class MedicalRecordController {

    @Value("${spring.application.name}")
    private String serviceName;

    private final MedicalRecordService medicalRecordService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LAB_USER')")
    public ResponseEntity<BaseResponse<?>> createRecord(@Valid @RequestBody MedicalRecordCreateDTO dto) {
        String createdByLabUser = AuthUtils.getCurrentUser().getPrincipal().toString();
        MedicalRecordDTO response = medicalRecordService.addRecord(dto, createdByLabUser);
        return ResponseEntity.ok(BaseResponse.success(serviceName, response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'LAB_USER')")
    public ResponseEntity<BaseResponse<Page<MedicalRecordDTO>>> getAllRecords(
            @PageableDefault(sort = {"visitDate"}, direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable) {
        Page<MedicalRecordDTO> records = medicalRecordService.getAllRecords(pageable);
        return ResponseEntity.ok(BaseResponse.success(serviceName, records));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LAB_USER')")
    public ResponseEntity<BaseResponse<MedicalRecordDTO>> getRecordById(@PathVariable UUID id) {
        String accessedBy = AuthUtils.getCurrentUser().getPrincipal().toString();
        MedicalRecordDTO response = medicalRecordService.getRecordById(id, accessedBy);
        return ResponseEntity.ok(BaseResponse.success(serviceName, response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LAB_USER')")
    public ResponseEntity<BaseResponse<MedicalRecordDTO>> updateRecord(@PathVariable UUID id, @Valid @RequestBody MedicalRecordUpdateDTO medicalRecordUpdateDTO) {
        String updatedBy = AuthUtils.getCurrentUser().getPrincipal().toString();
        MedicalRecordDTO response = medicalRecordService.updateRecord(id, medicalRecordUpdateDTO, updatedBy);
        return ResponseEntity.ok(BaseResponse.success(serviceName, response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LAB_USER')")
    public ResponseEntity<BaseResponse<String>> deleteRecord(@PathVariable UUID id) {
        String deletedByLabUser = AuthUtils.getCurrentUser().getPrincipal().toString();
        String response = medicalRecordService.deleteRecord(id, deletedByLabUser);
        return ResponseEntity.ok(BaseResponse.success(serviceName, response));
    }
}
