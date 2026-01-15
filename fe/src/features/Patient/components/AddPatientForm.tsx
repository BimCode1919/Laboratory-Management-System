// src/features/patients/components/AddPatientForm.tsx

import { useState } from "react";
import DynamicAddUpdateForm, { type FieldConfig } from "../../../components/DynamicAddUpdateForm";
import { createPatientSchema } from "../types/patientSchemas";
import type { MedicalRecord, MedicalRecordCreate } from "../types/Patient";
import { addMedicalRecord } from "../services/PatientService";

type AddPatientFormProps = {
  isOpen: boolean;
  onClose: () => void;
  onSuccess: (record: MedicalRecord, message: string) => void;
};

// Cấu hình các trường và cách nhóm chúng
const fieldConfig: FieldConfig<MedicalRecordCreate> = {
  fields: [
    { key: "fullName", label: "Họ và Tên", type: "text", required: true },
    { key: "dateOfBirth", label: "Ngày Sinh (YYYY-MM-DD)", type: "text", required: true },
    { key: "gender", label: "Giới Tính", type: "enum", options: ["MALE", "FEMALE", "OTHER"], required: true },
    { key: "phoneNumber", label: "Số Điện Thoại", type: "text", required: true },
    { key: "email", label: "Email", type: "text", required: true },
    { key: "address", label: "Địa Chỉ", type: "textarea", required: true },
  ],
};

const fieldGroups = [
  ["fullName"],
  ["dateOfBirth", "gender"],
  ["phoneNumber", "email"],
  ["address"],
];

export default function AddPatientForm({ isOpen, onClose, onSuccess }: AddPatientFormProps) {
  const [isSaving, setIsSaving] = useState(false);
  const [apiError, setApiError] = useState<string | null>(null);

  const handleSave = async (formData: MedicalRecordCreate) => {
    setIsSaving(true);
    setApiError(null);
    try {
      const result = await addMedicalRecord(formData);
      onSuccess(result, "Thêm hồ sơ thành công!");
      onClose();
    } catch (error) {
      const errorMessage = error instanceof Error ? error.message : "Đã xảy ra lỗi không xác định.";
      setApiError(errorMessage);
    } finally {
      setIsSaving(false);
    }
  };

  return (
    <DynamicAddUpdateForm
      isOpen={isOpen}
      onClose={onClose}
      schema={createPatientSchema}
      initialValue={{}} // Form thêm mới luôn bắt đầu trống
      onSave={handleSave}
      isSaving={isSaving}
      title="Thêm Hồ Sơ Bệnh Nhân"
      isUpdate={false} // Đánh dấu đây là form thêm mới
      fieldConfig={fieldConfig}
      fieldGroups={fieldGroups}
      apiError={apiError}
    />
  );
}