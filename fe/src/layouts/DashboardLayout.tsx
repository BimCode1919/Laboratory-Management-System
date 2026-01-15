// src/layouts/DashboardLayout.tsx

import { Outlet, useLocation, useNavigate } from "react-router-dom";
import { Box } from "@mui/material";
import DashboardSidebar from "../components/DashboardSidebar";
import AppHeader from "@/components/Header";
import { ToastContainer } from "react-toastify";
import "react-toastify/dist/ReactToastify.css";
import { useAuth } from "@/context/AuthContext";

export default function DashboardLayout() {
  const navigate = useNavigate();
  const location = useLocation();
  const { user, role } = useAuth();

  return (
    <Box sx={{ display: "flex", minHeight: "100vh", bgcolor: "grey.50" }}>
      <DashboardSidebar
        currentPage={location.pathname}
        onNavigate={(path) => navigate(path)}
      />

      <Box sx={{ flexGrow: 1, display: "flex", flexDirection: "column" }}>
        <AppHeader
          isLoggedIn={true}
          username={user?.fullName}
          userRole={role}
        />
        <ToastContainer
          position="top-right"
          autoClose={4000}
          hideProgressBar={false}
          newestOnTop={false}
          closeOnClick
          rtl={false}
          pauseOnFocusLoss
          draggable
          pauseOnHover
        />
        <Box
          component="main"
          sx={{
            flexGrow: 1,
            p: 3,
            overflow: "auto",
          }}
        >
          <Outlet />
        </Box>
      </Box>
    </Box>
  );
}
