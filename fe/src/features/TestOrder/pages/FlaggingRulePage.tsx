// src/features/TestOrder/pages/FlaggingRulePage.tsx

import { useCallback, useEffect, useState } from "react";
import {
  FlaggingRulesSchema,
  FlaggingRulesFormConfig,
  type FlaggingRules,
} from "../types/FlaggingRules";
import {
  getAllFlaggingRules,
  saveFlaggingRule,
  deleteFlaggingRule,
  enableFlaggingRule,
  disableFlaggingRule,
} from "../services/FlaggingRulesServices";
import DynamicAddUpdateForm, {
  type FieldConfig,
} from "../../../components/DynamicAddUpdateForm";

import {
  Box,
  CircularProgress,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
  Button,
  IconButton,
  Tooltip,
  Switch,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
} from "@mui/material";
import { DoNotDisturb, Edit, Delete, Add } from "@mui/icons-material";

export default function FlaggingRulePage() {
  const [rules, setRules] = useState<FlaggingRules[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(false);

  const [isFormOpen, setIsFormOpen] = useState(false);
  const [editingItem, setEditingItem] = useState<FlaggingRules | undefined>(
    undefined
  );

  const [apiError, setApiError] = useState<string | null>(null);
  const [shakeKey, setShakeKey] = useState(0);

  const [isConfirmDeleteOpen, setIsConfirmDeleteOpen] = useState(false);
  const [deletingItem, setDeletingItem] = useState<FlaggingRules | undefined>(
    undefined
  );

  const loadRules = useCallback(async () => {
    setLoading(true);
    setError(false);

    const data = await getAllFlaggingRules();

    if (data === null) {
      setError(true);
      setRules([]);
    } else {
      setRules(data);
    }

    setLoading(false);
  }, []);

  useEffect(() => {
    loadRules();
  }, [loadRules]);

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

  const handleEdit = (rule: FlaggingRules) => {
    setEditingItem(rule);
    setIsFormOpen(true);
    setApiError(null);
  };

  const handleSave = async (rule: FlaggingRules) => {
    setApiError(null);
    try {
      await saveFlaggingRule(rule);
      await loadRules();
      handleFormClose();
    } catch (e) {
      const errorMessage =
        e instanceof Error
          ? e.message
          : "An unknown error occurred while saving the rule.";

      setApiError(errorMessage);
      setShakeKey((prev) => prev + 1);
    }
  };

  const handleDeleteClick = (item: FlaggingRules) => {
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
        await deleteFlaggingRule(deletingItem.id);
        await loadRules();
      } catch (e) {
        console.error("Failed to delete flagging rule:", e);
      }
    }
    handleCancelDelete();
  };

  const handleToggleActivation = async (item: FlaggingRules) => {
    if (item.id === undefined) return;

    const action = item.isActivated ? disableFlaggingRule : enableFlaggingRule;

    const idToToggle = item.id;

    try {
      await action(idToToggle);
      await loadRules();
    } catch (e) {
      console.error("Failed to toggle activation:", e);
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
        <Typography color="error">Failed to load flagging rules.</Typography>
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
          Flagging Rules
        </Typography>

        <Button variant="contained" onClick={handleAdd}>
          <Add /> Add new Flagging Rule
        </Button>
      </Box>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell align="center">Parameter Name</TableCell>
              <TableCell align="center">Unit</TableCell>
              <TableCell align="center">Gender</TableCell>
              <TableCell align="center">Lower Bound</TableCell>
              <TableCell align="center">Upper Bound</TableCell>
              <TableCell align="center">Description</TableCell>
              <TableCell align="center">Active</TableCell>
              <TableCell align="center">Actions</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {rules.map((rule) => (
              <TableRow key={rule.id} hover>
                <TableCell align="center">{rule.parameterName}</TableCell>
                <TableCell align="center">
                  {rule.unit ?? (
                    <DoNotDisturb
                      fontSize="small"
                      sx={{ color: "text.disabled" }}
                    />
                  )}
                </TableCell>
                <TableCell align="center">
                  {rule.gender ? (
                    <Box
                      component="span"
                      sx={{
                        px: 1,
                        py: 0.2,
                        borderRadius: "4px",
                        fontSize: "0.75rem",
                        backgroundColor:
                          rule.gender === "MALE" ? "#b3e5fc" : "#f8bbd0",
                        color: rule.gender === "MALE" ? "#01579b" : "#c2185b",
                        fontWeight: 600,
                      }}
                    >
                      {rule.gender}
                    </Box>
                  ) : (
                    <DoNotDisturb fontSize="small" sx={{ color: "red" }} />
                  )}
                </TableCell>
                <TableCell align="center">
                  {rule.normalLow ?? (
                    <DoNotDisturb
                      fontSize="small"
                      sx={{ color: "text.disabled" }}
                    />
                  )}
                </TableCell>
                <TableCell align="center">
                  {rule.normalHigh ?? (
                    <DoNotDisturb
                      fontSize="small"
                      sx={{ color: "text.disabled" }}
                    />
                  )}
                </TableCell>
                <TableCell>{rule.description}</TableCell>

                <TableCell align="center" width={50} padding="checkbox">
                  <Tooltip title={rule.isActivated ? "Deactivate" : "Activate"}>
                    <Switch
                      checked={rule.isActivated}
                      onChange={() => handleToggleActivation(rule)}
                      inputProps={{ "aria-label": "toggle activation" }}
                      color={rule.isActivated ? "success" : "error"}
                    />
                  </Tooltip>
                </TableCell>

                <TableCell
                  align="center"
                  width={100}
                  onClick={(e) => e.stopPropagation()}
                >
                  <Tooltip title="Edit Rule">
                    <IconButton size="small" onClick={() => handleEdit(rule)}>
                      <Edit fontSize="small" />
                    </IconButton>
                  </Tooltip>
                  <Tooltip title="Delete Rule">
                    <IconButton
                      size="small"
                      color="error"
                      onClick={() => handleDeleteClick(rule)}
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
        <DynamicAddUpdateForm<FlaggingRules>
          isOpen={isFormOpen}
          onClose={handleFormClose}
          schema={FlaggingRulesSchema}
          fieldConfig={FlaggingRulesFormConfig as FieldConfig<FlaggingRules>}
          fieldGroups={FlaggingRulesFormConfig.fieldGroups}
          initialValue={editingItem}
          isUpdate={!!editingItem}
          onSave={handleSave}
          title="Flagging Rule"
          apiError={apiError}
          shakeKey={shakeKey}
        />
      )}

      <Dialog open={isConfirmDeleteOpen} onClose={handleCancelDelete}>
        <DialogTitle>Confirm Deletion</DialogTitle>
        <DialogContent>
          <DialogContentText>
            Are you sure you want to delete the flagging rule for:
            <Box
              component="span"
              fontWeight="bold"
              sx={{ display: "block", mt: 1 }}
            >
              Parameter: {deletingItem?.parameterName}
              <br />
              Gender: {deletingItem?.gender ?? "N/A"}
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
