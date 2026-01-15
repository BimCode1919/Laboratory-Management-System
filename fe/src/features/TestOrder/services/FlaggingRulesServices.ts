// src/features/TestOrder/services/FlaggingRulesServices.ts

import type { AxiosError } from "axios";
import api from "../../../api/AxiosInstance";
import { SERVICE_ENDPOINTS } from "../../../api/Endpoints";
import type { FlaggingRules } from "../types/FlaggingRules";
import type { BaseResponse } from "@/types/BaseResponse";

const FLAG_RULE_URL = `${SERVICE_ENDPOINTS.TEST_ORDER}/config/flag-rule`;

export const getAllFlaggingRules = async (): Promise<
  FlaggingRules[] | null
> => {
  try {
    const response = await api.get<BaseResponse<FlaggingRules[]>>(
      FLAG_RULE_URL
    );
    return response.data.data;
  } catch (error: unknown) {
    console.error("Error fetching flagging rules:", error);
    return null;
  }
};

export const saveFlaggingRule = async (
  rule: FlaggingRules
): Promise<FlaggingRules> => {
  const isUpdate = !!rule.id;
  try {
    let response;
    if (isUpdate) {
      response = await api.put<BaseResponse<FlaggingRules>>(
        `${FLAG_RULE_URL}/update/${rule.id}`,
        rule
      );
    } else {
      response = await api.post<BaseResponse<FlaggingRules>>(
        `${FLAG_RULE_URL}/add`,
        rule
      );
    }
    return response.data.data;
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

export const deleteFlaggingRule = async (
  id: number | string
): Promise<void> => {
  await api.delete(`${FLAG_RULE_URL}/delete/${id}`);
};

export const enableFlaggingRule = async (
  id: number | string
): Promise<FlaggingRules | null> => {
  try {
    const response = await api.put<BaseResponse<FlaggingRules>>(
      `${FLAG_RULE_URL}/enable/${id}`
    );
    return response.data.data;
  } catch (error: unknown) {
    console.error("Failure to enable Flagging Rule: ", error);
    return null;
  }
};

export const disableFlaggingRule = async (
  id: number | string
): Promise<FlaggingRules | null> => {
  try {
    const response = await api.put<BaseResponse<FlaggingRules>>(
      `${FLAG_RULE_URL}/disable/${id}`
    );
    return response.data.data;
  } catch (error: unknown) {
    console.error("Failure to disable Flagging Rule: ", error);
    return null;
  }
};
