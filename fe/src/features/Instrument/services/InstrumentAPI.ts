// import axiosInstance from '../../../api/axiosInstance';
import axiosInstance from '../../../api/AxiosInstance';
import type {
    ApiResponse,
    PaginatedApiResponse,
    Instrument,
    // InstrumentMode,
    ChangeModePayload,
    ChangeModeResult,
    RawTestResult,
    // Reagent,
    // ReagentStatus,
    InstalledReagent,
    ReagentInfo,
    InstallReagentPayload,
    UninstallReagentPayload,
    StartAnalysisPayload,
    AnalysisRun,
    RawTestResultDeleteDTO
} from '../types/types';

import { SERVICE_ENDPOINTS } from '../../../api/Endpoints';

const INSTRUMENT_BASE_URL = SERVICE_ENDPOINTS.INSTRUMENT;

// Generic API fetcher
type FetchOptions = {
    method?: string;
    data?: unknown;
    headers?: Record<string, string>;
    [key: string]: unknown;
};

async function fetchApi<T>(baseUrl: string, endpoint: string, options: FetchOptions = {}): Promise<ApiResponse<T>> {
    const { method = 'get', data, ...restOptions } = options;

    const response = await axiosInstance({
        method,
        url: `${baseUrl}${endpoint}`,
        data, // Pass payload object directly to axios
        headers: {
            'Content-Type': 'application/json',
            ...restOptions.headers,
        },
        ...restOptions,
    });

    if (response.status === 204) {
        return {
            service: '',
            code: 'OK',
            message: '',
            data: (null as unknown) as T,
            timestamp: new Date().toISOString(),
        };
    }

    const parsed = response.data;

    if (parsed && typeof parsed === 'object' && Object.prototype.hasOwnProperty.call(parsed, 'data')) {
        return parsed as ApiResponse<T>;
    }

    return {
        service: '',
        code: 'OK',
        message: '',
        data: parsed as T,
        timestamp: new Date().toISOString(),
    };
}

export type PendingTestOrder = {
    id: string;
    barCode: string;
    testOrderId: string;
    testType: string;
    priority: string;
    patientName: string;
    status: string;
    createdAt: string;
    updatedAt: string;
};

// New type for pending order check response
export type PendingTestOrderCheckResult = {
    exists: boolean;
    instrumentExists: boolean;
    hasMatchingConfig: boolean;
    testType?: string | null;
    pending: boolean;
};

