// src/pages/Payment/PaymentSuccess.tsx

import { useEffect, useRef, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { capturePaymentForTestOrder } from "@/features/TestOrder/services/TestOrderServices";
import {
  Box,
  Typography,
  CircularProgress,
  Button,
  Paper,
  Grid,
  Divider,
} from "@mui/material";
import CheckCircleOutlineIcon from "@mui/icons-material/CheckCircleOutline";
import ErrorOutlineIcon from "@mui/icons-material/ErrorOutline";
import type { PaymentDTO } from "@/features/TestOrder/types/TestOrder";

export default function PaymentSuccess() {
  const location = useLocation();
  const navigate = useNavigate();
  const [status, setStatus] = useState<string>("Capturing payment...");
  const [loading, setLoading] = useState(true);
  const [payment, setPayment] = useState<PaymentDTO | null>(null);

  const hasCaptured = useRef(false);

  useEffect(() => {
    if (hasCaptured.current) return;
    hasCaptured.current = true;

    const params = new URLSearchParams(location.search);
    const token = params.get("token");
    const paymentId = localStorage.getItem("paymentId");

    const capture = async () => {
      if (!paymentId || !token) {
        setStatus("Missing payment details. Cannot capture payment.");
        setLoading(false);
        return;
      }

      try {
        const response = await capturePaymentForTestOrder(paymentId, token);
        if (response) {
          setPayment(response);
          setStatus("Payment captured successfully!");
        }
      } catch (err) {
        setStatus("An error occurred while capturing payment.");
        console.error("Error: ", err);
      } finally {
        setLoading(false);
        localStorage.removeItem("paymentId");
      }
    };

    capture();
  }, [location.search]);

  const isSuccess = status.includes("successfully");
  const isFailure =
    status.includes("failed") ||
    status.includes("error") ||
    status.includes("Missing");
  const statusColor = isSuccess
    ? "success.main"
    : isFailure
    ? "error.main"
    : "text.primary";
  const StatusIcon = isSuccess ? CheckCircleOutlineIcon : ErrorOutlineIcon;
  
  // const cardBackgroundColor = isSuccess
  //   ? "success.light"
  //   : isFailure
  //   ? "error.light"
  //   : "background.paper";
  const cardShadowColor = isSuccess
    ? "rgba(62, 233, 67, 0.5)"
    : isFailure
    ? "rgba(244, 67, 54, 0.5)"
    : "none";

  return (
    <Box textAlign="center" mt={6} px={2}>
      {loading ? (
        <>
          <CircularProgress size={48} sx={{ color: "primary.main" }} />
          <Typography variant="h6" sx={{ mt: 2, color: "text.secondary" }}>
            {status}
          </Typography>
        </>
      ) : (
        <>
          <Box
            sx={{
              display: "inline-flex",
              alignItems: "center",
              p: 2,
              borderRadius: 3,
              backgroundColor: isSuccess
                ? "success.light"
                : isFailure
                ? "error.light"
                : "grey.100",
              boxShadow: isSuccess
                ? `0 0 10px 0 ${cardShadowColor}`
                : isFailure
                ? `0 0 10px 0 ${cardShadowColor}`
                : "none",
              mb: 4,
            }}
          >
            <StatusIcon sx={{ fontSize: 36, color: statusColor, mr: 2 }} />
            <Typography
              variant="h4"
              sx={{
                fontWeight: "bold",
                color: statusColor,
              }}
            >
              {status}
            </Typography>
          </Box>

          {payment && (
            <Paper
              elevation={8}
              sx={{
                p: 5,
                mt: 4,
                maxWidth: 800,
                mx: "auto",
                borderRadius: 4,
                border: `2px solid ${statusColor}`,
              }}
            >
              <Typography
                variant="h5"
                gutterBottom
                sx={{ fontWeight: "bold", mb: 3, color: "text.primary" }}
              >
                Transaction Details
              </Typography>
              <Divider sx={{ mb: 4 }} />

              <Grid container spacing={4} textAlign="left">
                <Grid sx={{ xs: 12, sm: 6 }}>
                  <Typography
                    variant="body2"
                    color="text.secondary"
                    sx={{ mb: 0.5 }}
                  >
                    Transaction ID
                  </Typography>
                  <Typography
                    variant="h6"
                    sx={{ fontWeight: 600, wordBreak: "break-word" }}
                  >
                    {payment.transactionId}
                  </Typography>
                </Grid>

                {/* Amount */}
                <Grid sx={{ xs: 12, sm: 6 }}>
                  <Typography
                    variant="body2"
                    color="text.secondary"
                    sx={{ mb: 0.5 }}
                  >
                    Amount Paid
                  </Typography>
                  <Typography
                    variant="h5"
                    sx={{ fontWeight: 700, color: "primary.dark" }}
                  >
                    {payment.amount.toLocaleString()} VND
                  </Typography>
                </Grid>

                <Divider sx={{ my: 3, width: "100%" }} />

                <Grid sx={{ xs: 12, sm: 6 }}>
                  <Typography
                    variant="body2"
                    color="text.secondary"
                    sx={{ mb: 0.5 }}
                  >
                    Payment Status
                  </Typography>
                  <Typography
                    variant="h6"
                    sx={{
                      fontWeight: 700,
                      color:
                        payment.status.toLowerCase() === "completed"
                          ? "success.dark"
                          : "warning.dark",
                    }}
                  >
                    {payment.status}
                  </Typography>
                </Grid>

                {/* Provider */}
                <Grid sx={{ xs: 12, sm: 6 }}>
                  <Typography
                    variant="body2"
                    color="text.secondary"
                    sx={{ mb: 0.5 }}
                  >
                    Payment Provider
                  </Typography>
                  <Typography variant="h6" sx={{ fontWeight: 600 }}>
                    {payment.provider}
                  </Typography>
                </Grid>
              </Grid>
            </Paper>
          )}

          <Button
            variant="contained"
            color="primary"
            onClick={() => navigate("/test-orders")}
            sx={{
              mt: 5,
              px: 6,
              py: 1.5,
              borderRadius: 3,
              fontWeight: "bold",
              fontSize: "1rem",
            }}
          >
            Back to Orders
          </Button>
        </>
      )}
    </Box>
  );
}
