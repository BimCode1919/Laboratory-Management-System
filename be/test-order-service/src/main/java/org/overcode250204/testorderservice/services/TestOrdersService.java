package org.overcode250204.testorderservice.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.overcode250204.testorderservice.dtos.TestOrderDTO;
import org.overcode250204.testorderservice.models.entites.TestOrders;
import org.overcode250204.testorderservice.models.enums.TestOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

public interface TestOrdersService {
    TestOrderDTO createTestOrder(TestOrderDTO request, String createdByLabUser) throws JsonProcessingException;

    TestOrderDTO updateTestOrder(UUID id, TestOrderDTO request, String updatedByLabUser);

    void deleteTestOrder(UUID id, String deletedByLabUser);

    Page<TestOrderDTO> getAllTestOrders(String patientName, TestOrderStatus status, Pageable pageable);

    Optional<TestOrderDTO> getTestOrderDetail(UUID id);
}