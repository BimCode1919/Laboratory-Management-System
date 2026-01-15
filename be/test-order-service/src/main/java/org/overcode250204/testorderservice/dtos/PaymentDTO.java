package org.overcode250204.testorderservice.dtos;
import lombok.*;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {
    private UUID id;
    private UUID orderId;
    private Double amount;
    private String provider;
    private String status;
    private String approvalUrl;
    private String transactionId;
}
