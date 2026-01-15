// src/features/Warehouse/services/VendorServices.ts

import api from "../../../api/AxiosInstance";
import { WarehouseEndpoints } from "../../../api/Endpoints";

export interface Vendor {
  id?: string;
  name: string;
  contact?: string;
  address?: string;
  phone?: string;
  email?: string;
  [key: string]: any;
}

export interface CreateVendorRequest {
  name: string;
  contact?: string;
  address?: string;
  phone?: string;
  email?: string;
}

export const getAllVendors = async (): Promise<Vendor[] | null> => {
  try {
    const response = await api.get(WarehouseEndpoints.VENDORS);
    const data = response.data;

    if (data && data.data && Array.isArray(data.data)) return data.data;
    if (Array.isArray(data)) return data;
    if (data && data.content && Array.isArray(data.content)) return data.content;
    if (data && data.items && Array.isArray(data.items)) return data.items;

    return [];
  } catch (error) {
    console.error("Error fetching vendors:", error);
    return null;
  }
};

export const createVendor = async (request: CreateVendorRequest): Promise<Vendor | null> => {
  try {
    const response = await api.post(WarehouseEndpoints.VENDORS, request);
    const data = response.data;
    // Backend may wrap in BaseResponse or return object directly
    return data?.data || data;
  } catch (error) {
    console.error("Error creating vendor:", error);
    throw error;
  }
};

export const updateVendor = async (id: string, request: CreateVendorRequest): Promise<Vendor | null> => {
  try {
    const response = await api.put(WarehouseEndpoints.VENDOR_DETAIL(id), request);
    const data = response.data;
    return data?.data || data;
  } catch (error) {
    console.error("Error updating vendor:", error);
    throw error;
  }
};

export const deleteVendor = async (id: string): Promise<void> => {
  try {
    await api.delete(WarehouseEndpoints.VENDOR_DETAIL(id));
  } catch (error) {
    console.error("Error deleting vendor:", error);
    throw error;
  }
};
