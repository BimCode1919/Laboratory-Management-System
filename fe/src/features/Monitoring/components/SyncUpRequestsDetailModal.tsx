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
  SyncUpRequests,
  SyncUpRequestsStatus,
} from "../types/SyncUpRequests";

type SyncUpRequestsDetailModalProps = {
  isOpen: boolean;
  onClose: () => void;
  record: SyncUpRequests | null;
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

export default function SyncUpRequestsDetailModal({
  isOpen,
  onClose,
  record,
}: SyncUpRequestsDetailModalProps) {
  if (!record) return null;

  return (
    <Dialog open={isOpen} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>
        <Box display="flex" justifyContent="space-between" alignItems="center">
          <Typography variant="h6">Chi tiết Sync Up Request</Typography>
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
              Sync Up Request ID
            </Typography>
            <Typography variant="body1">{record.syncUpRequestId}</Typography>
          </Paper>

          <Divider />

          <Box>
            <Typography variant="subtitle2" color="text.secondary" gutterBottom>
              Source Service
            </Typography>
            <Typography variant="body1">{record.sourceService}</Typography>
          </Box>

          <Box>
            <Typography variant="subtitle2" color="text.secondary" gutterBottom>
              Message ID
            </Typography>
            <Typography variant="body1">{record.messageId}</Typography>
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
              Processed At
            </Typography>
            <Typography variant="body1">
              {record.processedAt
                ? new Date(record.processedAt).toLocaleString()
                : "N/A"}
            </Typography>
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

