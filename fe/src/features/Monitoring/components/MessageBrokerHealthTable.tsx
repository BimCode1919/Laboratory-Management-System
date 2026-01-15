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
  MessageBrokerHealth,
  MessageBrokerHealthStatus,
} from "../types/MessageBrokerHealth";

type MessageBrokerHealthTableProps = {
  records: MessageBrokerHealth[];
  isLoading?: boolean;
  onViewDetail: (record: MessageBrokerHealth) => void;
  onDelete?: (id: string) => void;
};

const getStatusColor = (
  status: MessageBrokerHealthStatus
): "default" | "warning" | "error" | "success" | "info" => {
  switch (status) {
    case "HEALTHY":
      return "success";
    case "UNHEALTHY":
      return "error";
    case "DEGRADED":
      return "warning";
    case "UNKNOWN":
      return "info";
    default:
      return "default";
  }
};

const getStatusLabel = (status: MessageBrokerHealthStatus): string => {
  switch (status) {
    case "HEALTHY":
      return "Khỏe mạnh";
    case "UNHEALTHY":
      return "Không khỏe";
    case "DEGRADED":
      return "Suy giảm";
    case "UNKNOWN":
      return "Không xác định";
    default:
      return status;
  }
};

export default function MessageBrokerHealthTable({
  records,
  isLoading = false,
  onViewDetail,
  onDelete,
}: MessageBrokerHealthTableProps) {
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
              <TableCell>Broker Name</TableCell>
              <TableCell>Status</TableCell>
              <TableCell>Last Checked At</TableCell>
              <TableCell>Retry Attempts</TableCell>
              <TableCell>Error Code</TableCell>
              <TableCell>Recovered At</TableCell>
              <TableCell>Created At</TableCell>
              <TableCell align="right">Hành động</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {records.map((record) => (
              <TableRow key={record.messageBrokerHealthId} hover>
                <TableCell>{record.brokerName}</TableCell>
                <TableCell>
                  <Chip
                    label={getStatusLabel(record.status)}
                    color={getStatusColor(record.status)}
                    size="small"
                  />
                </TableCell>
                <TableCell>
                  {record.lastCheckedAt
                    ? new Date(record.lastCheckedAt).toLocaleString()
                    : "N/A"}
                </TableCell>
                <TableCell>
                  {record.retryAttempts !== null ? record.retryAttempts : "N/A"}
                </TableCell>
                <TableCell>
                  {record.errorCode || "N/A"}
                </TableCell>
                <TableCell>
                  {record.recoveredAt
                    ? new Date(record.recoveredAt).toLocaleString()
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
                        onClick={() => onDelete(record.messageBrokerHealthId)}
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

