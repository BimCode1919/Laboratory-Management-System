// src/features/Patient/pages/PatientViewPage.tsx

import {
  Box,
  Container,
  Typography,
  Paper,
  Card,
  CardContent,
  Grid,
  Divider,
  Chip,
  Alert,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Button,
} from "@mui/material";
import {
  Person as PersonIcon,
  Email as EmailIcon,
  Phone as PhoneIcon,
  LocationOn as LocationOnIcon,
  CalendarToday as CalendarTodayIcon,
  Assignment as AssignmentIcon,
  Comment as CommentIcon,
  Visibility as VisibilityIcon,
} from "@mui/icons-material";
import { useAuth } from "@/context/AuthContext";
import { useEffect, useState } from "react";
import type { UserProfileDTO } from "@/features/User/types/user.types";
import { getCurrentUser } from "@/features/User/services/user.api";

export default function PatientViewPage() {
  const [user, setUser] = useState<UserProfileDTO | null>(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    handleFetchUserData();
  }, []);

  const handleFetchUserData = async () => {
    setLoading(true);

    try {
      const response = await getCurrentUser();
      setUser(response);
    } catch (error) {
      console.error("Failed to fetch user data: ", error);
      return setUser(null);
    } finally {
      setLoading(false);
    }
  };

  const testOrders = [
    {
      id: 1,
      date: "2025-11-07",
      orderId: "TO-1234",
      testName: "Complete Blood Count (CBC)",
      status: "Completed",
      statusColor: "success" as const,
      result: "View Results",
    },
    {
      id: 2,
      date: "2025-11-05",
      orderId: "TO-1198",
      testName: "Comprehensive Metabolic Panel",
      status: "Reviewed",
      statusColor: "success" as const,
      result: "View Results",
    },
    {
      id: 3,
      date: "2025-10-28",
      orderId: "TO-1156",
      testName: "Lipid Panel",
      status: "Completed",
      statusColor: "success" as const,
      result: "View Results",
    },
    {
      id: 4,
      date: "2025-10-15",
      orderId: "TO-1089",
      testName: "Complete Blood Count (CBC)",
      status: "Pending",
      statusColor: "warning" as const,
      result: "Processing",
    },
  ];

  const comments = [
    {
      id: 1,
      date: "2025-11-07",
      author: "Dr. Michael Chen",
      comment:
        "WBC count is slightly elevated. Monitor and follow up in 2 weeks.",
    },
    {
      id: 2,
      date: "2025-11-05",
      author: "Dr. Emily Davis",
      comment:
        "All metabolic markers within normal range. Continue current treatment plan.",
    },
    {
      id: 3,
      date: "2025-10-28",
      author: "Dr. Michael Chen",
      comment:
        "Lipid levels improved since last visit. Keep up with diet and exercise.",
    },
  ];

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Box sx={{ mb: 4 }}>
        <Typography variant="h4" gutterBottom>
          Personal Medical Profile
        </Typography>
        <Typography variant="body2" color="text.secondary">
          View personal information, test results, and medical history
        </Typography>
      </Box>

      <Paper elevation={2} sx={{ p: 3, mb: 4 }}>
        <Box sx={{ display: "flex", alignItems: "center", gap: 2, mb: 3 }}>
          <Box
            sx={{
              width: 80,
              height: 80,
              borderRadius: "50%",
              bgcolor: "primary.main",
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              color: "white",
            }}
          >
            <PersonIcon sx={{ fontSize: 48 }} />
          </Box>
          <Box>
            <Typography variant="h5">{user?.fullName}</Typography>
          </Box>
        </Box>

        <Divider sx={{ my: 3 }} />

        <Grid container spacing={3}>
          <Grid sx={{ xs: 12, md: 6 }}>
            <Box sx={{ display: "flex", alignItems: "center", gap: 1, mb: 2 }}>
              <EmailIcon color="action" />
              <Box>
                <Typography
                  variant="caption"
                  color="text.secondary"
                  display="block"
                >
                  Email
                </Typography>
                <Typography variant="body2">{user?.email}</Typography>
              </Box>
            </Box>
          </Grid>
          <Grid sx={{ xs: 12, md: 6 }}>
            <Box sx={{ display: "flex", alignItems: "center", gap: 1, mb: 2 }}>
              <PhoneIcon color="action" />
              <Box>
                <Typography
                  variant="caption"
                  color="text.secondary"
                  display="block"
                >
                  Phone
                </Typography>
                <Typography variant="body2">{user?.phoneNumber}</Typography>
              </Box>
            </Box>
          </Grid>
          <Grid sx={{ xs: 12, md: 6 }}>
            <Box sx={{ display: "flex", alignItems: "center", gap: 1, mb: 2 }}>
              <CalendarTodayIcon color="action" />
              <Box>
                <Typography
                  variant="caption"
                  color="text.secondary"
                  display="block"
                >
                  Date of Birth
                </Typography>
                <Typography variant="body2">{user?.dob}</Typography>
              </Box>
            </Box>
          </Grid>
          <Grid sx={{ xs: 12, md: 6 }}>
            <Box sx={{ display: "flex", alignItems: "center", gap: 1, mb: 2 }}>
              <PersonIcon color="action" />
              <Box>
                <Typography
                  variant="caption"
                  color="text.secondary"
                  display="block"
                >
                  Gender
                </Typography>
                <Typography variant="body2">{user?.gender}</Typography>
              </Box>
            </Box>
          </Grid>
          <Grid sx={{ xs: 12 }}>
            <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
              <LocationOnIcon color="action" />
              <Box>
                <Typography
                  variant="caption"
                  color="text.secondary"
                  display="block"
                >
                  Address
                </Typography>
                <Typography variant="body2">{user?.address}</Typography>
              </Box>
            </Box>
          </Grid>
        </Grid>
      </Paper>

      <Card sx={{ mb: 4 }}>
        <CardContent>
          <Box sx={{ display: "flex", alignItems: "center", gap: 1, mb: 3 }}>
            <AssignmentIcon color="primary" />
            <Typography variant="h6">Test Order History</Typography>
          </Box>

          <TableContainer>
            <Table>
              <TableHead>
                <TableRow sx={{ bgcolor: "grey.100" }}>
                  <TableCell>Date</TableCell>
                  <TableCell>Order ID</TableCell>
                  <TableCell>Test Name</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell align="right">Actions</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {testOrders.map((order) => (
                  <TableRow key={order.id} hover>
                    <TableCell>{order.date}</TableCell>
                    <TableCell>{order.orderId}</TableCell>
                    <TableCell>{order.testName}</TableCell>
                    <TableCell>
                      <Chip
                        label={order.status}
                        color={order.statusColor}
                        size="small"
                      />
                    </TableCell>
                    <TableCell align="right">
                      <Button
                        size="small"
                        startIcon={<VisibilityIcon />}
                        disabled={order.status === "Pending"}
                      >
                        {order.result}
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </CardContent>
      </Card>

      <Card>
        <CardContent>
          <Box sx={{ display: "flex", alignItems: "center", gap: 1, mb: 3 }}>
            <CommentIcon color="primary" />
            <Typography variant="h6">Medical Comments & Notes</Typography>
          </Box>

          <Box sx={{ display: "flex", flexDirection: "column", gap: 2 }}>
            {comments.map((comment) => (
              <Paper key={comment.id} variant="outlined" sx={{ p: 2 }}>
                <Box
                  sx={{
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "flex-start",
                    mb: 1,
                  }}
                >
                  <Typography variant="subtitle2" color="primary">
                    {comment.author}
                  </Typography>
                  <Typography variant="caption" color="text.secondary">
                    {comment.date}
                  </Typography>
                </Box>
                <Typography variant="body2" color="text.secondary">
                  {comment.comment}
                </Typography>
              </Paper>
            ))}
          </Box>
        </CardContent>
      </Card>

      <Alert severity="info" sx={{ mt: 4 }}>
        Your medical information is protected under HIPAA regulations. Only
        authorized healthcare providers can access your records.
      </Alert>
    </Container>
  );
}
