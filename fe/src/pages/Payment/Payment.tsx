// src/pages/Payment/Payment.tsx

import { createPaymentForTestOrder } from "@/features/TestOrder/services/TestOrderServices";
import type { PaymentDTO } from "@/features/TestOrder/types/TestOrder";
import { Box, Typography, Paper, Grid, CircularProgress } from "@mui/material";
import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";

export default function Payment() {
  const { id } = useParams<{ id: string }>();
  const [payment, setPayment] = useState<PaymentDTO | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const createPayment = async () => {
      if (!id) return;
      try {
        const response = await createPaymentForTestOrder(id);
        if (response?.approvalUrl && response?.id) {
          setPayment(response);
          localStorage.setItem("paymentId", response.id);

          const link = document.createElement("a");
          link.href = response.approvalUrl;
          link.target = "_blank";
          document.body.appendChild(link);
          link.click();
          document.body.removeChild(link);
        }
      } catch (err) {
        console.error("Error creating payment:", err);
      } finally {
        setLoading(false);
      }
    };

    createPayment();
  }, [id]);

  return (
    <Box textAlign="center" mt={4}>
      {loading ? (
        <CircularProgress />
      ) : (
        <>
          <Typography variant="h6" gutterBottom>
            Processing payment for order {id}...
          </Typography>

          {payment && (
            <Paper
              elevation={3}
              sx={{ p: 3, mt: 3, maxWidth: 600, mx: "auto" }}
            >
              <Typography variant="h6" gutterBottom>
                Payment Details
              </Typography>
              <Grid container spacing={2}>
                <Grid sx={{ xs: 6 }}>
                  <Typography variant="body2" color="textSecondary">
                    Payment ID
                  </Typography>
                  <Typography variant="body1">{payment.id}</Typography>
                </Grid>
                <Grid sx={{ xs: 6 }}>
                  <Typography variant="body2" color="textSecondary">
                    Order ID
                  </Typography>
                  <Typography variant="body1">{payment.orderId}</Typography>
                </Grid>
                <Grid sx={{ xs: 6 }}>
                  <Typography variant="body2" color="textSecondary">
                    Amount
                  </Typography>
                  <Typography variant="body1">${payment.amount}</Typography>
                </Grid>
                <Grid sx={{ xs: 6 }}>
                  <Typography variant="body2" color="textSecondary">
                    Provider
                  </Typography>
                  <Typography variant="body1">{payment.provider}</Typography>
                </Grid>
                <Grid sx={{ xs: 6 }}>
                  <Typography variant="body2" color="textSecondary">
                    Status
                  </Typography>
                  <Typography variant="body1">{payment.status}</Typography>
                </Grid>
              </Grid>

              <Typography variant="body2" sx={{ mt: 2 }}>
                Redirecting to PayPal for approval...
              </Typography>
            </Paper>
          )}
        </>
      )}
    </Box>
  );
}
