// src/features/Monitoring/service/messageBrokerHealthApiService.ts

import axios from "axios";
import api from "../../../api/AxiosInstance";
import { SERVICE_ENDPOINTS } from "../../../api/Endpoints";
import type { BaseResponse } from "@/types/BaseResponse";
import type {
  MessageBrokerHealth,
  MessageBrokerHealthCreateDTO,
  MessageBrokerHealthUpdateDTO,
  MessageBrokerHealthPaginatedResponse,
  MessageBrokerHealthStatus,
} from "../types/MessageBrokerHealth";

const MESSAGE_BROKER_HEALTH_API_URL = `${SERVICE_ENDPOINTS.MONITORING}/message-broker-health`;

/**
 * Get all message broker health records with pagination
 */
export const getAllMessageBrokerHealths = async (
  page: number = 0,
  size: number = 10,
  sortField: string = "createdAt",
  sortOrder: string = "desc"
): Promise<MessageBrokerHealthPaginatedResponse> => {
  try {
    const response = await api.get<BaseResponse<MessageBrokerHealthPaginatedResponse>>(
      `${MESSAGE_BROKER_HEALTH_API_URL}?page=${page}&size=${size}&sortField=${sortField}&sortOrder=${sortOrder}`
    );
    return response.data.data;
  } catch (error: unknown) {
    console.error("Failed to fetch message broker health records:", error);
    const message = axios.isAxiosError(error)
      ? error.response?.data?.message || "Could not retrieve message broker health records."
      : "Could not retrieve message broker health records.";
    throw new Error(message);
  }
};

/**
 * Get message broker health by id
 */
export const getMessageBrokerHealthById = async (
  id: string
): Promise<MessageBrokerHealth> => {
  try {
    const response = await api.get<BaseResponse<MessageBrokerHealth>>(
      `${MESSAGE_BROKER_HEALTH_API_URL}/${id}`
    );
    return response.data.data;
  } catch (error: unknown) {
    console.error(`Failed to fetch message broker health with ID ${id}:`, error);
    const status = axios.isAxiosError(error) ? error.response?.status : undefined;
    if (status === 404) {
      throw new Error("Message broker health record not found.");
    }
    const message = axios.isAxiosError(error)
      ? error.response?.data?.message || "Could not retrieve message broker health record."
      : "Could not retrieve message broker health record.";
    throw new Error(message);
  }
};

/**
 * Get message broker health by broker name
 */
export const getMessageBrokerHealthByBrokerName = async (
  brokerName: string,
  page: number = 0,
  size: number = 10
): Promise<MessageBrokerHealthPaginatedResponse> => {
  try {
    const response = await api.get<BaseResponse<MessageBrokerHealthPaginatedResponse>>(
      `${MESSAGE_BROKER_HEALTH_API_URL}/broker/${brokerName}?page=${page}&size=${size}`
    );
    return response.data.data;
  } catch (error: unknown) {
    console.error(
      `Failed to fetch message broker health for broker ${brokerName}:`,
      error
    );
    const message = axios.isAxiosError(error)
      ? error.response?.data?.message || "Could not retrieve message broker health records."
      : "Could not retrieve message broker health records.";
    throw new Error(message);
  }
};

/**
 * Get message broker health by status
 */
export const getMessageBrokerHealthByStatus = async (
  status: MessageBrokerHealthStatus,
  page: number = 0,
  size: number = 10
): Promise<MessageBrokerHealthPaginatedResponse> => {
  try {
    const response = await api.get<BaseResponse<MessageBrokerHealthPaginatedResponse>>(
      `${MESSAGE_BROKER_HEALTH_API_URL}/status/${status}?page=${page}&size=${size}`
    );
    return response.data.data;
  } catch (error: unknown) {
    console.error(
      `Failed to fetch message broker health with status ${status}:`,
      error
    );
    const statusCode = axios.isAxiosError(error) ? error.response?.status : undefined;
    if (statusCode === 400) {
      throw new Error("Invalid status value.");
    }
    const message = axios.isAxiosError(error)
      ? error.response?.data?.message || "Could not retrieve message broker health records."
      : "Could not retrieve message broker health records.";
    throw new Error(message);
  }
};

/**
 * Create new message broker health record
 */
export const createMessageBrokerHealth = async (
  data: MessageBrokerHealthCreateDTO
): Promise<MessageBrokerHealth> => {
  try {
    const response = await api.post<BaseResponse<MessageBrokerHealth>>(
      `${MESSAGE_BROKER_HEALTH_API_URL}/create`,
      data
    );
    return response.data.data;
  } catch (error: unknown) {
    console.error("Failed to create message broker health record:", error);
    const message = axios.isAxiosError(error)
      ? error.response?.data?.message ||
        "An error occurred while creating the message broker health record."
      : "An error occurred while creating the message broker health record.";
    throw new Error(message);
  }
};

/**
 * Update message broker health record
 */
export const updateMessageBrokerHealth = async (
  id: string,
  data: MessageBrokerHealthUpdateDTO
): Promise<MessageBrokerHealth> => {
  try {
    const response = await api.put<BaseResponse<MessageBrokerHealth>>(
      `${MESSAGE_BROKER_HEALTH_API_URL}/update/${id}`,
      data
    );
    return response.data.data;
  } catch (error: unknown) {
    console.error(`Failed to update message broker health with ID ${id}:`, error);
    const status = axios.isAxiosError(error) ? error.response?.status : undefined;
    if (status === 404) {
      throw new Error("Message broker health record not found.");
    }
    const message = axios.isAxiosError(error)
      ? error.response?.data?.message ||
        "An error occurred while updating the message broker health record."
      : "An error occurred while updating the message broker health record.";
    throw new Error(message);
  }
};

/**
 * Delete message broker health by id
 */
export const deleteMessageBrokerHealth = async (id: string): Promise<void> => {
  try {
    await api.delete(`${MESSAGE_BROKER_HEALTH_API_URL}/${id}`);
  } catch (error: unknown) {
    console.error(`Failed to delete message broker health with ID ${id}:`, error);
    const status = axios.isAxiosError(error) ? error.response?.status : undefined;
    if (status === 404) {
      throw new Error("Message broker health record not found.");
    }
    const message = axios.isAxiosError(error)
      ? error.response?.data?.message ||
        "An error occurred while deleting the message broker health record."
      : "An error occurred while deleting the message broker health record.";
    throw new Error(message);
  }
};

