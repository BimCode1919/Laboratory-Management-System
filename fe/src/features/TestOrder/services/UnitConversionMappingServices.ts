// src/features/TestOrder/services/UnitConversionMappingServices.ts

import type { AxiosError } from "axios";
import api from "../../../api/AxiosInstance";
import { SERVICE_ENDPOINTS } from "../../../api/Endpoints";
import type { UnitConversionMapping } from "../types/UnitConversionMapping";
import type { BaseResponse } from "@/types/BaseResponse";

const UNIT_MAP_URL = `${SERVICE_ENDPOINTS.TEST_ORDER}/config/unit-map`;

export const getAllUnitConversionMappings = async (): Promise<
  UnitConversionMapping[] | null
> => {
  try {
    const response = await api.get<BaseResponse<UnitConversionMapping[]>>(UNIT_MAP_URL);
    return response.data.data;
  } catch (error: unknown) {
    console.error("Error fetching unit conversion mappings: ", error);

    return null;
  }
};

export const saveUnitConversionMapping = async (
  mapping: UnitConversionMapping
): Promise<UnitConversionMapping> => {
  const isUpdate = !!mapping.id;

  try {
    let response;
    if (isUpdate) {
      response = await api.put<BaseResponse<UnitConversionMapping>>(
        `${UNIT_MAP_URL}/update/${mapping.id}`,
        mapping
      );
    } else {
      response = await api.post<BaseResponse<UnitConversionMapping>>(
        `${UNIT_MAP_URL}/add`,
        mapping
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

export const deleteUnitConversionMapping = async (
  id: number | string
): Promise<void> => {
  await api.delete(`${UNIT_MAP_URL}/delete/${id}`);
};

export const enableUnitConversionMapping = async (
  id: number | string
): Promise<UnitConversionMapping | null> => {
  try {
    const response = await api.put<BaseResponse<UnitConversionMapping>>(
      `${UNIT_MAP_URL}/enable/${id}`
    );
    return response.data.data;
  } catch (error: unknown) {
    console.error("Error enabling Unit Conversion Map: ", error);
    return null;
  }
};

export const disableUnitConversionMapping = async (
  id: number | string
): Promise<UnitConversionMapping | null> => {
  try {
    const response = await api.put<BaseResponse<UnitConversionMapping>>(
      `${UNIT_MAP_URL}/disable/${id}`
    );
    return response.data.data;
  } catch (error: unknown) {
    console.error("Error disabling Unit Conversion Map: ", error);
    return null;
  }
};
