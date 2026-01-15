// src/features/Warehouse/types/Reagent.ts

import { z } from "zod";

export const ReagentSchema = z.object({
  reagentId: z.string(),
  name: z.string().min(1, "Reagent name is required."),
  catalogNumber: z.string().optional(),
  manufacturer: z.string().optional(),
  casNumber: z.string().optional(),
  quantity: z.union([z.number(), z.string()]).optional(),
  createdAt: z.string().optional(),
  updatedAt: z.string().optional(),
});

export type Reagent = z.infer<typeof ReagentSchema>;

export interface CreateReagentRequest {
  name: string;
  catalogNumber?: string;
  manufacturer?: string;
  casNumber?: string;
}

export interface UsageHistoryItem {
  reagentName?: string;
  usedBy?: string;
  user?: string;
  quantity?: number;
  note?: string;
  timestamp?: string;
  usedAt?: string;
}

export interface InventoryItem {
  reagentName?: string;
  quantity?: number;
  unitOfMeasure?: string;
  expirationDate?: string;
  storageLocation?: string;
  status?: string;
  poNumber?: string;
  lotNumber?: string;
}

export interface ImportReagentRequest {
  reagentId: string;
  vendorID?: string | null;
  poNumber?: string | null;
  orderDate?: string | null;
  receiptDate?: string | null;
  quantity: number;
  unitOfMeasure?: string | null;
  lotNumber?: string | null;
  expirationDate?: string | null;
  storageLocation?: string | null;
  status?: string | null;
  note?: string;
}

export const ReagentFormConfig = {
  fields: [
    {
      key: "name",
      label: "Name",
      type: "text" as const,
      required: true,
    },
    {
      key: "catalogNumber",
      label: "Catalog Number",
      type: "text" as const,
    },
    {
      key: "manufacturer",
      label: "Manufacturer",
      type: "text" as const,
    },
    {
      key: "casNumber",
      label: "CAS Number",
      type: "text" as const,
    },
  ],
};
