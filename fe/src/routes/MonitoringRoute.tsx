// src/routes/MonitoringRoute.tsx
// This route file contains all pages belonging to the Monitoring Service
// Every page under the "/monitoring" is registered here

import { Route, Routes, Navigate } from "react-router-dom";
import GeneralLayout from "../layouts/GeneralLayout";
import EventLogsListPage from "../features/Monitoring/pages/EventLogsListPage";
import MessageBrokerHealthListPage from "../features/Monitoring/pages/MessageBrokerHealthListPage";
import SyncUpRequestsListPage from "../features/Monitoring/pages/SyncUpRequestsListPage";
import HL7BackupListPage from "../features/Monitoring/pages/HL7BackupListPage";
import HealthEventLogsListPage from "../features/Monitoring/pages/HealthEventLogsListPage";
import DashboardLayout from "@/layouts/DashboardLayout";

export default function MonitoringRoute() {
  return (
    <Routes>
      {/* <Route element={<GeneralLayout />}>
        <Route path="event-logs" element={<EventLogsListPage />} />
        <Route path="message-broker-health" element={<MessageBrokerHealthListPage />} />
        <Route path="sync-up-requests" element={<SyncUpRequestsListPage />} />
        <Route path="hl7-backups" element={<HL7BackupListPage />} />
        <Route path="health-event-logs" element={<HealthEventLogsListPage />} />
        <Route index element={<Navigate to="event-logs" replace />} />
        <Route index element={<EventLogsListPage />} />
      </Route> */}

      <Route element={<DashboardLayout />}>
        <Route path="event-logs" element={<EventLogsListPage />} />
        <Route path="message-broker-health" element={<MessageBrokerHealthListPage />} />
        <Route path="sync-up-requests" element={<SyncUpRequestsListPage />} />
        <Route path="hl7-backups" element={<HL7BackupListPage />} />
        <Route path="health-event-logs" element={<HealthEventLogsListPage />} />
        <Route index element={<Navigate to="event-logs" replace />} />
        <Route index element={<EventLogsListPage />} />
      </Route>
    </Routes>
  );
}

