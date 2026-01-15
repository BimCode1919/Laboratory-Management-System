package org.overcode250204.testorderservice.repositories;

import org.aspectj.weaver.ast.Test;
import org.overcode250204.testorderservice.models.entites.TestComments;
import org.overcode250204.testorderservice.models.entites.TestOrders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TestCommentsRepository extends JpaRepository<TestComments, UUID> {
    // Lấy comment theo ID và chưa bị xóa
    Optional<TestComments> findByIdAndIsDeletedFalse(UUID id);

    List<TestComments> findAllByTestOrder_Id(UUID testOrderId);

    List<TestComments> findAllByTestOrder_IdAndIsDeletedFalse(UUID testOrderId);
}