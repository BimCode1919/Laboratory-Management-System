// src/pages/Payment/PaymentCancel.tsx

import { Box, Typography, Button, Paper } from "@mui/material";
import { useNavigate, useLocation } from "react-router-dom";

export default function PaymentCancel() {
  const navigate = useNavigate();
  const location = useLocation();

  const params = new URLSearchParams(location.search);
  const orderId = params.get("orderId");

  return (
    <Box textAlign="center" mt={6} px={2}>
      <Paper
        elevation={3}
        sx={{
          p: 4,
          mt: 4,
          maxWidth: 600,
          mx: "auto",
          borderRadius: 3,
          backgroundColor: "background.paper",
        }}
      >
        <Typography variant="body1" sx={{ mb: 2 }}>
          Your payment process was cancelled. No charges have been made.
        </Typography>

        {orderId && (
          <Typography variant="body2" color="text.secondary">
            Order ID: <strong>{orderId}</strong>
          </Typography>
        )}
      </Paper>

      <Box mt={4} display="flex" justifyContent="center" gap={2}>
        <Button
          variant="contained"
          color="primary"
          onClick={() => navigate("/test-orders")}
          sx={{ px: 4, py: 1.2, borderRadius: 2, fontWeight: "bold" }}
        >
          Back to Orders
        </Button>
      </Box>
    </Box>
  );
}
