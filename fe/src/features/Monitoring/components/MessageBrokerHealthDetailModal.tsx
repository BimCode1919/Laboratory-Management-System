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
  MessageBrokerHealth,
  MessageBrokerHealthStatus,
} from "../types/MessageBrokerHealth";

type MessageBrokerHealthDetailModalProps = {
  isOpen: boolean;
  onClose: () => void;
  record: MessageBrokerHealth | null;
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

export default function MessageBrokerHealthDetailModal({
  isOpen,
  onClose,
  record,
}: MessageBrokerHealthDetailModalProps) {
  if (!record) return null;

  return (
    <Dialog open={isOpen} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>
        <Box display="flex" justifyContent="space-between" alignItems="center">
          <Typography variant="h6">Chi tiết Message Broker Health</Typography>
          <Chip
            label={getStatusLabel(record.status)}
            color={getStatusColor(record.status)}
            size="small"
          />
        </Box>
      </DialogTitle>
      <DialogContent>
        <Box sx={{ display: "flex", flexDirection: "column", gap: 2 }}>
          <Paper elevation={0} sx={{ p: 2, bgcolor: "#f5f5f5" }}>
            <Typography variant="subtitle2" color="text.secondary">
              Message Broker Health ID
            </Typography>
            <Typography variant="body1">{record.messageBrokerHealthId}</Typography>
          </Paper>

          <Divider />

          <Box>
            <Typography variant="subtitle2" color="text.secondary" gutterBottom>
              Broker Name
            </Typography>
            <Typography variant="body1">{record.brokerName}</Typography>
          </Box>

          <Box>
            <Typography variant="subtitle2" color="text.secondary" gutterBottom>
              Status
            </Typography>
            <Chip
              label={getStatusLabel(record.status)}
              color={getStatusColor(record.status)}
              size="small"
            />
          </Box>

          <Box>
            <Typography variant="subtitle2" color="text.secondary" gutterBottom>
              Last Checked At
            </Typography>
            <Typography variant="body1">
              {record.lastCheckedAt
                ? new Date(record.lastCheckedAt).toLocaleString()
                : "N/A"}
            </Typography>
          </Box>

          <Box>
            <Typography variant="subtitle2" color="text.secondary" gutterBottom>
              Retry Attempts
            </Typography>
            <Typography variant="body1">
              {record.retryAttempts !== null ? record.retryAttempts : "N/A"}
            </Typography>
          </Box>

          {record.errorCode && (
            <Box>
              <Typography variant="subtitle2" color="text.secondary" gutterBottom>
                Error Code
              </Typography>
              <Typography variant="body1">{record.errorCode}</Typography>
            </Box>
          )}

          {record.errorMessage && (
            <Box>
              <Typography variant="subtitle2" color="text.secondary" gutterBottom>
                Error Message
              </Typography>
              <Paper
                elevation={0}
                sx={{
                  p: 2,
                  bgcolor: "#fafafa",
                  maxHeight: 200,
                  overflow: "auto",
                }}
              >
                <Typography variant="body2" color="error">
                  {record.errorMessage}
                </Typography>
              </Paper>
            </Box>
          )}

          {record.recoveredAt && (
            <Box>
              <Typography variant="subtitle2" color="text.secondary" gutterBottom>
                Recovered At
              </Typography>
              <Typography variant="body1">
                {new Date(record.recoveredAt).toLocaleString()}
              </Typography>
            </Box>
          )}

          <Divider />

          <Box>
            <Typography variant="subtitle2" color="text.secondary" gutterBottom>
              Created At
            </Typography>
            <Typography variant="body1">
              {new Date(record.createdAt).toLocaleString()}
            </Typography>
          </Box>

          <Box>
            <Typography variant="subtitle2" color="text.secondary" gutterBottom>
              Updated At
            </Typography>
            <Typography variant="body1">
              {new Date(record.updatedAt).toLocaleString()}
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

