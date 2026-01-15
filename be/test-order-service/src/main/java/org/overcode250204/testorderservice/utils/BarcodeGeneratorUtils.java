package org.overcode250204.testorderservice.utils;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.overcode250204.testorderservice.models.entites.BarcodeSequence;
import org.overcode250204.testorderservice.repositories.BarcodeSequenceRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@RequiredArgsConstructor
public class BarcodeGeneratorUtils {
    private final BarcodeSequenceRepository barcodeSequenceRepository;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyMMdd");

    @Transactional
    public String generateBarcode() {
        LocalDate today = LocalDate.now();
        String datePart = today.format(DATE_FORMATTER);

        BarcodeSequence dailySequence = barcodeSequenceRepository.findById(today)
                .orElseGet(() -> new BarcodeSequence(today, 0));

        int seq = dailySequence.getLastSequence() + 1;
        dailySequence.setLastSequence(seq);
        barcodeSequenceRepository.save(dailySequence);

        String base = String.format("BL%s%05d", datePart, seq);
        char checksum = calculateChecksum(base);

        return base + checksum;
    }

    private char calculateChecksum(String input) {
        int sum = 0;
        for (int i = 0; i < input.length(); i++) {
            sum += input.charAt(i);
        }
        int mod = sum % 11;
        return (char) ('A' + mod);
    }

}
