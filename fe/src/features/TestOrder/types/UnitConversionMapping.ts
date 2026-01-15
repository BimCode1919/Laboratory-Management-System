// src/features/TestOrder/types/UnitConversionMapping.ts

import { z } from "zod";

const STANDARD_UNITS = [
    "cells/μL", "K/μL", "million/μL", "g/dL", 
    "g/L", "%", "fL", "pg", "mmol/L", "mg/dL"
] as const;

const UnitSchema = z.enum(STANDARD_UNITS);

export const UnitConversionMappingSchema = z.object({
  id: z.union([z.string(), z.number()]).optional(),
  dataSource: z.string().min(1, "Data Source is required."),
  sourceUnit: UnitSchema,
  targetUnit: UnitSchema,
  multiplyFactor: z.number().min(0, "Factor must be non-negative.").default(1),
  calculationOffset: z.number().default(0),
  formula: z.string().optional(),
  description: z.string().optional(),
  isActivated: z.boolean().default(true),
});

export type UnitConversionMapping = z.infer<typeof UnitConversionMappingSchema>;

export const UnitConversionMappingFormConfig = {
  fields: [
    { key: "dataSource", label: "Data Source", type: "text", required: true },
    { key: "sourceUnit", label: "Source Unit", type: "enum", options: STANDARD_UNITS, required: true },
    { key: "targetUnit", label: "Target Unit", type: "enum", options: STANDARD_UNITS, required: true },
    {
      key: "multiplyFactor",
      label: "Multiply Factor",
      type: "number",
      required: true,
    },
    { key: "calculationOffset", label: "Offset", type: "number" },
    { key: "formula", label: "Formula (optional)", type: "textarea" },
    { key: "description", label: "Description (optional)", type: "textarea" },
    { key: "isActivated", label: "Activation Status", type: "boolean" },
  ],
  fieldGroups: [
    ["sourceUnit", "targetUnit"],
    ["multiplyFactor", "calculationOffset"],
    ["dataSource", "isActivated"],
    ["formula"],
    ["description"],
  ],
} as const;
