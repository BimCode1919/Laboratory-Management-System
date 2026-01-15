// src/pages/Home.tsx

import { Box, Button, Typography, Container, Paper } from "@mui/material";
import { useNavigate } from "react-router-dom";
import { useState } from "react";
import LoginForm from "../features/User/components/LoginForm";

export default function Home() {
  const navigate = useNavigate();
  const [showLogin, setShowLogin] = useState(false);

  return (
    <Container maxWidth="md" sx={{ py: 8 }}>
      <Paper
        elevation={3}
        sx={{
          p: 6,
          textAlign: "center",
          borderRadius: 3,
          backgroundColor: "#ffffffdd",
        }}
      >
        <Typography variant="h3" fontWeight="bold" gutterBottom>
          Welcome to the Lab Management System
        </Typography>

        <Typography variant="h6" color="text.secondary" sx={{ mb: 4 }}>
          Manage patients, test orders, instruments, and results — all in one
          place.
        </Typography>

        <Box sx={{ display: "flex", justifyContent: "center", gap: 2 }}>
          <Button
            variant="contained"
            size="large"
            onClick={() => navigate("/login")}
          >
            Login
          </Button>
        </Box>
        <Typography variant="body2" mt={5}>Temporary content</Typography>
      </Paper>
       {showLogin && <LoginForm onClose={() => setShowLogin(false)} />}
    </Container>
  );
}
