// src/features/Monitoring/types/SyncUpRequests.ts

export type SyncUpRequestsStatus =
  | "PENDING"
  | "IN_PROGRESS"
  | "COMPLETED"
  | "FAILED"
  | "CANCELLED";

export interface SyncUpRequests {
  syncUpRequestId: string;
  sourceService: string;
  messageId: string;
  status: SyncUpRequestsStatus;
  processedAt: string | null; // LocalDateTime
  createdAt: string; // LocalDateTime
  updatedAt: string; // LocalDateTime
}

// DTO for creating new sync up request
export interface SyncUpRequestsCreateDTO {
  sourceService: string;
  messageId: string;
  status: SyncUpRequestsStatus;
  processedAt?: string;
}

// DTO for updating sync up request
export interface SyncUpRequestsUpdateDTO {
  sourceService?: string;
  messageId?: string;
  status?: SyncUpRequestsStatus;
  processedAt?: string;
}

// Paginated response structure
export interface SyncUpRequestsPaginatedResponse {
  results: SyncUpRequests[];
  totalItems: number;
  totalPages: number;
  currentPage: number;
  pageSize: number;
}