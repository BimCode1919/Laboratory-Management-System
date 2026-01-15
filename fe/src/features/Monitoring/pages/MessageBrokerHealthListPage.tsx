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
  TablePagination,
  CircularProgress,
} from "@mui/material";
import { useState, useEffect, useCallback, useMemo } from "react";
import {
  getAllMessageBrokerHealths,
  getMessageBrokerHealthByStatus,
  getMessageBrokerHealthByBrokerName,
  deleteMessageBrokerHealth,
} from "../service/messageBrokerHealthApiService";
import type {
  MessageBrokerHealth,
  MessageBrokerHealthStatus,
} from "../types/MessageBrokerHealth";
import MessageBrokerHealthTable from "../components/MessageBrokerHealthTable";
import MessageBrokerHealthFilterPanel from "../components/MessageBrokerHealthFilterPanel";
import MessageBrokerHealthDetailModal from "../components/MessageBrokerHealthDetailModal";

export default function MessageBrokerHealthListPage() {
  const [records, setRecords] = useState<MessageBrokerHealth[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedRecord, setSelectedRecord] = useState<MessageBrokerHealth | null>(null);
  const [isDetailModalOpen, setIsDetailModalOpen] = useState(false);
  const [confirmDeleteOpen, setConfirmDeleteOpen] = useState(false);
  const [deletingRecordId, setDeletingRecordId] = useState<string | null>(null);

  // Pagination states
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [totalElements, setTotalElements] = useState(0);

  // Filter states
  const [searchText, setSearchText] = useState("");
  const [statusFilter, setStatusFilter] = useState("all");
  const [brokerNameFilter, setBrokerNameFilter] = useState("all");
  const sortField = "createdAt";
  const sortOrder = "desc";

  const [snackbar, setSnackbar] = useState<{
    open: boolean;
    message: string;
    severity: "success" | "error";
  }>({
    open: false,
    message: "",
    severity: "success",
  });

  const fetchRecords = useCallback(async () => {
    setIsLoading(true);
    setError(null);
    try {
      let data;
      
      // Apply filters (CRITICAL: Assuming service functions now return { data: [...], totalItems: N, ... })
      if (statusFilter !== "all" && brokerNameFilter !== "all") {
        // If both filters are set, we need to filter by status first, then client-side filter by broker name
        const statusData = await getMessageBrokerHealthByStatus(
          statusFilter as MessageBrokerHealthStatus,
          page,
          rowsPerPage
        );
        
        // --- FIX 1: Access statusData.data, use safety check (?? []) ---
        // Client-side filter by broker name
        const filtered = (statusData.data ?? []).filter( 
          (record) => record.brokerName === brokerNameFilter
        );
        
        // --- FIX 2: Use 'data' and 'totalItems' for the combined data object ---
        data = {
          ...statusData,
          data: filtered, 
          totalItems: filtered.length,
        };
      } else if (statusFilter !== "all") {
        data = await getMessageBrokerHealthByStatus(
          statusFilter as MessageBrokerHealthStatus,
          page,
          rowsPerPage
        );
      } else if (brokerNameFilter !== "all") {
        data = await getMessageBrokerHealthByBrokerName(
          brokerNameFilter,
          page,
          rowsPerPage
        );
      } else {
        data = await getAllMessageBrokerHealths(
          page,
          rowsPerPage,
          sortField,
          sortOrder
        );
      }

      // --- FIX 3: Safely retrieve the records array from data.data ---
      const recordsArray = data.data ?? []; 

      // Apply search filter client-side if search text exists
      let filteredContent = recordsArray; // Start with the safe array
      // --- FIX 4: Use data.totalItems for the total count ---
      let finalTotalElements = data.totalItems; 
      
      if (searchText && recordsArray.length > 0) {
        const searchLower = searchText.toLowerCase();
        
        // Use the safe array 'recordsArray' for filtering
        filteredContent = recordsArray.filter(
          (record) =>
            record.brokerName.toLowerCase().includes(searchLower) ||
            (record.errorCode && record.errorCode.toLowerCase().includes(searchLower)) ||
            (record.errorMessage &&
              record.errorMessage.toLowerCase().includes(searchLower))
        );
        
        // If client-side searching, the displayed total count should be the filtered length
        finalTotalElements = filteredContent.length;
      }

      setRecords(filteredContent);
      // --- FIX 5: Set total elements using the calculated safe value ---
      setTotalElements(finalTotalElements);
      
    } catch (err: unknown) {
      const errorMessage =
        err instanceof Error
          ? err.message
          : "Không thể tải danh sách message broker health records.";
      setError(errorMessage);
      setSnackbar({
        open: true,
        message: errorMessage,
        severity: "error",
      });
      setRecords([]);
      setTotalElements(0);
    } finally {
      setIsLoading(false);
    }
  }, [page, rowsPerPage, searchText, statusFilter, brokerNameFilter]);

  useEffect(() => {
    fetchRecords();
  }, [fetchRecords]);

  // Get unique broker names from records
  const availableBrokerNames = useMemo(() => {
    const brokerNames = new Set<string>();
    records.forEach((record) => {
      if (record.brokerName) {
        brokerNames.add(record.brokerName);
      }
    });
    return Array.from(brokerNames).sort();
  }, [records]);

  const handleViewDetail = (record: MessageBrokerHealth) => {
    setSelectedRecord(record);
    setIsDetailModalOpen(true);
  };

  const handleOpenDeleteDialog = (recordId: string) => {
    setDeletingRecordId(recordId);
    setConfirmDeleteOpen(true);
  };

  const handleCloseDeleteDialog = () => {
    setDeletingRecordId(null);
    setConfirmDeleteOpen(false);
  };

  const handleConfirmDelete = async () => {
    if (!deletingRecordId) return;
    try {
      await deleteMessageBrokerHealth(deletingRecordId);
      setSnackbar({
        open: true,
        message: "Xóa message broker health record thành công!",
        severity: "success",
      });
      fetchRecords();
    } catch (err: unknown) {
      setSnackbar({
        open: true,
        message:
          err instanceof Error
            ? err.message
            : "Đã xảy ra lỗi khi xóa message broker health record.",
        severity: "error",
      });
    } finally {
      handleCloseDeleteDialog();
    }
  };

  const handleClearFilters = () => {
    setSearchText("");
    setStatusFilter("all");
    setBrokerNameFilter("all");
    setPage(0);
  };

  const handleCloseSnackbar = () => {
    setSnackbar({ ...snackbar, open: false });
  };

  const handleChangePage = (_event: unknown, newPage: number) => {
    setPage(newPage);
  };

  const handleChangeRowsPerPage = (
    event: React.ChangeEvent<HTMLInputElement>
  ) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
  };

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4" fontWeight="bold">
          Quản lý Message Broker Health Check
        </Typography>
      </Box>

      {/* Filter Panel */}
      <MessageBrokerHealthFilterPanel
        searchText={searchText}
        onSearchChange={setSearchText}
        statusFilter={statusFilter}
        onStatusFilterChange={setStatusFilter}
        brokerNameFilter={brokerNameFilter}
        onBrokerNameFilterChange={setBrokerNameFilter}
        onClearFilters={handleClearFilters}
        availableBrokerNames={availableBrokerNames}
      />

      {/* Table */}
      {isLoading && records.length === 0 ? (
        <Box display="flex" justifyContent="center" alignItems="center" p={4}>
          <CircularProgress />
        </Box>
      ) : error && records.length === 0 ? (
        <Box p={4} textAlign="center">
          <Typography variant="body1" color="error">
            {error}
          </Typography>
        </Box>
      ) : (
        <>
          <MessageBrokerHealthTable
            records={records}
            isLoading={isLoading}
            onViewDetail={handleViewDetail}
            onDelete={handleOpenDeleteDialog}
          />
          <TablePagination
            rowsPerPageOptions={[5, 10, 25, 50]}
            component="div"
            count={totalElements}
            rowsPerPage={rowsPerPage}
            page={page}
            onPageChange={handleChangePage}
            onRowsPerPageChange={handleChangeRowsPerPage}
            labelRowsPerPage="Số hàng mỗi trang:"
            labelDisplayedRows={({ from, to, count }) =>
              `${from}-${to} của ${count !== -1 ? count : `nhiều hơn ${to}`}`
            }
          />
        </>
      )}

      {/* Detail Modal */}
      {selectedRecord && (
        <MessageBrokerHealthDetailModal
          isOpen={isDetailModalOpen}
          onClose={() => setIsDetailModalOpen(false)}
          record={selectedRecord}
        />
      )}

      {/* Delete Confirmation Dialog */}
      <Dialog open={confirmDeleteOpen} onClose={handleCloseDeleteDialog}>
        <DialogTitle>Xác nhận xóa</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Bạn có chắc chắn muốn xóa message broker health record này không? Hành động này không thể hoàn tác.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDeleteDialog}>Hủy</Button>
          <Button onClick={handleConfirmDelete} color="error" autoFocus>
            Xóa
          </Button>
        </DialogActions>
      </Dialog>

      {/* Snackbar */}
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

