package org.overcode250204.testorderservice.controllers;

import lombok.RequiredArgsConstructor;

import org.overcode250204.testorderservice.grpc.SyncUpTestOrderCommandHandler;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test-orders")
@RequiredArgsConstructor
public class SyncController {

    private final SyncUpTestOrderCommandHandler commandHandler;

    @PostMapping("/{barcode}/sync")
    public ResponseEntity<String> syncTestOrder(@PathVariable String barcode) {
        commandHandler.handle(barcode);
        return ResponseEntity.ok("Sync-up request sent to Monitoring for barcode=" + barcode);
    }
}