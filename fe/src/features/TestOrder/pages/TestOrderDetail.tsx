// src/features/TestOrder/pages/TestOrderDetail.tsx

import {
  Box,
  Container,
  Typography,
  Paper,
  Card,
  CardContent,
  Grid,
  Button,
  Chip,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Breadcrumbs,
  Link,
  CircularProgress,
  Alert,
  Tooltip,
} from "@mui/material";
import {
  Person as PersonIcon,
  CalendarToday as CalendarTodayIcon,
  Badge as BadgeIcon,
  FileDownload as FileDownloadIcon,
  NavigateNext as NavigateNextIcon,
  Warning as WarningIcon,
  Error as ErrorIcon,
  CheckCircle as CheckCircleIcon,
  Info as InfoIcon,
  Schedule as ScheduleIcon,
} from "@mui/icons-material";
import { useState, useEffect } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { getDetailedTestOrder } from "../services/TestOrderServices";
import type { TestOrderDTO } from "../types/TestOrder";
import type { TestResultGeneralDTO } from "../types/TestResults";
import { getAllTestResultsByTestOrderId } from "../services/TestResultServices";
import ExportModal from "../components/ExportModal";
import PatientLongitudinalChart from "../components/PatientLongitudinalChart";

const formatDate = (
  dateString: string | undefined,
  format: "date" | "datetime"
) => {
  if (!dateString) return "N/A";
  try {
    const date = new Date(dateString);
    if (isNaN(date.getTime())) return "N/A";

    const datePart = date.toLocaleDateString("en-GB");
    const timePart = date.toLocaleTimeString("en-GB", {
      hour: "2-digit",
      minute: "2-digit",
    });

    return format === "date" ? datePart : `${datePart} ${timePart}`;
  } catch {
    return "N/A";
  }
};

const getFlagIcon = (status: string) => {
  switch (status.toLowerCase()) {
    case "high":
    case "low":
      return <WarningIcon fontSize="small" />;
    case "normal":
      return <CheckCircleIcon fontSize="small" />;
    default:
      return <InfoIcon fontSize="small" />;
  }
};

const getFlagColor = (
  status: string
): "error" | "warning" | "success" | "default" => {
  switch (status.toLowerCase()) {
    case "critical":
      return "error";
    case "high":
    case "low":
      return "warning";
    case "normal":
      return "success";
    default:
      return "default";
  }
};

const truncateComment = (comment: string | null): string => {
  if (!comment) return "No comment.";
  const maxLength = 60;
  return comment.length > maxLength
    ? comment.substring(0, maxLength) + "..."
    : comment;
};

