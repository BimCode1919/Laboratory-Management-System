// src/layouts/GeneralLayout.tsx

import { Box, Container, Typography, useTheme } from "@mui/material";
import { Outlet } from "react-router-dom";
import AppHeader from "../components/Header";
import { useAuth } from "@/context/AuthContext";

const mockToggleTheme = () => {
  console.log("Theme toggle called (Placeholder)");
};

export default function GeneralLayout() {
  const theme = useTheme();
  const { user, role } = useAuth();

  return (
    <Box
      sx={{
        display: "flex",
        flexDirection: "column",
        minHeight: "100vh",
        bgcolor: "grey.50",
      }}
    >
      <AppHeader
        isLoggedIn={true}
        username={user?.fullName}
        userRole={role}
        onToggleTheme={mockToggleTheme}
      />
      <Box
        component="main"
        sx={{
          flexGrow: 1,
          px: 3,
          py: 3,
          minHeight: "calc(100vh - 64px - 48px)",
        }}
      >
        <Container maxWidth="lg" sx={{ height: "100%" }}>
          <Outlet />
        </Container>
      </Box>

      <Box
        component="footer"
        sx={{
          py: 2,
          px: 3,
          borderTop: 1,
          borderColor: "divider",
          bgcolor: "background.paper",
          height: 48,
          display: "flex",
          alignItems: "center",
          justifyContent: "center",
        }}
      >
        <Container maxWidth="lg">
          <Typography
            variant="caption"
            color="text.secondary"
            align="center"
            display="block"
          >
            © 2025 HemaLabManager - Laboratory Management System - HIPAA
            Compliant
          </Typography>
        </Container>
      </Box>
    </Box>
  );
}
