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
import type { EventLog, Severity } from "../types/EventLogs";

type EventLogDetailModalProps = {
  isOpen: boolean;
  onClose: () => void;
  eventLog: EventLog | null;
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

export default function EventLogDetailModal({
  isOpen,
  onClose,
  eventLog,
}: EventLogDetailModalProps) {
  if (!eventLog) return null;

  return (
    <Dialog open={isOpen} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>
        <Box display="flex" justifyContent="space-between" alignItems="center">
          <Typography variant="h6">Chi tiết Event Log</Typography>
          <Chip
            label={eventLog.severity}
            color={getSeverityColor(eventLog.severity)}
            size="small"
          />
        </Box>
      </DialogTitle>
      <DialogContent>
        <Box sx={{ display: "flex", flexDirection: "column", gap: 2 }}>
          <Paper elevation={0} sx={{ p: 2, bgcolor: "#f5f5f5" }}>
            <Typography variant="subtitle2" color="text.secondary">
              Event Log ID
            </Typography>
            <Typography variant="body1">{eventLog.eventLogId}</Typography>
          </Paper>

          <Divider />

          <Box>
            <Typography variant="subtitle2" color="text.secondary" gutterBottom>
              Event Name
            </Typography>
            <Typography variant="body1">{eventLog.eventName}</Typography>
          </Box>

          <Box>
            <Typography variant="subtitle2" color="text.secondary" gutterBottom>
              Source Service
            </Typography>
            <Typography variant="body1">{eventLog.sourceService}</Typography>
          </Box>

          <Box>
            <Typography variant="subtitle2" color="text.secondary" gutterBottom>
              Performed By
            </Typography>
            <Typography variant="body1">{eventLog.performedBy}</Typography>
          </Box>

          <Box>
            <Typography variant="subtitle2" color="text.secondary" gutterBottom>
              Message
            </Typography>
            <Typography variant="body1">{eventLog.message}</Typography>
          </Box>

          <Box>
            <Typography variant="subtitle2" color="text.secondary" gutterBottom>
              Created At
            </Typography>
            <Typography variant="body1">
              {new Date(eventLog.createdAt).toLocaleString()}
            </Typography>
          </Box>

          <Box>
            <Typography variant="subtitle2" color="text.secondary" gutterBottom>
              Payload
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
              <pre style={{ margin: 0, fontSize: "0.875rem" }}>
                {JSON.stringify(eventLog.payload, null, 2)}
              </pre>
            </Paper>
          </Box>
        </Box>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Đóng</Button>
      </DialogActions>
    </Dialog>
  );
}

