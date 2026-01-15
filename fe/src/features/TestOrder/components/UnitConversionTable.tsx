// src/features/TestOrder/components/UnitConversionTable.tsx

import DynamicAddUpdateForm, { type FieldConfig } from "@/components/DynamicAddUpdateForm";
import type { FilterData } from "@/components/DynamicFilterPanel";
import {
  Alert,
  Box,
  Button,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  IconButton,
  Paper,
  Switch,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Tooltip,
} from "@mui/material";
import {
  UnitConversionMappingFormConfig,
  UnitConversionMappingSchema,
  type UnitConversionMapping,
} from "../types/UnitConversionMapping";
import { Add, Delete, Edit } from "@mui/icons-material";
import {
  deleteUnitConversionMapping,
  disableUnitConversionMapping,
  enableUnitConversionMapping,
  getAllUnitConversionMappings,
  saveUnitConversionMapping,
} from "../services/UnitConversionMappingServices";
import { useCallback, useEffect, useState } from "react";

interface UnitConversionTableProps {
  filters: FilterData;
  reloadKey: number;
  onResultsCountChange: (count: number) => void;
}

export default function UnitConversionTable({
  filters,
  reloadKey,
  onResultsCountChange,
}: UnitConversionTableProps) {
  const [mappings, setMappings] = useState<UnitConversionMapping[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(false);

  const [isFormOpen, setIsFormOpen] = useState(false);
  const [editingItem, setEditingItem] = useState<
    UnitConversionMapping | undefined
  >(undefined);

  const [apiError, setApiError] = useState<string | null>(null);
  const [shakeKey, setShakeKey] = useState(0);

  const [isConfirmDeleteOpen, setIsConfirmDeleteOpen] = useState(false);
  const [deletingItem, setDeletingItem] = useState<
    UnitConversionMapping | undefined
  >(undefined);

  const loadMappings = useCallback(
    async (currentFilters: FilterData) => {
      setLoading(true);
      setError(false);

      try {
        const allData = await getAllUnitConversionMappings();

        if (allData === null) {
          setError(true);
          setMappings([]);
          onResultsCountChange(0);
          return;
        }

        const filteredData = allData.filter((item) => {
          const sourceMatch = currentFilters.sourceUnit
            ? item.sourceUnit
                .toLowerCase()
                .includes(currentFilters.sourceUnit.toLowerCase())
            : true;
          const targetMatch = currentFilters.targetUnit
            ? item.targetUnit
                .toLowerCase()
                .includes(currentFilters.targetUnit.toLowerCase())
            : true;
          const dataSourceMatch = currentFilters.dataSource
            ? item.dataSource
                .toLowerCase()
                .includes(currentFilters.dataSource.toLowerCase())
            : true;
          const activeMatch =
            currentFilters.showActive === true ? item.isActivated : true;

          return sourceMatch && targetMatch && dataSourceMatch && activeMatch;
        });

        setMappings(filteredData);
        onResultsCountChange(filteredData.length);
      } catch (e) {
        console.error("Failed to load mappings:", e);
        setError(true);
        setMappings([]);
        onResultsCountChange(0);
      } finally {
        setLoading(false);
      }
    },
    [onResultsCountChange]
  );

  useEffect(() => {
    loadMappings(filters);
  }, [reloadKey, loadMappings, filters]);

  const handleFormClose = () => {
    setIsFormOpen(false);
    setEditingItem(undefined);
    setApiError(null);
  };

  const handleAdd = () => {
    setEditingItem(undefined);
    setIsFormOpen(true);
    setApiError(null);
  };

  const handleEdit = (mapping: UnitConversionMapping) => {
    setEditingItem(mapping);
    setIsFormOpen(true);
    setApiError(null);
  };

  const handleSave = async (mapping: UnitConversionMapping) => {
    setApiError(null);
    try {
      await saveUnitConversionMapping(mapping);
      await loadMappings(filters);
      handleFormClose();
    } catch (e) {
      const errorMessage =
        e instanceof Error
          ? e.message
          : "An unknown error occurred while saving the mapping.";
      setApiError(errorMessage);
      setShakeKey((prev) => prev + 1);
    }
  };

  const handleToggleActivation = async (item: UnitConversionMapping) => {
    if (item.id === undefined) return;
    const action = item.isActivated
      ? disableUnitConversionMapping
      : enableUnitConversionMapping;
    try {
      await action(item.id);
      await loadMappings(filters);
    } catch (e) {
      console.error("Failed to toggle activation:", e);
    }
  };

  const handleDeleteClick = (item: UnitConversionMapping) => {
    setDeletingItem(item);
    setIsConfirmDeleteOpen(true);
  };

  const handleCancelDelete = () => {
    setIsConfirmDeleteOpen(false);
    setDeletingItem(undefined);
  };

  const handleConfirmDelete = async () => {
    if (deletingItem?.id) {
      try {
        await deleteUnitConversionMapping(deletingItem.id);
        await loadMappings(filters);
      } catch (e) {
        console.error("Failed to delete mapping:", e);
      }
    }
    handleCancelDelete();
  };

  const formatFormula = (mapping: UnitConversionMapping) => {
    const { multiplyFactor, calculationOffset, formula } = mapping;
    if (formula && formula.trim()) return formula;
    const factorText =
      multiplyFactor === 1 ? "Source" : `Source \\times ${multiplyFactor}`;
    const offsetText =
      calculationOffset === 0
        ? ""
        : calculationOffset > 0
        ? ` + ${calculationOffset}`
        : ` - ${Math.abs(calculationOffset)}`;
    return `Target = ${factorText}${offsetText}`;
  };

  if (loading && reloadKey === 0) {
    return (
      <Box
        display="flex"
        justifyContent="center"
        alignItems="center"
        height="60vh"
      >
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box>
      <Box display="flex" justifyContent="flex-end" alignItems="center" mb={2}>
        <Button onClick={handleAdd} variant="contained" startIcon={<Add />}>
          Add New Mapping
        </Button>
      </Box>

      {error && !loading && (
        <Alert severity="error" sx={{ mb: 3 }}>
          Failed to load mappings.
          <Button
            onClick={() => loadMappings(filters)}
            sx={{ ml: 2 }}
            variant="text"
          >
            Retry
          </Button>
        </Alert>
      )}

      {loading && reloadKey > 0 && (
        <Box display="flex" justifyContent="center" py={4}>
          <CircularProgress />
        </Box>
      )}

      {!loading && !error && (
        <TableContainer component={Paper}>
          <Table sx={{ minWidth: 650 }} aria-label="Unit Conversion Mappings">
            <TableHead>
              <TableRow sx={{ backgroundColor: "rgb(249 250 251)" }}>
                {/* ... Table Headers ... */}
                <TableCell
                  sx={{
                    fontWeight: 700,
                    fontSize: "0.75rem",
                    color: "rgb(107 114 128)",
                  }}
                >
                  Source | Target
                </TableCell>
                <TableCell
                  sx={{
                    fontWeight: 700,
                    fontSize: "0.75rem",
                    color: "rgb(107 114 128)",
                  }}
                >
                  Data Source
                </TableCell>
                <TableCell
                  sx={{
                    fontWeight: 700,
                    fontSize: "0.75rem",
                    color: "rgb(107 114 128)",
                  }}
                >
                  Conversion Formula
                </TableCell>
                <TableCell
                  align="center"
                  sx={{
                    fontWeight: 700,
                    fontSize: "0.75rem",
                    color: "rgb(107 114 128)",
                  }}
                >
                  Status
                </TableCell>
                <TableCell
                  align="center"
                  sx={{
                    fontWeight: 700,
                    fontSize: "0.75rem",
                    color: "rgb(107 114 128)",
                  }}
                >
                  Actions
                </TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {mappings.length === 0 ? (
                <TableRow>
                  <TableCell colSpan={5} align="center">
                    <Box className="text-center py-4 text-gray-500">
                      No unit conversion mappings found matching your criteria.
                    </Box>
                  </TableCell>
                </TableRow>
              ) : (
                mappings.map((mapping) => (
                  <TableRow
                    key={mapping.id}
                    hover
                    sx={{ "&:last-child td, &:last-child th": { border: 0 } }}
                  >
                    <TableCell component="th" scope="row">
                      <span className="font-semibold text-indigo-600">
                        {mapping.sourceUnit}
                      </span>
                      <span className="mx-2 text-gray-400">→</span>
                      <span className="font-semibold text-gray-900">
                        {mapping.targetUnit}
                      </span>
                    </TableCell>
                    <TableCell>{mapping.dataSource}</TableCell>
                    <TableCell sx={{ fontFamily: "monospace" }}>
                      {formatFormula(mapping)}
                    </TableCell>
                    <TableCell align="center" width={100} padding="none">
                      <Tooltip
                        title={mapping.isActivated ? "Deactivate" : "Activate"}
                      >
                        <Switch
                          checked={mapping.isActivated}
                          onChange={() => handleToggleActivation(mapping)}
                          inputProps={{ "aria-label": "toggle activation" }}
                          color={mapping.isActivated ? "success" : "error"}
                        />
                      </Tooltip>
                    </TableCell>
                    <TableCell
                      align="center"
                      width={100}
                      onClick={(e) => e.stopPropagation()}
                    >
                      <Box
                        sx={{
                          display: "flex",
                          justifyContent: "center",
                          gap: 1,
                        }}
                      >
                        <Tooltip title="Edit Mapping">
                          <IconButton
                            onClick={() => handleEdit(mapping)}
                            color="info"
                            size="small"
                            aria-label="Edit"
                          >
                            <Edit fontSize="small" />
                          </IconButton>
                        </Tooltip>
                        <Tooltip title="Delete Mapping">
                          <IconButton
                            onClick={() => handleDeleteClick(mapping)}
                            color="error"
                            size="small"
                            aria-label="Delete"
                          >
                            <Delete fontSize="small" />
                          </IconButton>
                        </Tooltip>
                      </Box>
                    </TableCell>
                  </TableRow>
                ))
              )}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      {isFormOpen && (
        <DynamicAddUpdateForm<UnitConversionMapping>
          isOpen={isFormOpen}
          onClose={handleFormClose}
          schema={UnitConversionMappingSchema}
          fieldConfig={UnitConversionMappingFormConfig as FieldConfig<UnitConversionMapping>}
          fieldGroups={UnitConversionMappingFormConfig.fieldGroups}
          initialValue={editingItem ?? undefined}
          isUpdate={!!editingItem}
          onSave={handleSave}
          title="Unit Conversion Mapping"
          apiError={apiError}
          shakeKey={shakeKey}
        />
      )}

      <Dialog open={isConfirmDeleteOpen} onClose={handleCancelDelete}>
        <DialogTitle>Confirm Deletion</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Are you sure you want to delete the unit conversion mapping:
            <Box
              component="span"
              fontWeight="bold"
              sx={{ display: "block", mt: 1 }}
            >
              Source Unit: {deletingItem?.sourceUnit}
              <br />
              Target Unit: {deletingItem?.targetUnit}
            </Box>
            This action cannot be undone.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCancelDelete}>Cancel</Button>
          <Button
            onClick={handleConfirmDelete}
            color="error"
            variant="contained"
            autoFocus
          >
            Delete
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
}
