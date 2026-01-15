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
  HL7Backup,
  RawTestResultStatus,
} from "../types/HL7Backup";

type HL7BackupDetailModalProps = {
  isOpen: boolean;
  onClose: () => void;
  record: HL7Backup | null;
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

export default function HL7BackupDetailModal({
  isOpen,
  onClose,
  record,
}: HL7BackupDetailModalProps) {
  if (!record) return null;

  return (
    <Dialog open={isOpen} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>
        <Box display="flex" justifyContent="space-between" alignItems="center">
          <Typography variant="h6">Chi tiết HL7 Backup</Typography>
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
              Backup ID
            </Typography>
            <Typography variant="body1">{record.backupId}</Typography>
          </Paper>

          <Divider />

          <Box>
            <Typography variant="subtitle2" color="text.secondary" gutterBottom>
              Run ID
            </Typography>
            <Typography variant="body1">{record.runId}</Typography>
          </Box>

          <Box>
            <Typography variant="subtitle2" color="text.secondary" gutterBottom>
              Barcode
            </Typography>
            <Typography variant="body1">{record.barcode}</Typography>
          </Box>

          <Box>
            <Typography variant="subtitle2" color="text.secondary" gutterBottom>
              Instrument ID
            </Typography>
            <Typography variant="body1">{record.instrumentId}</Typography>
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

          {record.s3Key && (
            <Box>
              <Typography variant="subtitle2" color="text.secondary" gutterBottom>
                S3 Key
              </Typography>
              <Typography variant="body1">{record.s3Key}</Typography>
            </Box>
          )}

          {record.reason && (
            <Box>
              <Typography variant="subtitle2" color="text.secondary" gutterBottom>
                Reason
              </Typography>
              <Typography variant="body1" color="error">
                {record.reason}
              </Typography>
            </Box>
          )}

          <Box>
            <Typography variant="subtitle2" color="text.secondary" gutterBottom>
              HL7 Message
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
              <pre style={{ margin: 0, fontSize: "0.875rem", whiteSpace: "pre-wrap", wordBreak: "break-word" }}>
                {record.hl7Message}
              </pre>
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

