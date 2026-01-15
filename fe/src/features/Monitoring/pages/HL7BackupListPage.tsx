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
  getAllHL7Backups,
  getHL7BackupsByInstrument,
  deleteHL7Backup,
} from "../service/hl7BackupApiService";
import type { HL7Backup, HL7BackupPaginatedResponse } from "../types/HL7Backup";
import HL7BackupTable from "../components/HL7BackupTable";
import HL7BackupFilterPanel from "../components/HL7BackupFilterPanel";
import HL7BackupDetailModal from "../components/HL7BackupDetailModal";

export default function HL7BackupListPage() {
  const [records, setRecords] = useState<HL7Backup[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedRecord, setSelectedRecord] = useState<HL7Backup | null>(null);
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
  const [instrumentIdFilter, setInstrumentIdFilter] = useState("all");
  const sortField = "createdAt";
  const sortOrder = "desc";

  // Sorting state
  const clientSortField: keyof HL7Backup = "createdAt";

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
      let data: HL7BackupPaginatedResponse;

      if (instrumentIdFilter !== "all") {
        data = await getHL7BackupsByInstrument(
          instrumentIdFilter,
          page,
          rowsPerPage
        );
      } else {
        data = await getAllHL7Backups(page, rowsPerPage, sortField, sortOrder);
      }

      let filteredContent = data.results ?? [];

      let finalTotalElements = data.totalItems ?? filteredContent.length;

      if (searchText) {
        const searchLower = searchText.toLowerCase();
        filteredContent = filteredContent.filter(
          (record) =>
            record.runId.toLowerCase().includes(searchLower) ||
            record.barcode.toLowerCase().includes(searchLower)
        );
        // If client-side searching, update the total count
        finalTotalElements = filteredContent.length;
      }

      // Apply status filter client-side
      if (statusFilter !== "all") {
        filteredContent = filteredContent.filter(
          (record) => record.status === statusFilter
        );
        // Update total count after client-side filtering
        finalTotalElements = filteredContent.length;
      }

      if (Array.isArray(filteredContent) && filteredContent.length > 0) {
        filteredContent.sort((a, b) => {
          const aValue = a[clientSortField];
          const bValue = b[clientSortField];

          if (aValue === bValue) return 0;

          let comparison = 0;

          if (
            clientSortField === "createdAt" &&
            typeof aValue === "string" &&
            typeof bValue === "string"
          ) {
            const aDate = new Date(aValue).getTime();
            const bDate = new Date(bValue).getTime();
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
          : "Không thể tải danh sách HL7 backups.";
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
  }, [page, rowsPerPage, searchText, statusFilter, instrumentIdFilter]);

  useEffect(() => {
    fetchRecords();
  }, [fetchRecords]);

  // Get unique instrument IDs from records
  const availableInstrumentIds = useMemo(() => {
    const instrumentIds = new Set<string>();
    records.forEach((record) => {
      if (record.instrumentId) {
        instrumentIds.add(record.instrumentId);
      }
    });
    return Array.from(instrumentIds).sort();
  }, [records]);

  const handleViewDetail = (record: HL7Backup) => {
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
      await deleteHL7Backup(deletingRecordId);
      setSnackbar({
        open: true,
        message: "Xóa HL7 backup thành công!",
        severity: "success",
      });
      fetchRecords();
    } catch (err: unknown) {
      setSnackbar({
        open: true,
        message:
          err instanceof Error
            ? err.message
            : "Đã xảy ra lỗi khi xóa HL7 backup.",
        severity: "error",
      });
    } finally {
      handleCloseDeleteDialog();
    }
  };

  const handleClearFilters = () => {
    setSearchText("");
    setStatusFilter("all");
    setInstrumentIdFilter("all");
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
          Quản lý HL7 Backups
        </Typography>
      </Box>

      {/* Filter Panel */}
      <HL7BackupFilterPanel
        searchText={searchText}
        onSearchChange={setSearchText}
        statusFilter={statusFilter}
        onStatusFilterChange={setStatusFilter}
        instrumentIdFilter={instrumentIdFilter}
        onInstrumentIdFilterChange={setInstrumentIdFilter}
        onClearFilters={handleClearFilters}
        availableInstrumentIds={availableInstrumentIds}
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
          <HL7BackupTable
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
        <HL7BackupDetailModal
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
            Bạn có chắc chắn muốn xóa HL7 backup này không? Hành động này không
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
