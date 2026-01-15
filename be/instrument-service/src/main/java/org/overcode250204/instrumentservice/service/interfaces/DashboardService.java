package org.overcode250204.instrumentservice.service.interfaces;

import org.overcode250204.instrumentservice.dtos.dashboard.InstrumentTestCountDTO;
import org.overcode250204.instrumentservice.dtos.dashboard.TestOverTimeDTO;

import java.util.List;

public interface DashboardService {
    List<InstrumentTestCountDTO> getTestsPerInstrument();
    List<TestOverTimeDTO> getTestsOverTime(int days);
}
