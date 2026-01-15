package org.overcode250204.testorderservice.repositories;

import org.overcode250204.testorderservice.dtos.dashboard.OrdersOverTimeDTO;
import org.overcode250204.testorderservice.dtos.dashboard.OrdersTypeCountDTO;
import org.overcode250204.testorderservice.models.entites.TestOrders;
import org.overcode250204.testorderservice.models.enums.Gender;
import org.overcode250204.testorderservice.models.enums.TestOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TestOrdersRepository extends JpaRepository<TestOrders, UUID> {
    @Query("SELECT o FROM TestOrders o JOIN FETCH o.patient WHERE o.barCode IN :barcodes")
    List<TestOrders> findByBarCodeInWithPatient(@Param("barcodes") List<String> barcodes);

    Optional<TestOrders> findByBarCode(String barCode);

    List<TestOrders> findByStatus(TestOrderStatus status);

    @Query("SELECT t.id FROM TestOrders t WHERE t.barCode = :barCode")
    Optional<UUID> findTestOrderIdByBarCode(@Param("barCode") String barCode);

    Optional<TestOrders> findByIdAndIsDeletedFalse(UUID id);

    @Query("SELECT t FROM TestOrders t WHERE (:patientName IS NULL OR t.patient.fullName LIKE %:patientName%) " +
            "AND (:status IS NULL OR t.status = :status) AND t.isDeleted = false")
    Page<TestOrders> searchTestOrders(@Param("patientName") String patientName,
                                      @Param("status") TestOrderStatus status,
                                      Pageable pageable);

    @Query("SELECT p.gender FROM PatientReference p WHERE p.patientId = :patientId")
    Gender findPatientGenderByPatientId(@Param("patientId") UUID patientId);

    /**
     * Lấy tất cả TestOrders và "eagerly fetch" thông tin Patient liên quan.
     * Rất quan trọng cho bulk reindexing để tránh N+1 queries.
     */
    @Query("SELECT o FROM TestOrders o JOIN FETCH o.patient WHERE o.isDeleted = false")
    Page<TestOrders> findAllWithPatient(Pageable pageable);

    @Query(value = """
    SELECT TO_CHAR(t.created_at, 'YYYY-MM-DD') AS date,
           t.test_type AS type,
           COUNT(*) AS count
    FROM test_orders t
    WHERE t.is_deleted = false
      AND t.created_at >= CURRENT_DATE - (:days * INTERVAL '1 day')
    GROUP BY TO_CHAR(t.created_at, 'YYYY-MM-DD'), t.test_type
    ORDER BY TO_CHAR(t.created_at, 'YYYY-MM-DD')
    """, nativeQuery = true)
    List<Object[]> countOrdersOverTime(@Param("days") int days);


    @Query("SELECT new org.overcode250204.testorderservice.dtos.dashboard.OrdersTypeCountDTO(" +
            "t.testType, COUNT(t)) " +
            "FROM TestOrders t " +
            "WHERE t.isDeleted = false " +
            "GROUP BY t.testType")
    List<OrdersTypeCountDTO> countOrdersByType();

}