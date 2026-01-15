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
import type { HealthEventType } from "../types/HealthEventLogs";

type HealthEventLogsFilterPanelProps = {
  searchText: string;
  onSearchChange: (value: string) => void;
  eventTypeFilter: string;
  onEventTypeFilterChange: (value: string) => void;
  brokerIdFilter: string;
  onBrokerIdFilterChange: (value: string) => void;
  onClearFilters: () => void;
  availableBrokerIds?: string[];
};

export default function HealthEventLogsFilterPanel({
  searchText,
  onSearchChange,
  eventTypeFilter,
  onEventTypeFilterChange,
  brokerIdFilter,
  onBrokerIdFilterChange,
  onClearFilters,
  availableBrokerIds = [],
}: HealthEventLogsFilterPanelProps) {
  const eventTypeOptions: HealthEventType[] = [
    "CONNECTED",
    "DISCONNECTED",
    "RECONNECTED",
    "ERROR",
    "TIMEOUT",
    "RETRY",
  ];

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

  const hasActiveFilters =
    searchText !== "" ||
    eventTypeFilter !== "all" ||
    brokerIdFilter !== "all";

  return (
    <Paper elevation={2} sx={{ p: 2, mb: 3 }}>
      <Box display="flex" gap={2} flexWrap="wrap" alignItems="center">
        <TextField
          label="Tìm kiếm (Broker ID, Details)"
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
          <InputLabel>Event Type</InputLabel>
          <Select
            value={eventTypeFilter}
            label="Event Type"
            onChange={(e) => onEventTypeFilterChange(e.target.value)}
          >
            <MenuItem value="all">Tất cả</MenuItem>
            {eventTypeOptions.map((eventType) => (
              <MenuItem key={eventType} value={eventType}>
                {getEventTypeLabel(eventType)}
              </MenuItem>
            ))}
          </Select>
        </FormControl>

        {availableBrokerIds.length > 0 && (
          <FormControl size="small" sx={{ minWidth: 200 }}>
            <InputLabel>Broker ID</InputLabel>
            <Select
              value={brokerIdFilter}
              label="Broker ID"
              onChange={(e) => onBrokerIdFilterChange(e.target.value)}
            >
              <MenuItem value="all">Tất cả</MenuItem>
              {availableBrokerIds.map((brokerId) => (
                <MenuItem key={brokerId} value={brokerId}>
                  {brokerId}
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