export const InstrumentApi = {
    // Instrument Management
    getAllInstruments: async (): Promise<Instrument[]> => {
        const response = await fetchApi<Instrument[]>(INSTRUMENT_BASE_URL, '/instruments');
        return response.data;
    },

    getInstrumentById: async (id: string): Promise<Instrument> => {
        const response = await fetchApi<Instrument>(INSTRUMENT_BASE_URL, `/instruments/${id}`);
        return response.data;
    },

    changeMode: async (instrumentId: string, payload: ChangeModePayload): Promise<ChangeModeResult> => {
        const response = await fetchApi<ChangeModeResult>(INSTRUMENT_BASE_URL, `/instruments/${instrumentId}/mode`, {
            method: 'POST',
            data: payload, // Pass object directly
        });
        return response.data;
    },

    getReagentsFromWarehouse: async (): Promise<ReagentInfo[]> => {
        // New: instrument-service exposes an internal grpc proxy for warehouse reagents.
        // The endpoint may return either a paginated shape { page, size, items: ReagentInfo[] }
        // or a direct array/object depending on implementation. Normalize to ReagentInfo[].
        type InstrumentWarehouseListResponse = {
            page?: number;
            size?: number;
            items?: ReagentInfo[];
        };

        try {
            const response = await fetchApi<InstrumentWarehouseListResponse>(INSTRUMENT_BASE_URL, '/internal/grpc/warehouse/reagents', { method: 'GET', cache: 'no-store' });
            const data = response.data as unknown;

            // If backend returns the paginated wrapper with items
            if (data && typeof data === 'object') {
                const maybe = data as InstrumentWarehouseListResponse & Record<string, unknown>;
                if (Array.isArray(maybe.items)) {
                    return (maybe.items as ReagentInfo[]) || [];
                }
                // Sometimes the service might directly return an array as `data`
                if (Array.isArray(data)) {
                    return data as ReagentInfo[];
                }
                // Fallback: if `data` itself looks like a single reagent, wrap it
                const asRecord = data as Record<string, unknown>;
                if (asRecord && typeof asRecord.reagentId === 'string') {
                    return [data as unknown as ReagentInfo];
                }
            }

            return [];
        } catch (err) {
            // Let callers handle the error; return empty array as a safe fallback
            console.error('Failed to fetch reagents from instrument-service internal warehouse endpoint', err);
            throw err;
        }
    },

    getInstalledReagents: async (instrumentId: string): Promise<InstalledReagent[]> => {
        const response = await fetchApi<InstalledReagent[]>(INSTRUMENT_BASE_URL, `/instruments/${instrumentId}/reagents`, { method: 'GET', cache: 'no-store' });
        return response.data;
    },

    installReagent: async (instrumentId: string, payload: InstallReagentPayload): Promise<ApiResponse<unknown>> => {
        const response = await fetchApi<unknown>(INSTRUMENT_BASE_URL, `/instruments/${instrumentId}/reagents/install`, {
            method: 'POST',
            data: payload,
        });
        // Return the full ApiResponse so the caller can inspect eventType/payload details
        return response;
    },

    uninstallReagent: async (instrumentId: string, payload: UninstallReagentPayload): Promise<void> => {
        const response = await fetchApi<void>(INSTRUMENT_BASE_URL, `/instruments/${instrumentId}/reagents/uninstall`, {
            method: 'POST',
            data: payload,
        });
        return response.data;
    },

    updateReagentInUseStatus: async (instrumentId: string, reagentId: string, inUse: boolean): Promise<InstalledReagent> => {
        const response = await fetchApi<InstalledReagent>(INSTRUMENT_BASE_URL, `/instruments/${instrumentId}/reagents/${reagentId}/in-use`, {
            method: 'PATCH',
            data: { inUse },
        });
        return response.data;
    },

    syncReagentsConfiguration: async (instrumentId: string): Promise<ApiResponse<unknown>> => {
        // Backend now determines who performed the sync; client should not provide installedBy
        const response = await fetchApi<unknown>(INSTRUMENT_BASE_URL, `/instruments/${instrumentId}/reagents/sync`, { method: 'POST' });
        return response;
    },

    // Sample Analysis
    startAnalysis: async (instrumentId: string, payload: StartAnalysisPayload): Promise<AnalysisRun> => {
        const response = await fetchApi<AnalysisRun>(INSTRUMENT_BASE_URL, `/instruments/${instrumentId}/analysis/start`, {
            method: 'POST',
            data: payload,
        });
        return response.data;
    },

    // Sync configuration for a single selected instrument
    syncInstrumentConfiguration: async (instrumentId: string): Promise<ApiResponse<unknown>> => {
        // Try the most likely endpoint first, but fall back to a couple of reasonable alternates
        const candidates = [
            `/instruments/${instrumentId}/configuration-sync`,
            `/instruments/configuration-sync/${instrumentId}`,
            `/instruments/${instrumentId}/configuration/sync`,
        ];

        let lastError: unknown = null;

        for (const endpoint of candidates) {
            try {
                const response = await fetchApi<unknown>(INSTRUMENT_BASE_URL, endpoint, { method: 'POST' });
                return response;
            } catch (err: unknown) {
                lastError = err;
                // Inspect axios-like error shape safely
                const resp = (err as { response?: { status?: number; data?: unknown } })?.response;
                const status = resp?.status;
                const data = resp?.data as Record<string, unknown> | undefined;

                // If backend explicitly reports INSTRUMENT_NOT_FOUND, prefer that and stop trying other candidates
                if (status === 404 && data && typeof data['code'] === 'string' && String(data['code']) === 'INSTRUMENT_NOT_FOUND') {
                    // Rethrow the original axios error so callers can handle the 404+INSTRUMENT_NOT_FOUND
                    throw err;
                }

                if (status && status !== 404) {
                    // Non-404 error — rethrow to allow caller to handle (e.g., 500 internal error)
                    throw err;
                }
                // otherwise continue to next candidate
                console.warn(`syncInstrumentConfiguration: endpoint ${endpoint} returned 404, trying next candidate`);
            }
        }

        // If we get here, all attempts failed — throw a richer error containing attempted endpoints + lastError
        const e = new Error(`All candidate endpoints failed for syncInstrumentConfiguration: ${candidates.join(', ')}`);
        // Attach details for callers using a typed intersection
        const enriched = e as Error & { attemptedEndpoints?: string[]; lastError?: unknown };
        enriched.attemptedEndpoints = candidates;
        enriched.lastError = lastError;
        throw enriched;
    },

    // Raw Test Results
    getRawTestResults: async (
        instrumentId: string,
        page: number = 0,
        size: number = 10,
        sortBy: string = 'createdAt',
        sortDir: 'asc' | 'desc' = 'desc'
    ): Promise<PaginatedApiResponse<RawTestResult>> => {
        const response = await fetchApi<PaginatedApiResponse<RawTestResult>>(
            INSTRUMENT_BASE_URL,
            `/raw-test-results/instrument/${instrumentId}?page=${page}&size=${size}&sortBy=${sortBy}&sortDir=${sortDir.toUpperCase()}`
        );
        return response.data;
    },

    getRawTestResultsBackupTrue: async (
        instrumentId: string,
        page: number = 0,
        size: number = 10,
        sortBy: string = 'createdAt',
        sortDir: 'asc' | 'desc' = 'desc'
    ): Promise<PaginatedApiResponse<RawTestResult>> => {
        const response = await fetchApi<PaginatedApiResponse<RawTestResult>>(
            INSTRUMENT_BASE_URL,
            `/raw-test-results/instrument/${instrumentId}/backup-true?page=${page}&size=${size}&sortBy=${sortBy}&sortDir=${sortDir.toUpperCase()}`
        );
        return response.data;
    },

    getRawTestResultsBackupFalse: async (
        instrumentId: string,
        page: number = 0,
        size: number = 10,
        sortBy: string = 'createdAt',
        sortDir: 'asc' | 'desc' = 'desc'
    ): Promise<PaginatedApiResponse<RawTestResult>> => {
        const response = await fetchApi<PaginatedApiResponse<RawTestResult>>(
            INSTRUMENT_BASE_URL,
            `/raw-test-results/instrument/${instrumentId}/backup-false?page=${page}&size=${size}&sortBy=${sortBy}&sortDir=${sortDir.toUpperCase()}`
        );
        return response.data;
    },

    deleteRawTestResult: async (runId: string, barcode: string): Promise<RawTestResultDeleteDTO> => {
        const response = await fetchApi<RawTestResultDeleteDTO>(INSTRUMENT_BASE_URL, `/raw-test-results/${runId}/${barcode}`, {
            method: 'DELETE',
        });
        return response.data;
    },

    syncAllConfigurations: async (): Promise<ApiResponse<unknown>> => {
        const response = await fetchApi<unknown>(INSTRUMENT_BASE_URL, '/instruments/configuration-all-sync', {
            method: 'POST',
        });
        return response;
    },

    getPendingTestOrders: async (
        page: number = 0,
        size: number = 10,
        sortBy: string = 'createdAt',
        sortDir: 'asc' | 'desc' = 'desc',
        type?: string,
        status?: string,
        priority?: string
    ): Promise<PaginatedApiResponse<PendingTestOrder>> => {
        let queryString = `/pending-test-orders?page=${page}&size=${size}&sortBy=${sortBy}&sortDir=${sortDir.toUpperCase()}`;
        if (type) {
            queryString += `&type=${type}`;
        }
        if (status) {
            queryString += `&status=${status}`;
        }
        if (priority) {
            queryString += `&priority=${priority}`;
        }
        const response = await fetchApi<PaginatedApiResponse<PendingTestOrder>>(
            INSTRUMENT_BASE_URL,
            queryString
        );
        return response.data;
    },

    // New: Check a single pending test order by barcode for a specific instrument
    checkPendingTestOrder: async (barcode: string, instrumentId: string): Promise<PendingTestOrderCheckResult> => {
        const encodedBarcode = encodeURIComponent(barcode);
        const encodedInstrument = encodeURIComponent(instrumentId);
        const response = await fetchApi<PendingTestOrderCheckResult>(INSTRUMENT_BASE_URL, `/pending-test-orders/check?barcode=${encodedBarcode}&instrumentId=${encodedInstrument}`);
        return response.data;
    },

    // New: Estimate analysis resource consumption before starting
    estimateAnalysis: async (instrumentId: string, payload: StartAnalysisPayload): Promise<unknown> => {
        // Return generic unknown for now; caller should cast to appropriate type
        const response = await fetchApi<unknown>(INSTRUMENT_BASE_URL, `/instruments/${instrumentId}/analysis/estimate`, {
            method: 'POST',
            data: payload,
        });
        return response.data;
    },

};
