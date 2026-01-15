package org.overcode250204.instrumentservice.service.implement;

import lombok.RequiredArgsConstructor;
import org.overcode250204.instrumentservice.dtos.dashboard.InstrumentTestCountDTO;
import org.overcode250204.instrumentservice.dtos.dashboard.TestOverTimeDTO;
import org.overcode250204.instrumentservice.repository.InstrumentRepository;
import org.overcode250204.instrumentservice.repository.RawTestResultRepository;
import org.overcode250204.instrumentservice.service.interfaces.DashboardService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {
    private final RawTestResultRepository rawTestResultRepository;
    private final InstrumentRepository instrumentRepository;

    @Override
    public List<InstrumentTestCountDTO> getTestsPerInstrument() {
        var rawCounts = rawTestResultRepository.countTestsPerInstrument();

        return rawCounts.stream().map(item -> {
            var instrument = instrumentRepository.findById(item.getInstrumentId())
                    .orElse(null);

            String name = instrument != null ? instrument.getName() : "Unknown";

            return new InstrumentTestCountDTO(
                    item.getInstrumentId(),
                    name,
                    item.getTestCount()
            );
        }).toList();
    }

    @Override
    public List<TestOverTimeDTO> getTestsOverTime(int days) {
        LocalDateTime start = LocalDateTime.now().minusDays(days);
        return rawTestResultRepository.countTestsOverTime(start);
    }
}
