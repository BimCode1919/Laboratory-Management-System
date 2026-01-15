// src/pages/ServerError.tsx

import { Box, Button, Paper, Typography } from "@mui/material";
import { useNavigate } from "react-router-dom";

export default function ServerError() {
  const navigate = useNavigate();

  return (
    <Box
      sx={{
        minHeight: "100vh",
        width: "100vw",
        display: "flex",
        alignItems: "center",
        justifyContent: "center",
        px: 2,
      }}
    >
      <Paper
        elevation={3}
        sx={{
          p: 6,
          textAlign: "center",
          borderRadius: 3,
          backgroundColor: "#fff8e1",
        }}
      >
        <Typography
          variant="h3"
          fontWeight={"fontWeightBold"}
          sx={{ color: "#d32f2f" }}
        >
          500 - Server Error
        </Typography>
        <Typography
          variant="h5"
          sx={{ color: "#3a3737ff", marginTop: "15px", marginBottom: "15px" }}
        >
          Something went wrong on our end. Please try again later.
        </Typography>
        <Button
          variant="contained"
          color="primary"
          onClick={() => navigate("/")}
          sx={{ fontWeight: "10" }}
        >
          Go to home
        </Button>
      </Paper>
    </Box>
  );
}
