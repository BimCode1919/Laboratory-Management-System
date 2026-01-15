// src/features/TestOrder/pages/TestOrderPage.tsx

import { Box } from "@mui/material";
import TestOrderTable from "../components/TestOrderTable";

export default function TestOrderPage() {
  return (
    <Box className="p-4 md:p-8">
      <TestOrderTable />
    </Box>
  );
}
