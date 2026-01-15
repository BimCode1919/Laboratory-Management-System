// src/features/Monitoring/types/HealthEventLogs.ts

export type HealthEventType = "CONNECTED" | "DISCONNECTED" | "RECONNECTED" | "ERROR" | "TIMEOUT" | "RETRY";

export interface HealthEventLog {
  healthEventLogId: string;
  brokerId: string;
  healthEventType: HealthEventType;
  details: string;
  createdAt: string; // LocalDateTime
}

// DTO for creating new health event log
export interface HealthEventLogCreateDTO {
  brokerId: string;
  healthEventType: HealthEventType;
  details: string;
}

// Paginated response structure
export interface HealthEventLogPaginatedResponse {
  logs: HealthEventLog[];
  totalItems: number;
  totalPages: number;
  pageSize: number;
  currentPage: number;
}

