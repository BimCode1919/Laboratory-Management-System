// src/features/TestOrder/services/TestResultServices.ts

import { SERVICE_ENDPOINTS } from "@/api/Endpoints";
import api from "@/api/AxiosInstance";
import type {
  TestResultDTO,
  TestResultGeneralDTO,
  TestResultTrendDTO,
  AvailableParameter,
} from "../types/TestResults";
import type { BaseResponse } from "@/types/BaseResponse";

const TEST_RESULTS_URL = `${SERVICE_ENDPOINTS.TEST_ORDER}/test-results`;

export const getAllTestResultsByTestOrderId = async (
  testOrderId: string
): Promise<TestResultDTO[] | TestResultGeneralDTO[] | null> => {
  try {
    const response = await api.get<
      BaseResponse<TestResultDTO[] | TestResultGeneralDTO[]>
    >(`${TEST_RESULTS_URL}/${testOrderId}`);
    return response.data.data;
  } catch (error: unknown) {
    console.log("Failure to get test results for order id: ", testOrderId);
    console.error("Failed to get test results, error: ", error);
    return null;
  }
};

export const getAvailableParameters = async (
  patientId: string
): Promise<AvailableParameter[] | null> => {
  try {
    const response = await api.get<BaseResponse<AvailableParameter[]>>(
      `${TEST_RESULTS_URL}/patients/${patientId}/parameters`
    );
    return response.data.data;
  } catch (error: unknown) {
    console.error(
      `Failure to get available parameters for patient ${patientId}. Error: `,
      error
    );
    return null;
  }
};

export const getPatientResultTrendData = async (
  patientId: string,
  startTime: string,
  endTime: string,
  parameterName: string
): Promise<TestResultTrendDTO[] | null> => {
  try {
    const response = await api.get<BaseResponse<TestResultTrendDTO[]>>(
      `${TEST_RESULTS_URL}/patients/${patientId}/trend`,
      {
        params: {
          startTime,
          endTime,
          parameterName,
        },
      }
    );

    return response.data.data;
  } catch (error: unknown) {
    console.error(
      `Failure to fetch trend data for patient ${patientId} and parameter ${parameterName}. Error: `,
      error
    );
    return null;
  }
};
