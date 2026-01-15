// src/features/Monitoring/service/healthEventLogsApiService.ts

import axios from "axios";
import api from "../../../api/AxiosInstance";
import { SERVICE_ENDPOINTS } from "../../../api/Endpoints";
import type { BaseResponse } from "@/types/BaseResponse";
import type {
  HealthEventLog,
  HealthEventLogCreateDTO,
  HealthEventLogPaginatedResponse,
  HealthEventType,
} from "../types/HealthEventLogs";

const HEALTH_EVENT_LOG_API_URL = `${SERVICE_ENDPOINTS.MONITORING}/health-event-logs`;

/**
 * Get all health event logs with pagination
 */
export const getAllHealthEventLogs = async (
  page: number = 0,
  size: number = 10,
  sortField: string = "createdAt",
  sortOrder: string = "desc"
): Promise<HealthEventLogPaginatedResponse> => {
  try {
    const response = await api.get<BaseResponse<HealthEventLogPaginatedResponse>>(
      `${HEALTH_EVENT_LOG_API_URL}?page=${page}&size=${size}&sortField=${sortField}&sortOrder=${sortOrder}`
    );
    return response.data.data;
  } catch (error: unknown) {
    console.error("Failed to fetch health event logs:", error);
    const message = axios.isAxiosError(error)
      ? error.response?.data?.message || "Could not retrieve health event logs."
      : "Could not retrieve health event logs.";
    throw new Error(message);
  }
};

/**
 * Get health event log by id
 */
export const getHealthEventLogById = async (
  id: string
): Promise<HealthEventLog> => {
  try {
    const response = await api.get<BaseResponse<HealthEventLog>>(
      `${HEALTH_EVENT_LOG_API_URL}/${id}`
    );
    return response.data.data;
  } catch (error: unknown) {
    console.error(`Failed to fetch health event log with ID ${id}:`, error);
    const status = axios.isAxiosError(error) ? error.response?.status : undefined;
    if (status === 404) {
      throw new Error("Health event log not found.");
    }
    const message = axios.isAxiosError(error)
      ? error.response?.data?.message || "Could not retrieve health event log."
      : "Could not retrieve health event log.";
    throw new Error(message);
  }
};

/**
 * Get health event logs by broker ID
 */
export const getHealthEventLogsByBrokerId = async (
  brokerId: string,
  page: number = 0,
  size: number = 10
): Promise<HealthEventLogPaginatedResponse> => {
  try {
    const response = await api.get<BaseResponse<HealthEventLogPaginatedResponse>>(
      `${HEALTH_EVENT_LOG_API_URL}/broker/${brokerId}?page=${page}&size=${size}`
    );
    return response.data.data;
  } catch (error: unknown) {
    console.error(
      `Failed to fetch health event logs for broker ${brokerId}:`,
      error
    );
    const message = axios.isAxiosError(error)
      ? error.response?.data?.message || "Could not retrieve health event logs."
      : "Could not retrieve health event logs.";
    throw new Error(message);
  }
};

/**
 * Get health event logs by event type
 */
export const getHealthEventLogsByEventType = async (
  eventType: HealthEventType,
  page: number = 0,
  size: number = 10
): Promise<HealthEventLogPaginatedResponse> => {
  try {
    const response = await api.get<BaseResponse<HealthEventLogPaginatedResponse>>(
      `${HEALTH_EVENT_LOG_API_URL}/type/${eventType}?page=${page}&size=${size}`
    );
    return response.data.data;
  } catch (error: unknown) {
    console.error(
      `Failed to fetch health event logs with type ${eventType}:`,
      error
    );
    const statusCode = axios.isAxiosError(error) ? error.response?.status : undefined;
    if (statusCode === 400) {
      throw new Error("Invalid event type value.");
    }
    const message = axios.isAxiosError(error)
      ? error.response?.data?.message || "Could not retrieve health event logs."
      : "Could not retrieve health event logs.";
    throw new Error(message);
  }
};

/**
 * Create new health event log
 */
export const createHealthEventLog = async (
  data: HealthEventLogCreateDTO
): Promise<HealthEventLog> => {
  try {
    const response = await api.post<BaseResponse<HealthEventLog>>(
      `${HEALTH_EVENT_LOG_API_URL}/create`,
      data
    );
    return response.data.data;
  } catch (error: unknown) {
    console.error("Failed to create health event log:", error);
    const message = axios.isAxiosError(error)
      ? error.response?.data?.message ||
        "An error occurred while creating the health event log."
      : "An error occurred while creating the health event log.";
    throw new Error(message);
  }
};

/**
 * Delete health event log by id
 */
export const deleteHealthEventLog = async (id: string): Promise<void> => {
  try {
    await api.delete(`${HEALTH_EVENT_LOG_API_URL}/delete/${id}`);
  } catch (error: unknown) {
    console.error(`Failed to delete health event log with ID ${id}:`, error);
    const status = axios.isAxiosError(error) ? error.response?.status : undefined;
    if (status === 404) {
      throw new Error("Health event log not found.");
    }
    const message = axios.isAxiosError(error)
      ? error.response?.data?.message ||
        "An error occurred while deleting the health event log."
      : "An error occurred while deleting the health event log.";
    throw new Error(message);
  }
};

