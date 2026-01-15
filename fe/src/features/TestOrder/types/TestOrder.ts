// src/features/TestOrder/types/TestOrder.ts

import { z } from "zod";

export const TestOrderPrioritySchema = z.enum(["LOW", "NORMAL", "HIGH"]);
export type TestOrderPriority = z.infer<typeof TestOrderPrioritySchema>;

export const TestOrderTypeSchema = z.enum(["CBC", "LFT", "HBA1C"]);
export type TestOrderType = z.infer<typeof TestOrderTypeSchema>;

export const patientReferenceSchema = z.object({
  patientId: z
    .string("Invalid patient ID format (must be UUID string)")
    .nullable(),
  patientCode: z
    .string("Invalid patient code format (must be UUID string)")
    .nullable(),
  fullName: z.string().min(1, "Patient full name is required"),
  dateOfBirth: z
    .string()
    .regex(/^\d{4}-\d{2}-\d{2}$/, "Invalid date of birth format (YYYY-MM-DD)")
    .nullable(),
  age: z.number().int().positive("Age must be a positive integer").nullable(),
  gender: z.string().min(1, "Gender is required"),
  address: z.string().optional().nullable(),
  phoneNumber: z.string().optional().nullable(),
  email: z.email("Invalid email format").optional().nullable(),
  lastSyncedAt: z.string().datetime({ offset: true }).optional().nullable(),
  isActive: z.boolean().optional(),
});
export type PatientReferenceDTO = z.infer<typeof patientReferenceSchema>;

export const testOrderSchema = z.object({
  id: z.string().uuid("Invalid ID format (must be UUID string)"),
  patient: patientReferenceSchema,
  orderCode: z.string().min(1, "Order code is required"),
  status: z.string().min(1, "Status is required"),
  createdAt: z.iso.datetime({ offset: true }),
  priority: TestOrderPrioritySchema,
  testType: TestOrderTypeSchema,
  notes: z.string().optional().nullable(),
  isDeleted: z.boolean(),
});

export type TestOrderDTO = z.infer<typeof testOrderSchema>;

export type TestOrderFormData = TestOrderDTO;

export const PatientReferenceFormConfig = {
  fields: [
    {
      key: "fullName",
      label: "Patient Full Name",
      type: "text",
      required: true,
    },
    {
      key: "patientCode",
      label: "Patient Code",
      type: "text",
      required: true,
    },
    {
      key: "dateOfBirth",
      label: "Date of Birth (YYYY-MM-DD)",
      type: "text",
      required: true,
    },
    {
      key: "gender",
      label: "Gender",
      type: "enum",
      required: true,
      options: ["MALE", "FEMALE", "OTHER"],
    },
    {
      key: "phoneNumber",
      label: "Phone Number",
      type: "text",
      required: true,
    },
    {
      key: "email",
      label: "Email",
      type: "text",
      required: true,
    },
    {
      key: "address",
      label: "Address",
      type: "textarea",
      required: true,
    },
  ],
  fieldGroups: [
    ["fullName", "patientCode"],
    ["dateOfBirth", "gender"],
    ["phoneNumber", "email"],
    ["address"],
  ],
} as const;

export const TestOrderCoreFormConfig = {
  fields: [
    {
      key: "orderCode",
      label: "Order Code",
      type: "text",
      required: true,
    },
    {
      key: "testType",
      label: "Test Type",
      type: "enum",
      required: true,
      options: TestOrderTypeSchema.options,
    },
    {
      key: "priority",
      label: "Priority",
      type: "enum",
      required: true,
      options: TestOrderPrioritySchema.options,
    },
    {
      key: "notes",
      label: "Notes",
      type: "textarea",
      required: true,
    },
  ],
  fieldGroups: [["orderCode", "testType"], ["priority"], ["notes"]],
} as const;

export interface PaymentDTO {
  id: string;
  orderId: string;
  amount: number;
  provider: string;
  status: string;
  approvalUrl: string;
  transactionId: string;
}

export interface TestPricingDTO {
  id: string;
  price: number;
  testType: TestOrderType;
}