package org.overcode250204.testorderservice.repositories;

import org.overcode250204.testorderservice.dtos.TestResultTrendDTO;
import org.overcode250204.testorderservice.models.entites.TestResults;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface TestResultsRepository extends JpaRepository<TestResults, UUID> {
    List<TestResults> findByTestOrder_Id(UUID testOrderId);

    List<TestResults> findByTestOrder_IdIn(List<UUID> orderIds);

    List<TestResults> findByTestOrderId(UUID testOrderId);

    @Query(value = "SELECT new org.overcode250204.testorderservice.dtos.TestResultTrendDTO(" +
            "to.id, " +
            "to.createdAt, " +
            "tr.parameterName, " +
            "tr.resultValue, " +
            "tr.unit, " +
            "tr.referenceLow, " +
            "tr.referenceHigh, " +
            "tr.alertLevel) " +
            "FROM TestResults tr JOIN tr.testOrder to " +
            "WHERE to.patient.patientId = :patientId " +
            "AND tr.parameterName = :parameterName " +
            "AND to.createdAt >= :startTime " +
            "AND to.createdAt <= :endTime " +
            "ORDER BY to.createdAt ASC")
    List<TestResultTrendDTO> findTrendResultsByPatientId(
            @Param("patientId") UUID patientId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("parameterName") String parameterName
    );

    @Query("SELECT DISTINCT tr.parameterName " +
            "FROM TestResults tr JOIN tr.testOrder to " +
            "WHERE to.patient.patientCode = :patientCode " +
            "ORDER BY tr.parameterName ASC")
    List<String> findDistinctParameterNamesByPatientCode(@Param("patientCode") UUID patientCode);

    @Query(value = "SELECT new org.overcode250204.testorderservice.dtos.TestResultTrendDTO(" +
            "to.id, " +
            "to.createdAt, " +
            "tr.parameterName, " +
            "tr.resultValue, " +
            "tr.unit, " +
            "tr.referenceLow, " +
            "tr.referenceHigh, " +
            "tr.alertLevel) " +
            "FROM TestResults tr JOIN tr.testOrder to " +
            "WHERE to.patient.patientCode = :patientCode " +
            "AND tr.parameterName = :parameterName " +
            "AND to.createdAt >= :startTime " +
            "AND to.createdAt <= :endTime " +
            "ORDER BY to.createdAt ASC")
    List<TestResultTrendDTO> findTrendResultsByPatientCode(
            @Param("patientCode") UUID patientCode,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("parameterName") String parameterName
    );
}
