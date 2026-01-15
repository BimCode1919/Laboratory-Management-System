import { Route, Routes, Navigate } from "react-router-dom";
import AdminDashboard from "../features/User/pages/AdminDashboard";
import UserList from "../features/User/pages/UserList";
import RoleList from "../features/User/pages/RoleList";
import DashboardLayout from "@/layouts/DashboardLayout";

export default function AdminRoute() {
  return (
    <Routes>
      <Route element={<DashboardLayout />}>
        <Route index element={<AdminDashboard />} /> 
        <Route path="users" element={<UserList />} />
        <Route path="roles" element={<RoleList />} />
        <Route path="monitoring" element={<Navigate to="/monitoring/event-logs" replace />} />
      </Route>
    </Routes>
  );
}