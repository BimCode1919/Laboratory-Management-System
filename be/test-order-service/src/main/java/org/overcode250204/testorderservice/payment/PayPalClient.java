package org.overcode250204.testorderservice.payment;

import com.paypal.core.PayPalHttpClient;
import com.paypal.http.HttpResponse;
import com.paypal.orders.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

@Component
public class PayPalClient {

    private final PayPalHttpClient client;

    public PayPalClient(@Qualifier("paypalHttpClient") PayPalHttpClient client) {
        this.client = client;
    }

    // Tạo order PayPal
    public Order createOrder(Double amount, String returnUrl, String cancelUrl) throws IOException {

        // Build order request
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.checkoutPaymentIntent("CAPTURE");

        AmountWithBreakdown amt = new AmountWithBreakdown()
                .currencyCode("USD")
                .value(String.format(Locale.US, "%.2f", amount));

        PurchaseUnitRequest purchaseUnit = new PurchaseUnitRequest()
                .amountWithBreakdown(amt);

        ApplicationContext appContext = new ApplicationContext()
                .returnUrl(returnUrl)
                .cancelUrl(cancelUrl);

        orderRequest.purchaseUnits(List.of(purchaseUnit));
        orderRequest.applicationContext(appContext);

        // PayPal API request
        OrdersCreateRequest request = new OrdersCreateRequest();
        request.prefer("return=representation");
        request.requestBody(orderRequest);

        HttpResponse<Order> response = client.execute(request);
        return response.result();
    }

    // Capture thanh toán
    public Order captureOrder(String orderId) throws IOException {
        OrdersCaptureRequest capture = new OrdersCaptureRequest(orderId);
        capture.requestBody(new OrderRequest());

        HttpResponse<Order> response = client.execute(capture);
        return response.result();
    }
}
