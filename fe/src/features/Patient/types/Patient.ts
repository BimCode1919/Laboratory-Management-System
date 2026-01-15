// src/features/patients/types/Patient.ts

export type Gender = "MALE" | "FEMALE" | "OTHER";
export type TestRecordStatus = "PENDING" | "PROCESSING" | "COMPLETED" | "CANCELLED" | "REVIEWED";
export type RecordStatus = string; // Thêm định nghĩa này

// Dữ liệu chi tiết của Patient
export interface Patient {
  id: string;
  patientCode: string;
  fullName: string;
  dateOfBirth: string;
  gender: Gender;
  address: string;
  phone: string;
  email: string;
  age: number;
}

// === TẠO TYPE MỚI CHO KẾT QUẢ TÌM KIẾM ===
// Type này khớp với MedicalRecordDocument của backend
export interface MedicalRecordSearchResult {
  id: string; // Sửa từ recordId để khớp với document
  patientId: string;
  patientCode: string;
  fullName: string;
  dateOfBirth: string;
  lastTestDate: string | null;
  // Lưu ý: gender và status không có trong kết quả tìm kiếm
}


// Type này đại diện cho dữ liệu của trang GET /medical/{id} (không phải search)
export interface MedicalRecord {
  recordId: string;
  patient: Patient;
  fullName: string;
  dateOfBirth: string;
  gender: Gender;
  status: RecordStatus;
  createdAt: string;
  createdBy: string;
  updatedAt: string | null;
  updatedBy: string | null;
  totalNotes: number;
  // Type này không có lastTestDate và patientCode ở cấp cao nhất
}

// DTO để tạo mới
export interface MedicalRecordCreate {
  fullName: string;
  dateOfBirth: string;
  gender: Gender;
  address: string;
  phoneNumber: string;
  email: string;
}

export interface ClinicalNoteInfo {
  id: string;
  note: string;
  notedBy: string;
  createdAt: string;
}

export interface TestRecordInfo {
  id: string;
  testOrderId: string;
  interpretation: string | null;
  status: TestRecordStatus;
  testCompletedAt: string | null;
  instrumentDetailsJson: string | null;
}

// Dữ liệu chi tiết cho trang edit/detail
export interface MedicalRecordDetail {
  id: string; 
  patient: Patient; 
  lastTestDate: string | null;
  clinicalNotes: ClinicalNoteInfo[];
  testRecords: TestRecordInfo[];
}


export interface PatientUpdateDTO {
  fullName?: string;
  address?: string;
  phone?: string;
  email?: string;
}

export interface InterpretationUpdateDTO {
  testRecordId: string;
  interpretation: string;
  status?: TestRecordStatus;
}

export interface MedicalRecordUpdate {
  patientDTO?: PatientUpdateDTO;
  clinicalNotes?: string[];
  interpretation?: InterpretationUpdateDTO[];
}