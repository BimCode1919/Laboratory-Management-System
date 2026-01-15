package org.overcode250204.instrumentservice.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.overcode250204.instrumentservice.service.interfaces.RawTestResultService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class RawResultCleanupScheduler {

    private final RawTestResultService rawTestResultService;

    @Scheduled(cron = "0 0 2 * * *")
    public void scheduleAutoCleanup() {
        log.info("[Scheduler] Starting automatic cleanup for backed-up raw test results...");
        try {
            rawTestResultService.autoCleanupBackedUpResults();
            log.info("[Scheduler] Auto cleanup completed successfully.");
        } catch (Exception e) {
            log.error("[Scheduler] Auto cleanup failed: {}", e.getMessage(), e);
        }
    }
}
