package org.overcode250204.warehouseservice.grpc.mapper;

import org.overcode250204.common.grpc.InstrumentInfo;
import org.overcode250204.warehouseservice.model.entities.Instrument;

public class InstrumentMapper {

    public static InstrumentInfo toGrpc(Instrument instrument) {
        if (instrument == null) return null;
        InstrumentInfo.Builder b = InstrumentInfo.newBuilder();
        if (instrument.getInstrumentId() != null) b.setInstrumentId(instrument.getInstrumentId().toString());
        if (instrument.getInstrumentCode() != null) b.setInstrumentCode(instrument.getInstrumentCode());
        if (instrument.getName() != null) b.setName(instrument.getName());
        if (instrument.getModel() != null) b.setModel(instrument.getModel());
        if (instrument.getSerialNumber() != null) b.setSerialNumber(instrument.getSerialNumber());
        if (instrument.getLocation() != null) b.setLocation(instrument.getLocation());
        if (instrument.getStatus() != null) b.setStatus(instrument.getStatus().name());
        if (instrument.getCreatedAt() != null) b.setCreatedAt(instrument.getCreatedAt().toString());
        if (instrument.getUpdatedAt() != null) b.setUpdatedAt(instrument.getUpdatedAt().toString());
        return b.build();
    }
}

