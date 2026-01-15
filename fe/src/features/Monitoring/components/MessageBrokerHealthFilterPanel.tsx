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
import type { MessageBrokerHealthStatus } from "../types/MessageBrokerHealth";

type MessageBrokerHealthFilterPanelProps = {
  searchText: string;
  onSearchChange: (value: string) => void;
  statusFilter: string;
  onStatusFilterChange: (value: string) => void;
  brokerNameFilter: string;
  onBrokerNameFilterChange: (value: string) => void;
  onClearFilters: () => void;
  availableBrokerNames?: string[];
};

export default function MessageBrokerHealthFilterPanel({
  searchText,
  onSearchChange,
  statusFilter,
  onStatusFilterChange,
  brokerNameFilter,
  onBrokerNameFilterChange,
  onClearFilters,
  availableBrokerNames = [],
}: MessageBrokerHealthFilterPanelProps) {
  const statusOptions: MessageBrokerHealthStatus[] = [
    "HEALTHY",
    "UNHEALTHY",
    "DEGRADED",
    "UNKNOWN",
  ];

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

  const hasActiveFilters =
    searchText !== "" ||
    statusFilter !== "all" ||
    brokerNameFilter !== "all";

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

        {availableBrokerNames.length > 0 && (
          <FormControl size="small" sx={{ minWidth: 200 }}>
            <InputLabel>Broker Name</InputLabel>
            <Select
              value={brokerNameFilter}
              label="Broker Name"
              onChange={(e) => onBrokerNameFilterChange(e.target.value)}
            >
              <MenuItem value="all">Tất cả</MenuItem>
              {availableBrokerNames.map((brokerName) => (
                <MenuItem key={brokerName} value={brokerName}>
                  {brokerName}
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

