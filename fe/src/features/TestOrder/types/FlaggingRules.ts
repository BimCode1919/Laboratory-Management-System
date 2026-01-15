// src/features/TestOrder/types/FlaggingRules.ts

import { z } from "zod";

export const FlaggingRulesSchema = z.object({
  id: z.union([z.string(), z.number()]).optional(),

  parameterName: z.string().min(1, "Parameter Name is required."),
  unit: z.string().optional(),

  gender: z.enum(["MALE", "FEMALE"]).optional(),

  normalLow: z.number().optional(),
  normalHigh: z.number().optional(),

  description: z.string().optional(),

  isActivated: z.boolean().default(true),
});

export type FlaggingRules = z.infer<typeof FlaggingRulesSchema>;

export const FlaggingRulesFormConfig = {
  fields: [
    {
      key: "parameterName",
      label: "Parameter Name",
      type: "text",
      required: true,
    },
    { key: "unit", label: "Unit (optional)", type: "text" },
    {
      key: "gender",
      label: "Gender (optional)",
      type: "enum",
      options: ["MALE", "FEMALE"],
    },
    { key: "normalLow", label: "Lower Bound", type: "number" },
    { key: "normalHigh", label: "Upper Bound", type: "number" },
    { key: "description", label: "Description (optional)", type: "textarea" },
    {
      key: "isActivated",
      label: "Activation Status",
      type: "boolean",
      default: true,
    },
  ],
  fieldGroups: [
    ["parameterName", "unit"],
    ["normalLow", "normalHigh"],
    ["gender", "isActivated"],
    ["description"],
  ],
} as const;
