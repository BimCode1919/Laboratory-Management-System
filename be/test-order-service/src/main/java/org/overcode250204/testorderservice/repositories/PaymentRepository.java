package org.overcode250204.testorderservice.repositories;

import org.overcode250204.testorderservice.models.entites.Payment;
import org.overcode250204.testorderservice.models.entites.TestOrders;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Payment findByTestOrder(TestOrders testOrder);

    Payment findByTransactionId(String transactionId);
}
