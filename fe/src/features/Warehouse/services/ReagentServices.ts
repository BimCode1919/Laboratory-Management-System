// src/features/Warehouse/services/ReagentServices.ts

import api from "../../../api/AxiosInstance";
import { WarehouseEndpoints } from "../../../api/Endpoints";
import type { 
  Reagent, 
  CreateReagentRequest, 
  UsageHistoryItem, 
  InventoryItem,
  ImportReagentRequest 
} from "../types/Reagent";

// Use centralized endpoint from WarehouseEndpoints
const SUPPLY_HISTORY_ENDPOINT = WarehouseEndpoints.REAGENT_INVENTORY;

export const getAllReagents = async (): Promise<Reagent[] | null> => {
  try {
    const response = await api.get(WarehouseEndpoints.REAGENTS);
    
    console.log("Raw API response:", response);
    console.log("Response data:", response.data);
    
    const data = response.data;
    
    // Handle different response formats
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
    
    // Format 4: Items wrapper { items: [...] }
    if (data && data.items && Array.isArray(data.items)) {
      console.log("Format 4: Items array", data.items);
      return data.items;
    }
    
    console.warn("Unexpected response format:", data);
    return [];
  } catch (error) {
    console.error("Error fetching reagents:", error);
    if (error && typeof error === 'object' && 'response' in error) {
      console.error("Error response:", (error as any).response);
    }
    return null;
  }
};

export const createReagent = async (
  request: CreateReagentRequest
): Promise<Reagent> => {
  try {
    const response = await api.post(WarehouseEndpoints.REAGENTS, request);
    return response.data;
  } catch (error) {
    console.error("Error creating reagent:", error);
    throw error;
  }
};

export const updateReagent = async (
  reagentId: string,
  request: Partial<CreateReagentRequest>
): Promise<Reagent> => {
  try {
    const response = await api.put(WarehouseEndpoints.REAGENT_DETAIL(String(reagentId)), request);
    return response.data;
  } catch (error) {
    console.error("Error updating reagent:", error);
    throw error;
  }
};

export const deleteReagent = async (reagentId: string): Promise<void> => {
  try {
    await api.delete(`${WarehouseEndpoints.REAGENTS}/${reagentId}`);
  } catch (error) {
    console.error("Error deleting reagent:", error);
    throw error;
  }
};

export const getUsageHistory = async (
  reagentId: string
): Promise<UsageHistoryItem[] | null> => {
  try {
    const response = await api.get(WarehouseEndpoints.USAGE_BY_REAGENT(String(reagentId)));
    
    const data = response.data;
    
    // Handle different response formats
    if (data && data.data && Array.isArray(data.data)) {
      return data.data;
    }
    
    if (Array.isArray(data)) {
      return data;
    }
    
    if (data && data.content && Array.isArray(data.content)) {
      return data.content;
    }
    
    if (data && data.items && Array.isArray(data.items)) {
      return data.items;
    }
    
    return [];
  } catch (error) {
    console.error("Error fetching usage history:", error);
    return null;
  }
};

export const getAllUsage = async (): Promise<UsageHistoryItem[] | null> => {
  try {
    const response = await api.get(WarehouseEndpoints.USAGE);

    const data = response.data;

    if (data && data.data && Array.isArray(data.data)) {
      return data.data;
    }

    if (Array.isArray(data)) {
      return data;
    }

    if (data && data.content && Array.isArray(data.content)) {
      return data.content;
    }

    if (data && data.items && Array.isArray(data.items)) {
      return data.items;
    }

    return [];
  } catch (error) {
    console.error("Error fetching all usage history:", error);
    return null;
  }
};

export const deleteUsage = async (usageId: string): Promise<void> => {
  try {
    await api.delete(WarehouseEndpoints.USAGE_DELETE(String(usageId)));
  } catch (error) {
    console.error("Error deleting usage history:", error);
    throw error;
  }
};

export const getInventory = async (): Promise<InventoryItem[] | null> => {
  try {
    // Use explicit endpoint constant that maps to GET /inventory on backend
    const response = await api.get(WarehouseEndpoints.REAGENT_INVENTORY_LIST);

    const data = response.data;

    // Handle different response formats
    if (data && data.data && Array.isArray(data.data)) {
      return data.data;
    }

    if (Array.isArray(data)) {
      return data;
    }

    if (data && data.content && Array.isArray(data.content)) {
      return data.content;
    }

    if (data && data.items && Array.isArray(data.items)) {
      return data.items;
    }

    return [];
  } catch (error) {
    console.error("Error fetching inventory:", error);
    return null;
  }
};

export const importReagent = async (
  request: ImportReagentRequest
): Promise<void> => {
  try {
    await api.post(SUPPLY_HISTORY_ENDPOINT, request);
  } catch (error) {
    console.error("Error importing reagent:", error);
    throw error;
  }
};

export const getInventoryById = async (reagentId: string): Promise<InventoryItem[] | null> => {
  try {
    const response = await api.get(WarehouseEndpoints.REAGENT_INVENTORY_BY_ID(String(reagentId)));

    const data = response.data;

    if (data && data.data && Array.isArray(data.data)) {
      return data.data;
    }

    if (Array.isArray(data)) {
      return data;
    }

    if (data && data.content && Array.isArray(data.content)) {
      return data.content;
    }

    if (data && data.items && Array.isArray(data.items)) {
      return data.items;
    }

    return [];
  } catch (error) {
    console.error("Error fetching inventory by id:", error);
    return null;
  }
};

// Utility functions
export const formatDate = (val: any): string => {
  if (!val) return "-";
  try {
    return new Date(val).toLocaleDateString();
  } catch {
    return String(val);
  }
};

export const formatDateTime = (val: any): string => {
  if (!val) return "-";
  try {
    return new Date(val).toLocaleString();
  } catch {
    return String(val);
  }
};
