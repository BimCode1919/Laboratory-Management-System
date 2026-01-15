package org.overcode250204.testorderservice.controllers;

import lombok.RequiredArgsConstructor;
import org.overcode250204.base.BaseResponse;
import org.overcode250204.testorderservice.dtos.PaymentDTO;
import org.overcode250204.testorderservice.mappers.PaymentMapper;
import org.overcode250204.testorderservice.models.entites.Payment;
import org.overcode250204.testorderservice.services.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final PaymentMapper paymentMapper;

    @PostMapping("/create/{testOrderId}")
    public ResponseEntity<BaseResponse<PaymentDTO>> createPayment(
            @PathVariable UUID testOrderId
    ) {
        Payment payment = paymentService.createPaymentForOrder(testOrderId);
        PaymentDTO dto = paymentMapper.toDTO(payment);
        return ResponseEntity.ok(BaseResponse.success("test-order-service", dto));
    }


    @GetMapping("/capture")
    public ResponseEntity<BaseResponse<PaymentDTO>> capturePayment(
            @RequestParam UUID paymentId,
            @RequestParam("token") String paypalOrderId
    ) {
        Payment payment = paymentService.capturePayment(paymentId, paypalOrderId);
        PaymentDTO dto = paymentMapper.toDTO(payment);
        return ResponseEntity.ok(BaseResponse.success("test-order-service", dto));
    }

    @PostMapping("/webhook")
    public ResponseEntity<BaseResponse<String>> handleWebhook(
            @RequestBody Map<String, Object> payload
    ) {
        paymentService.handlePayPalWebhook(payload);
        return ResponseEntity.ok(BaseResponse.success("test-order-service", "Webhook received"));
    }
}
