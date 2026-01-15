package org.overcode250204.warehouseservice.grpc.impls;

import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.overcode250204.common.grpc.*;
import org.overcode250204.warehouseservice.grpc.mapper.ReagentMapper;
import org.overcode250204.warehouseservice.grpc.mapper.InstrumentMapper;
import org.overcode250204.warehouseservice.model.entities.Reagent;
import org.overcode250204.warehouseservice.model.entities.ReagentSupplyHistory;
import org.overcode250204.warehouseservice.repositories.ReagentSupplyHistoryRepository;
import org.overcode250204.warehouseservice.repositories.ReagentUsageHistoryRepository;
import org.overcode250204.warehouseservice.repositories.ReagentsRepository;
import org.overcode250204.warehouseservice.repositories.InstrumentsRepository;
import org.overcode250204.warehouseservice.model.entities.Instrument;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class WarehouseGrpcServiceImpl extends WarehouseServiceGrpc.WarehouseServiceImplBase {

    private final ReagentsRepository reagentsRepository;
    private final ReagentSupplyHistoryRepository supplyHistoryRepository;
    private final ReagentUsageHistoryRepository usageHistoryRepository;
    private final InstrumentsRepository instrumentsRepository;

    @Override
    public void getReagent(ReagentRequest request, StreamObserver<ReagentInfo> responseObserver) {
        try {
            log.info("Warehouse gRPC: GetReagent {}", request.getReagentId());
            UUID id = UUID.fromString(request.getReagentId());
            Reagent reagent = reagentsRepository.findById(id).orElse(null);
            if (reagent == null) {
                responseObserver.onError(Status.NOT_FOUND.withDescription("Reagent not found").asRuntimeException());
                return;
            }

            BigDecimal totalSupply = supplyHistoryRepository.calculateTotalSupplyByReagentId(reagent.getReagentId());
            BigDecimal totalUsage = usageHistoryRepository.calculateTotalUsageByReagentId(reagent.getReagentId());
            if (totalSupply == null) totalSupply = BigDecimal.ZERO;
            if (totalUsage == null) totalUsage = BigDecimal.ZERO;
            BigDecimal available = totalSupply.subtract(totalUsage);

            // Determine earliest expiration date among supply history for this reagent (if any)
            List<ReagentSupplyHistory> supplies = supplyHistoryRepository.findByReagent_ReagentId(reagent.getReagentId());
            LocalDate expirationDate = (supplies == null || supplies.isEmpty()) ? null :
                    supplies.stream().map(ReagentSupplyHistory::getExpirationDate).min(Comparator.naturalOrder()).orElse(null);

            ReagentInfo info = ReagentMapper.toGrpc(reagent, available, totalSupply, expirationDate);
            responseObserver.onNext(info);
            responseObserver.onCompleted();
        } catch (IllegalArgumentException ex) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Invalid UUID").asRuntimeException());
        } catch (Exception ex) {
            log.error("Error in getReagent", ex);
            responseObserver.onError(Status.INTERNAL.withDescription(ex.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void listReagents(ListReagentsRequest request, StreamObserver<ListReagentsResponse> responseObserver) {
        try {
            log.info("Warehouse gRPC: ListReagents page={} size={}", request.getPage(), request.getSize());
            List<Reagent> all = reagentsRepository.findAll();
            List<ReagentInfo> items = all.stream().map(r -> {
                BigDecimal totalSupply = supplyHistoryRepository.calculateTotalSupplyByReagentId(r.getReagentId());
                BigDecimal totalUsage = usageHistoryRepository.calculateTotalUsageByReagentId(r.getReagentId());
                if (totalSupply == null) totalSupply = BigDecimal.ZERO;
                if (totalUsage == null) totalUsage = BigDecimal.ZERO;
                BigDecimal available = totalSupply.subtract(totalUsage);
                List<ReagentSupplyHistory> supplies = supplyHistoryRepository.findByReagent_ReagentId(r.getReagentId());
                LocalDate expirationDate = (supplies == null || supplies.isEmpty()) ? null :
                        supplies.stream().map(ReagentSupplyHistory::getExpirationDate).min(Comparator.naturalOrder()).orElse(null);
                return ReagentMapper.toGrpc(r, available, totalSupply, expirationDate);
            }).collect(Collectors.toList());
            ListReagentsResponse resp = ListReagentsResponse.newBuilder().addAllReagents(items).build();
            responseObserver.onNext(resp);
            responseObserver.onCompleted();
        } catch (Exception ex) {
            log.error("Error in listReagents", ex);
            responseObserver.onError(Status.INTERNAL.withDescription(ex.getMessage()).asRuntimeException());
        }
    }

    @Override
    public void getInstrument(InstrumentRequest request, StreamObserver<InstrumentInfo> responseObserver) {
        try {
            log.info("Warehouse gRPC: GetInstrument {}", request.getInstrumentId());
            UUID id = UUID.fromString(request.getInstrumentId());
            Instrument instrument = instrumentsRepository.findInstrumentsByInstrumentId(id).orElse(null);
            if (instrument == null) {
                responseObserver.onError(Status.NOT_FOUND.withDescription("Instrument not found").asRuntimeException());
                return;
            }
            InstrumentInfo info = InstrumentMapper.toGrpc(instrument);
            responseObserver.onNext(info);
            responseObserver.onCompleted();
        } catch (IllegalArgumentException ex) {
            responseObserver.onError(Status.INVALID_ARGUMENT.withDescription("Invalid UUID").asRuntimeException());
        } catch (Exception ex) {
            log.error("Error in getInstrument", ex);
            responseObserver.onError(Status.INTERNAL.withDescription(ex.getMessage()).asRuntimeException());
        }
    }
}
