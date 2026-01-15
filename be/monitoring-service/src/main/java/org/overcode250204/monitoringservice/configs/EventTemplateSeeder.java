package org.overcode250204.monitoringservice.configs;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.overcode250204.monitoringservice.entities.EventTemplate;
import org.overcode250204.monitoringservice.repositories.EventTemplateRepo;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventTemplateSeeder {

    private final EventTemplateRepo repo;

    @PostConstruct
    public void init() {

        // ==================================================================
        // === IDENTITY & ACCESS MANAGEMENT (IAM) SERVICE EVENTS ===
        // ==================================================================

        seed("IAM_USER_LOGIN",
                "User {ipAddress} successfully logged in",
                "INFO",
                "Triggered on successful user authentication (iam-service)");

        seed("USER_LOGIN_FAILURE",
                "Failed login attempt for user {userIdAction} (Reason: {exception})",
                "AUDIT",
                "Triggered on failed user authentication (iam-service)");

        seed("IAM_USER_CREATED",
                "New user {aggregateType} (UserId: {userId}) was created by {performedBy} {changes}",
                "AUDIT",
                "Triggered when a new user account is created (iam-service)");

        seed("IAM_USER_UPDATED",
                "User profile {userIdAction} was updated by {performedBy} {changes}",
                "AUDIT",
                "Triggered when user information is updated (iam-service)");

        seed("IAM_DISABLE_USER",
                "User {userIdAction} has been DISABLED by {performedBy} {changes}",
                "AUDIT",
                "Triggered when a user account is disabled/locked (iam-service)");

        seed("IAM_ENABLE_USER",
                "User {userIdAction} has been ENABLED by {performedBy} {changes}",
                "AUDIT",
                "Triggered when a user account is enabled/unlocked (iam-service)");

        seed("USER_FIRSTLOGIN_COMPLETED",
                "User {userIdAction} completed first login password change",
                "INFO",
                "Triggered when user completes the mandatory first login (iam-service)");

        seed("USER_PASSWORD_FORGOT_REQUESTED",
                "Password reset requested for email {userIdAction}",
                "INFO",
                "Triggered when a user requests a password reset (iam-service)");

        seed("USER_PASSWORD_FORGOT_FAILED",
                "Failed password reset request for {userIdAction} (Reason: {exception})",
                "AUDIT",
                "Triggered when a password reset request fails (iam-service)");

        seed("USER_CONFIRM_PASSWORD_FAILED",
                "Failed password confirmation for {userIdAction} (Reason: {exception})",
                "AUDIT",
                "Triggered when password confirmation fails (iam-service)");

        seed("USER_PASSWORD_RESET_COMPLETED",
                "Password for user {userIdAction} was reset successfully",
                "AUDIT",
                "Triggered on successful password reset (iam-service)");

        seed("USER_TOKEN_REFRESHED",
                "User {userIdAction} successfully refreshed authentication token",
                "INFO",
                "Triggered when a user token is refreshed (iam-service)");

        seed("USER_TOKEN_REFRESH_FAILED",
                "Failed token refresh attempt for user {userIdAction} (Reason: {exception})",
                "AUDIT",
                "Triggered when a token refresh fails (iam-service)");

        seed("IAM_ROLE_CREATED",
                "New role '{changes}' was created by {performedBy}",
                "AUDIT",
                "Triggered when a new role is created (iam-service)");

        seed("IAM_ROLE_UPDATED",
                "Role '{changes}' was updated by {performedBy}",
                "AUDIT",
                "Triggered when a role's details or privileges are updated (iam-service)");

        seed("IAM_ROLE_DELETED",
                "Role '{changes}' was deleted by {performedBy}",
                "AUDIT",
                "Triggered when a role is deleted (iam-service)");


        // ==================================================================
        // === PATIENT SERVICE EVENTS ===
        // ==================================================================

        seed("PATIENT_MEDICALRECORD_CREATED",
                "{changes} by {performedBy}",
                "AUDIT",
                "Triggered when a new patient medical record is added (patient-service)");

        seed("PATIENT_MEDICALRECORD_UPDATED",
                "{changes} was updated by {performedBy}",
                "AUDIT",
                "Triggered when a patient medical record is updated (patient-service)");


        // ==================================================================
        // === TEST ORDER SERVICE EVENTS ===
        // ==================================================================

        seed("TEST_RESULTS_PROCESSED",
                "Test results for order {testOrderId} (Barcode: {barcode}) have been processed",
                "INFO",
                "Triggered when raw results are parsed and saved (test-order-service)");

        seed("TEST_COMMENT_ADDED",
                "Comment added to order {testOrderId} by {userId}",
                "AUDIT",
                "Event message used when new comment of test result is added (test-order-service)");

        seed("TEST_COMMENT_MODIFIED",
                "Comment {commentId} on order {testOrderId} was modified by {userId}",
                "AUDIT",
                "Event message used when comment of test result is modified (test-order-service)");

        seed("TEST_COMMENT_DELETED",
                "Comment {commentId} on order {testOrderId} was deleted by {userId}",
                "AUDIT",
                "Event message used when comment of test result is deleted (test-order-service)");

        seed("TEST_SYNC_REQUESTED",
                "Sync-up requested for test order with barcode {barcode} by {requestedBy}",
                "AUDIT",
                "Triggered when a sync-up request is made for a test order (test-order-service)");

        // ==================================================================
        // === INSTRUMENT SERVICE EVENTS ===
        // ==================================================================

        seed("INSTRUMENT_RUN_STARTED",
                "Instrument {instrumentId} started run {runId} ({sampleCount} samples)",
                "INFO",
                "Triggered when instrument starts processing a batch");

        // Event này có tên "..._LOG" để phân biệt với event gửi cho test-order
        seed("INSTRUMENT_RUN_COMPLETION_LOG",
                "Instrument {instrumentId} completed run {runId} (success={success}, failed={failed})",
                "INFO",
                "Triggered when instrument finishes processing samples (log event)");

        seed("INSTRUMENT_MODE_CHANGED",
                "Instrument {instrumentId} mode changed from {oldMode} to {newMode} by {performedBy} (reason={reason})",
                "AUDIT",
                "Triggered when instrument mode is changed (SRS E_00009)");

        seed("INSTRUMENT_RAW_DELETED",
                "Raw test {barcode} deleted by {deletedBy} (instrument={instrumentId}, run={runId}, at={deletedAt})",
                "AUDIT",
                "Triggered when a raw result is deleted manually");

        seed("RAW_RESULT_AUTO_CLEANUP",
                "System auto-cleanup deleted {deletedCount} results in {durationMs}ms",
                "INFO",
                "Triggered when automatic cleanup of raw test results occurs");

        seed("INSTRUMENT_CONFIGURATION_SYNC_REQUEST_LOG",
                "Configuration sync requested: instrument {instrumentId}, eventId={eventId}, requestedBy={performedBy}",
                "INFO",
                "Triggered when a configuration sync request is sent for a single instrument");

        seed("INSTRUMENT_CONFIGURATION_ALL_SYNC_REQUEST_LOG",
                "Configuration all sync requested: eventId={eventId}, requestedBy={performedBy}",
                "INFO",
                "Triggered when a global configuration sync request is sent");

        seed("REAGENT_INSTALL_REQUEST_LOG",
                "Reagent install requested: instrument={instrumentId}, reagent={reagentId}, quantity={quantity}, installedBy={performedBy}, eventId={eventId}",
                "INFO",
                "Triggered when a reagent install request is sent to warehouse");

        seed("REAGENT_SYNC_REQUEST_LOG",
                "Reagent sync requested: instrument {instrumentId}, eventId={eventId}, requestedBy={performedBy}",
                "INFO",
                "Triggered when reagent sync request is sent to warehouse");

        seed("REAGENT_UNINSTALL_REQUEST_LOG",
                "Reagent uninstall requested: instrument={instrumentId}, reagent={reagentId}, quantityRemoved={quantityRemaining}, removedBy={performedBy}, eventId={eventId}",
                "INFO",
                "Triggered when a reagent uninstall request is sent to warehouse");


        // ==================================================================
        // === WAREHOUSE SERVICE EVENTS ===
        // ==================================================================

        seed("INSTRUMENT_CREATED",
                "New instrument {name} (Serial: {serialNumber} with ID : {instrumentId}, ) was added by {performedBy}",
                "AUDIT",
                "Triggered when a new instrument is added to the warehouse");

        seed("INSTRUMENT_UPDATED",
                "Instrument with (ID: {instrumentId}) was updated new status {newStatus} by {performedBy} reason = {reason}",
                "AUDIT",
                "Triggered when a new instrument is added to the warehouse");

        seed("INSTRUMENT_ACTIVE",
                "Instrument with (ID: {instrumentId}) was updated new status {status} by {performedBy}",
                "AUDIT",
                "Triggered when a new instrument is added to the warehouse");

        seed("INSTRUMENT_INACTIVE",
                "Instrument with (ID: {instrumentId}) was updated new status {status} by {performedBy}",
                "AUDIT",
                "Triggered when a new instrument is added to the warehouse");

        seed("INSTRUMENT_AUTO_DELETED",
                "Instrument with (ID: {instrumentId}) was auto-deleted by {performedBy} reason {reason}",
                "INFO",
                "Triggered when an instrument is auto-deleted after 3 months");

        seed("REAGENT_SUPPLY_ADDED",
                "New reagent supply logged: {reagentName} Qty: {quantity}",
                "INFO",
                "Triggered when new reagent supply history is added");

        seed("REAGENT_CREATE",
                "Reagent {reagentName} with ID {reagentID} created by {performedBy}",
                "INFO",
                "Triggered when reagent usage is logged");

        seed("REAGENT_UPDATE",
                "Reagent {reagentName} with ID {reagentID} updated by {performedBy} ",
                "INFO",
                "Triggered when reagent usage is logged");

        seed("REAGENT_DELETED",
                "Reagent {reagentName} with ID {reagentID} deleted by {performedBy}",
                "INFO",
                "Triggered when reagent usage is logged");

        seed("CONFIGURATION_CREATED",
                "New configuration '{configKey}' with name {name} created by {performedBy}",
                "AUDIT",
                "Triggered when a new system configuration is created");

        seed("CONFIGURATION_UPDATED",
                "Configuration '{configKey}' with name {name} was updated by {performedBy}",
                "AUDIT",
                "Triggered when a system configuration is modified");

        seed("CONFIGURATION_DELETED",
                "Configuration '{configKey}' was deleted by {performedBy}",
                "AUDIT",
                "Triggered when a system configuration is deleted");

        // ==================================================================
        // === MONITORING SERVICE (Internal) EVENTS ===
        // ==================================================================

        seed("HL7_BACKUP_CONFIRMED",
                "HL7 Backup confirmed for barcode {barcode} (Run: {runId})",
                "INFO",
                "Triggered by OutboxPublisher after HL7 backup is confirmed (monitoring-service)");

        seed("BROKER_HEALTH_DOWN",
                "Message Broker '{brokerName}' is UNHEALTHY. (Reason: {reason})",
                "AUDIT",
                "Triggered by Health Check Worker when broker is confirmed down (SRS 3.2.3.1)");

        seed("BROKER_HEALTH_RECOVERED",
                "Message Broker '{brokerName}' has recovered and is HEALTHY.",
                "INFO",
                "Triggered by Health Check Worker when broker connection is restored (SRS 3.2.3.1)");

        seed("SYNC_UP_REQUESTED",
                "Sync-Up job requested by {serviceName} for {barcodeCount} barcodes",
                "AUDIT",
                "Triggered when a service requests a data sync-up via gRPC (SRS 3.2.2.2)");

        seed("SYNC_UP_DATA_MISSING",
                "Sync-Up data missing for barcode {barcode}. Requesting from Instrument Service.",
                "AUDIT",
                "Triggered by Sync-Up Worker when backup data is missing (SRS 3.2.2.2)");
    }

    private void seed(String eventName, String template, String severity, String desc) {
        if (!repo.existsByEventName(eventName)) {
            repo.save(EventTemplate.builder()
                    .eventName(eventName)
                    .template(template)
                    .severity(severity)
                    .description(desc)
                    .build());
            log.info("[EventTemplateSeeder] Inserted template for {}", eventName);
        }
    }
}