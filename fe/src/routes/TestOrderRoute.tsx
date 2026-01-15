// src/routes/TestOrderRoute.tsx
// This route file contains all pages belonging to the Test Order Service
// Every page under the "/test-orders" is registered here

import { Route, Routes } from "react-router-dom";
import GeneralLayout from "../layouts/GeneralLayout";
import DashboardLayout from "../layouts/DashboardLayout";

// EXAMPLES:
import TestOrderPage from "../features/TestOrder/pages/TestOrderPage";
import UnitConversionMappingPage from "../features/TestOrder/pages/UnitConversionMappingPage";
import FlaggingRulePage from "../features/TestOrder/pages/FlaggingRulePage";
import ResultParameterMappingPage from "../features/TestOrder/pages/ResultParameterMappingPage";
import TestOrderTable from "@/features/TestOrder/components/TestOrderTable";
import TestOrderDetail from "@/features/TestOrder/pages/TestOrderDetail";

export default function TestOrderRoute() {
  return (
    <Routes>
      {/* <Route element={<GeneralLayout />}>
        <Route path="" element={<TestOrderPage />} />
        <Route path="config/unit-map" element={<UnitConversionMappingPage />} />
        <Route path="config/flag-rule" element={<FlaggingRulePage />} />
        <Route path="config/param-map" element={<ResultParameterMappingPage />} />
        <Route path="list" element={<TestOrderTable/>} />
        <Route path=":id" element={<TestOrderDetail />} />
      </Route> */}

      <Route element={<DashboardLayout />}>
      <Route path="" element={<TestOrderPage />} />
        <Route path="config/unit-map" element={<UnitConversionMappingPage />} />
        <Route path="config/flag-rule" element={<FlaggingRulePage />} />
        <Route path="config/param-map" element={<ResultParameterMappingPage />} />
        <Route path="list" element={<TestOrderTable/>} />
        <Route path=":id" element={<TestOrderDetail />} />
      </Route>
    </Routes>
  );
}
