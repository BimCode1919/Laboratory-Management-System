// src/features/TestOrder/components/TestOrderTable.tsx

import {
  Box,
  Typography,
  TextField,
  InputAdornment,
  Paper,
  Chip,
  Button,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  IconButton,
  Checkbox,
  Snackbar,
  Alert,
  Skeleton,
  Pagination,
  Stack,
  Tooltip,
  // Các component mới cho Search/Filter
  Autocomplete,
  CircularProgress,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Grid,
  debounce,
} from "@mui/material";

// Date Picker imports
import { AdapterDateFns } from "@mui/x-date-pickers/AdapterDateFns";
import { LocalizationProvider } from "@mui/x-date-pickers/LocalizationProvider";
import { DatePicker } from "@mui/x-date-pickers/DatePicker";
import { format } from "date-fns";

import {
  Search as SearchIcon,
  Visibility as VisibilityIcon,
  FilterList as FilterListIcon,
  Add as AddIcon,
  Edit as EditIcon,
  Delete as DeleteIcon,
  Sync as SyncIcon,
} from "@mui/icons-material";
import { reIndexTestOrderDocuments } from "../services/TestOrderServices";
import { useState, useEffect, useMemo } from "react";
import { AxiosError } from "axios";
import api from "../../../api/AxiosInstance";
import { SERVICE_ENDPOINTS } from "../../../api/Endpoints";
import {
  TestOrderFormModal,
  type TestOrderFormData,
  type FullTestOrderDTO,
} from "../../../features/TestOrder/components/TestOrderFormModal";
import { useNavigate } from "react-router-dom";
import CreditCardIcon from "@mui/icons-material/CreditCard";
import type { TestOrderDTO } from "../types/TestOrder";
import type { BaseResponse } from "@/types/BaseResponse";

// Interface mới: Dữ liệu trả về từ Elastic là dạng phẳng (flat)
interface TestOrderListDTO {
  id: string;
  orderCode?: string;
  patientFullName: string; // Thay vì patient.fullName
  patientCode: string;
  testType: "CBC" | "LFT" | "HBA1C";
  status: "PENDING" | "PROCESSING" | "COMPLETED" | "REVIEWED" | "CANCELLED";
  createdAt: string;
  priority: "HIGH" | "LOW" | "NORMAL";
}

interface ApiErrorResponse {
  service: string;
  code: string;
  message: string;
  status: number;
  path: string;
  timestamp: string;
}
interface SpringPage<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  number: number;
  size: number;
}

// Interface cho API Suggest
interface SuggestionDTO {
  id: string;
  patientFullName: string;
}

// Interface quản lý bộ lọc
interface Filters {
  name: string;
  status: string;
  testType: string;
  patientCode: string;
  orderCode: string;
  createdAtFrom: Date | null;
  createdAtTo: Date | null;
  resultParamName: string;
  resultStatus: string;
}

const PAGE_SIZE = 7;

// Helper format ngày cho API (yyyy-MM-dd)
const formatDateForApi = (date: Date | null): string | null => {
  if (!date) return null;
  return format(date, "yyyy-MM-dd");
};

