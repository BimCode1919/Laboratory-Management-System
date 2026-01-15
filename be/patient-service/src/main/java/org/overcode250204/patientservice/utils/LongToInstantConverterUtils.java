package org.overcode250204.patientservice.utils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@ReadingConverter
public class LongToInstantConverterUtils implements Converter<Long, Instant> {
    @Override
    public Instant convert(Long source) {
        return Instant.ofEpochMilli(source);
    }
}
