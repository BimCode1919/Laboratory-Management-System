// src/features/Monitoring/types/HL7Backup.ts

export type RawTestResultStatus = "PENDING" | "PROCESSED" | "FAILED" | "ARCHIVED";

export interface HL7Backup {
  backupId: string;
  runId: string;
  barcode: string;
  s3Key: string | null;
  instrumentId: string;
  hl7Message: string;
  status: RawTestResultStatus;
  reason: string | null;
  createdAt: string; // LocalDateTime
}

// DTO for creating new HL7 backup
export interface HL7BackupCreateDTO {
  runId: string;
  barcode: string;
  s3Key?: string;
  instrumentId: string;
  hl7Message: string;
  status: RawTestResultStatus;
  reason?: string;
}

// Paginated response structure
export interface HL7BackupPaginatedResponse {
  results: HL7Backup[];
  totalItems: number;
  totalPages: number;
  currentPage: number;
  pageSize: number;
}

