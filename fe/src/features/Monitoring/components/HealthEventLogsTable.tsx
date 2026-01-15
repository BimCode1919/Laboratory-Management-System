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
  HealthEventLog,
  HealthEventType,
} from "../types/HealthEventLogs";

type HealthEventLogsTableProps = {
  records: HealthEventLog[];
  isLoading?: boolean;
  onViewDetail: (record: HealthEventLog) => void;
  onDelete?: (id: string) => void;
};

const getEventTypeColor = (
  eventType: HealthEventType
): "default" | "warning" | "error" | "success" | "info" => {
  switch (eventType) {
    case "CONNECTED":
    case "RECONNECTED":
      return "success";
    case "DISCONNECTED":
    case "ERROR":
      return "error";
    case "TIMEOUT":
      return "warning";
    case "RETRY":
      return "info";
    default:
      return "default";
  }
};

const getEventTypeLabel = (eventType: HealthEventType): string => {
  switch (eventType) {
    case "CONNECTED":
      return "Đã kết nối";
    case "DISCONNECTED":
      return "Đã ngắt kết nối";
    case "RECONNECTED":
      return "Đã kết nối lại";
    case "ERROR":
      return "Lỗi";
    case "TIMEOUT":
      return "Hết thời gian chờ";
    case "RETRY":
      return "Thử lại";
    default:
      return eventType;
  }
};

export default function HealthEventLogsTable({
  records,
  isLoading = false,
  onViewDetail,
  onDelete,
}: HealthEventLogsTableProps) {
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
              <TableCell>Broker ID</TableCell>
              <TableCell>Event Type</TableCell>
              <TableCell>Details</TableCell>
              <TableCell>Created At</TableCell>
              <TableCell align="right">Hành động</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {records.map((record) => (
              <TableRow key={record.healthEventLogId} hover>
                <TableCell>{record.brokerId}</TableCell>
                <TableCell>
                  <Chip
                    label={getEventTypeLabel(record.healthEventType)}
                    color={getEventTypeColor(record.healthEventType)}
                    size="small"
                  />
                </TableCell>
                <TableCell
                  sx={{
                    maxWidth: 400,
                    overflow: "hidden",
                    textOverflow: "ellipsis",
                    whiteSpace: "nowrap",
                  }}
                >
                  {record.details}
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
                        onClick={() => onDelete(record.healthEventLogId)}
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

