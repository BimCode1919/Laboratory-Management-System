package org.overcode250204.warehouseservice.controllers;

import lombok.RequiredArgsConstructor;
import org.overcode250204.base.BaseResponse;
import org.overcode250204.warehouseservice.model.dto.instrument.CreateInstrumentRequest;
import org.overcode250204.warehouseservice.model.entities.Instrument;
import org.overcode250204.warehouseservice.model.enums.Status;
import org.overcode250204.warehouseservice.services.interfaces.InstrumentService;
import org.overcode250204.warehouseservice.utils.AuthUtils;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/instruments")
@RequiredArgsConstructor
public class InstrumentController {

    private final InstrumentService instrumentService;
    private static final String SERVICE_NAME = "warehouse-service";

    // === CREATE ===
    @PostMapping
    @PreAuthorize("hasAuthority('INSTRUMENT_CREATE')")
    public ResponseEntity<BaseResponse<Instrument>> createInstrument(@RequestBody CreateInstrumentRequest request) {
        UUID createdBy = UUID.fromString((String) AuthUtils.getCurrentUser().getPrincipal());
        Instrument created = instrumentService.createInstrument(request, createdBy);
        return ResponseEntity.ok(BaseResponse.success(SERVICE_NAME, created));
    }

    // === READ ALL ===
    @GetMapping
    @PreAuthorize("hasAuthority('INSTRUMENT_READ')")
    public ResponseEntity<BaseResponse<List<Instrument>>> getAllInstruments() {
        List<Instrument> instruments = instrumentService.getInstruments();
        return ResponseEntity.ok(BaseResponse.success(SERVICE_NAME, instruments));
    }

    // === READ ONE ===
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('INSTRUMENT_READ')")
    public ResponseEntity<BaseResponse<?>> getInstrumentById(@PathVariable UUID id) {
        Instrument instrument = instrumentService.getInstrumentById(id);
        if (instrument == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(BaseResponse.error(SERVICE_NAME, "404", "Instrument not found"));
        }
        return ResponseEntity.ok(BaseResponse.success(SERVICE_NAME, instrument));
    }

    // === FILTER BY STATUS ===
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAuthority('INSTRUMENT_READ')")
    public ResponseEntity<BaseResponse<List<Instrument>>> getByStatus(@PathVariable Status status) {
        List<Instrument> instruments = instrumentService.getInstrumentsByStatus(status);
        return ResponseEntity.ok(BaseResponse.success(SERVICE_NAME, instruments));
    }

    // === FILTER BY CREATED TIME RANGE ===
    @GetMapping("/created-at")
    @PreAuthorize("hasAuthority('INSTRUMENT_READ')")
    public ResponseEntity<BaseResponse<List<Instrument>>> getByCreatedAtBetween(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime end) {

        List<Instrument> instruments = instrumentService.getInstrumentsByCreatedAtBetween(start, end);
        return ResponseEntity.ok(BaseResponse.success(SERVICE_NAME, instruments));
    }

    // === UPDATE STATUS (Generic) ===

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('INSTRUMENT_READ')")
    public ResponseEntity<BaseResponse<?>> updateStatus(
            @PathVariable UUID id,
            @RequestParam Status newStatus,
            @RequestParam(required = false) String reason) {
        UUID updatedBy = UUID.fromString((String) AuthUtils.getCurrentUser().getPrincipal());

        Instrument updated = instrumentService.editInstrumentStatus(id, newStatus, reason, updatedBy);
        if (updated == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(BaseResponse.error(SERVICE_NAME, "404", "Instrument not found"));
        }
        return ResponseEntity.ok(BaseResponse.success(SERVICE_NAME, updated));
    }

    // === ACTIVATE ===
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('INSTRUMENT_UPDATE')")
    public ResponseEntity<BaseResponse<?>> activateInstrument(@PathVariable UUID id, @RequestParam UUID updatedBy) {
        Instrument activated = instrumentService.activateInstrument(id, updatedBy);
        if (activated == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(BaseResponse.error(SERVICE_NAME, "404", "Instrument not found or cannot be activated"));
        }
        return ResponseEntity.ok(BaseResponse.success(SERVICE_NAME, activated));
    }

    // === DEACTIVATE ===
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('INSTRUMENT_UPDATE')")
    public ResponseEntity<BaseResponse<?>> deactivateInstrument(@PathVariable UUID id, @RequestParam UUID updatedBy) {
        Instrument deactivated = instrumentService.deactivateInstrument(id, updatedBy);
        if (deactivated == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(BaseResponse.error(SERVICE_NAME, "404", "Instrument not found or cannot be deactivated"));
        }
        return ResponseEntity.ok(BaseResponse.success(SERVICE_NAME, deactivated));
    }

    // === CHECK STATUS ===
    @GetMapping("/{id}/status")
    @PreAuthorize("hasAuthority('INSTRUMENT_READ')")
    public ResponseEntity<BaseResponse<String>> checkInstrumentStatus(@PathVariable UUID id) {
        String result = instrumentService.checkInstrumentStatus(id);
        return ResponseEntity.ok(BaseResponse.success(SERVICE_NAME, result));
    }

    // === DELETE AUTO (SYSTEM CLEANUP) ===
    @DeleteMapping("/auto-delete")
    public ResponseEntity<BaseResponse<String>> autoDeleteDeactivatedInstruments() {
        int deletedCount = instrumentService.autoDeleteDeactivatedInstruments();
        String msg = "Deleted " + deletedCount + " deactivated instruments older than 3 months";
        return ResponseEntity.ok(BaseResponse.success(SERVICE_NAME, msg));
    }
}
