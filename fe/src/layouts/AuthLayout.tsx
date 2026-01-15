// src/layouts/AuthLayout.tsx

import { LocalHospital } from "@mui/icons-material";
import { Box, Card, CardContent, Typography } from "@mui/material";
import { Outlet } from "react-router-dom";

export default function AuthLayout() {
  return (
    <Box
      sx={{
        minHeight: "100vh",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        background: "linear-gradient(to bottom right, #E3F8FF, #F8F8FC)",
        p: 2,
      }}
    >
      <Card
        sx={{
          maxWidth: 450,
          width: "100%",
          boxShadow: 8,
          borderRadius: 2,
        }}
      >
        <CardContent sx={{ p: { xs: 3, sm: 4 } }}>
          <Box sx={{ textAlign: "center", mb: 4 }}>
            <Box
              sx={{
                width: 256,
                height: 256,
                borderRadius: "50%",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                mx: "auto",
                mb: -1,
              }}
            >
              <img
                 src="/HemaLabManager - Altered.png"
                // src="/public/HemaLabManager - Full.png"
                alt="Logo of a Blood Lab Management System depicting the Rod of Asclepius, a serpent-entwined staff, and the primary symbol for medicine and healthcare, representing the Greek god of healing, Asclepius."
              />
            </Box>
            <Typography variant="h5" gutterBottom sx={{ fontWeight: 600 }}>
              Laboratory Management System
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Secure access for healthcare professionals
            </Typography>
          </Box>

          <Outlet />

          <Box sx={{ mt: 4, pt: 3, borderTop: 1, borderColor: "divider" }}>
            <Typography
              variant="caption"
              color="text.secondary"
              align="center"
              display="block"
            >
              © 2025 HemaLabManager - Laboratory Management System
            </Typography>
            <Typography
              variant="caption"
              color="text.secondary"
              align="center"
              display="block"
            >
              HIPAA Compliant | All access logged and monitored
            </Typography>
          </Box>
        </CardContent>
      </Card>
    </Box>
  );
}
