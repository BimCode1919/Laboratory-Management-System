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
import type { SyncUpRequestsStatus } from "../types/SyncUpRequests";

type SyncUpRequestsFilterPanelProps = {
  searchText: string;
  onSearchChange: (value: string) => void;
  statusFilter: string;
  onStatusFilterChange: (value: string) => void;
  sourceServiceFilter: string;
  onSourceServiceFilterChange: (value: string) => void;
  onClearFilters: () => void;
  availableSourceServices?: string[];
};

export default function SyncUpRequestsFilterPanel({
  searchText,
  onSearchChange,
  statusFilter,
  onStatusFilterChange,
  sourceServiceFilter,
  onSourceServiceFilterChange,
  onClearFilters,
  availableSourceServices = [],
}: SyncUpRequestsFilterPanelProps) {
  const statusOptions: SyncUpRequestsStatus[] = [
    "PENDING",
    "IN_PROGRESS",
    "COMPLETED",
    "FAILED",
    "CANCELLED",
  ];

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

  const hasActiveFilters =
    searchText !== "" ||
    statusFilter !== "all" ||
    sourceServiceFilter !== "all";

  return (
    <Paper elevation={2} sx={{ p: 2, mb: 3 }}>
      <Box display="flex" gap={2} flexWrap="wrap" alignItems="center">
        <TextField
          label="Tìm kiếm"
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

        {availableSourceServices.length > 0 && (
          <FormControl size="small" sx={{ minWidth: 200 }}>
            <InputLabel>Source Service</InputLabel>
            <Select
              value={sourceServiceFilter}
              label="Source Service"
              onChange={(e) => onSourceServiceFilterChange(e.target.value)}
            >
              <MenuItem value="all">Tất cả</MenuItem>
              {availableSourceServices.map((service) => (
                <MenuItem key={service} value={service}>
                  {service}
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

