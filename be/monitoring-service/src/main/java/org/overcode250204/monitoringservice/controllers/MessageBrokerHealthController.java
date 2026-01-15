package org.overcode250204.monitoringservice.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.overcode250204.base.BaseResponse;
import org.overcode250204.monitoringservice.dtos.MessageBrokerHealthDTO;
import org.overcode250204.monitoringservice.entities.MessageBrokerHealth;
import org.overcode250204.monitoringservice.enums.MessageBrokerHealthStatus;
import org.overcode250204.monitoringservice.services.MessageBrokerHealthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Message Broker Health APIs", description = "APIs to monitor message broker health")
@RestController
@RequestMapping("/message-broker-health")
@RequiredArgsConstructor
public class MessageBrokerHealthController {

    private final MessageBrokerHealthService service;
    private static final String SERVICE_NAME = "monitoring-service";

    @Operation(summary = "Get all message broker health records with pagination")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Get list success")
    })
    @GetMapping
    public ResponseEntity<BaseResponse<Map<String, Object>>> getAll(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortField,
            @RequestParam(defaultValue = "desc") String sortOrder
    ) {
        Map<String, Object> data = service.getAllMessageBrokerHealths(page, size, sortField, sortOrder);
        return ResponseEntity.ok(BaseResponse.success(SERVICE_NAME, data));
    }

    @Operation(summary = "Get message broker health by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found broker health record"),
            @ApiResponse(responseCode = "404", description = "Record not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse<MessageBrokerHealthDTO>> getById(@PathVariable String id) {
        MessageBrokerHealth record = service.getMessageBrokerHealthById(id);
        if (record == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body((BaseResponse<MessageBrokerHealthDTO>) (BaseResponse<?>)
                            BaseResponse.error(SERVICE_NAME, "404", "Record not found"));
        }
        MessageBrokerHealthDTO dto = MessageBrokerHealthDTO.fromEntity(record);
        return ResponseEntity.ok(BaseResponse.success(SERVICE_NAME, dto));
    }


    @Operation(summary = "Get message broker health by broker name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found records by broker name")
    })
    @GetMapping("/broker/{brokerName}")
    public ResponseEntity<BaseResponse<Map<String, Object>>> getByBrokerName(
            @PathVariable String brokerName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Map<String, Object> data = service.getMessageBrokerHealthByBrokerName(brokerName, page, size);
        return ResponseEntity.ok(BaseResponse.success(SERVICE_NAME, data));
    }

    @Operation(summary = "Get message broker health by status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Found records by status"),
            @ApiResponse(responseCode = "400", description = "Invalid status")
    })
    @GetMapping("/status/{status}")
    public ResponseEntity<BaseResponse<Map<String, Object>>> getByStatus(
            @PathVariable String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        MessageBrokerHealthStatus parsedStatus;
        try {
            parsedStatus = MessageBrokerHealthStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body((BaseResponse<Map<String, Object>>) (BaseResponse<?>)
                            BaseResponse.error(SERVICE_NAME, "400", "Invalid status value"));
        }

        Map<String, Object> data = service.getMessageBrokerHealthByStatus(parsedStatus, page, size);
        return ResponseEntity.ok(BaseResponse.success(SERVICE_NAME, data));
    }


    @Operation(summary = "Create new message broker health record")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Create success")
    })
    @PostMapping("/create")
    public ResponseEntity<BaseResponse<MessageBrokerHealthDTO>> create(
            @RequestBody MessageBrokerHealthDTO request
    ) {
        MessageBrokerHealth created = service.createMessageBrokerHealth(request);
        MessageBrokerHealthDTO dto = MessageBrokerHealthDTO.fromEntity(created);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(BaseResponse.of(SERVICE_NAME, "201", "Message broker health created successfully", dto));
    }

    @Operation(summary = "Update message broker health record")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Update success"),
            @ApiResponse(responseCode = "404", description = "Record not found")
    })
    @PutMapping("/update/{id}")
    public ResponseEntity<BaseResponse<MessageBrokerHealthDTO>> update(
            @PathVariable String id,
            @RequestBody MessageBrokerHealthDTO request
    ) {
        MessageBrokerHealth updated = service.updateMessageBrokerHealth(id, request);
        if (updated == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body((BaseResponse<MessageBrokerHealthDTO>) (BaseResponse<?>)
                            BaseResponse.error(SERVICE_NAME, "404", "Record not found"));
        }
        MessageBrokerHealthDTO dto = MessageBrokerHealthDTO.fromEntity(updated);
        return ResponseEntity.ok(BaseResponse.of(SERVICE_NAME, "200", "Message broker health updated successfully", dto));
    }


    @Operation(summary = "Delete message broker health by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Delete success")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse<Void>> deleteById(@PathVariable String id) {
        service.deleteMessageBrokerHealthById(id);
        return ResponseEntity.ok(BaseResponse.of(SERVICE_NAME, "200", "Message broker health deleted successfully", null));
    }
}
