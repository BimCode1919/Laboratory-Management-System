package org.overcode250204.instrumentservice.grpc.mapper;

import com.google.protobuf.Timestamp;
import org.overcode250204.common.grpc.RawTestResultMessage;
import org.overcode250204.instrumentservice.entity.RawTestResult;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public class RawTestResultMapper {

    public static RawTestResultMessage toGrpc(RawTestResult entity) {
        RawTestResultMessage.Builder builder = RawTestResultMessage.newBuilder();

        if (entity.getRunId() != null)
            builder.setRunId(entity.getRunId().toString());
        if (entity.getBarcode() != null)
            builder.setBarcode(entity.getBarcode());
        if (entity.getHl7Message() != null)
            builder.setHl7Message(entity.getHl7Message());
        if (entity.getStatus() != null)
            builder.setStatus(entity.getStatus());
        if (entity.getTestType() != null)
            builder.setTestType(entity.getTestType());

        builder.setBackedUp(entity.isBackedUp());

        if (entity.getInstrumentId() != null)
            builder.setInstrumentCode(entity.getInstrumentId().toString());

        if (entity.getCreatedAt() != null) {
            LocalDateTime time = entity.getCreatedAt();
            var instant = time.toInstant(ZoneOffset.UTC);
            builder.setCreatedAt(
                    Timestamp.newBuilder()
                            .setSeconds(instant.getEpochSecond())
                            .setNanos(instant.getNano())
                            .build()
            );
        }

        return builder.build();
    }

    private static String nullToEmpty(String value) {
        return value == null ? "" : value;
    }
}
