// src/components/DynamicFilterPanel.tsx

import {
  Box,
  Divider,
  FormControl,
  FormControlLabel,
  MenuItem,
  Select,
  Switch,
  TextField,
  Typography,
} from "@mui/material";
import React from "react";
import { useEffect, useMemo, useState } from "react";

export type FilterFieldType = "text" | "boolean" | "enum" | "date" | "number";

export interface FilterConfigItem {
  key: string;
  label: string;
  type: FilterFieldType;
  defaultValue?: any;
  options?: { value: string | number; label: string }[];
  placeholder?: string;
  group?: string;
}

export type FilterData = Record<string, any>;

interface DynamicFilterPanelProps {
  config: FilterConfigItem[];
  onFiltersChange: (filters: FilterData) => void;
  resetKey?: number;
}

export default function DynamicFilterPanel({
  config,
  onFiltersChange,
  resetKey,
}: DynamicFilterPanelProps) {
  const initialFilterState = useMemo(() => {
    return config.reduce((acc, item) => {
      if (item.type === "boolean") acc[item.key] = item.defaultValue ?? false;
      else acc[item.key] = item.defaultValue ?? "";
      return acc;
    }, {} as FilterData);
  }, [config]);

  const [filters, setFilters] = useState<FilterData>(initialFilterState);

  useEffect(() => {
    setFilters(initialFilterState);
    onFiltersChange(initialFilterState);
  }, [resetKey, initialFilterState]);

  const handleChange = (key: string, value: any) => {
    const newFilters = {
      ...filters,
      [key]: value,
    };

    setFilters(newFilters);
    onFiltersChange(newFilters);
  };

  const renderField = (item: FilterConfigItem) => {
    const key = item.key;
    const currentValue = filters[key];

    switch (item.type) {
      case "enum":
        return (
          <FormControl fullWidth size="small">
            <Select
              value={currentValue}
              label={item.label}
              onChange={(e) => handleChange(key, e.target.value)}
            >
              <MenuItem value="">
                <em>{item.placeholder || `All ${item.label}`}</em>
              </MenuItem>
              {item.options?.map((option) => (
                <MenuItem key={option.value} value={option.value}>
                  {option.label}
                </MenuItem>
              ))}
            </Select>
          </FormControl>
        );

      case "boolean":
        return (
          <FormControlLabel
            control={
              <Switch
                checked={currentValue}
                onChange={(e) => handleChange(key, e.target.value)}
              />
            }
            label={item.label}
          />
        );

      case "date":
        return (
          <TextField
            fullWidth
            label={item.label}
            type="date"
            size="small"
            value={currentValue}
            onChange={(e) => handleChange(key, e.target.value)}
            InputLabelProps={{ shrink: true }}
          />
        );
      case "number":
      case "text":
      default:
        return (
          <TextField
            fullWidth
            label={item.label}
            type={item.type}
            size="small"
            placeholder={item.placeholder}
            value={currentValue}
            onChange={(e) => handleChange(key, e.target.value)}
          />
        );
    }
  };

  const groupedConfig = config.reduce((acc, item) => {
    const groupKey = item.group || "Search Parameters";
    if (!acc[groupKey]) {
      acc[groupKey] = [];
    }
    acc[groupKey].push(item);
    return acc;
  }, {} as Record<string, FilterConfigItem[]>);

  return (
    <Box sx={{ width: "100%" }}>
      {Object.keys(groupedConfig).map((groupName, groupIndex) => (
        <Box key={groupName} sx={{ mb: 3 }}>
          <Typography
            variant="subtitle1"
            sx={{
              mt: groupIndex === 0 ? 0 : 2,
              mb: 1,
              fontWeight: 600,
              textTransform: "uppercase",
              color: "text.secondary",
            }}
          >
            {groupName}
          </Typography>
          <Divider sx={{ mb: 2 }} />

          <Box display="flex" flexDirection="column" gap={2}>
            {groupedConfig[groupName].map((item) => (
              <React.Fragment key={item.key}>
                {renderField(item)}
              </React.Fragment>
            ))}
          </Box>
        </Box>
      ))}
    </Box>
  );
}
