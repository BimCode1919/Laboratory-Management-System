// src/features/patients/pages/PatientListPage.tsx

import {
  Box, Button, CircularProgress, Paper, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Typography, TablePagination, Snackbar,
  Alert, IconButton, Tooltip, Dialog, DialogActions, DialogContent,
  DialogContentText, DialogTitle, TextField, Grid
} from "@mui/material";
import AddIcon from "@mui/icons-material/Add";
import EditIcon from "@mui/icons-material/Edit";
import DeleteIcon from "@mui/icons-material/Delete";
import SearchIcon from '@mui/icons-material/Search';
import VisibilityIcon from '@mui/icons-material/Visibility';
import { useEffect, useState, useCallback } from "react";
import { searchMedicalRecords, deleteMedicalRecord } from "../services/PatientService";
// Sửa lại import: Chỉ cần MedicalRecordSearchResult cho trang này
import type { MedicalRecordSearchResult } from "../types/Patient";
import AddPatientForm from "../components/AddPatientForm";
import { useNavigate } from "react-router-dom";

export default function PatientListPage() {
  // === FIX 1: SỬ DỤNG ĐÚNG TYPE CHO STATE ===
  const [records, setRecords] = useState<MedicalRecordSearchResult[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [isAddFormOpen, setIsAddFormOpen] = useState(false);
  const [confirmDeleteOpen, setConfirmDeleteOpen] = useState(false);
  const [deletingRecordId, setDeletingRecordId] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(10);
  const [totalElements, setTotalElements] = useState(0);
  const navigate = useNavigate();
  const [snackbar, setSnackbar] = useState<{ open: boolean; message: string; severity: "success" | "error" }>({
    open: false, message: "", severity: "success",
  });
  const [keyword, setKeyword] = useState('');
  const [startDate, setStartDate] = useState('');
  const [endDate, setEndDate] = useState('');

  const fetchRecords = useCallback(() => {
    setIsLoading(true);
    const searchParams = {
      page,
      size: rowsPerPage,
      keyword: keyword || undefined,
      startDate: startDate ? new Date(startDate).toISOString() : undefined,
      endDate: endDate ? new Date(endDate).toISOString() : undefined,
    };

    searchMedicalRecords(searchParams)
      .then((data) => {
        if (data && data.content) {
          setRecords(data.content);
          setTotalElements(data.totalElements);
        } else {
          setRecords([]);
          setTotalElements(0);
        }
        setError(null);
      })
      .catch((err) => {
        setError(err.message);
        setRecords([]);
        setTotalElements(0);
        setSnackbar({ open: true, message: err.message, severity: "error" });
      })
      .finally(() => setIsLoading(false));
  }, [page, rowsPerPage, keyword, startDate, endDate]);

  useEffect(() => {
    fetchRecords();
  }, [fetchRecords]);

  const handleSearch = () => {
    setPage(0);
    fetchRecords();
  };

  // Dùng `any` ở đây vì form thêm mới trả về type khác với type tìm kiếm
  const handleFormSuccess = (_record: any, message: string) => {
    fetchRecords();
    setSnackbar({ open: true, message, severity: "success" });
  };

  const handleChangePage = (_event: unknown, newPage: number) => setPage(newPage);
  const handleChangeRowsPerPage = (event: React.ChangeEvent<HTMLInputElement>) => {
    setRowsPerPage(parseInt(event.target.value, 10));
    setPage(0);
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
      await deleteMedicalRecord(deletingRecordId);
      setSnackbar({ open: true, message: "Xóa hồ sơ thành công!", severity: "success" });
      setRecords(prevRecords => prevRecords.filter(record => record.id !== deletingRecordId));
      // Cập nhật lại tổng số phần tử
      setTotalElements(prevTotal => prevTotal - 1);
      //fetchRecords();
    } catch (err: any) {
      setSnackbar({ open: true, message: err.message, severity: "error" });
    } finally {
      handleCloseDeleteDialog();
    }
  };

  const handleCloseSnackbar = () => setSnackbar({ ...snackbar, open: false });

  return (
    <Box>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4" fontWeight="bold">Quản lý Hồ sơ Bệnh nhân</Typography>
        <Button variant="contained" startIcon={<AddIcon />} onClick={() => setIsAddFormOpen(true)}>
          Thêm Hồ sơ
        </Button>
      </Box>

      <Paper elevation={2} sx={{ p: 2, mb: 3 }}>
        {/* === FIX 2: SỬA CÚ PHÁP GRID V2 (BỎ PROP 'item') === */}
        <Grid container spacing={2} alignItems="center">
          <Grid sx={{ xs:12, sm:4 }}>
            <TextField
              fullWidth
              label="Tìm theo Tên hoặc Mã BN"
              variant="outlined"
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
            />
          </Grid>
          <Grid sx={{ xs:12, sm:3 }}>
            <TextField
              fullWidth
              label="Từ ngày"
              type="date"
              InputLabelProps={{ shrink: true }}
              value={startDate}
              onChange={(e) => setStartDate(e.target.value)}
            />
          </Grid>
          <Grid sx={{ xs:12, sm:3 }}>
            <TextField
              fullWidth
              label="Đến ngày"
              type="date"
              InputLabelProps={{ shrink: true }}
              value={endDate}
              onChange={(e) => setEndDate(e.target.value)}
            />
          </Grid>
          <Grid sx={{ xs:12, sm:2 }}>
            <Button
              fullWidth
              variant="contained"
              startIcon={<SearchIcon />}
              onClick={handleSearch}
              sx={{ height: '100%' }}
            >
              Tìm kiếm
            </Button>
          </Grid>
        </Grid>
      </Paper>

      <Paper elevation={2}>
        <TableContainer>
          <Table>
            <TableHead>
              {/* === FIX 3: BỎ CỘT KHÔNG CÓ DỮ LIỆU (Giới Tính) === */}
              <TableRow sx={{ backgroundColor: '#f5f5f5' }}>
                <TableCell>Họ và Tên</TableCell>
                <TableCell>Mã Bệnh Nhân</TableCell>
                <TableCell>Ngày Sinh</TableCell>
                <TableCell>Ngày Xét nghiệm cuối</TableCell>
                <TableCell align="right">Hành động</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {isLoading ? (
                <TableRow><TableCell colSpan={5} align="center"><CircularProgress /></TableCell></TableRow>
              ) : error ? (
                <TableRow><TableCell colSpan={5} align="center" sx={{ color: 'error.main' }}>{error}</TableCell></TableRow>
              ) : records.length === 0 ? (
                 <TableRow><TableCell colSpan={5} align="center">Không tìm thấy hồ sơ nào.</TableCell></TableRow>
              ) : (
                records.map((record) => (
                  // === FIX 4: SỬA LẠI CÁCH TRUY CẬP DỮ LIỆU CHO ĐÚNG ===
                  <TableRow key={record.id} hover>
                    <TableCell>{record.fullName}</TableCell>
                    <TableCell>{record.patientCode}</TableCell>
                    <TableCell>{new Date(record.dateOfBirth).toLocaleDateString()}</TableCell>
                    <TableCell>{record.lastTestDate ? new Date(record.lastTestDate).toLocaleString() : 'N/A'}</TableCell>
                    <TableCell align="right">
                      <Tooltip title="Xem Chi tiết">
                        <IconButton onClick={() => navigate(`/patients/detail/${record.id}`)}>
                          <VisibilityIcon />
                        </IconButton>
                      </Tooltip>
                      <Tooltip title="Chỉnh sửa">
                        <IconButton onClick={() => navigate(`/patients/edit/${record.id}`)}>
                          <EditIcon />
                        </IconButton>
                      </Tooltip>
                      <Tooltip title="Xóa">
                        <IconButton onClick={() => handleOpenDeleteDialog(record.id)}><DeleteIcon color="error" /></IconButton>
                      </Tooltip>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </TableContainer>
        <TablePagination
          rowsPerPageOptions={[5, 10, 25]}
          component="div"
          count={totalElements}
          rowsPerPage={rowsPerPage}
          page={page}
          onPageChange={handleChangePage}
          onRowsPerPageChange={handleChangeRowsPerPage}
        />
      </Paper>

      <AddPatientForm isOpen={isAddFormOpen} onClose={() => setIsAddFormOpen(false)} onSuccess={handleFormSuccess} />

      <Dialog open={confirmDeleteOpen} onClose={handleCloseDeleteDialog}>
        <DialogTitle>Xác nhận xóa</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Bạn có chắc chắn muốn xóa hồ sơ bệnh nhân này không? Hành động này không thể hoàn tác.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDeleteDialog}>Hủy</Button>
          <Button onClick={handleConfirmDelete} color="error" autoFocus>Xóa</Button>
        </DialogActions>
      </Dialog>
      
      <Snackbar open={snackbar.open} autoHideDuration={6000} onClose={handleCloseSnackbar}>
        <Alert onClose={handleCloseSnackbar} severity={snackbar.severity} sx={{ width: '100%' }}>
          {snackbar.message}
        </Alert>
      </Snackbar>
    </Box>
  );
}