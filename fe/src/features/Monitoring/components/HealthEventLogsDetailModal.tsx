import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Typography,
  Box,
  Chip,
  Divider,
  Paper,
} from "@mui/material";
import type {
  HealthEventLog,
  HealthEventType,
} from "../types/HealthEventLogs";

type HealthEventLogsDetailModalProps = {
  isOpen: boolean;
  onClose: () => void;
  record: HealthEventLog | null;
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

export default function HealthEventLogsDetailModal({
  isOpen,
  onClose,
  record,
}: HealthEventLogsDetailModalProps) {
  if (!record) return null;

  return (
    <Dialog open={isOpen} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>
        <Box display="flex" justifyContent="space-between" alignItems="center">
          <Typography variant="h6">Chi tiết Health Event Log</Typography>
          <Chip
            label={getEventTypeLabel(record.healthEventType)}
            color={getEventTypeColor(record.healthEventType)}
            size="small"
          />
        </Box>
      </DialogTitle>
      <DialogContent>
        <Box sx={{ display: "flex", flexDirection: "column", gap: 2 }}>
          <Paper elevation={0} sx={{ p: 2, bgcolor: "#f5f5f5" }}>
            <Typography variant="subtitle2" color="text.secondary">
              Health Event Log ID
            </Typography>
            <Typography variant="body1">{record.healthEventLogId}</Typography>
          </Paper>

          <Divider />

          <Box>
            <Typography variant="subtitle2" color="text.secondary" gutterBottom>
              Broker ID
            </Typography>
            <Typography variant="body1">{record.brokerId}</Typography>
          </Box>

          <Box>
            <Typography variant="subtitle2" color="text.secondary" gutterBottom>
              Event Type
            </Typography>
            <Chip
              label={getEventTypeLabel(record.healthEventType)}
              color={getEventTypeColor(record.healthEventType)}
              size="small"
            />
          </Box>

          <Box>
            <Typography variant="subtitle2" color="text.secondary" gutterBottom>
              Details
            </Typography>
            <Paper
              elevation={0}
              sx={{
                p: 2,
                bgcolor: "#fafafa",
                maxHeight: 300,
                overflow: "auto",
              }}
            >
              <Typography variant="body2">{record.details}</Typography>
            </Paper>
          </Box>

          <Divider />

          <Box>
            <Typography variant="subtitle2" color="text.secondary" gutterBottom>
              Created At
            </Typography>
            <Typography variant="body1">
              {new Date(record.createdAt).toLocaleString()}
            </Typography>
          </Box>
        </Box>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Đóng</Button>
      </DialogActions>
    </Dialog>
  );
}

