package org.overcode250204.testorderservice.mappers;

import org.overcode250204.testorderservice.dtos.PaymentDTO;
import org.overcode250204.testorderservice.models.entites.Payment;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {

    public PaymentDTO toDTO(Payment p) {
        if (p == null) return null;

        PaymentDTO dto = new PaymentDTO();
        dto.setId(p.getId());
        dto.setOrderId(p.getTestOrder().getId());
        dto.setAmount(p.getAmount());
        dto.setProvider(p.getProvider().name());
        dto.setStatus(p.getStatus().name());
        dto.setApprovalUrl(p.getApprovalUrl());
        dto.setTransactionId(p.getTransactionId());

        return dto;
    }
}
