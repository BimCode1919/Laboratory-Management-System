// src/features/TestOrder/services/TestCommentServices.ts

import { SERVICE_ENDPOINTS } from "@/api/Endpoints";
import type {
  AdminTestCommentDTO,
  TestCommentDTO,
} from "../types/TestComments";
import api from "@/api/AxiosInstance";
import type { BaseResponse } from "@/types/BaseResponse";

const ORDER_COMMENT_URL = `${SERVICE_ENDPOINTS.TEST_ORDER}/api/test-comments`;

export const getAllTestCommentsByOrderId = async (
  testOrderId: string
): Promise<AdminTestCommentDTO[] | null> => {
  try {
    const response = await api.get<BaseResponse<AdminTestCommentDTO[]>>(
      `${ORDER_COMMENT_URL}/all/${testOrderId}`
    );
    return response.data.data;
  } catch (error) {
    console.log("Error fetching comments for test order: ", testOrderId);
    console.error("Error: ", error);
    return null;
  }
};

export const getVisibleCommentsByOrderId = async (
  testOrderId: string
): Promise<TestCommentDTO[] | null> => {
  try {
    const response = await api.get<BaseResponse<TestCommentDTO[]>>(
      `${ORDER_COMMENT_URL}/visible/${testOrderId}`
    );
    return response.data.data;
  } catch (error) {
    console.log("Error fetching comments for test order: ", testOrderId);
    console.error("Error: ", error);
    return null;
  }
};

export const addComment = async (
  resultId: string,
  request: TestCommentDTO
): Promise<TestCommentDTO | null> => {
  try {
    const response = await api.post<BaseResponse<TestCommentDTO>>(
      `${ORDER_COMMENT_URL}/${resultId}`,
      request
    );
    return response.data.data;
  } catch (error) {
    console.error(
      `Error adding comment for result: ${resultId}.
        Error: `,
      error
    );
    return null;
  }
};

export const editComment = async (
  id: string,
  request: TestCommentDTO
): Promise<TestCommentDTO | null> => {
  try {
    const response = await api.put<BaseResponse<TestCommentDTO>>(
      `${ORDER_COMMENT_URL}/${id}`,
      request
    );
    return response.data.data;
  } catch (error) {
    console.error("Error editing comment: ", id, error);
    return null;
  }
};

export const deleteComment = async (id: string): Promise<boolean> => {
  try {
    await api.delete<BaseResponse<void>>(`${ORDER_COMMENT_URL}/${id}`);
    return true;
  } catch (error) {
    console.error("Error deleting comment: ", id, error);
    return false;
  }
};
