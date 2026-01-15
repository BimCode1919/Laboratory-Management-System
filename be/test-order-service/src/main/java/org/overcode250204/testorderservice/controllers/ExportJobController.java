package org.overcode250204.testorderservice.controllers;

import lombok.RequiredArgsConstructor;
import org.overcode250204.base.BaseResponse;
import org.overcode250204.testorderservice.services.ExportJobService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/export")
public class ExportJobController {
    private final ExportJobService exportJobService;

    @GetMapping("/csv/{testOrderId}")
    public ResponseEntity<BaseResponse<String>> exportCsv(@PathVariable UUID testOrderId) {
        String key = exportJobService.writeCsv(testOrderId);
        String url = exportJobService.generatePresignedUrl(key);
        return ResponseEntity.ok(BaseResponse.success("test-order-service",url));
    }

    @GetMapping("/excel/{testOrderId}")
    public ResponseEntity<BaseResponse<String>> exportExcel(@PathVariable UUID testOrderId) {
        String key = exportJobService.writeExcel(testOrderId);
        String url = exportJobService.generatePresignedUrl(key);
        return ResponseEntity.ok(BaseResponse.success("test-order-service", url));
    }

    @GetMapping("/pdf/{testOrderId}")
    public ResponseEntity<BaseResponse<String>> exportPdf(@PathVariable UUID testOrderId) {
        String key = exportJobService.writePdf(testOrderId);
        String url = exportJobService.generatePresignedUrl(key);
        return ResponseEntity.ok(BaseResponse.success("test-order-service", url));
    }

}
