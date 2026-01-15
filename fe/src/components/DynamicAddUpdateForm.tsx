// src/components/DynamicAddUpdateForm.tsx

import {
  Alert,
  Box,
  Button,
  Dialog as ConfirmationDialog,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  FormControl,
  FormControlLabel,
  InputLabel,
  keyframes,
  MenuItem,
  Select,
  Switch,
  TextField,
} from "@mui/material";
import { useEffect, useMemo, useState } from "react";
import { z } from "zod";

type FieldConfigItem = {
  key: string;
  label: string;
  type: "text" | "number" | "boolean" | "enum" | "textarea";
  required?: boolean;
  options?: string[];
};

export type FieldConfig<T> = {
  fields: ReadonlyArray<FieldConfigItem>;
  fieldGroups?: ReadonlyArray<ReadonlyArray<string>>;
};

type DynamicAddUpdateFormProps<T> = {
  isOpen: boolean;
  onClose: () => void;
  schema: z.ZodObject<any>;
  initialValue?: T;
  onSave: (data: T) => Promise<void>;
  isSaving?: boolean;
  title?: string;
  isUpdate?: boolean;
  fieldGroups?: ReadonlyArray<ReadonlyArray<string>>;
  fieldConfig?: {
    fields: ReadonlyArray<FieldConfigItem>;
    fieldGroups?: ReadonlyArray<ReadonlyArray<string>>;
  };
  apiError: string | null;
  shakeKey: number;
};

const shakeAnimation = keyframes`
  0%, 100% {transform: translateX(0);}
  20%, 60% {transform: translateX(-5px);}
  40%, 80% {transform: translateX(5px);}
`;

export default function DynamicAddUpdateForm<T>({
  isOpen,
  onClose,
  schema,
  initialValue,
  onSave,
  isSaving = false,
  title = "Create",
  isUpdate = false,
  fieldGroups = [],
  fieldConfig,
  apiError,
  shakeKey,
}: DynamicAddUpdateFormProps<T>) {
  const [formValues, setFormValues] = useState<Record<string, any>>({});
  const [error, setError] = useState<Record<string, any>>({});
  const [confirmCloseOpen, setConfirmCloseOpen] = useState(false);

  useEffect(() => {
    setFormValues(initialValue ?? {});
    setError({});
  }, [initialValue]);

  const dirty = useMemo(() => {
    return JSON.stringify(formValues) !== JSON.stringify(initialValue ?? {});
  }, [formValues, initialValue]);

  const handleClose = () => {
    if (dirty) setConfirmCloseOpen(true);
    else onClose();
  };

  const confirmDiscard = () => {
    setConfirmCloseOpen(false);
    onClose();
  };

  const handleChange = (field: string, value: any) => {
    setFormValues((prev) => ({ ...prev, [field]: value }));
  };

  const prettifyLabel = (field: string) => {
    return field
      .replace(/([A-Z])/g, " $1")
      .replace(/^./, (str) => str.toUpperCase());
  };

  const schemaShape = (schema as any).shape ?? {};

  const renderInput = (field: string) => {
    const fieldSchema = schemaShape[field];

    const configItem = fieldConfig?.fields.find((f) => f.key === field);

    const schemaType = fieldSchema?._def?.typeName;
    const typeHint = configItem?.type ?? schemaType;
    const label = configItem?.label ?? prettifyLabel(field);
    const value = formValues[field] ?? "";

    if (schemaType === "ZodBoolean" || typeHint === "boolean") {
      return (
        <FormControlLabel
          control={
            <Switch
              checked={!!value}
              onChange={(e) => handleChange(field, e.target.checked)}
            />
          }
          label={label}
        />
      );
    }

    if (schemaType === "ZodEnum" || typeHint === "enum") {
      const options = configItem?.options ?? fieldSchema?._def?.values ?? [];
      return (
        <FormControl fullWidth>
          <InputLabel>{label}</InputLabel>
          <Select
            label={label}
            value={value ?? ""}
            onChange={(e) => handleChange(field, e.target.value)}
          >
            {options.map((opt: string) => (
              <MenuItem key={opt} value={opt}>
                {opt.charAt(0) + opt.slice(1).toLowerCase()}
              </MenuItem>
            ))}
          </Select>
        </FormControl>
      );
    }

    if (schemaType === "ZodNumber" || typeHint === "number") {
      return (
        <TextField
          fullWidth
          type="number"
          label={label}
          value={value ?? ""}
          inputProps={{ step: "0.01", min: 0 }}
          onChange={(e) => {
            const target = e.target as HTMLInputElement;
            handleChange(
              field,
              target.value === "" ? "" : target.valueAsNumber
            );
          }}
        />
      );
    }

    if (typeHint === "textarea") {
      return (
        <TextField
          fullWidth
          multiline
          rows={3}
          label={label}
          value={value ?? ""}
          onChange={(e) => handleChange(field, e.target.value)}
        />
      );
    }

    // String type (default)
    return (
      <TextField
        fullWidth
        type="text"
        label={label}
        value={value ?? ""}
        onChange={(e) => handleChange(field, e.target.value)}
      />
    );
  };

  const handleSave = async () => {
    const validation = schema.safeParse(formValues);
    if (!validation.success) {
      const formatted: Record<string, string> = {};
      validation.error.issues.forEach((issue) => {
        const field = issue.path[0] as string;
        formatted[field] = issue.message;
      });
      setError(formatted);
      return;
    }
    setError({});
    await onSave(validation.data as T);
  };

  return (
    <>
      <Dialog
        open={isOpen}
        onClose={handleClose}
        maxWidth="md"
        fullWidth
        sx={{
          "&. MuiPaper-root": {
            animation: apiError ? `${shakeAnimation} 0.4s ease-in-out` : "none",
          },
        }}
      >
        <DialogTitle>
          {isUpdate ? `Update ${title}` : `Create ${title}`}
        </DialogTitle>

        <DialogContent dividers sx={{ pt: 2 }}>
          {apiError && (
            <Alert severity="error" sx={{ mb: 2 }} key={`error-${shakeKey}`}>
              {apiError}
            </Alert>
          )}

          <Box display="flex" flexDirection={"column"} gap={2}>
            {fieldGroups.map((group, idx) => (
              <Box key={idx} display={"flex"} flexDirection={"row"} gap={2}>
                {group.map((field) => (
                  <Box flex={1} key={field}>
                    {renderInput(field)}
                  </Box>
                ))}
              </Box>
            ))}
          </Box>
        </DialogContent>

        <DialogActions sx={{ p: 2 }}>
          <Button variant="outlined" onClick={handleClose} disabled={isSaving}>
            Cancel
          </Button>

          <Button variant="contained" disabled={isSaving} onClick={handleSave}>
            {isUpdate ? "Update" : "Create"}
          </Button>
        </DialogActions>
      </Dialog>

      <ConfirmationDialog
        open={confirmCloseOpen}
        onClose={() => setConfirmCloseOpen(false)}
      >
        <DialogTitle>Discard changes?</DialogTitle>
        <DialogContent>
          <DialogContentText>
            You have unsaved changes. Are you sure you want to discard them?
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirmCloseOpen(false)}>Cancel</Button>
          <Button onClick={confirmDiscard} color="error">
            Yes, discard
          </Button>
        </DialogActions>
      </ConfirmationDialog>
    </>
  );
}
