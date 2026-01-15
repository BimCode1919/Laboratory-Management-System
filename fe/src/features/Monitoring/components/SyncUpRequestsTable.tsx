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
  SyncUpRequests,
  SyncUpRequestsStatus,
} from "../types/SyncUpRequests";

type SyncUpRequestsTableProps = {
  records: SyncUpRequests[];
  isLoading?: boolean;
  onViewDetail: (record: SyncUpRequests) => void;
  onDelete?: (id: string) => void;
};

const getStatusColor = (
  status: SyncUpRequestsStatus
): "default" | "warning" | "error" | "success" | "info" => {
  switch (status) {
    case "COMPLETED":
      return "success";
    case "FAILED":
      return "error";
    case "IN_PROGRESS":
      return "info";
    case "PENDING":
      return "warning";
    case "CANCELLED":
      return "default";
    default:
      return "default";
  }
};

const getStatusLabel = (status: SyncUpRequestsStatus): string => {
  switch (status) {
    case "PENDING":
      return "Đang chờ";
    case "IN_PROGRESS":
      return "Đang xử lý";
    case "COMPLETED":
      return "Hoàn thành";
    case "FAILED":
      return "Thất bại";
    case "CANCELLED":
      return "Đã hủy";
    default:
      return status;
  }
};

export default function SyncUpRequestsTable({
  records,
  isLoading = false,
  onViewDetail,
  onDelete,
}: SyncUpRequestsTableProps) {
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
              <TableCell>Source Service</TableCell>
              <TableCell>Message ID</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Processed At</TableCell>
              <TableCell>Created At</TableCell>
              <TableCell align="right">Hành động</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {records.map((record) => (
              <TableRow key={record.syncUpRequestId} hover>
                <TableCell>{record.sourceService}</TableCell>
                <TableCell>{record.messageId}</TableCell>
                <TableCell>
                  <Chip
                    label={getStatusLabel(record.status)}
                    color={getStatusColor(record.status)}
                    size="small"
                  />
                </TableCell>
                <TableCell>
                  {record.processedAt
                    ? new Date(record.processedAt).toLocaleString()
                    : "N/A"}
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
                        onClick={() => onDelete(record.syncUpRequestId)}
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

