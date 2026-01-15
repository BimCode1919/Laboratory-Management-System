// src/features/Warehouse/services/InstrumentServices.ts

import type { AxiosError } from "axios";
import AxiosInstance from "@/api/AxiosInstance";
import { WarehouseEndpoints } from "../../../api/Endpoints";
import type { Instrument } from "../types/Instrument";
import type { BaseResponse } from "@/types/BaseResponse";

export const getAllInstruments = async (): Promise<Instrument[] | null> => {
  try {
    const response = await AxiosInstance.get(WarehouseEndpoints.INSTRUMENTS);
    
    console.log("Raw API response:", response);
    console.log("Response data:", response.data);
    
    // Handle different response formats
    const data = response.data;
    
    // Format 1: BaseResponse wrapper { data: [...] }
    if (data && data.data && Array.isArray(data.data)) {
      console.log("Format 1: BaseResponse with data array", data.data);
      return data.data;
    }
    
    // Format 2: Direct array
    if (Array.isArray(data)) {
      console.log("Format 2: Direct array", data);
      return data;
    }
    
    // Format 3: Paginated response { content: [...] }
    if (data && data.content && Array.isArray(data.content)) {
      console.log("Format 3: Paginated content", data.content);
      return data.content;
    }
    
    console.warn("Unexpected response format:", data);
    return [];
  } catch (error) {
    console.error("Error fetching instruments:", error);
    if (error && typeof error === 'object' && 'response' in error) {
      console.error("Error response:", (error as any).response);
    }
    return null;
  }
};

export const saveInstrument = async (
  instrument: Instrument
): Promise<Instrument> => {
  const isUpdate = !!instrument.id;

  try {
    let response;
    if (isUpdate) {
      response = await AxiosInstance.put<BaseResponse<Instrument>>(
        WarehouseEndpoints.INSTRUMENT_DETAIL(String(instrument.id)),
        instrument
      );
    } else {
      response = await AxiosInstance.post<BaseResponse<Instrument>>(
        WarehouseEndpoints.CREATE_INSTRUMENT,
        instrument
      );
    }
    return response.data.data || response.data;
  } catch (error) {
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

export const deleteInstrument = async (
  id: number | string
): Promise<void> => {
  await AxiosInstance.delete(WarehouseEndpoints.INSTRUMENT_DETAIL(String(id)));
};

export const activateInstrument = async (
  id: number | string,
  updatedBy: string = "admin"
): Promise<Instrument | null> => {
  try {
    const response = await AxiosInstance.put<BaseResponse<Instrument>>(
      WarehouseEndpoints.ACTIVATE(String(id), updatedBy)
    );
    return response.data.data || response.data;
  } catch (error) {
    console.error("Failed to activate instrument:", error);
    return null;
  }
};

export const deactivateInstrument = async (
  id: number | string,
  updatedBy: string = "admin"
): Promise<Instrument | null> => {
  try {
    const response = await AxiosInstance.put<BaseResponse<Instrument>>(
      WarehouseEndpoints.DEACTIVATE(String(id), updatedBy)
    );
    return response.data.data || response.data;
  } catch (error) {
    console.error("Failed to deactivate instrument:", error);
    return null;
  }
};

export const cloneConfigsToInstrument = async (
  instrumentId: string
): Promise<any[] | null> => {
  try {
    const response = await AxiosInstance.post(
      WarehouseEndpoints.CLONE_CONFIGS(instrumentId)
    );
    return response.data?.data || response.data;
  } catch (error) {
    console.error("Failed to clone configs to instrument:", error);
    return null;
  }
};

export const updateInstrumentStatus = async (
  instrumentId: string | number,
  newStatus: string,
  updatedBy?: string,
  reason?: string
): Promise<Instrument | null> => {
  try {
    // Backend expects PATCH /instruments/{id}/status with request params: newStatus, updatedBy, reason
    const params: Record<string, any> = { newStatus };
    if (updatedBy) params.updatedBy = updatedBy;
    if (reason) params.reason = reason;

        console.debug("Updating instrument status (params & body)", { instrumentId, params, url: WarehouseEndpoints.INSTRUMENT_STATUS(String(instrumentId)) });
        // send both query params and request body to accommodate backend variations
        const body = { newStatus, updatedBy, reason };
        const response = await AxiosInstance.patch<BaseResponse<Instrument>>(
          WarehouseEndpoints.INSTRUMENT_STATUS(String(instrumentId)),
          body,
          { params }
        );

        console.debug("Instrument status PATCH response", { status: response.status, data: response.data });

        // Some backends return 204 No Content — treat any 2xx as success and return the parsed data or an empty object
        if (response.status >= 200 && response.status < 300) {
          return response.data?.data || response.data || ({} as Instrument);
        }

        return null;
  } catch (error) {
    console.error("Failed to update instrument status:", error);
    return null;
  }
};
