// src/features/TestOrder/pages/ResultParameterMappingPage.tsx

import { useCallback, useEffect, useState } from "react";
import {
  deleteResultParameterMapping,
  disableResultParameterMapping,
  enableResultParameterMapping,
  getAllResultParameterMappings,
  saveResultParameterMapping,
} from "../services/ResultParameterMappingServices";
import DynamicAddUpdateForm, {
  type FieldConfig,
} from "../../../components/DynamicAddUpdateForm";
import {
  ResultParameterMappingSchema,
  ResultParameterMappingFormConfig,
  type ResultParameterMapping,
} from "../types/ResultParameterMapping";

import {
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
  Typography,
} from "@mui/material";
import { Add, Delete, Edit } from "@mui/icons-material";

export default function ResultParameterMappingPage() {
  const [mappings, setMappings] = useState<ResultParameterMapping[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(false);

  const [isFormOpen, setIsFormOpen] = useState(false);
  const [editingItem, setEditingItem] = useState<
    ResultParameterMapping | undefined
  >(undefined);

  const [apiError, setApiError] = useState<string | null>(null);
  const [shakeKey, setShakeKey] = useState(0);
  const [isConfirmDeleteOpen, setIsConfirmDeleteOpen] = useState(false);
  const [deletingItem, setDeletingItem] = useState<
    ResultParameterMapping | undefined
  >(undefined);

  const loadMappings = useCallback(async () => {
    setLoading(true);
    setError(false);

    const data = await getAllResultParameterMappings();

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

  const handleAdd = () => {
    setEditingItem(undefined);
    setIsFormOpen(true);
    setApiError(null);
  };

  const handleUpdate = (item: ResultParameterMapping) => {
    setEditingItem(item);
    setIsFormOpen(true);
    setApiError(null);
  };

  const handleSave = async (data: ResultParameterMapping) => {
    setApiError(null);
    try {
      await saveResultParameterMapping(data);
      await loadMappings();
      setIsFormOpen(false);
      setEditingItem(undefined);
    } catch (err) {
      const errorMessage =
        err instanceof Error
          ? err.message
          : "Unknown error occurred while saving mapping.";
      setApiError(errorMessage);
      setShakeKey((prev) => prev + 1);
    }
  };

  const handleFormClose = () => {
    setIsFormOpen(false);
    setEditingItem(undefined);
    setApiError(null);
  };

  const handleDelete = (item: ResultParameterMapping) => {
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
        await deleteResultParameterMapping(deletingItem.id);
        await loadMappings();
      } catch (e) {
        console.error("Failed to delete mapping: ", e);
      }
    }

    handleCancelDelete();
  };

  const handleToggleActivation = async (item: ResultParameterMapping) => {
    if (item.id === undefined) return;

    const action = item.isActivated
      ? disableResultParameterMapping
      : enableResultParameterMapping;
    try {
      await action(item.id);
      await loadMappings();
    } catch (e) {
      console.error("Failed to toggle activision: ", e);
    }
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
      <Box p={3}>
        <Typography color="error">
          Failed to load result parameter mappings.
        </Typography>
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
          Result Parameter Mappings
        </Typography>

        <Button variant="contained" onClick={handleAdd}>
          <Add /> Add new Param Map
        </Button>
      </Box>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>External Parameter</TableCell>
              <TableCell>Internal Parameter</TableCell>
              <TableCell>Data Source</TableCell>
              <TableCell>Status</TableCell>
              <TableCell align="center">Active</TableCell>
              <TableCell align="center">Actions</TableCell>
            </TableRow>
          </TableHead>

          <TableBody>
            {mappings.map((mapping) => (
              <TableRow key={mapping.id} hover>
                <TableCell>{mapping.externalParamName}</TableCell>
                <TableCell>{mapping.internalParamName}</TableCell>
                <TableCell>{mapping.dataSource}</TableCell>
                <TableCell>
                  <Box
                    component="span"
                    sx={{
                      px: 1.5,
                      py: 0.5,
                      borderRadius: "12px",
                      fontSize: "0.8rem",
                      fontWeight: 600,
                      color: mapping.isActivated ? "#0f5132" : "#842029",
                      backgroundColor: mapping.isActivated
                        ? "#d1e7dd"
                        : "#f8d7da",
                    }}
                  >
                    {mapping.isActivated ? "ACTIVATED" : "DEACTIVATED"}
                  </Box>
                </TableCell>
                <TableCell align="center" width={50} padding="checkbox">
                  <Tooltip
                    title={mapping.isActivated ? "Deactivate" : "Activate"}
                  >
                    <Switch
                      checked={mapping.isActivated}
                      onChange={() => handleToggleActivation(mapping)}
                      inputProps={{ "aria-label": "toggle activation" }}
                    />
                  </Tooltip>
                </TableCell>
                <TableCell
                  align="center"
                  width={100}
                  onClick={(e) => e.stopPropagation()}
                >
                  <Tooltip title="Edit Mapping">
                    <IconButton
                      size="small"
                      onClick={() => handleUpdate(mapping)}
                    >
                      <Edit fontSize="small" />
                    </IconButton>
                  </Tooltip>
                  <Tooltip title="Delete Mapping">
                    <IconButton
                      size="small"
                      color="error"
                      onClick={() => handleDelete(mapping)}
                    >
                      <Delete fontSize="small" />
                    </IconButton>
                  </Tooltip>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      {isFormOpen && (
        <DynamicAddUpdateForm<ResultParameterMapping>
          isOpen={isFormOpen}
          onClose={handleFormClose}
          schema={ResultParameterMappingSchema}
          fieldConfig={
            ResultParameterMappingFormConfig as FieldConfig<ResultParameterMapping>
          }
          fieldGroups={ResultParameterMappingFormConfig.fieldGroups}
          initialValue={editingItem}
          isUpdate={!!editingItem}
          onSave={handleSave}
          title="Result Parameter Mapping"
          apiError={apiError}
          shakeKey={shakeKey}
        />
      )}

      <Dialog open={isConfirmDeleteOpen} onClose={handleCancelDelete}>
        <DialogTitle>Confirm Deletion</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Are you sure you want to delete the mapping for:
            <Box
              component="span"
              fontWeight="bold"
              sx={{ display: "block", mt: 1 }}
            >
              External Param: {deletingItem?.externalParamName}
              <br />
              Data Source: {deletingItem?.dataSource}
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
