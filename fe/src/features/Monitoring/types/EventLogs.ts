// src/features/eventlogs/types/EventLog.ts

export type Severity = "INFO" | "WARN" | "ERROR" | "CRITICAL";

export interface EventLog {
  eventLogId: string;
  eventName: string;
  sourceService: string;
  payload: Record<string, unknown>; //Map<String, Object>
  severity: Severity;
  performedBy: string;
  message: string;
  createdAt: string; // LocalDateTime
}

// DTO khi tạo mới log (nếu có API create)
export interface EventLogCreateDTO {
  eventName: string;
  sourceService: string;
  payload?: Record<string, unknown>;
  severity: Severity;
  performedBy: string;
  message: string;
}

//update DTO cho EventLog
export interface EventLogUpdateDTO {
  severity?: Severity;
  message?: string;
}

// Paginated response structure
export interface EventLogPaginatedResponse {
  totalItems: number;
  totalPages: number;
  currentPage: number;
  logs: EventLog[];
}