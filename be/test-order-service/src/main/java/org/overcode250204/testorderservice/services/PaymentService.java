package org.overcode250204.testorderservice.services;

import org.overcode250204.testorderservice.models.entites.Payment;

import java.util.Map;
import java.util.UUID;

public interface PaymentService {

    Payment createPaymentForOrder(UUID testOrderId);

    Payment capturePayment(UUID paymentId, String paypalOrderId);

    Payment handlePayPalWebhook(Map<String, Object> webhookEvent);
}

