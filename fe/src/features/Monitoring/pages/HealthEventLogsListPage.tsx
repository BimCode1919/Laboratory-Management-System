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
  getAllHealthEventLogs,
  getHealthEventLogsByEventType,
  getHealthEventLogsByBrokerId,
  deleteHealthEventLog,
} from "../service/healthEventLogsApiService";
import type {
  HealthEventLog,
  HealthEventType,
  HealthEventLogPaginatedResponse, // Import thêm interface này để rõ ràng hơn
} from "../types/HealthEventLogs";
import HealthEventLogsTable from "../components/HealthEventLogsTable";
import HealthEventLogsFilterPanel from "../components/HealthEventLogsFilterPanel";
import HealthEventLogsDetailModal from "../components/HealthEventLogsDetailModal";

export default function HealthEventLogsListPage() {
  const [records, setRecords] = useState<HealthEventLog[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [selectedRecord, setSelectedRecord] = useState<HealthEventLog | null>(null);
  const [isDetailModalOpen, setIsDetailModalOpen] = useState(false);
  const [confirmDeleteOpen, setConfirmDeleteOpen] = useState(false);
  const [deletingRecordId, setDeletingRecordId] = useState<string | null>(null);

  // Pagination states
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [totalElements, setTotalElements] = useState(0);

  // Filter states
  const [searchText, setSearchText] = useState("");
  const [eventTypeFilter, setEventTypeFilter] = useState("all");
  const [brokerIdFilter, setBrokerIdFilter] = useState("all");
  const sortField = "createdAt";
  const sortOrder = "desc";

  // Sorting state
  const clientSortField: keyof HealthEventLog = "createdAt";

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
      // Khai báo data là loại trả về từ API (hoặc null/undefined ban đầu)
      let apiResponseData: HealthEventLogPaginatedResponse | null = null; 
      
      // 1. Lấy dữ liệu từ API dựa trên bộ lọc
      if (eventTypeFilter !== "all" && brokerIdFilter !== "all") {
        // 1a. Fetch data by Event Type
        const eventTypeData = await getHealthEventLogsByEventType(
          eventTypeFilter as HealthEventType,
          page,
          rowsPerPage
        );
        
        // 1b. Client-side filter by Broker ID
        // Đảm bảo logs được xử lý là một mảng (dùng ?? [] )
        const logsToFilter = eventTypeData.logs ?? [];
        
        const filteredLogs = logsToFilter.filter(
          (record) => record.brokerId === brokerIdFilter
        );
        
        // Gán lại kết quả đã lọc với cấu trúc chính xác
        apiResponseData = {
          ...eventTypeData,
          logs: filteredLogs, 
          totalItems: filteredLogs.length, // Cập nhật tổng số item sau khi lọc client-side
        };
      } else if (eventTypeFilter !== "all") {
        apiResponseData = await getHealthEventLogsByEventType(
          eventTypeFilter as HealthEventType,
          page,
          rowsPerPage
        );
      } else if (brokerIdFilter !== "all") {
        apiResponseData = await getHealthEventLogsByBrokerId(
          brokerIdFilter,
          page,
          rowsPerPage
        );
      } else {
        apiResponseData = await getAllHealthEventLogs(
          page,
          rowsPerPage,
          sortField,
          sortOrder
        );
      }

      let filteredContent = apiResponseData?.logs ?? [];
      let totalItems = apiResponseData?.totalItems ?? filteredContent.length;

      // 3. Apply search filter client-side
      if (searchText) {
        const searchLower = searchText.toLowerCase();
        filteredContent = filteredContent.filter(
          (record) =>
            record.brokerId.toLowerCase().includes(searchLower) ||
            record.details.toLowerCase().includes(searchLower)
        );
        // Cập nhật lại totalItems sau khi tìm kiếm client-side
        totalItems = filteredContent.length; 
      }

      // 4. Apply client-side sorting
      // Logic sort chỉ chạy khi mảng có phần tử, và filteredContent đã chắc chắn là mảng
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
      setTotalElements(totalItems); 
    } catch (err: unknown) {
      const errorMessage =
        err instanceof Error
          ? err.message
          : "Không thể tải danh sách health event logs.";
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
  }, [page, rowsPerPage, searchText, eventTypeFilter, brokerIdFilter]);

  useEffect(() => {
    fetchRecords();
  }, [fetchRecords]);

  // Get unique broker IDs from records
  const availableBrokerIds = useMemo(() => {
    const brokerIds = new Set<string>();
    // Lấy ID từ bản ghi hiện tại
    records.forEach((record) => { 
      if (record.brokerId) {
        brokerIds.add(record.brokerId);
      }
    });
    return Array.from(brokerIds).sort();
  }, [records]);

  const handleViewDetail = (record: HealthEventLog) => {
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
      await deleteHealthEventLog(deletingRecordId);
      setSnackbar({
        open: true,
        message: "Xóa health event log thành công!",
        severity: "success",
      });
      fetchRecords();
    } catch (err: unknown) {
      setSnackbar({
        open: true,
        message:
          err instanceof Error
            ? err.message
            : "Đã xảy ra lỗi khi xóa health event log.",
        severity: "error",
      });
    } finally {
      handleCloseDeleteDialog();
    }
  };

  const handleClearFilters = () => {
    setSearchText("");
    setEventTypeFilter("all");
    setBrokerIdFilter("all");
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
          Quản lý Health Event Logs
        </Typography>
      </Box>

      {/* Filter Panel */}
      <HealthEventLogsFilterPanel
        searchText={searchText}
        onSearchChange={setSearchText}
        eventTypeFilter={eventTypeFilter}
        onEventTypeFilterChange={setEventTypeFilter}
        brokerIdFilter={brokerIdFilter}
        onBrokerIdFilterChange={setBrokerIdFilter}
        onClearFilters={handleClearFilters}
        availableBrokerIds={availableBrokerIds}
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
          <HealthEventLogsTable
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
        <HealthEventLogsDetailModal
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
            Bạn có chắc chắn muốn xóa health event log này không? Hành động này không thể hoàn tác.
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