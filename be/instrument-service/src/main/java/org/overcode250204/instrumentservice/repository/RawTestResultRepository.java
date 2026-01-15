package org.overcode250204.instrumentservice.repository;

import org.overcode250204.instrumentservice.dtos.dashboard.InstrumentTestCountDTO;
import org.overcode250204.instrumentservice.dtos.dashboard.TestOverTimeDTO;
import org.overcode250204.instrumentservice.entity.RawTestResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RawTestResultRepository extends MongoRepository<RawTestResult, String> {

    List<RawTestResult> findByRunId(UUID runId);

    Optional<RawTestResult> findByRunIdAndBarcode(UUID runId, String barcode);

    Optional<RawTestResult> findByBarcode(String barcode);

    Page<RawTestResult> findByInstrumentId(UUID instrumentId, Pageable pageable);

    long countByBackedUpTrue();

    @Query(value = "{}", sort = "{createdAt: -1}")
    List<RawTestResult> findTopN(int limit);

    @Query("{ 'backedUp': true }")
    @Modifying
    void deleteAllByBackedUpTrue();

    Page<RawTestResult> findAllByBackedUpTrue(boolean backedUp, Pageable pageable);

    Page<RawTestResult> findAllByBackedUpFalse(boolean backedUp, Pageable pageable);

    Page<RawTestResult> findByInstrumentIdAndBackedUpTrue(UUID instrumentId, boolean backedUp, Pageable pageable);

    Page<RawTestResult> findByInstrumentIdAndBackedUpFalse(UUID instrumentId, boolean backedUp, Pageable pageable);

    @Aggregation(pipeline = {
            "{ $group: { _id: '$instrumentId', testCount: { $sum: 1 } } }",
            "{ $project: { instrumentId: '$_id', testCount: 1, _id: 0 } }"
    })
    List<InstrumentTestCountDTO> countTestsPerInstrument();

    @Aggregation(pipeline = {
            "{ $match: { createdAt: { $gte: ?0 } } }",
            "{ $group: { _id: { $dateToString: { format: '%Y-%m-%d', date: '$createdAt' } }, testCount: { $sum: 1 } } }",
            "{ $sort: { _id: 1 } }",
            "{ $project: { date: '$_id', testCount: 1, _id: 0 } }"
    })
    List<TestOverTimeDTO> countTestsOverTime(java.time.LocalDateTime startDate);

}
