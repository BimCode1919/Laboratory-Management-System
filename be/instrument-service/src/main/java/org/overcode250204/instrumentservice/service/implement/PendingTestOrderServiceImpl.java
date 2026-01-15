package org.overcode250204.instrumentservice.service.implement;

import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.overcode250204.instrumentservice.dtos.PendingTestOrderCheckResponse;
import org.overcode250204.instrumentservice.entity.*;
import org.overcode250204.instrumentservice.enums.Priority;
import org.overcode250204.instrumentservice.enums.Status;
import org.overcode250204.instrumentservice.repository.InstrumentRepository;
import org.overcode250204.instrumentservice.repository.PendingTestOrderRepository;
import org.overcode250204.instrumentservice.service.interfaces.PendingTestOrderService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PendingTestOrderServiceImpl implements PendingTestOrderService {

    private final PendingTestOrderRepository repository;
    private final InstrumentRepository instrumentRepository;

    @Override
    public Page<PendingTestOrder> findAll(String type, Status status, Priority priority, Pageable pageable) {
        Specification<PendingTestOrder> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(type)) {
                predicates.add(cb.equal(root.get("testType"), type));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (priority != null) {
                predicates.add(cb.equal(root.get("priority"), priority));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return repository.findAll(spec, pageable);
    }

    @Override
    public PendingTestOrderCheckResponse checkByBarCodeAndInstrumentId(String barCode, UUID instrumentId) {
        if (barCode == null || barCode.isBlank()) {
            return PendingTestOrderCheckResponse.builder()
                    .exists(false)
                    .isPending(false)
                    .instrumentExists(false)
                    .hasMatchingConfig(false)
                    .testType(null)
                    .build();
        }

        PendingTestOrder order = repository.findByBarCode(barCode);
        boolean exists = order != null;
        boolean isPending = exists && order.getStatus() == Status.PENDING;

        boolean instrumentExists = false;
        boolean hasMatchingConfig = false;

        // guard against null order
        String testType = exists ? order.getTestType() : null;

        if (instrumentId != null) {
            Instrument instrument = instrumentRepository.findById(instrumentId).orElse(null);
            instrumentExists = instrument != null;
            if (instrumentExists && exists && testType != null) {
                List<InstrumentConfiguration> configs = instrument.getConfigurations();
                if (configs != null) {
                    for (InstrumentConfiguration cfg : configs) {
                        if (cfg != null && cfg.getConfigKey() != null && cfg.getConfigKey().equals(testType)) {
                            hasMatchingConfig = true;
                            break;
                        }
                    }
                }
            }
        }

        return PendingTestOrderCheckResponse.builder()
                .exists(exists)
                .isPending(isPending)
                .instrumentExists(instrumentExists)
                .hasMatchingConfig(hasMatchingConfig)
                .testType(testType)
                .build();
    }
}