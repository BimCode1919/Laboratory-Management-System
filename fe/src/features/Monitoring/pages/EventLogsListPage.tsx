//Fixed
import {
  Box,
  Typography,
  Snackbar,
  Alert,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  Button,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  IconButton,
  Tooltip,
  Chip,
  CircularProgress,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
} from "@mui/material";
import {
  Visibility as VisibilityIcon,
  Delete as DeleteIcon,
  Search as SearchIcon,
  Clear as ClearIcon,
} from "@mui/icons-material";
import { useState, useEffect, useCallback, useMemo } from "react";
import {
  getAllEventLogs,
  deleteEventLog,
} from "../service/eventLogsApiService";
import type { EventLog, Severity } from "../types/EventLogs";
import EventLogDetailModal from "../components/EventLogDetailModal";

export default function EventLogsListPage() {
  const [eventLogs, setEventLogs] = useState<EventLog[]>([]);
  const [filteredEventLogs, setFilteredEventLogs] = useState<EventLog[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedEventLog, setSelectedEventLog] = useState<EventLog | null>(
    null
  );
  const [isDetailModalOpen, setIsDetailModalOpen] = useState(false);
  const [confirmDeleteOpen, setConfirmDeleteOpen] = useState(false);
  const [deletingEventLogId, setDeletingEventLogId] = useState<string | null>(
    null
  );

  // Filter states
  const [searchText, setSearchText] = useState("");
  const [severityFilter, setSeverityFilter] = useState("all");
  const [sourceServiceFilter, setSourceServiceFilter] = useState("all");

  const [snackbar, setSnackbar] = useState<{
    open: boolean;
    message: string;
    severity: "success" | "error";
  }>({
    open: false,
    message: "",
    severity: "success",
  });

  const fetchEventLogs = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      const data = await getAllEventLogs();
      setEventLogs(data.logs);
    } catch (err: unknown) {
      const errorMessage =
        err instanceof Error
          ? err.message
          : "Không thể tải danh sách event logs.";
      setError(errorMessage);
      setSnackbar({
        open: true,
        message: errorMessage,
        severity: "error",
      });
    } finally {
      setIsLoading(false);
    }
  }, []);

  useEffect(() => {
    fetchEventLogs();
  }, [fetchEventLogs]);

  const availableSourceServices = useMemo(() => {
    const services = new Set<string>();

    if (Array.isArray(eventLogs)) {
      eventLogs.forEach((log) => {
        if (log.sourceService) {
          services.add(log.sourceService);
        }
      });
    }

    return Array.from(services).sort();
  }, [eventLogs]);

  // Apply filters
  useEffect(() => {
    let filtered = [...(eventLogs || [])];

    // Search filter
    if (searchText) {
      const searchLower = searchText.toLowerCase();
      filtered = filtered.filter(
        (log) =>
          log.eventName.toLowerCase().includes(searchLower) ||
          log.message.toLowerCase().includes(searchLower) ||
          log.sourceService.toLowerCase().includes(searchLower) ||
          log.performedBy.toLowerCase().includes(searchLower)
      );
    }

    // Severity filter
    if (severityFilter !== "all") {
      filtered = filtered.filter((log) => log.severity === severityFilter);
    }

    // Source service filter
    if (sourceServiceFilter !== "all") {
      filtered = filtered.filter(
        (log) => log.sourceService === sourceServiceFilter
      );
    }

    setFilteredEventLogs(filtered);
  }, [eventLogs, searchText, severityFilter, sourceServiceFilter]);

  const handleViewDetail = (eventLog: EventLog) => {
    setSelectedEventLog(eventLog);
    setIsDetailModalOpen(true);
  };

  const handleOpenDeleteDialog = (eventLogId: string) => {
    setDeletingEventLogId(eventLogId);
    setConfirmDeleteOpen(true);
  };

  const handleCloseDeleteDialog = () => {
    setDeletingEventLogId(null);
    setConfirmDeleteOpen(false);
  };

  const handleConfirmDelete = async () => {
    if (!deletingEventLogId) return;
    try {
      await deleteEventLog(deletingEventLogId);
      setSnackbar({
        open: true,
        message: "Xóa event log thành công!",
        severity: "success",
      });
      fetchEventLogs();
    } catch (err: unknown) {
      setSnackbar({
        open: true,
        message:
          err instanceof Error
            ? err.message
            : "Đã xảy ra lỗi khi xóa event log.",
        severity: "error",
      });
    } finally {
      handleCloseDeleteDialog();
    }
  };

  const handleClearFilters = () => {
    setSearchText("");
    setSeverityFilter("all");
    setSourceServiceFilter("all");
  };

  const handleCloseSnackbar = () => {
    setSnackbar({ ...snackbar, open: false });
  };

  const getSeverityColor = (
    severity: Severity
  ): "default" | "warning" | "error" | "info" => {
    switch (severity) {
      case "CRITICAL":
        return "error";
      case "ERROR":
        return "error";
      case "WARN":
        return "warning";
      case "INFO":
        return "info";
      default:
        return "default";
    }
  };

  const severityOptions: Severity[] = ["INFO", "WARN", "ERROR", "CRITICAL"];
  const hasActiveFilters =
    searchText !== "" ||
    severityFilter !== "all" ||
    sourceServiceFilter !== "all";

  return (
    <Box>
      <Box
        display="flex"
        justifyContent="space-between"
        alignItems="center"
        mb={3}
      >
        <Typography variant="h4" fontWeight="bold">
          Quản lý Event Logs
        </Typography>
      </Box>

      {/* Filter Panel */}
      <Paper elevation={2} sx={{ p: 2, mb: 3 }}>
        <Box display="flex" gap={2} flexWrap="wrap" alignItems="center">
          <TextField
            label="Tìm kiếm"
            variant="outlined"
            size="small"
            value={searchText}
            onChange={(e) => setSearchText(e.target.value)}
            InputProps={{
              startAdornment: (
                <SearchIcon sx={{ mr: 1, color: "text.secondary" }} />
              ),
            }}
            sx={{ minWidth: 250 }}
          />

          <FormControl size="small" sx={{ minWidth: 150 }}>
            <InputLabel>Severity</InputLabel>
            <Select
              value={severityFilter}
              label="Severity"
              onChange={(e) => setSeverityFilter(e.target.value)}
            >
              <MenuItem value="all">Tất cả</MenuItem>
              {severityOptions.map((severity) => (
                <MenuItem key={severity} value={severity}>
                  {severity}
                </MenuItem>
              ))}
            </Select>
          </FormControl>

          {availableSourceServices.length > 0 && (
            <FormControl size="small" sx={{ minWidth: 200 }}>
              <InputLabel>Source Service</InputLabel>
              <Select
                value={sourceServiceFilter}
                label="Source Service"
                onChange={(e) => setSourceServiceFilter(e.target.value)}
              >
                <MenuItem value="all">Tất cả</MenuItem>
                {availableSourceServices.map((service) => (
                  <MenuItem key={service} value={service}>
                    {service}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          )}

          {hasActiveFilters && (
            <Button
              variant="outlined"
              startIcon={<ClearIcon />}
              onClick={handleClearFilters}
              size="small"
            >
              Xóa bộ lọc
            </Button>
          )}
        </Box>
      </Paper>

      {/* Table */}
      <Paper elevation={2}>
        <TableContainer>
          <Table>
            <TableHead>
              <TableRow sx={{ backgroundColor: "#f5f5f5" }}>
                <TableCell>Event Name</TableCell>
                <TableCell>Source Service</TableCell>
                <TableCell>Severity</TableCell>
                <TableCell>Performed By</TableCell>
                <TableCell>Message</TableCell>
                <TableCell>Created At</TableCell>
                <TableCell align="right">Hành động</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {isLoading ? (
                <TableRow>
                  <TableCell colSpan={7} align="center">
                    <CircularProgress />
                  </TableCell>
                </TableRow>
              ) : error && filteredEventLogs.length === 0 ? (
                <TableRow>
                  <TableCell
                    colSpan={7}
                    align="center"
                    sx={{ color: "error.main" }}
                  >
                    {error}
                  </TableCell>
                </TableRow>
              ) : filteredEventLogs.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={7} align="center">
                    <Typography variant="body1" color="text.secondary">
                      Không có event log nào
                    </Typography>
                  </TableCell>
                </TableRow>
              ) : (
                filteredEventLogs.map((eventLog) => (
                  <TableRow key={eventLog.eventLogId} hover>
                    <TableCell>{eventLog.eventName}</TableCell>
                    <TableCell>{eventLog.sourceService}</TableCell>
                    <TableCell>
                      <Chip
                        label={eventLog.severity}
                        color={getSeverityColor(eventLog.severity)}
                        size="small"
                      />
                    </TableCell>
                    <TableCell>{eventLog.performedBy}</TableCell>
                    <TableCell
                      sx={{
                        maxWidth: 300,
                        overflow: "hidden",
                        textOverflow: "ellipsis",
                        whiteSpace: "nowrap",
                      }}
                    >
                      {eventLog.message}
                    </TableCell>
                    <TableCell>
                      {new Date(eventLog.createdAt).toLocaleString()}
                    </TableCell>
                    <TableCell align="right">
                      <Tooltip title="Xem chi tiết">
                        <IconButton
                          onClick={() => handleViewDetail(eventLog)}
                          size="small"
                        >
                          <VisibilityIcon />
                        </IconButton>
                      </Tooltip>
                      <Tooltip title="Xóa">
                        <IconButton
                          onClick={() =>
                            handleOpenDeleteDialog(eventLog.eventLogId)
                          }
                          size="small"
                          color="error"
                        >
                          <DeleteIcon />
                        </IconButton>
                      </Tooltip>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </TableContainer>
      </Paper>

      {selectedEventLog && (
        <EventLogDetailModal
          isOpen={isDetailModalOpen}
          onClose={() => setIsDetailModalOpen(false)}
          eventLog={selectedEventLog}
        />
      )}

      <Dialog open={confirmDeleteOpen} onClose={handleCloseDeleteDialog}>
        <DialogTitle>Xác nhận xóa</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Bạn có chắc chắn muốn xóa event log này không? Hành động này không
            thể hoàn tác.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDeleteDialog}>Hủy</Button>
          <Button onClick={handleConfirmDelete} color="error" autoFocus>
            Xóa
          </Button>
        </DialogActions>
      </Dialog>

      <Snackbar
        open={snackbar.open}
        autoHideDuration={6000}
        onClose={handleCloseSnackbar}
      >
        <Alert
          onClose={handleCloseSnackbar}
          severity={snackbar.severity}
          sx={{ width: "100%" }}
        >
          {snackbar.message}
        </Alert>
      </Snackbar>
    </Box>
  );
}
