package org.overcode250204.testorderservice.utils;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class CodeGenerator {
    @Value("${prefix-record-code}")
    private String prefixRecordCode;

    public String generateRecordCode() {
        String prefix = prefixRecordCode;
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        return prefix + "-" + timestamp;
    }
}
