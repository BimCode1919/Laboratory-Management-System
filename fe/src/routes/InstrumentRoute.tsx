// src/routes/InstrumentRoute.tsx
// This route file contains all pages belonging to the Instrument Service
// Every page under the "/instruments" is registered here

import { Routes, Route } from "react-router-dom";
import InstrumentDashboard from "../features/Instrument/pages/InstrumentDashboard";
import InstrumentHome from "../features/Instrument/pages/InstrumentHome";
import GeneralLayout from "@/layouts/GeneralLayout";
import DashboardLayout from "@/layouts/DashboardLayout";

export default function InstrumentRoute() {
  return (
    <Routes>
      <Route element={<GeneralLayout />}>
        {/* <Route index element={<InstrumentHome />} />
        <Route path="home" element={<InstrumentHome />} />
        <Route path="dashboard" element={<InstrumentDashboard />} /> */}
      </Route>

      <Route element={<DashboardLayout />}>
        <Route index element={<InstrumentHome />} />
        <Route path="home" element={<InstrumentHome />} />
        <Route path="dashboard" element={<InstrumentDashboard />} />
      </Route>
    </Routes>
  );
}
