// src/features/TestOrder/services/TestOrderServices.ts

import type { AxiosError } from "axios";
import api from "../../../api/AxiosInstance";
import { SERVICE_ENDPOINTS } from "../../../api/Endpoints";
import type { PaymentDTO, TestOrderDTO } from "../types/TestOrder";
import type { BaseResponse } from "@/types/BaseResponse";

const TEST_ORDER_URL = `${SERVICE_ENDPOINTS.TEST_ORDER}/api/test-orders`;

// export const getAllTestOrders = async (): Promise<TestOrderDTO[] | null> => {
//     try {
//         const response = await api.get<BaseResponse>
//     }
// }
export const reIndexTestOrderDocuments = async () => {
  await api.post(
    `${SERVICE_ENDPOINTS.TEST_ORDER}/api/elastic/admin/jobs/reindex-test-orders`
  );
  console.log("Reindexing documents...");
};

export const getDetailedTestOrder = async (
  testOrderId: string
): Promise<TestOrderDTO | null> => {
  try {
    const response = await api.get<BaseResponse<TestOrderDTO>>(
      `${TEST_ORDER_URL}/${testOrderId}`
    );
    return response.data.data;
  } catch (error: unknown) {
    console.error("Error fetching detailed data of the test order: ", error);
    return null;
  }
};

export const createPaymentForTestOrder = async (
  testOrderId: string
): Promise<PaymentDTO | null> => {
  try {
    const response = await api.post<BaseResponse<PaymentDTO>>(
      `${SERVICE_ENDPOINTS.TEST_ORDER}/payments/create/${testOrderId}`
    );
    return response.data.data;
  } catch (error: unknown) {
    console.error(`Error creating payment for ${testOrderId}: `, error);
    return null;
  }
};

export const capturePaymentForTestOrder = async (
  paymentId: string,
  token: string
): Promise<PaymentDTO | null> => {
  try {
    const response = await api.get<BaseResponse<PaymentDTO>>(
      `${SERVICE_ENDPOINTS.TEST_ORDER}/payments/capture`,
      {
        params: {
          paymentId,
          token,
        },
      }
    );
    return response.data.data;
  } catch (error: unknown) {
    console.error(`Error capturing payment for payment ${paymentId}: `, error);
    return null;
  }
};

