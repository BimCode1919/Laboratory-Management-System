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
import type { EventLog, Severity } from "../types/EventLogs";

type EventLogsTableProps = {
  eventLogs: EventLog[];
  isLoading?: boolean;
  onViewDetail: (eventLog: EventLog) => void;
  onDelete?: (eventLogId: string) => void;
};

const getSeverityColor = (severity: Severity): "default" | "warning" | "error" | "info" => {
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

export default function EventLogsTable({
  eventLogs,
  isLoading = false,
  onViewDetail,
  onDelete,
}: EventLogsTableProps) {
  if (isLoading) {
    return (
      <Box display="flex" justifyContent="center" alignItems="center" p={4}>
        <CircularProgress />
      </Box>
    );
  }

  if (eventLogs.length === 0) {
    return (
      <Paper elevation={2}>
        <Box p={4} textAlign="center">
          <Typography variant="body1" color="text.secondary">
            Không có event log nào
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
            {eventLogs.map((eventLog) => (
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
                      onClick={() => onViewDetail(eventLog)}
                      size="small"
                    >
                      <VisibilityIcon />
                    </IconButton>
                  </Tooltip>
                  {onDelete && (
                    <Tooltip title="Xóa">
                      <IconButton
                        onClick={() => onDelete(eventLog.eventLogId)}
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