export default function TestOrderTable() {
  // State cho Filters
  const [filters, setFilters] = useState<Filters>({
    name: "",
    status: "all",
    testType: "all",
    patientCode: "",
    orderCode: "",
    createdAtFrom: null,
    createdAtTo: null,
    resultParamName: "",
    resultStatus: "all",
  });
  const [appliedFilters, setAppliedFilters] = useState<Filters>(filters);
  const [filterModalOpen, setFilterModalOpen] = useState(false);

  // State cho Suggestions
  const [suggestions, setSuggestions] = useState<SuggestionDTO[]>([]);
  const [isSuggestionsLoading, setIsSuggestionsLoading] = useState(false);

  // State bảng dữ liệu
  const [orders, setOrders] = useState<TestOrderListDTO[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [snackbar, setSnackbar] = useState<{
    open: boolean;
    message: string;
    severity: "success" | "error";
  } | null>(null);

  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);

  // State Modal CUD (Giữ nguyên logic cũ)
  const [modalOpen, setModalOpen] = useState(false);
  const [modalMode, setModalMode] = useState<"create" | "edit" | "view">(
    "create"
  );
  const [selectedOrderData, setSelectedOrderData] =
    useState<FullTestOrderDTO | null>(null);
  const [isModalLoading, setIsModalLoading] = useState(false);

  const navigate = useNavigate();

  const handleReindexDocuments = async () => {
    reIndexTestOrderDocuments();
    fetchOrders();
  };

  const handleSendToPayment = (testOrderId: string) => {
    navigate(`/payment/${testOrderId}`);
  };

  const handleCloseSnackbar = () => {
    setSnackbar(null);
  };

  const getStatusChipProps = (status: string) => {
    switch (status) {
      case "PENDING":
        return { color: "warning" as const, variant: "outlined" as const };
      case "PROCESSING":
        return { color: "info" as const, variant: "outlined" as const };
      case "COMPLETED":
        return { color: "success" as const, variant: "outlined" as const };
      case "REVIEWED":
        return { color: "default" as const, variant: "filled" as const };
      case "CANCELLED":
        return { color: "error" as const, variant: "outlined" as const };
      default:
        return { color: "default" as const, variant: "outlined" as const };
    }
  };

  const isApiError = (
    error: unknown
  ): error is AxiosError<ApiErrorResponse> => {
    return (
      error instanceof AxiosError &&
      error.response?.data &&
      typeof error.response.data.message === "string"
    );
  };

  // Gọi API Search (Elasticsearch)
  const fetchOrders = async () => {
    setIsLoading(true);
    try {
      const params = {
        page: page - 1,
        size: PAGE_SIZE,
        name: appliedFilters.name || null,
        status: appliedFilters.status === "all" ? null : appliedFilters.status,
        testType:
          appliedFilters.testType === "all" ? null : appliedFilters.testType,
        patientCode: appliedFilters.patientCode || null,
        orderCode: appliedFilters.orderCode || null,
        createdAtFrom: formatDateForApi(appliedFilters.createdAtFrom),
        createdAtTo: formatDateForApi(appliedFilters.createdAtTo),
        resultParamName: appliedFilters.resultParamName || null,
        resultStatus:
          appliedFilters.resultStatus === "all"
            ? null
            : appliedFilters.resultStatus,
      };

      const response = await api.get<SpringPage<TestOrderListDTO>>(
        `${SERVICE_ENDPOINTS.TEST_ORDER}/api/elastic/test-orders/search`,
        {
          params,
          paramsSerializer: (params) => {
            return Object.entries(params)
              .filter(([_, v]) => v != null && v !== "")
              .map(([k, v]) => `${k}=${encodeURIComponent(v as string)}`)
              .join("&");
          },
        }
      );

      const pageData = response.data;
      setOrders(pageData?.content || []);
      setTotalPages(pageData?.totalPages || 0);
      setTotalElements(pageData?.totalElements || 0);
    } catch (error) {
      console.error("Fetch error:", error);
      if (isApiError(error)) {
        setSnackbar({
          open: true,
          message: error.response!.data.message,
          severity: "error",
        });
      } else {
        setSnackbar({
          open: true,
          message: "Failed to fetch orders.",
          severity: "error",
        });
      }
      setOrders([]); // Reset data nếu lỗi
    } finally {
      setIsLoading(false);
    }
  };

  // Gọi API Suggest (Autocomplete)
  const fetchSuggestions = useMemo(
    () =>
      debounce(async (query: string) => {
        if (!query || query.length < 2) {
          setSuggestions([]);
          return;
        }

        setIsSuggestionsLoading(true);
        try {
          const response = await api.get<
            BaseResponse<SpringPage<SuggestionDTO>>
          >(`${SERVICE_ENDPOINTS.TEST_ORDER}/api/elastic/test-orders/suggest`, {
            params: { query },
          });
          // Kiểm tra cấu trúc trả về của suggest (có thể bọc hoặc không bọc BaseResponse tùy BE)
          // Ở đây giả định là BaseResponse -> data -> content
          setSuggestions(response.data.data?.content || []);
        } catch (error) {
          console.error("Suggest error:", error);
          setSuggestions([]);
        } finally {
          setIsSuggestionsLoading(false);
        }
      }, 300),
    []
  );

  // Effect: Fetch lại data khi trang hoặc filter thay đổi
  useEffect(() => {
    fetchOrders();
  }, [page, appliedFilters]);

  // Effect: Fetch suggest khi người dùng nhập tên
  useEffect(() => {
    fetchSuggestions(filters.name);
  }, [filters.name, fetchSuggestions]);

  // Handlers cho UI
  const handleFilterApply = () => {
    setPage(1);
    setAppliedFilters(filters);
    setFilterModalOpen(false);
  };

  const handlePageChange = (event: unknown, newPage: number) => {
    setPage(newPage);
  };

  const handleFilterChange = (e: React.ChangeEvent<any>) => {
    const { name, value } = e.target;
    setFilters((prev) => ({ ...prev, [name]: value }));
  };

  const handleDateChange = (
    name: "createdAtFrom" | "createdAtTo",
    date: Date | null
  ) => {
    setFilters((prev) => ({ ...prev, [name]: date }));
  };

  const handleClearFilters = () => {
    const cleared = {
      name: "",
      status: "all",
      testType: "all",
      patientCode: "",
      orderCode: "",
      createdAtFrom: null,
      createdAtTo: null,
      resultParamName: "",
      resultStatus: "all",
    };
    setFilters(cleared);
    setAppliedFilters(cleared);
    setFilterModalOpen(false);
  };

  // Logic Modal CUD cũ (Giữ nguyên)
  const handleOpenModal = async (
    mode: "create" | "edit" | "view",
    orderId: string | null = null
  ) => {
    setModalMode(mode);
    setModalOpen(true);
    setSelectedOrderData(null);

    if (mode === "create") {
      //
    } else if (orderId) {
      setIsModalLoading(true);
      try {
        const response = await api.get<BaseResponse<FullTestOrderDTO>>(
          `${SERVICE_ENDPOINTS.TEST_ORDER}/api/test-orders/${orderId}`
        );
        setSelectedOrderData(response.data.data);
      } catch (error) {
        if (isApiError(error)) {
          setSnackbar({
            open: true,
            message: error.response!.data.message,
            severity: "error",
          });
        } else {
          setSnackbar({
            open: true,
            message: "Failed to load order details.",
            severity: "error",
          });
        }
        setModalOpen(false);
      } finally {
        setIsModalLoading(false);
      }
    }
  };

  const handleCloseModal = () => {
    setModalOpen(false);
    setSelectedOrderData(null);
  };

  const handleModalSubmit = async (
    mode: "create" | "edit",
    data: TestOrderFormData,
    id: string | null
  ) => {
    try {
      if (mode === "create") {
        await api.post(`${SERVICE_ENDPOINTS.TEST_ORDER}/api/test-orders`, data);
        setSnackbar({
          open: true,
          message: "Test Order created successfully!",
          severity: "success",
        });
      } else if (mode === "edit") {
        const patientCode = selectedOrderData?.patient?.patientCode;
        if (!patientCode) throw new Error("Patient Code is missing.");

        await api.put(
          `${SERVICE_ENDPOINTS.PATIENT}/patients/${patientCode}`,
          data.patient
        );
        setSnackbar({
          open: true,
          message: "Patient updated successfully!",
          severity: "success",
        });
      }
      setModalOpen(false);
      // Chờ 500ms - 800ms để đảm bảo Elasticsearch đã kịp reindexing
      // dữ liệu mới từ Backend trước khi chúng ta gọi API search để lấy lại danh sách Test Orders.
      setTimeout(() => {
        fetchOrders(); // Refresh list
      }, 800);
    } catch (error: any) {
      let errorMessage = "An unexpected error occurred.";
      if (isApiError(error)) errorMessage = error.response!.data.message;
      throw new Error(errorMessage);
    }
  };

  const handleDeleteOrder = async (orderId: string) => {
    if (!window.confirm("Are you sure you want to delete this order?")) return;
    try {
      await api.delete(
        `${SERVICE_ENDPOINTS.TEST_ORDER}/api/test-orders/${orderId}`
      );
      setSnackbar({
        open: true,
        message: "Test Order deleted/cancelled.",
        severity: "success",
      });
      fetchOrders();
    } catch (error) {
      if (isApiError(error)) {
        setSnackbar({
          open: true,
          message: error.response!.data.message,
          severity: "error",
        });
      } else {
        setSnackbar({
          open: true,
          message: "Failed to delete order.",
          severity: "error",
        });
      }
    }
  };

  const handleViewDetail = (testOrderId: string) => {
    navigate(`/test-orders/${testOrderId}`);
  };

  return (
    <LocalizationProvider dateAdapter={AdapterDateFns}>
      <Box sx={{ p: 4 }}>
        {/* Header */}
        <Box
          sx={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
            mb: 3,
          }}
        >
          <Box>
            <Typography variant="h4" gutterBottom>
              Test Orders
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Review and manage patient test orders
            </Typography>
          </Box>
          <Box>
            <Button
              variant="contained"
              startIcon={<AddIcon />}
              size="large"
              onClick={() => handleOpenModal("create")}
            >
              Create New Test Order
            </Button>
            <IconButton onClick={() => handleReindexDocuments()}>
              <SyncIcon />
            </IconButton>
          </Box>
        </Box>

        {/* Search Bar & Filters */}
        <Paper sx={{ p: 2, mb: 3 }}>
          <Box sx={{ display: "flex", gap: 2 }}>
            <Autocomplete
              fullWidth
              freeSolo
              options={suggestions}
              getOptionLabel={(opt) =>
                typeof opt === "string" ? opt : opt.patientFullName
              }
              loading={isSuggestionsLoading}
              onInputChange={(_, val) =>
                setFilters((prev) => ({ ...prev, name: val }))
              }
              onChange={(_, val) => {
                const name =
                  typeof val === "string" ? val : val?.patientFullName || "";
                setFilters((prev) => ({ ...prev, name }));
              }}
              renderInput={(params) => (
                <TextField
                  {...params}
                  placeholder="Search by Patient Name..."
                  size="small"
                  onKeyDown={(e) => e.key === "Enter" && handleFilterApply()}
                  InputProps={{
                    ...params.InputProps,
                    startAdornment: (
                      <InputAdornment position="start">
                        <SearchIcon />
                      </InputAdornment>
                    ),
                    endAdornment: (
                      <>
                        {isSuggestionsLoading ? (
                          <CircularProgress size={20} />
                        ) : null}
                        {params.InputProps.endAdornment}
                      </>
                    ),
                  }}
                />
              )}
              renderOption={(props, option) => (
                <li {...props} key={option.id}>
                  {option.patientFullName}
                </li>
              )}
            />
            <Button
              variant="outlined"
              startIcon={<FilterListIcon />}
              sx={{ minWidth: 120 }}
              onClick={() => setFilterModalOpen(true)}
            >
              Filters
            </Button>
          </Box>
        </Paper>

        {/* Table */}
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow sx={{ bgcolor: "grey.100" }}>
                <TableCell padding="checkbox">
                  <Checkbox />
                </TableCell>
                <TableCell>Order Code</TableCell>
                <TableCell>Patient Name</TableCell>
                <TableCell>Test Type</TableCell>
                <TableCell>Status</TableCell>
                <TableCell>Date Created</TableCell>
                <TableCell align="center">Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {isLoading
                ? Array.from(new Array(PAGE_SIZE)).map((_, index) => (
                    <TableRow key={index}>
                      <TableCell padding="checkbox">
                        <Skeleton variant="circular" width={18} height={18} />
                      </TableCell>
                      <TableCell>
                        <Skeleton variant="text" />
                      </TableCell>
                      <TableCell>
                        <Skeleton variant="text" />
                      </TableCell>
                      <TableCell>
                        <Skeleton variant="text" />
                      </TableCell>
                      <TableCell>
                        <Skeleton variant="rounded" width={80} height={24} />
                      </TableCell>
                      <TableCell>
                        <Skeleton variant="text" />
                      </TableCell>
                      <TableCell align="center">
                        <Stack
                          direction="row"
                          spacing={0.5}
                          justifyContent="center"
                        >
                          <Skeleton variant="circular" width={34} height={34} />
                          <Skeleton variant="circular" width={34} height={34} />
                          <Skeleton variant="circular" width={34} height={34} />
                        </Stack>
                      </TableCell>
                    </TableRow>
                  ))
                : orders.map((order) => (
                    <TableRow key={order.id} hover>
                      <TableCell padding="checkbox">
                        <Checkbox />
                      </TableCell>
                      <TableCell>{order.orderCode}</TableCell>
                      {/* Dùng field phẳng */}
                      <TableCell>{order.patientFullName}</TableCell>
                      <TableCell>{order.testType}</TableCell>
                      <TableCell>
                        <Chip
                          label={order.status}
                          size="small"
                          {...getStatusChipProps(order.status)}
                        />
                      </TableCell>
                      <TableCell>
                        {new Date(order.createdAt).toLocaleString()}
                      </TableCell>
                      <TableCell align="center">
                        <Stack
                          direction="row"
                          spacing={0.5}
                          justifyContent="center"
                        >
                          <Tooltip title="View Details">
                            <IconButton
                              size="small"
                              color="default"
                              onClick={() => handleViewDetail(order.id)}
                            >
                              <VisibilityIcon />
                            </IconButton>
                          </Tooltip>
                          <Tooltip title="Edit Patient Info">
                            <IconButton
                              size="small"
                              color="primary"
                              onClick={() => handleOpenModal("edit", order.id)}
                            >
                              <EditIcon />
                            </IconButton>
                          </Tooltip>
                          <Tooltip title="Delete Order">
                            <IconButton
                              size="small"
                              color="error"
                              onClick={() => handleDeleteOrder(order.id)}
                            >
                              <DeleteIcon />
                            </IconButton>
                          </Tooltip>
                          <Tooltip title="Pay Order">
                            <IconButton
                              size="small"
                              color="success"
                              onClick={() => handleSendToPayment(order.id)}
                            >
                              <CreditCardIcon />
                            </IconButton>
                          </Tooltip>
                        </Stack>
                      </TableCell>
                    </TableRow>
                  ))}
            </TableBody>
          </Table>
        </TableContainer>

        <Box
          sx={{
            display: "flex",
            justifyContent: "space-between",
            alignItems: "center",
            mt: 2,
          }}
        >
          <Typography variant="body2" color="text.secondary">
            Hiển thị {orders.length > 0 ? (page - 1) * PAGE_SIZE + 1 : 0}-
            {(page - 1) * PAGE_SIZE + orders.length} trên {totalElements}
          </Typography>
          <Pagination
            count={totalPages}
            page={page}
            onChange={handlePageChange}
            color="primary"
            disabled={isLoading}
          />
        </Box>

        {/* Modal CUD */}
        <TestOrderFormModal
          open={modalOpen}
          onClose={handleCloseModal}
          onSubmit={handleModalSubmit}
          mode={modalMode}
          initialData={selectedOrderData}
          isLoading={isModalLoading}
        />

        {/* Modal Filter */}
        <Dialog
          open={filterModalOpen}
          onClose={() => setFilterModalOpen(false)}
        >
          <DialogTitle>Advanced Filters</DialogTitle>
          <DialogContent>
            <Grid container spacing={2} sx={{ pt: 1, minWidth: 400 }}>
              <Grid sx={{ xs: 6 }}>
                <FormControl fullWidth size="small">
                  <InputLabel>Status</InputLabel>
                  <Select
                    name="status"
                    value={filters.status}
                    label="Status"
                    onChange={handleFilterChange}
                  >
                    <MenuItem value="all">All Statuses</MenuItem>
                    <MenuItem value="PENDING">Pending</MenuItem>
                    <MenuItem value="PROCESSING">Processing</MenuItem>
                    <MenuItem value="COMPLETED">Completed</MenuItem>
                    <MenuItem value="REVIEWED">Reviewed</MenuItem>
                    <MenuItem value="CANCELLED">Cancelled</MenuItem>
                  </Select>
                </FormControl>
              </Grid>
              <Grid sx={{ xs: 6 }}>
                <FormControl fullWidth size="small">
                  <InputLabel>Test Type</InputLabel>
                  <Select
                    name="testType"
                    value={filters.testType}
                    label="Test Type"
                    onChange={handleFilterChange}
                  >
                    <MenuItem value="all">All Types</MenuItem>
                    <MenuItem value="CBC">CBC</MenuItem>
                    <MenuItem value="LFT">LFT</MenuItem>
                    <MenuItem value="HBA1C">HBA1C</MenuItem>
                  </Select>
                </FormControl>
              </Grid>
              <Grid sx={{ xs: 6 }}>
                <TextField
                  name="patientCode"
                  label="Patient Code"
                  size="small"
                  fullWidth
                  value={filters.patientCode}
                  onChange={handleFilterChange}
                />
              </Grid>
              <Grid sx={{ xs: 6 }}>
                <TextField
                  name="orderCode"
                  label="Order Code"
                  size="small"
                  fullWidth
                  value={filters.orderCode}
                  onChange={handleFilterChange}
                />
              </Grid>
              <Grid sx={{ xs: 6 }}>
                <DatePicker
                  label="Created From"
                  format="MM/dd/yyyy"
                  value={filters.createdAtFrom}
                  onChange={(d) => handleDateChange("createdAtFrom", d)}
                  slotProps={{ textField: { size: "small", fullWidth: true } }}
                />
              </Grid>
              <Grid sx={{ xs: 6 }}>
                <DatePicker
                  label="Created To"
                  format="MM/dd/yyyy"
                  value={filters.createdAtTo}
                  onChange={(d) => handleDateChange("createdAtTo", d)}
                  slotProps={{ textField: { size: "small", fullWidth: true } }}
                />
              </Grid>
              <Grid sx={{ xs: 12 }}>
                <Typography variant="subtitle2" gutterBottom sx={{ mt: 1 }}>
                  Result Filters (Nested)
                </Typography>
              </Grid>
              <Grid sx={{ xs: 6 }}>
                <TextField
                  name="resultParamName"
                  label="Result Parameter Name"
                  size="small"
                  fullWidth
                  value={filters.resultParamName}
                  onChange={handleFilterChange}
                />
              </Grid>
              <Grid sx={{ xs: 6 }}>
                <FormControl fullWidth size="small">
                  <InputLabel>Result Status</InputLabel>
                  <Select
                    name="resultStatus"
                    value={filters.resultStatus}
                    label="Result Status"
                    onChange={handleFilterChange}
                  >
                    <MenuItem value="all">All Statuses</MenuItem>
                    <MenuItem value="PENDING">Pending</MenuItem>
                    <MenuItem value="REVIEWING">Reviewing</MenuItem>
                    <MenuItem value="REVIEWED">Reviewed</MenuItem>
                  </Select>
                </FormControl>
              </Grid>
            </Grid>
          </DialogContent>
          <DialogActions>
            <Button onClick={handleClearFilters}>Clear Filters</Button>
            <Button
              onClick={() => {
                setFilters(appliedFilters);
                setFilterModalOpen(false);
              }}
            >
              Cancel
            </Button>
            <Button variant="contained" onClick={handleFilterApply}>
              Apply Search
            </Button>
          </DialogActions>
        </Dialog>

        {/* Snackbar */}
        {snackbar && (
          <Snackbar
            open={snackbar.open}
            autoHideDuration={6000}
            onClose={handleCloseSnackbar}
            anchorOrigin={{ vertical: "bottom", horizontal: "right" }}
          >
            <Alert
              onClose={handleCloseSnackbar}
              severity={snackbar.severity}
              sx={{ width: "100%" }}
            >
              {snackbar.message}
            </Alert>
          </Snackbar>
        )}
      </Box>
    </LocalizationProvider>
  );
}
