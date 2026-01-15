export interface ApiResponse<T> {
    service: string;
    code: string;
    message: string;
    data: T;
    timestamp: string;
}

export interface PaginatedApiResponse<T> {
    content: T[];
    totalElements: number;
    totalPages: number;
    number: number;
    size: number;
    first: boolean;
    last: boolean;
    empty: boolean;
}

export type InstrumentMode = "READY" | "MAINTENANCE" | "INACTIVE";

export type InstrumentStatus = "READY" | "ERROR" | "UNKNOWN";

export interface Configuration {
    configId: string;
    configName: string;
    configKey: string;
    configValue: string;
    defaultValue: string;
    description: string;
    isGlobal: boolean;
    createdAt: string;
    updatedAt: string;
    createdBy: string;
    updatedBy: string;
}

export interface Instrument {
    id: string;
    instrumentCode: string;
    name: string;
    model: string;
    serialNumber: string;
    location: string | null;
    status: InstrumentStatus;
    mode: InstrumentMode;
    isOnline: boolean | null;
    configVersion?: string | null;
    lastConfigSyncAt?: string | null;
    configurations: Configuration[];
}

export interface ChangeModePayload {
    mode: InstrumentMode;
    reason?: string;
}

export interface ChangeModeResult {
    instrumentId: string;
    newMode: InstrumentMode;
    oldMode: InstrumentMode;
    reason: string;
    performedBy: string;
    changedAt: string;
}

export interface ReagentInstrument {
    instrumentId: string;
    name: string;
    serialNumber: string;
}

export interface ReagentInfo {
    reagentId: string;
    name: string;
    catalogNumber: string;
    manufacturer: string;
    casNumber: string | null;
    quantity: number;
    // Additional optional fields returned by instrument-service internal grpc proxy
    totalQuantity?: number;
    expirationDate?: string | null;
    createdBy?: string | null;
    updatedBy?: string | null;
    createdAt?: string | null;
    updatedAt?: string | null;
}

export interface InstalledReagent {
    installedReagentId: string;
    reagentId: string;
    reagentName: string;
    lotNumber: string;
    vendorName: string;
    quantityRemaining: number;
    unit: string;
    expirationDate: string | null;
    inUse: boolean;
    installedAt: string;
    installedBy: string | null;
}

export interface InstallReagentPayload {
    reagentId: string;
    quantity: number;
}

export interface UninstallReagentPayload {
    reagentId: string;
    quantityRemaining: number;
}

export interface StartAnalysisPayload {
    testType: string;
    barcodes: string[];
    autoCreateTestOrder: boolean;
    expectedSamples?: number;
    batchCode?: string;
    note?: string;
}

export interface AnalysisRun {
    runId: string;
    instrumentId: string;
    instrumentName: string;
    status: "RUNNING" | "COMPLETED" | "FAILED";
    totalSamplesExpected: number;
    successfulSamples: number | null;
    failedSamples: number | null;
    startTime: string;
    endTime: string | null;
    createdBy: string;
    reagentSnapshot: {
        count: number;
    };
    results: unknown[];
}

// New: Estimate response type returned by POST /instruments/{id}/analysis/estimate
export interface EstimateResult {
    sufficient: boolean;
    samplesRequested: number;
    requiredPerReagent: Record<string, number>;
    availablePerReagent: Record<string, number>;
    shortfallPerReagent: Record<string, number>;
    runsLeftPerReagent: Record<string, number>;
    estimatedRunsPossible: number;
    // added: details of installed reagent lots per reagent key
    installedDetailsPerReagent?: Record<string, {
        lotNumber: string;
        quantityRemaining: number;
        inUse: boolean;
        unit: string;
        // new fields returned by instrument-service estimate endpoint
        expirationDate?: string | null;
        expired?: boolean;
    }[]>;
}

export interface RawTestResult {
    id: string;
    runId: string;
    instrumentId: string;
    barcode: string;
    createdBy: string | null;
    rawData: Record<string, unknown> | null;
    hl7Message: string | null;
    status: "QUEUED" | "SENT" | "FAILED" | "ACKNOWLEDGED";
    publishedAt: string | null;
    backedUp: boolean;
    testType: string;
    errorMessage: string | null;
    createdAt: string;
}

export interface RawTestResultDeleteDTO {
    barcode: string;
    deletedBy: string;
    deletedAt: string;
    deleteMode: string;
}


export interface RawResult {
    barcode: string;
    test_order_id: string;
    publish_status: boolean;
    raw_hl7_data_snippet: string;
    timestamp: string;
}

export type ReagentStatus = "In Use" | "Not in Use";

export interface Reagent {
    id: string;
    name: string;
    quantity: number;
    expiration_date: string;
    vendor_id: string;
    status: ReagentStatus;
}
