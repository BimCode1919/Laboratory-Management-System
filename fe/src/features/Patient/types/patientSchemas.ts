// src/features/patients/types/PatientSchema.ts

import { z } from "zod";

// Schema cho việc tạo mới
export const createPatientSchema = z.object({
  fullName: z.string().min(2, "Họ tên phải có ít nhất 2 ký tự"),
  dateOfBirth: z.string().refine((dob) => !isNaN(Date.parse(dob)), {
    message: "Ngày sinh không hợp lệ (ví dụ: YYYY-MM-DD)",
  }),
  gender: z.enum(["MALE", "FEMALE", "OTHER"], {
    required_error: "Vui lòng chọn giới tính.",
  }),
  phoneNumber: z.string().min(10, "Số điện thoại không hợp lệ"),
  email: z.string().email("Địa chỉ email không hợp lệ"),
  address: z.string().min(5, "Địa chỉ phải có ít nhất 5 ký tự"),
});




const patientUpdateSchema = z.object({
  fullName: z.string().min(2, "Họ tên phải có ít nhất 2 ký tự"),
  phone: z.string().min(10, "Số điện thoại không hợp lệ"),
  email: z.string().email("Địa chỉ email không hợp lệ"),
  address: z.string().min(5, "Địa chỉ phải có ít nhất 5 ký tự"),
});

const interpretationUpdateSchema = z.object({
    testRecordId: z.string(), // là testOrderId
    interpretation: z.string().optional(),
    status: z.enum(["PENDING", "PROCESSING", "COMPLETED", "CANCELLED", "REVIEWED"]),
});

export const editMedicalRecordSchema = z.object({
    patientDTO: patientUpdateSchema,
    interpretation: z.array(interpretationUpdateSchema).optional(),
    newClinicalNote: z.string().optional(),
});