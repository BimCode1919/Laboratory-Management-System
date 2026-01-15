// src/features/TestOrder/types/TestResults.ts

import { z } from "zod";

export const TestResultAlertLevelSchema = z.enum(["NORMAL", "LOW", "HIGH"]);
export type TestResultAlertLevel = z.infer<typeof TestResultAlertLevelSchema>;

export const TestResultStatusSchema = z.enum([
  "PENDING",
  "REVIEWING",
  "REVIEWED",
]);
export type TestResultStatus = z.infer<typeof TestResultStatusSchema>;

export const TestResultRawSchema = z.object({
  id: z.string().uuid().min(1),
  runId: z.string().uuid().min(1),
  instrumentId: z.string().uuid().min(1),
  barcode: z.string().min(1).max(100),
  rawParameter: z.string().min(1),
  rawValue: z.string().min(1),
  rawUnit: z.string().nullable().optional(),
  rawFlag: z.string().max(50).nullable().optional(),
  instrumentTimestamp: z.iso.datetime().nullable().optional(),
  receivedAt: z.iso.datetime().default(new Date().toISOString()),
  isProcessed: z.boolean().default(false),
  processedAt: z.iso.datetime().nullable().optional(),
  testOrderId: z.string().uuid().nullable().optional(),
  reagentSnapshotJson: z.string().nullable().optional(),
  instrumentDetailJson: z.string().nullable().optional(),
});
export type TestResultRawDTO = z.infer<typeof TestResultRawSchema>;

export const TestResultSchema = z.object({
  id: z.string().uuid().min(1),
  testOrder: z.object({ id: z.string().uuid() }).optional(),
  rawTestResults: TestResultRawSchema,
  paramMap: z.object({ id: z.string().uuid() }).optional(),
  conversion: z.object({ id: z.string().uuid() }).optional(),
  flagRule: z.object({ id: z.string().uuid() }).optional(),
  parameterName: z.string().min(1).max(100),
  resultValue: z.number().nullable().optional(),
  unit: z.string().max(15).nullable().optional(),
  referenceLow: z.string().max(20).nullable().optional(),
  referenceHigh: z.string().max(20).nullable().optional(),
  alertLevel: TestResultAlertLevelSchema.nullable().optional(),
  aiHasIssue: z.boolean().default(false),
  aiReviewComment: z.string().nullable().optional(),
  isReviewed: z.boolean().default(false),
  reviewedBy: z.string().uuid().nullable().optional(),
  reviewedAt: z.iso.datetime().nullable().optional(),
  status: TestResultStatusSchema,
  createdAt: z.iso.datetime().default(new Date().toISOString()),
});
export type TestResultDTO = z.infer<typeof TestResultSchema>;

// new schema for general view by excluding these fields
export const TestResultGeneralSchema = TestResultSchema.omit({
  rawTestResults: true,
  paramMap: true,
  conversion: true,
  flagRule: true,
});

export type TestResultGeneralDTO = z.infer<typeof TestResultGeneralSchema>;

export const TestResultTrendSchema = z.object({
  testOrderId: z.string().uuid(),
  orderCreatedAt: z.string().datetime(),
  parameterName: z.string().min(1),
  resultValue: z.number(),
  unit: z.string().max(15).nullable(),
  referenceLow: z.string().max(20).nullable(),
  referenceHigh: z.string().max(20).nullable(),
  alertLevel: TestResultAlertLevelSchema,
});

export type TestResultTrendDTO = z.infer<typeof TestResultTrendSchema>;

export type AvailableParameter = z.infer<typeof z.string>;