export default function TestOrderDetail() {
  const [exportModalOpen, setExportModalOpen] = useState(false);
  const [testOrder, setTestOrder] = useState<TestOrderDTO | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [testResults, setTestResults] = useState<TestResultGeneralDTO[]>([]);

  const navigate = useNavigate();
  const { id } = useParams<{ id: string }>();

  const testOrderId = id;

  useEffect(() => {
    if (!testOrderId) {
      setLoading(false);
      setError("Test Order ID is missing from the URL.");
      return;
    }

    const fetchOrderDetails = async () => {
      setLoading(true);
      setError(null);
      try {
        const orderData = await getDetailedTestOrder(testOrderId);
        const resultsData = await getAllTestResultsByTestOrderId(testOrderId);

        if (orderData) {
          setTestOrder(orderData);

          if (resultsData) {
            setTestResults(resultsData as TestResultGeneralDTO[]);
          } else {
            setTestResults([]);
          }
        } else {
          setError(`Test Order with ID ${testOrderId} not found.`);
        }
      } catch (err) {
        console.error("Fetch error:", err);
        setError("Failed to load test order details. Please try again.");
      } finally {
        setLoading(false);
      }
    };

    fetchOrderDetails();
  }, [testOrderId]);

  if (loading) {
    return (
      <Container maxWidth="lg" sx={{ py: 8, textAlign: "center" }}>
        <CircularProgress />
        <Typography variant="h6" sx={{ mt: 2 }}>
          Loading Test Order...
        </Typography>
      </Container>
    );
  }

  if (error || !testOrder) {
    return (
      <Container maxWidth="lg" sx={{ py: 8 }}>
        <Alert severity="error">
          {error || `Failed to load details for Test Order ID: ${testOrderId}.`}
        </Alert>
        <Button
          variant="contained"
          sx={{ mt: 3 }}
          onClick={() => navigate("/doctor/test-orders")}
        >
          Back to List
        </Button>
      </Container>
    );
  }

  const patientInfo = testOrder.patient;
  const chartPatientId = testOrder.patient.patientId;

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Breadcrumbs
        separator={<NavigateNextIcon fontSize="small" />}
        sx={{ mb: 3 }}
      >
        <Link
          underline="hover"
          color="inherit"
          href="#"
          onClick={(e) => {
            e.preventDefault();
            navigate("/test-orders");
          }}
          sx={{
            display: "flex",
            alignItems: "center",
            gap: 0.5,
            cursor: "pointer",
          }}
        >
          Test Orders
        </Link>
        <Typography color="text.primary">
          {testOrder.orderCode || testOrder.id}
        </Typography>
      </Breadcrumbs>

      <Box
        sx={{
          mb: 4,
          display: "flex",
          justifyContent: "space-between",
          alignItems: "flex-start",
        }}
      >
        <Box>
          <Typography variant="h4" gutterBottom>
            Test Order Detail: {testOrder.orderCode || testOrder.id}
          </Typography>
          <Typography variant="body2" color="text.secondary">
            View detailed test results and analysis
          </Typography>
        </Box>
        <Button
          variant="contained"
          size="large"
          startIcon={<FileDownloadIcon />}
          onClick={() => setExportModalOpen(true)}
        >
          Export Results
        </Button>
      </Box>

      <Card sx={{ mb: 4, borderLeft: 4, borderColor: "primary.main" }}>
        <CardContent>
          <Box sx={{ display: "flex", alignItems: "center", gap: 2, mb: 2 }}>
            <Box
              sx={{
                width: 56,
                height: 56,
                borderRadius: "50%",
                bgcolor: "primary.main",
                display: "flex",
                alignItems: "center",
                justifyContent: "center",
                color: "white",
              }}
            >
              <PersonIcon sx={{ fontSize: 32 }} />
            </Box>
            <Box sx={{ flex: 1 }}>
              <Typography variant="h6">{patientInfo.fullName}</Typography>
              <Typography variant="body2" color="text.secondary">
                {patientInfo.email ||
                  patientInfo.phoneNumber ||
                  "No contact info"}
              </Typography>
            </Box>
            <Chip
              label={testOrder.status}
              color={testOrder.status === "COMPLETED" ? "success" : "info"}
              sx={{ fontWeight: 600 }}
            />
          </Box>

          <Grid container spacing={3}>
            <Grid sx={{ xs: 12, sm: 3 }}>
              <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
                <BadgeIcon color="action" fontSize="small" />
                <Box>
                  <Typography
                    variant="caption"
                    color="text.secondary"
                    display="block"
                  >
                    Patient Code
                  </Typography>
                  <Typography variant="body2" fontWeight={500}>
                    {patientInfo.patientCode || "N/A"}
                  </Typography>
                </Box>
              </Box>
            </Grid>
            <Grid sx={{ xs: 12, sm: 3 }}>
              <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
                <CalendarTodayIcon color="action" fontSize="small" />
                <Box>
                  <Typography
                    variant="caption"
                    color="text.secondary"
                    display="block"
                  >
                    Date of Birth / Age
                  </Typography>
                  <Typography variant="body2" fontWeight={500}>
                    {formatDate(patientInfo.dateOfBirth, "date")} (
                    {patientInfo.age || "N/A"} years)
                  </Typography>
                </Box>
              </Box>
            </Grid>
            <Grid sx={{ xs: 12, sm: 3 }}>
              <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
                <PersonIcon color="action" fontSize="small" />
                <Box>
                  <Typography
                    variant="caption"
                    color="text.secondary"
                    display="block"
                  >
                    Gender
                  </Typography>
                  <Typography variant="body2" fontWeight={500}>
                    {patientInfo.gender || "N/A"}
                  </Typography>
                </Box>
              </Box>
            </Grid>
            <Grid sx={{ xs: 12, sm: 3 }}>
              <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
                <ScheduleIcon color="action" fontSize="small" />
                <Box>
                  <Typography
                    variant="caption"
                    color="text.secondary"
                    display="block"
                  >
                    Last Synced
                  </Typography>
                  <Typography variant="body2" fontWeight={500}>
                    {formatDate(patientInfo.lastSyncedAt, "datetime")}
                  </Typography>
                </Box>
              </Box>
            </Grid>
          </Grid>
        </CardContent>
      </Card>

      <Paper sx={{ p: 3, mb: 4 }}>
        <Typography variant="h6" gutterBottom>
          Test Information
        </Typography>
        <Grid container spacing={3}>
          <Grid sx={{ xs: 12, sm: 3 }}>
            <Typography
              variant="caption"
              color="text.secondary"
              display="block"
            >
              Order Code
            </Typography>
            <Typography variant="body1" fontWeight={500}>
              {testOrder.orderCode}
            </Typography>
          </Grid>
          <Grid sx={{ xs: 12, sm: 3 }}>
            <Typography
              variant="caption"
              color="text.secondary"
              display="block"
            >
              Test Type
            </Typography>
            <Typography variant="body1" fontWeight={500}>
              {testOrder.testType}
            </Typography>
          </Grid>
          <Grid sx={{ xs: 12, sm: 3 }}>
            <Typography
              variant="caption"
              color="text.secondary"
              display="block"
            >
              Date Created
            </Typography>
            <Typography variant="body1" fontWeight={500}>
              {formatDate(testOrder.createdAt, "datetime")}
            </Typography>
          </Grid>

          <Grid sx={{ xs: 12, sm: 3 }}>
            <Typography
              variant="caption"
              color="text.secondary"
              display="block"
            >
              Notes
            </Typography>
            <Typography
              variant="body1"
              sx={{ fontStyle: "italic", color: "text.primary" }}
            >
              {testOrder.notes || "No specific notes provided for this order."}
            </Typography>
          </Grid>
        </Grid>
      </Paper>

      <Paper>
        <Box sx={{ p: 3, borderBottom: 1, borderColor: "divider" }}>
          <Typography variant="h6">Test Results</Typography>
          <Typography variant="body2" color="text.secondary">
            Detailed parameter analysis, AI flags, and review status.
          </Typography>
        </Box>

        <TableContainer>
          <Table>
            <TableHead>
              <TableRow sx={{ bgcolor: "grey.100" }}>
                <TableCell sx={{ fontWeight: 600 }}>Parameter Name</TableCell>
                <TableCell sx={{ fontWeight: 600 }} align="right">
                  Result Value
                </TableCell>
                <TableCell sx={{ fontWeight: 600 }}>Unit</TableCell>
                <TableCell sx={{ fontWeight: 600 }}>Reference Range</TableCell>
                <TableCell sx={{ fontWeight: 600 }} align="center">
                  Alert Status
                </TableCell>
                <TableCell sx={{ fontWeight: 600 }} align="center">
                  AI Issue
                </TableCell>
                <TableCell sx={{ fontWeight: 600 }}>
                  AI Review Comment
                </TableCell>
                <TableCell sx={{ fontWeight: 600 }} align="center">
                  Status
                </TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {testResults.length > 0 ? (
                testResults.map((result) => (
                  <TableRow
                    key={result.id}
                    hover
                    sx={{
                      ...(result.alertLevel !== "NORMAL" && {
                        bgcolor:
                          result.alertLevel === "LOW" ||
                          result.alertLevel === "HIGH"
                            ? "error.lightest"
                            : "warning.lightest",
                      }),
                    }}
                  >
                    <TableCell>
                      <Typography variant="body2" fontWeight={500}>
                        {result.parameterName}
                      </Typography>
                    </TableCell>
                    <TableCell align="right">
                      <Typography variant="body2" fontWeight={600}>
                        {result.resultValue}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2" color="text.secondary">
                        {result.unit}
                      </Typography>
                    </TableCell>
                    <TableCell>
                      <Typography variant="body2" color="text.secondary">
                        {result.referenceLow} - {result.referenceHigh}
                      </Typography>
                    </TableCell>
                    <TableCell align="center">
                      <Chip
                        icon={getFlagIcon(result.alertLevel)}
                        label={result.alertLevel}
                        color={getFlagColor(result.alertLevel)}
                        size="small"
                      />
                    </TableCell>
                    <TableCell align="center">
                      {result.aiHasIssue ? (
                        <Tooltip
                          title="AI flagged potential issue with automated interpretation."
                          arrow
                        >
                          <Chip
                            icon={<ErrorIcon />}
                            label="FLAGGED"
                            color="error"
                            size="small"
                          />
                        </Tooltip>
                      ) : (
                        <Chip label="OK" color="success" size="small" />
                      )}
                    </TableCell>
                    <TableCell>
                      {result.aiReviewComment ? (
                        <Tooltip
                          title={result.aiReviewComment}
                          placement="top-start"
                          arrow
                        >
                          <Typography
                            variant="body2"
                            sx={{
                              cursor: "help",
                              maxWidth: 300,
                              whiteSpace: "nowrap",
                              overflow: "hidden",
                              textOverflow: "ellipsis",
                            }}
                          >
                            {truncateComment(result.aiReviewComment)}
                          </Typography>
                        </Tooltip>
                      ) : (
                        <Typography variant="body2" color="text.secondary">
                          No comment.
                        </Typography>
                      )}
                    </TableCell>
                    <TableCell align="center">
                      <Chip
                        label={result.status}
                        color={getFlagColor(result.status)}
                        size="small"
                        variant="outlined"
                      />
                    </TableCell>
                  </TableRow>
                ))
              ) : (
                <TableRow>
                  <TableCell colSpan={8} align="center" sx={{ py: 4 }}>
                    <Typography variant="body1" color="text.secondary">
                      No detailed test results available.
                    </Typography>
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
      </Paper>

      {chartPatientId ? (
        <Box sx={{ mt: 5 }}>
            <PatientLongitudinalChart patientId={chartPatientId} />
        </Box>
      ) : (
        <Alert severity="warning" sx={{ mt: 5 }}>
            Cannot display the trend chart: Patient ID is missing.
        </Alert>
      )}

      <ExportModal
        open={exportModalOpen}
        onClose={() => setExportModalOpen(false)}
        testOrderId={testOrder.id}
        orderCode={testOrder.orderCode}
      />
    </Container>
  );
}
