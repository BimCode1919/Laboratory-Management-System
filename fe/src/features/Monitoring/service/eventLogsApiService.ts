// src/features/eventlogs/services/EventLogService.ts

import axios from "axios";
import api from "../../../api/AxiosInstance";
import { SERVICE_ENDPOINTS } from "../../../api/Endpoints";
import type { BaseResponse } from "@/types/BaseResponse";
import type { EventLog, EventLogCreateDTO, EventLogPaginatedResponse } from "../types/EventLogs";

const EVENT_LOG_API_URL = `${SERVICE_ENDPOINTS.MONITORING}/event-logs`;

export const getAllEventLogs = async (
  page: number = 0,
  size: number = 10,
  sortField: string = "createdAt",
  sortOrder: string = "desc"
): Promise<EventLogPaginatedResponse> => {
  try {
    const response = await api.get<BaseResponse<EventLogPaginatedResponse>>(
      `${EVENT_LOG_API_URL}?page=${page}&size=${size}&sortField=${sortField}&sortOrder=${sortOrder}`
    );
    return response.data.data;
  } catch (error: unknown) {
    console.error("Failed to fetch event logs:", error);
    const message = axios.isAxiosError(error)
      ? error.response?.data?.message || "Could not retrieve event logs."
      : "Could not retrieve event logs.";
    throw new Error(message);
  }
};

export const getEventLogById = async (id: string): Promise<EventLog> => {
  try {
    const response = await api.get<BaseResponse<EventLog>>(`${EVENT_LOG_API_URL}/${id}`);
    return response.data.data;
  } catch (error: unknown) {
    console.error(`Failed to fetch event log with ID ${id}:`, error);
    const status = axios.isAxiosError(error) ? error.response?.status : undefined;
    if (status === 404) {
      throw new Error("Event log not found.");
    }
    const message = axios.isAxiosError(error)
      ? error.response?.data?.message || "Could not retrieve event log."
      : "Could not retrieve event log.";
    throw new Error(message);
  }
};

export const createEventLog = async (eventData: EventLogCreateDTO): Promise<EventLog> => {
  try {
    const response = await api.post<BaseResponse<EventLog>>(EVENT_LOG_API_URL, eventData);
    return response.data.data;
  } catch (error: unknown) {
    console.error("Failed to create event log:", error);
    const message = axios.isAxiosError(error)
      ? error.response?.data?.message || "An error occurred while creating the event log."
      : "An error occurred while creating the event log.";
    throw new Error(message);
  }
};

export const deleteEventLog = async (id: string): Promise<void> => {
  try {
    await api.delete(`${EVENT_LOG_API_URL}/${id}`);
  } catch (error: unknown) {
    console.error(`Failed to delete event log with ID ${id}:`, error);
    const status = axios.isAxiosError(error) ? error.response?.status : undefined;
    if (status === 404) {
      throw new Error("Event log not found.");
    }
    const message = axios.isAxiosError(error)
      ? error.response?.data?.message || "An error occurred while deleting the event log."
      : "An error occurred while deleting the event log.";
    throw new Error(message);
  }
};
