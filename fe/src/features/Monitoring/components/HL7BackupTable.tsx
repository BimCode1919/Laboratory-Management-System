import {
  Box,
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
  Typography,
} from "@mui/material";
import {
  Visibility as VisibilityIcon,
  Delete as DeleteIcon,
} from "@mui/icons-material";
import type {
  HL7Backup,
  RawTestResultStatus,
} from "../types/HL7Backup";

type HL7BackupTableProps = {
  records: HL7Backup[];
  isLoading?: boolean;
  onViewDetail: (record: HL7Backup) => void;
  onDelete?: (id: string) => void;
};

const getStatusColor = (
  status: RawTestResultStatus
): "default" | "warning" | "error" | "success" | "info" => {
  switch (status) {
    case "PROCESSED":
      return "success";
    case "FAILED":
      return "error";
    case "PENDING":
      return "warning";
    case "ARCHIVED":
      return "info";
    default:
      return "default";
  }
};

const getStatusLabel = (status: RawTestResultStatus): string => {
  switch (status) {
    case "PENDING":
      return "Đang chờ";
    case "PROCESSED":
      return "Đã xử lý";
    case "FAILED":
      return "Thất bại";
    case "ARCHIVED":
      return "Đã lưu trữ";
    default:
      return status;
  }
};

export default function HL7BackupTable({
  records,
  isLoading = false,
  onViewDetail,
  onDelete,
}: HL7BackupTableProps) {
  if (isLoading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" p={4}>
        <CircularProgress />
      </Box>
    );
  }

  if (records.length === 0) {
    return (
      <Paper elevation={2}>
        <Box p={4} textAlign="center">
          <Typography variant="body1" color="text.secondary">
            Không có bản ghi nào
          </Typography>
        </Box>
      </Paper>
    );
  }

  return (
    <Paper elevation={2}>
      <TableContainer>
        <Table>
          <TableHead>
            <TableRow sx={{ backgroundColor: "#f5f5f5" }}>
              <TableCell>Run ID</TableCell>
              <TableCell>Barcode</TableCell>
              <TableCell>Instrument ID</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>S3 Key</TableCell>
              <TableCell>Created At</TableCell>
              <TableCell align="right">Hành động</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {records.map((record) => (
              <TableRow key={record.backupId} hover>
                <TableCell>{record.runId}</TableCell>
                <TableCell>{record.barcode}</TableCell>
                <TableCell>{record.instrumentId}</TableCell>
                <TableCell>
                  <Chip
                    label={getStatusLabel(record.status)}
                    color={getStatusColor(record.status)}
                    size="small"
                  />
                </TableCell>
                <TableCell>
                  {record.s3Key || "N/A"}
                </TableCell>
                <TableCell>
                  {new Date(record.createdAt).toLocaleString()}
                </TableCell>
                <TableCell align="right">
                  <Tooltip title="Xem chi tiết">
                    <IconButton
                      onClick={() => onViewDetail(record)}
                      size="small"
                    >
                      <VisibilityIcon />
                    </IconButton>
                  </Tooltip>
                  {onDelete && (
                    <Tooltip title="Xóa">
                      <IconButton
                        onClick={() => onDelete(record.backupId)}
                        size="small"
                        color="error"
                      >
                        <DeleteIcon />
                      </IconButton>
                    </Tooltip>
                  )}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>
    </Paper>
  );
}

