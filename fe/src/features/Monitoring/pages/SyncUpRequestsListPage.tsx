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
  getAllSyncUpRequests,
  getSyncUpRequestsByStatus,
  deleteSyncUpRequest,
} from "../service/syncUpRequestsApiService";
import type {
  SyncUpRequests,
  SyncUpRequestsPaginatedResponse,
  SyncUpRequestsStatus,
} from "../types/SyncUpRequests";
import SyncUpRequestsTable from "../components/SyncUpRequestsTable";
import SyncUpRequestsFilterPanel from "../components/SyncUpRequestsFilterPanel";
import SyncUpRequestsDetailModal from "../components/SyncUpRequestsDetailModal";

export default function SyncUpRequestsListPage() {
  const [records, setRecords] = useState<SyncUpRequests[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedRecord, setSelectedRecord] = useState<SyncUpRequests | null>(
    null
  );
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
  const [sourceServiceFilter, setSourceServiceFilter] = useState("all");
  const sortField = "createdAt";
  const sortOrder = "desc";

  // Sorting state
  const clientSortField: keyof SyncUpRequests = "createdAt";

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
      let data: SyncUpRequestsPaginatedResponse;

      if (statusFilter !== "all") {
        data = await getSyncUpRequestsByStatus(
          statusFilter as SyncUpRequestsStatus,
          page,
          rowsPerPage
        );
      } else {
        data = await getAllSyncUpRequests(
          page,
          rowsPerPage,
          sortField,
          sortOrder
        );
      }

      const recordsArray = data.results ?? [];

      let filteredContent = recordsArray;
      let finalTotalElements = data.totalItems ?? recordsArray.length;

      if (searchText) {
        const searchLower = searchText.toLowerCase();
        filteredContent = filteredContent.filter(
          (record) =>
            record.sourceService.toLowerCase().includes(searchLower) ||
            record.messageId.toLowerCase().includes(searchLower)
        );
        finalTotalElements = filteredContent.length;
      }

      if (sourceServiceFilter !== "all") {
        filteredContent = filteredContent.filter(
          (record) => record.sourceService === sourceServiceFilter
        );
        finalTotalElements = filteredContent.length;
      }

      if (Array.isArray(filteredContent) && filteredContent.length > 0) {
        filteredContent.sort((a, b) => {
          const aValue = a[clientSortField];
          const bValue = b[clientSortField];

          if (aValue === bValue) return 0;

          let comparison = 0;

          if (
            (clientSortField === "createdAt" ||
              clientSortField === "updatedAt" ||
              clientSortField === "processedAt") &&
            typeof aValue === "string" &&
            typeof bValue === "string"
          ) {
            const aDate = new Date(aValue).getTime();
            const bDate = new Date(bValue || 0).getTime();
            comparison = aDate - bDate;
          } else if (typeof aValue === "string" && typeof bValue === "string") {
            comparison = aValue.localeCompare(bValue);
          } else {
            if (aValue < bValue) comparison = -1;
            else if (aValue > bValue) comparison = 1;
            else comparison = 0;
          }

          return -comparison;
        });
      }

      setRecords(filteredContent);
      setTotalElements(finalTotalElements);
    } catch (err: unknown) {
      const errorMessage =
        err instanceof Error
          ? err.message
          : "Không thể tải danh sách sync up requests.";
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
  }, [page, rowsPerPage, searchText, statusFilter, sourceServiceFilter]); 

  useEffect(() => {
    fetchRecords();
  }, [fetchRecords]);

  const availableSourceServices = useMemo(() => {
    const services = new Set<string>();
    records.forEach((record) => {
      if (record.sourceService) {
        services.add(record.sourceService);
      }
    });
    return Array.from(services).sort();
  }, [records]);

  const handleViewDetail = (record: SyncUpRequests) => {
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
      await deleteSyncUpRequest(deletingRecordId);
      setSnackbar({
        open: true,
        message: "Xóa sync up request thành công!",
        severity: "success",
      });
      fetchRecords();
    } catch (err: unknown) {
      setSnackbar({
        open: true,
        message:
          err instanceof Error
            ? err.message
            : "Đã xảy ra lỗi khi xóa sync up request.",
        severity: "error",
      });
    } finally {
      handleCloseDeleteDialog();
    }
  };

  const handleClearFilters = () => {
    setSearchText("");
    setStatusFilter("all");
    setSourceServiceFilter("all");
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
      <Box
        display="flex"
        justifyContent="space-between"
        alignItems="center"
        mb={3}
      >
        <Typography variant="h4" fontWeight="bold">
          Quản lý Sync Up Requests
        </Typography>
      </Box>

      {/* Filter Panel */}
      <SyncUpRequestsFilterPanel
        searchText={searchText}
        onSearchChange={setSearchText}
        statusFilter={statusFilter}
        onStatusFilterChange={setStatusFilter}
        sourceServiceFilter={sourceServiceFilter}
        onSourceServiceFilterChange={setSourceServiceFilter}
        onClearFilters={handleClearFilters}
        availableSourceServices={availableSourceServices}
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
          <SyncUpRequestsTable
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
        <SyncUpRequestsDetailModal
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
            Bạn có chắc chắn muốn xóa sync up request này không? Hành động này
            không thể hoàn tác.
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
