package org.overcode250204.instrumentservice.grpc;

import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.overcode250204.common.grpc.*;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@Slf4j
public class InstrumentWarehouseClientService {

    @GrpcClient("warehouse-service")
    private WarehouseServiceGrpc.WarehouseServiceBlockingStub warehouseStub;

    public ReagentInfo getReagent(UUID reagentId) {
        try {
            ReagentRequest req = ReagentRequest.newBuilder().setReagentId(reagentId.toString()).build();
            return warehouseStub.getReagent(req);
        } catch (Exception ex) {
            log.error("Failed to fetch reagent {}: {}", reagentId, ex.getMessage());
            throw ex;
        }
    }

    public ListReagentsResponse listReagents(int page, int size) {
        try {
            ListReagentsRequest req = ListReagentsRequest.newBuilder().setPage(page).setSize(size).build();
            return warehouseStub.listReagents(req);
        } catch (Exception ex) {
            log.error("Failed to list reagents: {}", ex.getMessage());
            throw ex;
        }
    }

    public InstrumentInfo getInstrument(UUID instrumentId) {
        try {
            InstrumentRequest req = InstrumentRequest.newBuilder().setInstrumentId(instrumentId.toString()).build();
            return warehouseStub.getInstrument(req);
        } catch (io.grpc.StatusRuntimeException ex) {
            if (ex.getStatus().getCode() == io.grpc.Status.Code.NOT_FOUND) {
                log.debug("Instrument {} not found in warehouse: {}", instrumentId, ex.getMessage());
                return null;
            }
            log.error("Failed to fetch instrument {} via warehouse gRPC: {}", instrumentId, ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            log.error("Failed to fetch instrument {} via warehouse gRPC: {}", instrumentId, ex.getMessage());
            throw ex;
        }
    }
}
