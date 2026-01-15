// src/features/TestOrder/types/ResultParameterMapping.ts

import { z } from "zod";

export const ResultParameterMappingSchema = z.object({
  id: z.union([z.string(), z.number()]).optional(),
  externalParamName: z.string().min(1),
  internalParamName: z.string().min(1),
  dataSource: z.string().min(1),
  isActivated: z.boolean().default(true),
});

export type ResultParameterMapping = z.infer<typeof ResultParameterMappingSchema>;

export const ResultParameterMappingFormConfig = {
  fields: [
    { key: "externalParamName", label: "External Parameter Name", type: "text", required: true },
    { key: "internalParamName", label: "Internal Parameter Name", type: "text", required: true },
    { key: "dataSource", label: "Data Source", type: "text", required: true },
    { key: "isActivated", label: "Activated", type: "boolean", default: true },
  ],
  fieldGroups: [
    ["externalParamName", "internalParamName"],
    ["dataSource"]
  ]
} as const;
