package org.overcode250204.patientservice.controllers;

import lombok.RequiredArgsConstructor;
import org.overcode250204.base.BaseResponse;
import org.overcode250204.patientservice.dtos.PatientDTO;
import org.overcode250204.patientservice.services.PatientService;
import org.overcode250204.patientservice.utils.AuthUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/patients")
@RequiredArgsConstructor
public class PatientController {

    @Value("${spring.application.name}")
    private String serviceName;

    private final PatientService patientService;

    @PutMapping("/{patientCode}")
    @PreAuthorize("hasAnyRole('ADMIN', 'LAB_USER')")
    public ResponseEntity<BaseResponse<?>> updatePatient(@PathVariable("patientCode") UUID patientCode, @RequestBody PatientDTO patientDTO) {
        String updatedBy = AuthUtils.getCurrentUser().getPrincipal().toString();
        PatientDTO response =  patientService.updatePatient(patientCode, patientDTO, updatedBy);
        return ResponseEntity.ok(BaseResponse.success(serviceName, response));
    }




}
