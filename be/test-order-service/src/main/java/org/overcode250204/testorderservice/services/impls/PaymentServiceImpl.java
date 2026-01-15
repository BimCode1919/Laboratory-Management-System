package org.overcode250204.testorderservice.services.impls;

import com.paypal.orders.LinkDescription;
import com.paypal.orders.Order;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.overcode250204.testorderservice.exceptions.ErrorCode;
import org.overcode250204.testorderservice.exceptions.TestOrderException;
import org.overcode250204.testorderservice.models.entites.Payment;
import org.overcode250204.testorderservice.models.entites.TestOrders;
import org.overcode250204.testorderservice.models.entites.TestPricing;
import org.overcode250204.testorderservice.models.enums.PaymentProvider;
import org.overcode250204.testorderservice.models.enums.PaymentStatus;
import org.overcode250204.testorderservice.models.enums.TestOrderStatus;
import org.overcode250204.testorderservice.payment.PayPalClient;
import org.overcode250204.testorderservice.repositories.PaymentRepository;
import org.overcode250204.testorderservice.repositories.TestOrdersRepository;
import org.overcode250204.testorderservice.repositories.TestPricingRepository;
import org.overcode250204.testorderservice.services.PaymentService;
import org.overcode250204.testorderservice.utils.AuthUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final TestOrdersRepository testOrdersRepository;
    private final PaymentRepository paymentRepository;
    private final TestPricingRepository testPricingRepository;
    private final PayPalClient payPalClient;

    @Value("${paypal.return-url}")
    private String returnUrl;

    @Value("${paypal.cancel-url}")
    private String cancelUrl;

    @Value("${paypal.rate-vnd-usd}")
    private Double rateVndUsd;


    @Override
    @Transactional
    public Payment createPaymentForOrder(UUID testOrderId) {

        TestOrders testOrder = testOrdersRepository.findById(testOrderId)
                .orElseThrow(() -> new TestOrderException(ErrorCode.TEST_NOT_FOUND));

        // VALIDATION: chỉ cho phép PENDING thanh toán
        if (testOrder.getStatus() != TestOrderStatus.PENDING) {
            throw new TestOrderException(ErrorCode.FAIL_TO_CREAT_TEST_ORDER);
        }

        Payment exist = paymentRepository.findByTestOrder(testOrder);
        if (exist != null && exist.getStatus() != PaymentStatus.FAILED) {
            throw new TestOrderException(ErrorCode.PAYMENT_ALREADY_EXISTS);
        }

        TestPricing pricing = testPricingRepository.findByTestType(testOrder.getTestType())
                .orElseThrow(() -> new TestOrderException(ErrorCode.PRICING_NOT_FOUND));

        Double amount = pricing.getPrice();
        if (amount == null || amount <= 0) {
            throw new TestOrderException(ErrorCode.INVALID_PRICE);
        }

        try {
            // CALL PAYPAL CREATE
            Double amountVnd = amount;
            Double amountUsd = Math.round((amountVnd / rateVndUsd) * 100.0) / 100.0;

            Order paypalOrder = payPalClient.createOrder(
                    amountUsd,
                    returnUrl,
                    cancelUrl
            );

            String approvalUrl = null;
            for (LinkDescription link : paypalOrder.links()) {
                if ("approve".equalsIgnoreCase(link.rel())) {
                    approvalUrl = link.href();
                    break;
                }
            }

            Payment payment = Payment.builder()
                    .testOrder(testOrder)
                    .amount(amount)
                    .provider(PaymentProvider.PAYPAL)
                    .status(PaymentStatus.PENDING)
                    .approvalUrl(approvalUrl)
                    .transactionId(paypalOrder.id())
                    .build();

            log.info("[PAYMENT] Created payment {} for order {}", payment.getId(), testOrderId);

            return paymentRepository.save(payment);

        } catch (IOException e) {
            log.error("[PAYMENT] Failed to create payment for order {}: {}", testOrderId, e.getMessage());
            throw new TestOrderException(ErrorCode.PAYMENT_CREATE_FAILED);
        }
    }


    @Override
    @Transactional
    public Payment capturePayment(UUID paymentId, String paypalOrderId) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new TestOrderException(ErrorCode.PAYMENT_NOT_FOUND));

        if (!payment.getTransactionId().equals(paypalOrderId)) {
            throw new TestOrderException(ErrorCode.PAYMENT_INVALID_ORDER);
        }


        if (payment.getStatus() == PaymentStatus.PAID) {
            log.warn("[PAYMENT] Capture called again for already PAID payment {}", paymentId);
            return payment;
        }

        try {
            Order result = payPalClient.captureOrder(paypalOrderId);

            // VERIFY AMOUNT
            Double paidAmount = extractPayPalPaidAmount(result);
            Double expectedUsd = Math.round((payment.getAmount() / rateVndUsd) * 100.0) / 100.0;

            if (!paidAmount.equals(expectedUsd)) {
                throw new TestOrderException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
            }

            payment.setStatus(PaymentStatus.PAID);
            payment.setTransactionId(result.id());

            log.info("[PAYMENT] Capture success for payment {}", paymentId);

            return paymentRepository.save(payment);

        } catch (Exception e) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);

            log.error("[PAYMENT] Capture FAILED for payment {}: {}", paymentId, e.getMessage());

            throw new TestOrderException(ErrorCode.PAYMENT_CAPTURE_FAILED);
        }
    }

    // Helper — extract amount from PayPal capture result
    private Double extractPayPalPaidAmount(Order result) {
        try {
            return Double.valueOf(
                    result.purchaseUnits().get(0)
                            .payments()
                            .captures().get(0)
                            .amount()
                            .value()
            );
        } catch (Exception e) {
            throw new TestOrderException(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
    }


    @SuppressWarnings("unchecked")
    @Override
    public Payment handlePayPalWebhook(Map<String, Object> event) {

        String eventType = (String) event.get("event_type");
        if (!"CHECKOUT.ORDER.APPROVED".equals(eventType) &&
                !"CHECKOUT.ORDER.COMPLETED".equals(eventType)
        ){
            throw new TestOrderException(ErrorCode.WEBHOOK_INVALID_EVENT);
        }

        // resource
        Map<String, Object> resource = (Map<String, Object>) event.get("resource");
        if (resource == null) {
            throw new TestOrderException(ErrorCode.WEBHOOK_MISSING_ORDER_ID);
        }

        // captureId
        String captureId = (String) resource.get("id");

        // supplementary_data
        Map<String, Object> supplementary =
                (Map<String, Object>) resource.get("supplementary_data");

        if (supplementary == null) {
            throw new TestOrderException(ErrorCode.WEBHOOK_MISSING_ORDER_ID);
        }

        // related_ids
        Map<String, Object> relatedIds =
                (Map<String, Object>) supplementary.get("related_ids");

        if (relatedIds == null) {
            throw new TestOrderException(ErrorCode.WEBHOOK_MISSING_ORDER_ID);
        }

        // orderId
        String orderId = (String) relatedIds.get("order_id");
        if (orderId == null) {
            throw new TestOrderException(ErrorCode.WEBHOOK_MISSING_ORDER_ID);
        }

        // tìm payment theo PayPal orderId
        Payment payment = paymentRepository.findByTransactionId(orderId);
        if (payment == null) {
            throw new TestOrderException(ErrorCode.PAYMENT_NOT_FOUND);
        }

        // idempotency
        if (payment.getStatus() == PaymentStatus.PAID) {
            return payment;
        }

        // update
        payment.setStatus(PaymentStatus.PAID);
        payment.setTransactionId(captureId);

        return paymentRepository.save(payment);
    }

}




