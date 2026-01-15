package org.overcode250204.warehouseservice.controllers;


import org.overcode250204.base.BaseResponse;
import org.overcode250204.warehouseservice.services.interfaces.VendorService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestBody;
import org.overcode250204.warehouseservice.model.dto.reagent.VendorRequest;
import org.overcode250204.warehouseservice.model.dto.reagent.VendorResponse;
import org.overcode250204.warehouseservice.services.implement.VendorServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/vendors")
public class VendorController {
    @Value("${spring.application.name}")
    private String serviceName;
    @Autowired
    private VendorService vendorService;

    @PreAuthorize("hasAuthority('REAGENT_CREATE')")
    @PostMapping
    public ResponseEntity<BaseResponse<VendorResponse>> createVendor(@RequestBody VendorRequest request) {
        return ResponseEntity.ok(BaseResponse.success(serviceName, vendorService.createVendor(request)));
    }

    @PreAuthorize("hasAuthority('REAGENT_UPDATE')")
    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse<VendorResponse>> updateVendor(@PathVariable UUID id, @RequestBody VendorRequest request) {
        return ResponseEntity.ok(BaseResponse.success(serviceName, vendorService.updateVendor(id, request)));
    }

    @PreAuthorize("hasAuthority('REAGENT_DELETE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<String>> deleteVendor(@PathVariable UUID id) {
        return ResponseEntity.ok(BaseResponse.success(serviceName, vendorService.deleteVendor(id)));
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'LAB_USER', 'LAB_MANAGER','SERVICE_USER')")
    @GetMapping
    public ResponseEntity<BaseResponse<List<VendorResponse>>> getAllVendors() {
        return ResponseEntity.ok(BaseResponse.success(serviceName, vendorService.getAllVendors()));
    }
}
