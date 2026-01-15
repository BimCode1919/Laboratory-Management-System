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
import type { Severity } from "../types/EventLogs";

type EventLogFilterPanelProps = {
  searchText: string;
  onSearchChange: (value: string) => void;
  severityFilter: string;
  onSeverityFilterChange: (value: string) => void;
  sourceServiceFilter: string;
  onSourceServiceFilterChange: (value: string) => void;
  onClearFilters: () => void;
  availableSourceServices?: string[];
};

export default function EventLogFilterPanel({
  searchText,
  onSearchChange,
  severityFilter,
  onSeverityFilterChange,
  sourceServiceFilter,
  onSourceServiceFilterChange,
  onClearFilters,
  availableSourceServices = [],
}: EventLogFilterPanelProps) {
  const severityOptions: Severity[] = ["INFO", "WARN", "ERROR", "CRITICAL"];

  const hasActiveFilters =
    searchText !== "" || severityFilter !== "all" || sourceServiceFilter !== "all";

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
          <InputLabel>Severity</InputLabel>
          <Select
            value={severityFilter}
            label="Severity"
            onChange={(e) => onSeverityFilterChange(e.target.value)}
          >
            <MenuItem value="all">Tất cả</MenuItem>
            {severityOptions.map((severity) => (
              <MenuItem key={severity} value={severity}>
                {severity}
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

