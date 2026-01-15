package org.overcode250204.instrumentservice.service.implement;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.overcode250204.instrumentservice.configs.ReagentConsumptionConfig;
import org.overcode250204.instrumentservice.dtos.AnalyzeCommand;
import org.overcode250204.instrumentservice.dtos.AnalysisResultDTO;
import org.overcode250204.instrumentservice.dtos.ReagentEstimateDTO;
import org.overcode250204.instrumentservice.dtos.ReagentDTO;
import org.overcode250204.instrumentservice.entity.*;
import org.overcode250204.instrumentservice.enums.InstrumentMode;
import org.overcode250204.instrumentservice.enums.InstrumentRunStatus;
import org.overcode250204.instrumentservice.enums.Status;
import org.overcode250204.instrumentservice.events.AnalysisEventPublisher;
import org.overcode250204.instrumentservice.events.SystemEventPublisher;
import org.overcode250204.instrumentservice.exception.ErrorCode;
import org.overcode250204.instrumentservice.exception.InstrumentException;
import org.overcode250204.instrumentservice.repository.InstrumentRepository;
import org.overcode250204.instrumentservice.repository.InstrumentRunRepository;
import org.overcode250204.instrumentservice.repository.PendingTestOrderRepository;
import org.overcode250204.instrumentservice.repository.RawTestResultRepository;
import org.overcode250204.instrumentservice.service.interfaces.BloodAnalysisService;
import org.overcode250204.instrumentservice.service.interfaces.EventLogService;
import org.overcode250204.instrumentservice.service.interfaces.ReagentService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class BloodAnalysisServiceImpl implements BloodAnalysisService {

    private final InstrumentRepository instrumentRepository;
    private final InstrumentRunRepository instrumentRunRepository;
    private final RawTestResultRepository rawTestResultRepository;
    private final ReagentService reagentService;
    private final EventLogService eventService;
    private final SystemEventPublisher systemEventPublisher;
    private final AnalysisEventPublisher analysisEventPublisher;
    private final ObjectMapper objectMapper;
    private final PendingTestOrderRepository pendingTestOrderRepository;
    private final ReagentConsumptionConfig reagentConfig;

    @Override
    @Transactional
    public AnalysisResultDTO analyze(UUID instrumentId, AnalyzeCommand req) throws JsonProcessingException {
        Instrument instrument = instrumentRepository.findById(instrumentId)
                .orElseThrow(() -> new InstrumentException(ErrorCode.INSTRUMENT_NOT_FOUND));

        if (instrument.getMode() == null || instrument.getMode() != InstrumentMode.READY) {
            throw new InstrumentException(ErrorCode.INSTRUMENT_NOT_READY);
        }

        List<String> barcodes = Optional.ofNullable(req.getBarcodes()).orElse(Collections.emptyList());
        if (barcodes.isEmpty()) {
            throw new InstrumentException(ErrorCode.BARCODE_LIST_NOT_FOUND);
        }

        for (String barcode : barcodes) {
            PendingTestOrder pendingTestOrder = Optional.ofNullable(pendingTestOrderRepository.findByBarCode(barcode))
                    .orElseThrow(() -> new InstrumentException(ErrorCode.BARCODE_NOT_FOUND ));

            if (pendingTestOrder.getStatus() == Status.PENDING) {
                pendingTestOrder.setStatus(Status.TESTED);
                pendingTestOrderRepository.save(pendingTestOrder);
            } else {
                log.warn("Test order for barcode '{}' is already in status '{}' and will not be updated.", barcode, pendingTestOrder.getStatus());
            }
        }

        int sampleCount = barcodes.size();
        log.info("Starting blood analysis on instrument {} with {} samples",
                instrument.getInstrumentCode(), sampleCount);


        if (!reagentService.hasSufficientReagent(instrumentId, req.getTestType(), sampleCount)) {
            eventService.logEvent(instrumentId, "REAGENT_LOW",
                    String.format("Insufficient reagent for test type %s with %d samples",
                            req.getTestType(), sampleCount),
                    req.getUserId());
            throw new InstrumentException(ErrorCode.REAGENT_LOW);
        }

        // create new run
        InstrumentRun run = new InstrumentRun();
        run.setRunId(UUID.randomUUID());
        run.setInstrumentId(instrumentId);
        run.setStatus(InstrumentRunStatus.RUNNING);
        run.setCreatedBy(req.getUserId());
        run.setStartTime(LocalDateTime.now());
        run.setTotalSamplesExpected(sampleCount);
        run.setReagentSnapshot(reagentService.snapshotReagent(instrumentId));
        instrumentRunRepository.save(run);

        // create queued RawTestResult and send Kafka request
        for (String barcode : req.getBarcodes()) {
            RawTestResult queued = new RawTestResult();
            queued.setRunId(run.getRunId());
            queued.setInstrumentId(instrumentId);
            queued.setBarcode(barcode);
            queued.setStatus("QUEUED");
            queued.setCreatedAt(LocalDateTime.now());
            queued.setTestType(req.getTestType());
            rawTestResultRepository.save(queued);

            Map<String, Object> payload = new HashMap<>();
            payload.put("instrumentId", instrumentId.toString());
            payload.put("runId", run.getRunId().toString());
            payload.put("barcode", barcode);
            payload.put("testType", req.getTestType());
            payload.put("userId", req.getUserId().toString());
            payload.put("timestamp", LocalDateTime.now().toString());
            payload.put("instrument", objectMapper.writeValueAsString(instrument));
            analysisEventPublisher.publishAnalysisRequest(instrumentId.toString(), payload);
            log.info("Sent analysis request for barcode {} to adapter", barcode);
        }

        // log run start
        eventService.logEvent(instrumentId, "RUN_STARTED",
                "Run started with ID " + run.getRunId(), req.getUserId());

        systemEventPublisher.publishMonitoringEvent("INSTRUMENT_RUN_STARTED", Map.of(
                "instrumentId", instrumentId.toString(),
                "runId", run.getRunId().toString(),
                "sampleCount", sampleCount
        ));

        // return minimal info, result will be updated asynchronously by listener
        return AnalysisResultDTO.builder()
                .runId(run.getRunId())
                .instrumentId(instrumentId)
                .instrumentName(instrument.getName())
                .status(run.getStatus())
                .totalSamplesExpected(run.getTotalSamplesExpected())
                .startTime(run.getStartTime())
                .createdBy(run.getCreatedBy())
                .reagentSnapshot(run.getReagentSnapshot())
                .results(Collections.emptyList())
                .build();
    }

    @Override
    @Transactional
    public void finishRun(UUID runId, UUID userId) {
        InstrumentRun run = instrumentRunRepository.findByRunId(runId)
                .orElseThrow(() -> new InstrumentException(ErrorCode.RUN_NOT_FOUND));

        run.setStatus(InstrumentRunStatus.COMPLETED);
        run.setEndTime(LocalDateTime.now());
        instrumentRunRepository.save(run);

        eventService.logEvent(run.getInstrumentId(), "RUN_FINISHED",
                "Run " + runId + " marked finished manually", userId);
    }

    @Override
    public AnalysisResultDTO getRun(UUID runId) {
        InstrumentRun run = instrumentRunRepository.findByRunId(runId)
                .orElseThrow(() -> new InstrumentException(ErrorCode.RUN_NOT_FOUND));

        Instrument instrument = instrumentRepository.findById(run.getInstrumentId())
                .orElseThrow(() -> new InstrumentException(ErrorCode.INSTRUMENT_NOT_FOUND));

        List<RawTestResult> rawResults = rawTestResultRepository.findByRunId(runId);
        List<AnalysisResultDTO.RawSampleResult> results = rawResults.stream()
                .map(r -> new AnalysisResultDTO.RawSampleResult(r.getBarcode(), r.getStatus(), r.getRawData()))
                .toList();

        return AnalysisResultDTO.builder()
                .runId(run.getRunId())
                .instrumentId(run.getInstrumentId())
                .instrumentName(instrument.getName())
                .status(run.getStatus())
                .startTime(run.getStartTime())
                .endTime(run.getEndTime())
                .successfulSamples(run.getSuccessfulSamples())
                .failedSamples(run.getFailedSamples())
                .totalSamplesExpected(run.getTotalSamplesExpected())
                .reagentSnapshot(run.getReagentSnapshot())
                .createdBy(run.getCreatedBy())
                .results(results)
                .build();
    }

    @Override
    public List<AnalysisResultDTO> getRunsByInstrument(UUID instrumentId) {
        return instrumentRunRepository.findByInstrumentId(instrumentId).stream()
                .map(run -> AnalysisResultDTO.builder()
                        .runId(run.getRunId())
                        .instrumentId(instrumentId)
                        .status(run.getStatus())
                        .startTime(run.getStartTime())
                        .endTime(run.getEndTime())
                        .successfulSamples(run.getSuccessfulSamples())
                        .failedSamples(run.getFailedSamples())
                        .totalSamplesExpected(run.getTotalSamplesExpected())
                        .build())
                .toList();
    }

    @Override
    public ReagentEstimateDTO estimateReagentRequirement(UUID instrumentId, AnalyzeCommand req) {
        // Check installed reagents for expiration and log warnings (non-blocking)
        List<ReagentDTO> installedForCheck = reagentService.getInstalledReagents(instrumentId);
        installedForCheck.stream()
                .filter(r -> r.getExpirationDate() != null && r.getExpirationDate().isBefore(java.time.LocalDate.now()))
                .forEach(r -> log.warn("Installed reagent expired: reagentId={} name={} expirationDate={} instrument={}", r.getReagentId(), r.getReagentName(), r.getExpirationDate(), instrumentId));

        int samples;
        if (req.getExpectedSamples() != null && req.getExpectedSamples() > 0) {
            samples = req.getExpectedSamples();
        } else if (req.getBarcodes() != null && !req.getBarcodes().isEmpty()) {
            samples = req.getBarcodes().size();
        } else {
            samples = 0;
        }

        Map<String, Double> consumptionMap = reagentConfig.getConsumption().getOrDefault(req.getTestType().toUpperCase(), Map.of());

        Map<String, Double> requiredPerReagent = new HashMap<>();
        for (Map.Entry<String, Double> e : consumptionMap.entrySet()) {
            requiredPerReagent.put(e.getKey(), e.getValue() * samples);
        }

        List<ReagentDTO> installed = reagentService.getInstalledReagents(instrumentId);

        // build installed details (all batches) and compute available quantities from batches that are marked inUse
        Map<String, List<ReagentEstimateDTO.InstalledReagentDetail>> installedDetailsPerReagent = new HashMap<>();
        Map<String, Double> availablePerReagent = new HashMap<>();

        for (ReagentDTO r : installed) {
            String name = r.getReagentName();
            Double qty = Optional.ofNullable(r.getQuantityRemaining()).orElse(0.0);

            java.time.LocalDate expDate = r.getExpirationDate();
            boolean expiredFlag = expDate != null && expDate.isBefore(java.time.LocalDate.now());
            installedDetailsPerReagent.computeIfAbsent(name, k -> new ArrayList<>())
                    .add(new ReagentEstimateDTO.InstalledReagentDetail(r.getLotNumber(), qty, r.isInUse(), r.getUnit(), expDate, expiredFlag));

            if (r.isInUse()) {
                availablePerReagent.merge(name, qty, Double::sum);
            }
        }

        Map<String, Double> shortfallPerReagent = new HashMap<>();
        Map<String, Double> runsLeftPerReagent = new HashMap<>();

        int estimatedRunsPossible = Integer.MAX_VALUE;
        for (Map.Entry<String, Double> reqEntry : requiredPerReagent.entrySet()) {
            String reagentName = reqEntry.getKey();
            Double required = reqEntry.getValue();
            Double available = availablePerReagent.getOrDefault(reagentName, 0.0);
            double shortfall = Math.max(0.0, required - available);
            shortfallPerReagent.put(reagentName, shortfall);

            double perSample = consumptionMap.get(reagentName);
            double runsLeft = (perSample > 0) ? (available / perSample) : Double.POSITIVE_INFINITY;
            runsLeftPerReagent.put(reagentName, runsLeft);

            int runsLeftInt = (perSample > 0) ? (int) Math.floor(runsLeft) : Integer.MAX_VALUE;
            estimatedRunsPossible = Math.min(estimatedRunsPossible, runsLeftInt);
        }

        boolean sufficient = shortfallPerReagent.values().stream().allMatch(v -> v <= 0.0);
        if (estimatedRunsPossible == Integer.MAX_VALUE) estimatedRunsPossible = 0;

        return ReagentEstimateDTO.builder()
                .sufficient(sufficient)
                .samplesRequested(samples)
                .requiredPerReagent(requiredPerReagent)
                .availablePerReagent(availablePerReagent)
                .shortfallPerReagent(shortfallPerReagent)
                .runsLeftPerReagent(runsLeftPerReagent)
                .estimatedRunsPossible(estimatedRunsPossible)
                .installedDetailsPerReagent(installedDetailsPerReagent)
                .build();
    }
}
