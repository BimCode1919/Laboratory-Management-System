// src/features/TestOrder/services/ResultParameterMappingService.ts

import type { AxiosError } from "axios";
import api from "../../../api/AxiosInstance";
import { SERVICE_ENDPOINTS } from "../../../api/Endpoints";
import type { ResultParameterMapping } from "../types/ResultParameterMapping";
import type { BaseResponse } from "@/types/BaseResponse";

const RESULT_MAPPING_URL = `${SERVICE_ENDPOINTS.TEST_ORDER}/config/param-map`;

export const getAllResultParameterMappings = async (): Promise<
  ResultParameterMapping[] | null
> => {
  try {
    const response = await api.get<BaseResponse<ResultParameterMapping[]>>(
      RESULT_MAPPING_URL
    );
    return response.data.data;
  } catch (error: unknown) {
    console.error("Error fetching result parameter mappings:", error);
    return null;
  }
};

export const saveResultParameterMapping = async (
  mapping: ResultParameterMapping
): Promise<ResultParameterMapping> => {
  const isUpdate = !!mapping.id;

  try {
    if (isUpdate) {
      const response = await api.put<BaseResponse<ResultParameterMapping>>(
        `${RESULT_MAPPING_URL}/update/${mapping.id}`,
        mapping
      );
      return response.data.data;
    } else {
      const response = await api.post<BaseResponse<ResultParameterMapping>>(
        `${RESULT_MAPPING_URL}/add`,
        mapping
      );
      return response.data.data;
    }
  } catch (error: unknown) {
    const axiosError = error as AxiosError;
    if (axiosError.response && axiosError.response.data) {
      const errorData = axiosError.response.data as { message?: string };
      if (errorData.message) {
        throw new Error(errorData.message);
      }
    }
    throw error;
  }
};

export const deleteResultParameterMapping = async (
  id: number | string
): Promise<void> => {
  await api.delete(`${RESULT_MAPPING_URL}/delete/${id}`);
};

export const enableResultParameterMapping = async (
  id: number | string
): Promise<ResultParameterMapping | null> => {
  try {
    const response = await api.put<BaseResponse<ResultParameterMapping>>(
      `${RESULT_MAPPING_URL}/enable/${id}`
    );
    return response.data.data;
  } catch (error: unknown) {
    console.error("Error enabling the result parameter mapping: ", error);
    return null;
  }
};

export const disableResultParameterMapping = async (
  id: number | string
): Promise<ResultParameterMapping | null> => {
  try {
    const response = await api.put<BaseResponse<ResultParameterMapping>>(
      `${RESULT_MAPPING_URL}/disable/${id}`
    );
    return response.data.data;
  } catch (error: unknown) {
    console.error("Error enabling the result parameter mapping: ", error);
    return null;
  }
};
