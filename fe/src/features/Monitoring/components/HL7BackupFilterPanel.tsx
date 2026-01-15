import {
  Box,
  TextField,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  Button,
  Paper,
} from "@mui/material";
import { Search as SearchIcon, Clear as ClearIcon } from "@mui/icons-material";
import type { RawTestResultStatus } from "../types/HL7Backup";

type HL7BackupFilterPanelProps = {
  searchText: string;
  onSearchChange: (value: string) => void;
  statusFilter: string;
  onStatusFilterChange: (value: string) => void;
  instrumentIdFilter: string;
  onInstrumentIdFilterChange: (value: string) => void;
  onClearFilters: () => void;
  availableInstrumentIds?: string[];
};

export default function HL7BackupFilterPanel({
  searchText,
  onSearchChange,
  statusFilter,
  onStatusFilterChange,
  instrumentIdFilter,
  onInstrumentIdFilterChange,
  onClearFilters,
  availableInstrumentIds = [],
}: HL7BackupFilterPanelProps) {
  const statusOptions: RawTestResultStatus[] = [
    "PENDING",
    "PROCESSED",
    "FAILED",
    "ARCHIVED",
  ];

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

  const hasActiveFilters =
    searchText !== "" ||
    statusFilter !== "all" ||
    instrumentIdFilter !== "all";

  return (
    <Paper elevation={2} sx={{ p: 2, mb: 3 }}>
      <Box display="flex" gap={2} flexWrap="wrap" alignItems="center">
        <TextField
          label="Tìm kiếm (Run ID, Barcode)"
          variant="outlined"
          size="small"
          value={searchText}
          onChange={(e) => onSearchChange(e.target.value)}
          InputProps={{
            startAdornment: <SearchIcon sx={{ mr: 1, color: "text.secondary" }} />,
          }}
          sx={{ minWidth: 250 }}
        />

        <FormControl size="small" sx={{ minWidth: 150 }}>
          <InputLabel>Status</InputLabel>
          <Select
            value={statusFilter}
            label="Status"
            onChange={(e) => onStatusFilterChange(e.target.value)}
          >
            <MenuItem value="all">Tất cả</MenuItem>
            {statusOptions.map((status) => (
              <MenuItem key={status} value={status}>
                {getStatusLabel(status)}
              </MenuItem>
            ))}
          </Select>
        </FormControl>

        {availableInstrumentIds.length > 0 && (
          <FormControl size="small" sx={{ minWidth: 200 }}>
            <InputLabel>Instrument ID</InputLabel>
            <Select
              value={instrumentIdFilter}
              label="Instrument ID"
              onChange={(e) => onInstrumentIdFilterChange(e.target.value)}
            >
              <MenuItem value="all">Tất cả</MenuItem>
              {availableInstrumentIds.map((instrumentId) => (
                <MenuItem key={instrumentId} value={instrumentId}>
                  {instrumentId}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        )}

        {hasActiveFilters && (
          <Button
            variant="outlined"
            startIcon={<ClearIcon />}
            onClick={onClearFilters}
            size="small"
          >
            Xóa bộ lọc
          </Button>
        )}
      </Box>
    </Paper>
  );
}

