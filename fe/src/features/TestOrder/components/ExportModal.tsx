// src/features/TestOrder/components/ExportModal.tsx

import {
  Box,
  Button,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  IconButton,
  List,
  ListItem,
  ListItemIcon,
  ListItemText,
  Typography,
  Snackbar,
  Alert,
} from "@mui/material";
import { useState } from "react";
import {
  exportAsCsv,
  exportAsXlsx,
  exportAsPdf,
} from "../services/ExportService";

import PictureAsPdfIcon from "@mui/icons-material/PictureAsPdf";
import BarChartIcon from "@mui/icons-material/BarChart";
import DescriptionIcon from "@mui/icons-material/Description";
import DownloadIcon from "@mui/icons-material/Download";
import CloseIcon from "@mui/icons-material/Close";

type Notification = {
  open: boolean;
  message: string;
  severity: "success" | "error" | "info" | "warning";
};

interface ExportModalProps {
  open: boolean;
  onClose: () => void;
  testOrderId: string | null;
  orderCode: string | null;
}

const exportOptions = [
  {
    type: "pdf",
    label: "Export as PDF",
    icon: PictureAsPdfIcon,
    color: "error",
  },
  {
    type: "xlsx",
    label: "Export as Excel",
    icon: BarChartIcon,
    color: "success",
  },
  {
    type: "csv",
    label: "Export as CSV",
    icon: DescriptionIcon,
    color: "primary",
  },
];

export default function ExportModal({
  open,
  onClose,
  testOrderId,
  orderCode,
}: ExportModalProps) {
  const [exportType, setExportType] = useState<"csv" | "xlsx" | "pdf" | null>(
    null
  );
  const [loading, setLoading] = useState(false);
  const [localError, setLocalError] = useState<string | null>(null);

  const [notification, setNotification] = useState<Notification>({
    open: false,
    message: "",
    severity: "info",
  });

  const handleCloseSnackbar = (
    event?: React.SyntheticEvent | Event,
    reason?: string
  ) => {
    if (reason === "clickaway") {
      return;
    }
    setNotification({ ...notification, open: false });
  };

  const handleExportApi = async (type: "csv" | "xlsx" | "pdf", id: string) => {
    let downloadLink: string | null = null;
    setLocalError(null);

    try {
      setLoading(true);
      setExportType(type);

      switch (type) {
        case "csv":
          downloadLink = await exportAsCsv(id);
          break;
        case "xlsx":
          downloadLink = await exportAsXlsx(id);
          break;
        case "pdf":
          downloadLink = await exportAsPdf(id);
          break;
      }

      if (downloadLink) {
        const link = document.createElement("a");
        link.href = downloadLink;
        link.target = "_blank";
        
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);

        setNotification({
          open: true,
          message: `Export to ${type.toUpperCase()} successful. Download initiated.`,
          severity: "success",
        });
        onClose();
      } else {
        setLocalError(
          `Export failed for ${type.toUpperCase()}. Server did not return a file.`
        );
      }
    } catch (err: any) {
      console.error(`Export error for ${type}:`, err);
      setNotification({
        open: true,
        message: `Export failed: ${
          err.message || "An unexpected error occurred."
        }`,
        severity: "error",
      });
      setLocalError(
        err.message || "An unexpected error occurred during export."
      );
    } finally {
      setLoading(false);
      setExportType(null);
    }
  };

  const handleDownload = (type: "csv" | "xlsx" | "pdf") => {
    if (!testOrderId) {
      setLocalError("Cannot export: Test Order ID is missing.");
      return;
    }
    handleExportApi(type, testOrderId);
  };

  const handleClose = () => {
    if (!loading) {
      setLocalError(null);
      onClose();
    }
  };

  return (
    <>
      <Dialog
        open={open}
        onClose={handleClose}
        aria-labelledby="export-dialog-title"
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle id="export-dialog-title" sx={{ m: 0, p: 2 }}>
          <Box
            display={"flex"}
            alignItems={"center"}
            justifyContent={"space-between"}
          >
            <Box display="flex" alignItems="center">
              <DownloadIcon color="primary" sx={{ mr: 1 }} />
              <Typography variant="h6">Export Test Order</Typography>
            </Box>
            <IconButton
              aria-label="close"
              onClick={handleClose}
              disabled={loading}
              sx={{
                color: (theme) => theme.palette.grey[500],
              }}
            >
              <CloseIcon />
            </IconButton>
          </Box>
        </DialogTitle>

        <DialogContent dividers sx={{ p: 3 }}>
          <Typography variant="body1" sx={{ mb: 2 }}>
            Select the format to export Test Order: {orderCode || "N/A"}
          </Typography>

          <List disablePadding>
            {exportOptions.map((option) => (
              <ListItem
                key={option.type}
                disablePadding
                sx={{ py: 1 }}
                secondaryAction={
                  <Button
                    variant="outlined"
                    size="small"
                    onClick={() =>
                      handleDownload(option.type as "csv" | "xlsx" | "pdf")
                    }
                    disabled={loading || !testOrderId}
                    startIcon={
                      loading && exportType === option.type ? (
                        <CircularProgress size={16} color="inherit" />
                      ) : (
                        <DownloadIcon />
                      )
                    }
                    color={
                      option.color as
                        | "primary"
                        | "secondary"
                        | "error"
                        | "success"
                    }
                  >
                    Export
                  </Button>
                }
              >
                <ListItemIcon>
                  <option.icon
                    color={
                      option.color as
                        | "primary"
                        | "secondary"
                        | "error"
                        | "success"
                    }
                  />
                </ListItemIcon>
                <ListItemText
                  primary={option.label}
                />
              </ListItem>
            ))}
          </List>

          {localError && (
            <Typography
              color="error"
              variant="body2"
              sx={{
                mt: 2,
                p: 1,
                border: "1px solid",
                borderColor: "error.main",
                borderRadius: 1,
              }}
            >
              {localError}
            </Typography>
          )}
        </DialogContent>

        <DialogActions>
          <Button onClick={handleClose} color="primary" disabled={loading}>
            Close
          </Button>
        </DialogActions>
      </Dialog>

      <Snackbar
        open={notification.open}
        autoHideDuration={6000}
        onClose={handleCloseSnackbar}
        anchorOrigin={{ vertical: "bottom", horizontal: "center" }}
      >
        <Alert
          onClose={handleCloseSnackbar}
          severity={notification.severity}
          sx={{ width: "100%" }}
          variant="filled"
        >
          {notification.message}
        </Alert>
      </Snackbar>
    </>
  );
}