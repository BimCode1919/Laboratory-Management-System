// src/features/TestOrder/pages/UnitConversionMappingPage.tsx

import { useState, useEffect, useCallback } from "react";
import { Edit, Delete, Add } from "@mui/icons-material";
import {
  CircularProgress,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Box,
  Button,
  Typography,
  Tooltip,
  IconButton,
  Switch,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
} from "@mui/material";

import DynamicAddUpdateForm, {
  type FieldConfig,
} from "../../../components/DynamicAddUpdateForm";
import {
  UnitConversionMappingSchema,
  UnitConversionMappingFormConfig,
  type UnitConversionMapping,
} from "../types/UnitConversionMapping";
import {
  getAllUnitConversionMappings,
  saveUnitConversionMapping,
  deleteUnitConversionMapping,
  enableUnitConversionMapping,
  disableUnitConversionMapping,
} from "../services/UnitConversionMappingServices";

export default function UnitConversionMappingPage() {
  const [mappings, setMappings] = useState<UnitConversionMapping[]>([]);
  const [loading, setLoading] = useState(true);

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

  const [error, setError] = useState(false);

  const loadMappings = useCallback(async () => {
    setLoading(true);
    setError(false);

    const data = await getAllUnitConversionMappings();

    if (data === null) {
      setError(true);
      setMappings([]);
    } else {
      setMappings(data);
    }

    setLoading(false);
  }, []);

  useEffect(() => {
    loadMappings();
  }, [loadMappings]);

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
      await loadMappings();
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

    const idToToggle = item.id;

    try {
      await action(idToToggle);
      await loadMappings();
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
        await loadMappings();
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
      multiplyFactor === 1 ? "Source" : `Source × ${multiplyFactor}`;
    const offsetText =
      calculationOffset === 0
        ? ""
        : calculationOffset > 0
        ? ` + ${calculationOffset}`
        : ` - ${Math.abs(calculationOffset)}`;

    return `Target = ${factorText}${offsetText}`;
  };

  if (loading) {
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

  if (error) {
    return (
      <Box className="text-center py-12 text-red-500">
        Failed to load mappings. Please try again.
        <Button onClick={loadMappings} sx={{ mt: 2 }} variant="outlined">
          Retry
        </Button>
      </Box>
    );
  }

  return (
    <Box p={3}>
      <Box
        display="flex"
        justifyContent="space-between"
        alignItems="center"
        mb={2}
      >
        <Typography variant="h5" fontWeight={600}>
          Unit Conversion Mappings
        </Typography>

        <Button onClick={handleAdd} variant="contained" startIcon={<Add />}>
          Add New Mapping
        </Button>
      </Box>

      <TableContainer component={Paper}>
        <Table sx={{ minWidth: 650 }} aria-label="Unit Conversion Mappings">
          <TableHead>
            <TableRow sx={{ backgroundColor: "rgb(249 250 251)" }}>
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
                    No unit conversion mappings found. Click "Add New Mapping"
                    to begin.
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

      <DynamicAddUpdateForm<UnitConversionMapping>
        isOpen={isFormOpen}
        onClose={handleFormClose}
        schema={UnitConversionMappingSchema}
        fieldConfig={
          UnitConversionMappingFormConfig as FieldConfig<UnitConversionMapping>
        }
        fieldGroups={UnitConversionMappingFormConfig.fieldGroups}
        initialValue={editingItem ?? undefined}
        isUpdate={!!editingItem}
        onSave={handleSave}
        title="Unit Conversion Mapping"
        apiError={apiError}
        shakeKey={shakeKey}
      />

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
