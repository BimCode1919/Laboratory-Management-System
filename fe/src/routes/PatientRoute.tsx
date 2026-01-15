// src/routes/PatientRoute.tsx
// This route file contains all pages belonging to the Patient Service
// Every page under the "/patients" is registered here
import { Route, Routes } from "react-router-dom";
import GeneralLayout from "../layouts/GeneralLayout";
import PatientListPage from "../features/Patient/pages/PatientListPage";
import EditRecordPage from "../features/Patient/pages/EditRecordPage";
import MedicalRecordDetailPage from "../features/Patient/pages/MedicalRecordDetailPage";
import DashboardLayout from "@/layouts/DashboardLayout";
import PatientViewPage from "@/features/Patient/pages/PatientViewPage";

export default function PatientRoute() {
  return (
    <Routes>
      <Route element={<GeneralLayout />}>
        <Route path="me" element={<PatientViewPage />} />
      </Route>

      <Route element={<DashboardLayout />}>
        <Route path="list" element={<PatientListPage />} />
        <Route path="detail/:id" element={<MedicalRecordDetailPage />} />
        <Route path="edit/:id" element={<EditRecordPage />} />
      </Route>
    </Routes>
  );
}
