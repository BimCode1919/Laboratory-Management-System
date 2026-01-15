// src/features/patients/services/PatientService.ts

import api from "../../../api/AxiosInstance";
import { SERVICE_ENDPOINTS } from "../../../api/Endpoints";
import type { BaseResponse } from "@/types/BaseResponse";
// Import các type đã được cập nhật
import type { MedicalRecord, MedicalRecordCreate, MedicalRecordUpdate, MedicalRecordDetail, MedicalRecordSearchResult } from "../types/Patient";

const MEDICAL_API_URL = `${SERVICE_ENDPOINTS.PATIENT}/medical`;
const MEDICAL_RECORDS_API_URL = `${SERVICE_ENDPOINTS.PATIENT}/medical-records`;
const MEDICAL_RECORDS_API = `${SERVICE_ENDPOINTS.PATIENT}/medical`;

/**
 * Lấy danh sách hồ sơ bệnh nhân (API có lỗi, không nên dùng).
 */
export const getAllMedicalRecords = async (
  page = 0,
  size = 10
): Promise<{ content: MedicalRecord[]; totalElements: number }> => {
  try {
    const response = await api.get<BaseResponse<{ content: MedicalRecord[]; totalElements: number }>>(
      `${MEDICAL_API_URL}?page=${page}&size=${size}`
    );
    return response.data.data;
  } catch (error) {
    console.error("Failed to fetch medical records:", error);
    throw new Error("Không thể tải danh sách hồ sơ bệnh nhân.");
  }
};

/**
 * Tìm kiếm hồ sơ bệnh án theo từ khóa và khoảng thời gian (API hoạt động tốt).
 */
export const searchMedicalRecords = async (
  params: {
    keyword?: string;
    startDate?: string;
    endDate?: string;
    page?: number;
    size?: number;
    sortBy?: string;
    sortDirection?: 'ASC' | 'DESC';
  }
  // === FIX: SỬA LẠI KIỂU DỮ LIỆU TRẢ VỀ CHO ĐÚNG ===
): Promise<{ content: MedicalRecordSearchResult[]; totalElements: number }> => {
  try {
    const queryParams = new URLSearchParams();
    if (params.keyword) queryParams.append('keyword', params.keyword);
    if (params.startDate) queryParams.append('startDate', params.startDate);
    if (params.endDate) queryParams.append('endDate', params.endDate);
    queryParams.append('page', (params.page || 0).toString());
    queryParams.append('size', (params.size || 10).toString());
    queryParams.append('sortBy', params.sortBy || 'lastTestDate');
    queryParams.append('sortDirection', params.sortDirection || 'DESC');

    const response = await api.get<BaseResponse<{ content: MedicalRecordSearchResult[]; totalElements: number }>>(
      `${MEDICAL_RECORDS_API_URL}?${queryParams.toString()}`
    );
    return response.data.data;
  } catch (error) {
    console.error("Failed to search medical records:", error);
    throw new Error("Không thể thực hiện tìm kiếm hồ sơ bệnh nhân.");
  }
};

/**
 * Thêm một hồ sơ bệnh nhân mới.
 */
export const addMedicalRecord = async (
  recordData: MedicalRecordCreate
): Promise<MedicalRecord> => {
  try {
    const response = await api.post<BaseResponse<MedicalRecord>>(
      MEDICAL_API_URL,
      recordData
    );
    return response.data.data;
  } catch (error: any) {
    console.error("Failed to add medical record:", error);
    if (error.response?.data?.errorCode === 'PATIENT_EXISTED') {
      throw new Error("Bệnh nhân với thông tin này đã tồn tại.");
    }
    const message = error.response?.data?.message || "Đã xảy ra lỗi khi thêm hồ sơ.";
    throw new Error(message);
  }
};

/**
 * Lấy chi tiết hồ sơ bệnh án.
 */
export const getMedicalRecordDetail = async (
  recordId: string,
  // Thêm tham số params tùy chọn
  params?: {
    testType?: string;
    instrumentUsed?: string;
    startDate?: string;
    endDate?: string;
  }
): Promise<MedicalRecordDetail> => {
  try {
    // === FIX: SỬ DỤNG URLSearchParams ĐỂ XÂY DỰNG URL ĐÚNG CÁCH ===

    // 1. Tạo một đối tượng URLSearchParams
    const queryParams = new URLSearchParams();

    // 2. Thêm các tham số vào nếu chúng tồn tại
    if (params?.testType) {
      queryParams.append('testType', params.testType);
    }
    if (params?.instrumentUsed) {
      queryParams.append('instrumentUsed', params.instrumentUsed);
    }
    
    const queryString = queryParams.toString();

    const url = `${MEDICAL_RECORDS_API}/${recordId}`;

    console.log("Đang gọi API:", url);

    const response = await api.get<BaseResponse<MedicalRecordDetail>>(url);
    console.log(response);
    return response.data.data;
  } catch (error) {
    console.error("Failed to fetch medical record details:", error);
    throw new Error("Không thể tải chi tiết hồ sơ.");
  }
};

export const updateMedicalRecord = async (
  recordId: string,
  recordData: MedicalRecordUpdate
): Promise<MedicalRecord> => {
  try {
    const response = await api.put<BaseResponse<MedicalRecord>>(
      `${MEDICAL_API_URL}/${recordId}`,
      recordData
    );
    return response.data.data;
  } catch (error: any) {
    console.error("Failed to update medical record:", error);
    const message = error.response?.data?.message || "Đã xảy ra lỗi khi cập nhật hồ sơ.";
    if (error.response?.data?.errorCode === 'EMAIL_ALREADY_USED') {
      throw new Error("Email này đã được sử dụng bởi bệnh nhân khác.");
    }
    if (error.response?.data?.errorCode === 'PHONE_ALREADY_USED') {
        throw new Error("Số điện thoại này đã được sử dụng bởi bệnh nhân khác.");
    }
    throw new Error(message);
  }
};

export const deleteMedicalRecord = async (recordId: string): Promise<void> => {
  try {
    await api.delete(`${MEDICAL_API_URL}/${recordId}`);
  } catch (error: any) {
    console.error("Lỗi khi xóa hồ sơ:", error);
    if (error.response?.data?.errorCode === 'DELETE_NOT_ALLOWED') {
        throw new Error("Không thể xóa hồ sơ có xét nghiệm đang chờ xử lý.");
    }
    const errorMessage = error.response?.data?.message || "Đã xảy ra lỗi khi xóa hồ sơ.";
    throw new Error(errorMessage);
  }
};