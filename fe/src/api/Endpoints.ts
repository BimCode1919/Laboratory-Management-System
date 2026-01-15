// src/api/Endpoints.ts

export const API_BASE_URL = import.meta.env.VITE_GATEWAY_URL || "http://localhost:8100";

// Gateway-routed service endpoints
export const SERVICE_ENDPOINTS = {
  AUTH: `${API_BASE_URL}/iam-service`,
  PATIENT: `${API_BASE_URL}/patient-service`,
  TEST_ORDER: `${API_BASE_URL}/test-order-service`,
  MONITORING: `${API_BASE_URL}/monitoring-service`,
  INSTRUMENT: `${API_BASE_URL}/instrument-service`,
  WAREHOUSE: `${API_BASE_URL}/warehouse-service`,
};

/**
 * VD:
 * api.get(`${SERVICE_ENDPOINTS.TEST_ORDER}/config/flag-rule`) 
 * =
 * api.get("http://localhost:8100/test-order-service/config/flag-rule")
 * 
 * api.post(`${SERVICE_ENDPOINTS.AUTH}/login`, { username, password })
 * =
 * api.post("http://localhost:8100/iam-service/login", {username, password })
 * 
 */

export const REFRESH_TOKEN_ENDPOINT = `${SERVICE_ENDPOINTS.AUTH}/refresh-token`;
export const LOGOUT_ENDPOINT = `${SERVICE_ENDPOINTS.AUTH}/logout`;

export const WarehouseEndpoints = {
  WAREHOUSE_BASE: SERVICE_ENDPOINTS.WAREHOUSE,
  CONFIGURATIONS: `${SERVICE_ENDPOINTS.WAREHOUSE}/configurations`,
  CLONE_CONFIGS: (instrumentId: string) => `${SERVICE_ENDPOINTS.WAREHOUSE}/configurations/clone/${instrumentId}`,
  INSTRUMENTS: `${SERVICE_ENDPOINTS.WAREHOUSE}/instruments`,
  CREATE_INSTRUMENT: `${SERVICE_ENDPOINTS.WAREHOUSE}/instruments`,
  INSTRUMENT_DETAIL: (id: string) => `${SERVICE_ENDPOINTS.WAREHOUSE}/instruments/${id}`,
  INSTRUMENT_STATUS: (id: string) => `${SERVICE_ENDPOINTS.WAREHOUSE}/instruments/${id}/status`,
  ACTIVATE: (id: string, updatedBy: string) =>
    `${SERVICE_ENDPOINTS.WAREHOUSE}/instruments/${id}/activate?updatedBy=${updatedBy}`,
  DEACTIVATE: (id: string, updatedBy: string) =>
    `${SERVICE_ENDPOINTS.WAREHOUSE}/instruments/${id}/deactivate?updatedBy=${updatedBy}`,
  // Reagents
  REAGENTS: `${SERVICE_ENDPOINTS.WAREHOUSE}/reagents`,
  REAGENT_DETAIL: (id: string) => `${SERVICE_ENDPOINTS.WAREHOUSE}/reagents/${id}`,
  // Supply-history (reagent inventory) endpoints
  // Base: POST to create a supply-history record
  REAGENT_INVENTORY: `${SERVICE_ENDPOINTS.WAREHOUSE}/reagents/supply-history`,
  // GET list of inventory items
  REAGENT_INVENTORY_LIST: `${SERVICE_ENDPOINTS.WAREHOUSE}/reagents/supply-history/inventory`,
  // GET inventory for a specific reagent by id
  REAGENT_INVENTORY_BY_ID: (id: string) => `${SERVICE_ENDPOINTS.WAREHOUSE}/reagents/supply-history/inventory/${id}`,
  // Usage history endpoints (warehouse-service/usage)
  USAGE: `${SERVICE_ENDPOINTS.WAREHOUSE}/usage`,
  USAGE_BY_REAGENT: (id: string) => `${SERVICE_ENDPOINTS.WAREHOUSE}/usage/${id}`,
  USAGE_DELETE: (id: string) => `${SERVICE_ENDPOINTS.WAREHOUSE}/usage/${id}`,
  // Vendors
  VENDORS: `${SERVICE_ENDPOINTS.WAREHOUSE}/vendors`,
  VENDOR_DETAIL: (id: string) => `${SERVICE_ENDPOINTS.WAREHOUSE}/vendors/${id}`,
};