package org.overcode250204.instrumentservice.controllers;

import lombok.RequiredArgsConstructor;
import org.overcode250204.base.BaseResponse;
import org.overcode250204.instrumentservice.dtos.dashboard.InstrumentTestCountDTO;
import org.overcode250204.instrumentservice.dtos.dashboard.TestOverTimeDTO;
import org.overcode250204.instrumentservice.service.interfaces.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dashboard")
public class DashboardStatController {
    private final DashboardService dashboardService;

    @GetMapping("/tests-per-instrument")
    public ResponseEntity<BaseResponse<List<InstrumentTestCountDTO>>> getTestsPerInstrument() {
        List<InstrumentTestCountDTO> data = dashboardService.getTestsPerInstrument();
        return ResponseEntity.ok(BaseResponse.success("instrument-service", data));
    }

    @GetMapping("/tests-over-time")
    public ResponseEntity<BaseResponse<List<TestOverTimeDTO>>> getTestsOverTime(
            @RequestParam(defaultValue = "7") int days
    ) {
        List<TestOverTimeDTO> data = dashboardService.getTestsOverTime(days);
        return ResponseEntity.ok(BaseResponse.success("instrument-service", data));
    }

}
