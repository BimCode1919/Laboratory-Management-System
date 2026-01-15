// src/features/Warehouse/types/Instrument.ts

import { z } from "zod";

export const InstrumentSchema = z.object({
  id: z.union([z.string(), z.number()]).optional(),
  name: z.string().min(1, "Instrument name is required."),
  brand: z.string().optional(),
  model: z.string().optional(),
  serialNumber: z.string().optional(),
  status: z.enum(["Ready", "Maintenance", "Offline"]).default("Ready"),
  active: z.boolean().default(true),
  description: z.string().optional(),
  location: z.string().optional(),
  purchaseDate: z.string().optional(),
  warrantyExpiry: z.string().optional(),
  maintenanceSchedule: z.string().optional(),
  createdAt: z.string().optional(),
});

export type Instrument = z.infer<typeof InstrumentSchema>;

export const InstrumentFormConfig = {
  fields: [
    {
      key: "name",
      label: "Instrument Name",
      type: "text" as const,
      required: true,
    },
    {
      key: "brand",
      label: "Brand/Manufacturer",
      type: "text" as const,
    },
    {
      key: "model",
      label: "Model",
      type: "text" as const,
    },
    {
      key: "serialNumber",
      label: "Serial Number",
      type: "text" as const,
    },
    {
      key: "status",
      label: "Status",
      type: "enum" as const,
      options: ["Ready", "Maintenance", "Offline", "Calibrating"],
      default: "Ready",
    },
    {
      key: "location",
      label: "Location",
      type: "text" as const,
    },
    {
      key: "purchaseDate",
      label: "Purchase Date",
      type: "text" as const,
    },
    {
      key: "warrantyExpiry",
      label: "Warranty Expiry",
      type: "text" as const,
    },
    {
      key: "maintenanceSchedule",
      label: "Maintenance Schedule",
      type: "text" as const,
    },
    {
      key: "description",
      label: "Description",
      type: "textarea" as const,
    },
    {
      key: "active",
      label: "Active Status",
      type: "boolean" as const,
      default: true,
    },
  ],
  fieldGroups: [
    ["name", "brand"],
    ["model", "serialNumber"],
    ["status", "location"],
    ["purchaseDate", "warrantyExpiry"],
    ["maintenanceSchedule", "active"],
    ["description"],
  ],
};
