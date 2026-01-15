// src/features/Monitoring/types/MessageBrokerHealth.ts

export type MessageBrokerHealthStatus =
  | "HEALTHY"
  | "UNHEALTHY"
  | "DEGRADED"
  | "UNKNOWN";

export interface MessageBrokerHealth {
  messageBrokerHealthId: string;
  brokerName: string;
  status: MessageBrokerHealthStatus;
  lastCheckedAt: string;
  retryAttempts: number | null;
  errorCode: string | null;
  errorMessage: string | null;
  recoveredAt: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface MessageBrokerHealthCreateDTO {
  brokerName: string;
  status: MessageBrokerHealthStatus;
  lastCheckedAt?: string;
  retryAttempts?: number;
  errorCode?: string;
  errorMessage?: string;
  recoveredAt?: string;
}

// DTO for updating message broker health record
export interface MessageBrokerHealthUpdateDTO {
  brokerName?: string;
  status?: MessageBrokerHealthStatus;
  lastCheckedAt?: string;
  retryAttempts?: number;
  errorCode?: string;
  errorMessage?: string;
  recoveredAt?: string;
}

export interface MessageBrokerHealthPaginatedResponse {
  data: MessageBrokerHealth[];
  totalItems: number;
  totalPages: number;
  currentPage: number;
}