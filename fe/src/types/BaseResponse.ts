// src/types/BaseResponse.ts

export interface BaseResponse<T> {
  service: string;
  code: string;
  message: string;
  data: T;
  timestamp: string;
}