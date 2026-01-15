// src/routes/WarehouseRoute.tsx
// This route file contains all pages belonging to the Warehouse Service
// Every page under the "/warehouse" is registered here


import InstrumentList from "@/features/Warehouse/pages/Instrument/InstrumentList";
import ReagentList from "@/features/Warehouse/pages/Reagent/ReagentList";
import ConfigurationPage from "@/features/Warehouse/pages/Instrument/ConfigurationPage";
import DashboardLayout from "@/layouts/DashboardLayout";
import GeneralLayout from "@/layouts/GeneralLayout";
import { Route, Routes } from "react-router-dom";

export default function WarehouseRoute() {
  return (
    <Routes>
      {/* <Route element={<GeneralLayout />}>
        <Route path="instruments" element={<InstrumentList />} />
        <Route path="configs" element={<ConfigurationPage />} />
        <Route path="reagents" element={<ReagentList />} />
      </Route> */}

      <Route element={<DashboardLayout />}>
        <Route path="instruments" element={<InstrumentList />} />
        <Route path="configs" element={<ConfigurationPage />} />
        <Route path="reagents" element={<ReagentList />} />
      </Route>
    </Routes>
  );
}