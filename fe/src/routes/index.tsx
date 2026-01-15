// src/routes/index.tsx

import { Route, Routes, Navigate, useNavigate } from "react-router-dom";
import ProtectedRoute from "../components/ProtectedRoute";

//Layout
import GeneralLayout from "../layouts/GeneralLayout";
import AuthLayout from "@/layouts/AuthLayout";

//pages
import Unauthorized from "../pages/Unauthorized";
import NotFound from "../pages/NotFound";
import ServerError from "../pages/ServerError";
import AdminRoute from "./AdminRoute";
import TestOrderRoute from "./TestOrderRoute";
import InstrumentRoute from "./InstrumentRoute";
import PatientRoute from "./PatientRoute";
import MonitoringRoute from "./MonitoringRoute";
import Login from "../features/User/pages/Auth/Login";
import Payment from "@/pages/Payment/Payment";

import WarehouseRoute from "./WarehouseRoute";
import { useEffect } from "react";
import { setNavigator } from "@/utils/Navigation";
import PaymentSuccess from "@/pages/Payment/PaymentSuccess";
import PaymentCancel from "@/pages/Payment/PaymentCancel";


export default function AppRouting() {
  const navigate = useNavigate();

  useEffect(() => {
    setNavigator(navigate);
  }, [navigate]);

  return (
    <>
      <Routes>
        <Route element={<AuthLayout />}>
          <Route index element={<Login />} />
        </Route>

        <Route element={<GeneralLayout />}>
          {/* <Route index element={<Home />} /> */}
          <Route path="/unauthorized" element={<Unauthorized />} />
          <Route path="*" element={<NotFound />} />
          <Route path="/server-error" element={<ServerError />} />
          <Route path="/payment/:id" element={<Payment />} />
          <Route path="/payment/success" element={<PaymentSuccess />} />
          <Route path="/payment/cancel" element={<PaymentCancel />} />
        </Route>

        {/* <Route path="/warehouse/*" element={<WarehouseRoute />} />
        <Route path="/test-orders/*" element={<TestOrderRoute />} />
        <Route path="/instruments/*" element={<InstrumentRoute />} />
        <Route path="/instrument/dashboard" element={<Navigate to="/instruments/dashboard" replace />} />
        <Route path="/instrument/*" element={<Navigate to="/instruments" replace />} />

        <Route path="/patients/*" element={<PatientRoute />} />
        <Route path="/monitoring/*" element={<MonitoringRoute />} /> */}

        <Route element={<ProtectedRoute allowedRoles={["PATIENT"]}/>}>
          <Route path="/patients/*" element={<PatientRoute />} />
        </Route>

        <Route
          element={
            <ProtectedRoute allowedRoles={["ADMIN", "LAB_USER", "LAB USER"]} />
          }
        >
          <Route path="/admin/*" element={<AdminRoute />} />
          <Route path="/warehouse/*" element={<WarehouseRoute />} />
          <Route path="/test-orders/*" element={<TestOrderRoute />} />
          <Route path="/instruments/*" element={<InstrumentRoute />} />
          <Route
            path="/instrument/dashboard"
            element={<Navigate to="/instruments/dashboard" replace />}
          />
          <Route
            path="/instrument/*"
            element={<Navigate to="/instruments" replace />}
          />

          <Route path="/patients/*" element={<PatientRoute />} />
          <Route path="/monitoring/*" element={<MonitoringRoute />} />
        </Route>
      </Routes>
    </>
  );
}
