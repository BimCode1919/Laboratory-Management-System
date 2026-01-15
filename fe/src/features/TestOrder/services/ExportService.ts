// src/features/TestOrder/services/ExportService.ts

import api from "@/api/AxiosInstance";
import { SERVICE_ENDPOINTS } from "@/api/Endpoints";
import type { BaseResponse } from "@/types/BaseResponse";

const EXPORT_URL = `${SERVICE_ENDPOINTS.TEST_ORDER}/export`;

export const exportAsCsv = async (testOrderId: string) => {
  try {
    const response = await api.get<BaseResponse<string | null>>(
      `${EXPORT_URL}/csv/${testOrderId}`
    );
    return response.data.data;
  } catch (error: unknown) {
    console.log("Error exporting test order to CSV: ", error);
    return null;
  }
};

export const exportAsXlsx = async (testOrderId: string) => {
  try {
    const response = await api.get<BaseResponse<string | null>>(
      `${EXPORT_URL}/excel/${testOrderId}`
    );
    return response.data.data;
  } catch (error: unknown) {
    console.log("Error exporting test order to Excel: ", error);
    return null;
  }
};

export const exportAsPdf = async (testOrderId: string) => {
  try {
    const response = await api.get<BaseResponse<string | null>>(
      `${EXPORT_URL}/pdf/${testOrderId}`
    );
    return response.data.data;
  } catch (error: unknown) {
    console.log("Error exporting test order to PDF: ", error);
    return null;
  }
};
