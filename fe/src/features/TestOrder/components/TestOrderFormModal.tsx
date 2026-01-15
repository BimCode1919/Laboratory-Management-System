import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  Grid,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
  FormLabel,
  RadioGroup,
  FormControlLabel,
  Radio,
  Typography,
  Divider,
  Box,
  Alert,
  CircularProgress,
} from "@mui/material";
import {
  Add as AddIcon,
  Close as CloseIcon,
  Edit as EditIcon,
  Visibility as VisibilityIcon,
} from "@mui/icons-material";
import { useState, useEffect } from "react";
import { z } from "zod";

// Zod Schema (Giữ nguyên)
const testOrderSchema = z.object({
  patient: z.object({
    fullName: z.string().min(1, "Patient name is required"),
    email: z.string().email("Invalid email format").min(1, "Email is required"),
    phoneNumber: z.string().min(1, "Phone number is required"),
    dateOfBirth: z.string().min(1, "Date of Birth is required"),
    gender: z.string().min(1, "Gender is required"),
    address: z.string().min(1, "Address is required"),
  }),
  priority: z.enum(["HIGH", "LOW", "NORMAL"]),
  testType: z.enum(["CBC", "LFT", "HBA1C"]),
  notes: z.string().optional(),
});

export type TestOrderFormData = z.infer<typeof testOrderSchema>;

// (Interface FullTestOrderDTO giữ nguyên)
export interface FullTestOrderDTO {
  id: string;
  patient: {
    patientCode: string;
    fullName: string;
    dateOfBirth: string;
    gender: string;
    address: string;
    phoneNumber: string;
    email: string;
  };
  status: string;
  createdAt: string;
  priority: "HIGH" | "LOW" | "NORMAL";
  testType: "CBC" | "LFT" | "HBA1C";
  notes?: string;
}

interface TestOrderFormModalProps {
  open: boolean;
  onClose: () => void;
  onSubmit: (
    mode: "create" | "edit",
    data: TestOrderFormData,
    id: string | null
  ) => Promise<void>;
  mode: "create" | "edit" | "view";
  initialData: FullTestOrderDTO | null;
  isLoading: boolean;
}

export function TestOrderFormModal({
  open,
  onClose,
  onSubmit,
  mode,
  initialData,
  isLoading,
}: TestOrderFormModalProps) {
  const defaultState: TestOrderFormData = {
    patient: {
      fullName: "",
      email: "",
      phoneNumber: "",
      dateOfBirth: "",
      gender: "",
      address: "",
    },
    priority: "NORMAL",
    testType: "CBC",
    notes: "",
  };

  const [formData, setFormData] = useState<TestOrderFormData>(defaultState);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [submitError, setSubmitError] = useState<string>("");
  const [isSubmitting, setIsSubmitting] = useState(false);
  
  // 2. Thêm state để theo dõi "đã bấm submit"
  const [hasAttemptedSubmit, setHasAttemptedSubmit] = useState(false);

  const isViewMode = mode === "view";

  // (useEffect điền data giữ nguyên)
  useEffect(() => {
    if (initialData && (mode === "edit" || mode === "view")) {
      setFormData({
        patient: {
          fullName: initialData.patient.fullName,
          email: initialData.patient.email,
          phoneNumber: initialData.patient.phoneNumber,
          dateOfBirth: initialData.patient.dateOfBirth,
          gender: initialData.patient.gender,
          address: initialData.patient.address,
        },
        priority: initialData.priority,
        testType: initialData.testType,
        notes: initialData.notes || "",
      });
    } else {
      setFormData(defaultState);
    }
  }, [initialData, mode, open]);

  const handlePatientChange = (
    field: keyof TestOrderFormData["patient"],
    value: string | number
  ) => {
    setFormData((prev) => ({
      ...prev,
      patient: { ...prev.patient, [field]: value },
    }));
    setErrors((prev) => {
      const newErrors = { ...prev };
      delete newErrors[`patient.${field}`];
      return newErrors;
    });
  };
  const handleChange = (
    field: keyof Omit<TestOrderFormData, "patient">,
    value: string
  ) => {
    setFormData((prev) => ({ ...prev, [field]: value }));
    setErrors((prev) => {
      const newErrors = { ...prev };
      delete newErrors[field];
      return newErrors;
    });
  };
  const handleDobChange = (value: string) => {
    handlePatientChange("dateOfBirth", value);
  };

  const handleSubmit = async () => {
    setErrors({});
    setSubmitError("");
    const validationResult = testOrderSchema.safeParse(formData);
    if (!validationResult.success) {
      const fieldErrors: Record<string, string> = {};
      validationResult.error.errors.forEach((err) => {
        const path = err.path.join(".");
        fieldErrors[path] = err.message;
      });
      setErrors(fieldErrors);
      setSubmitError("Please fix the validation errors before submitting.");
      return;
    }

    setIsSubmitting(true);
    try {
      await onSubmit(
        mode as "create" | "edit",
        validationResult.data,
        initialData?.id || null
      );
      resetForm();
      onClose(); 
    } catch (apiError: any) {
      setSubmitError(
        apiError.message || "An unexpected error occurred. Please try again."
      );
    } finally {
      setIsSubmitting(false);
    }
  };

  const resetForm = () => {
    setFormData(defaultState);
    setErrors({});
    setSubmitError("");
  };

  const handleClose = () => {
    if (isSubmitting) return;
    resetForm();
    onClose();
  };

  const getTitle = () => {
    if (mode === "create")
      return {
        icon: <AddIcon color="primary" />,
        text: "Create New Test Order",
      };
    if (mode === "edit")
      return { icon: <EditIcon color="primary" />, text: "Edit Patient Info" };
    return {
      icon: <VisibilityIcon color="primary" />,
      text: "View Test Order Details",
    };
  };
  const { icon, text } = getTitle();

  return (
    <Dialog
      open={open}
      onClose={handleClose}
      maxWidth="md"
      fullWidth
      PaperProps={{ sx: { minHeight: "80vh" } }}
    >
      <DialogTitle
        sx={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
        }}
      >
        <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
          {icon}
          <Typography variant="h6">{text}</Typography>
        </Box>
        <Button onClick={handleClose} color="inherit" sx={{ minWidth: "auto" }}>
          <CloseIcon />
        </Button>
      </DialogTitle>

      <DialogContent dividers>
        {isLoading ? (
          <Box
            sx={{
              display: "flex",
              justifyContent: "center",
              alignItems: "center",
              height: "60vh",
            }}
          >
            <CircularProgress />
          </Box>
        ) : (
          <>
            {submitError && (
              <Alert severity="error" sx={{ mb: 3 }}>
                {submitError}
              </Alert>
            )}
            <Box sx={{ mb: 4 }}>
              <Typography
                variant="h6"
                gutterBottom
                sx={{ color: "primary.main" }}
              >
                Patient Information
              </Typography>
              <Divider sx={{ mb: 3 }} />

              <Grid container spacing={2}>
                <Grid sx={{ xs:12 }}>
                  <TextField
                    label="Patient Name"
                    fullWidth
                    required
                    value={formData.patient.fullName}
                    onChange={(e) =>
                      handlePatientChange("fullName", e.target.value)
                    }
                    error={!!errors["patient.fullName"]}
                    helperText={errors["patient.fullName"]}
                    disabled={isViewMode || isSubmitting}
                  />
                </Grid>
                <Grid sx={{ xs:12, sm:6 }}>
                  <TextField
                    label="Email"
                    type="email"
                    fullWidth
                    required
                    value={formData.patient.email}
                    onChange={(e) =>
                      handlePatientChange("email", e.target.value)
                    }
                    error={!!errors["patient.email"]}
                    helperText={errors["patient.email"]}
                    disabled={isViewMode || isSubmitting}
                  />
                </Grid>
                <Grid sx={{ xs:12, sm:6 }}>
                  <TextField
                    label="Phone Number"
                    fullWidth
                    required
                    value={formData.patient.phoneNumber}
                    onChange={(e) =>
                      handlePatientChange("phoneNumber", e.target.value)
                    }
                    error={!!errors["patient.phoneNumber"]}
                    helperText={errors["patient.phoneNumber"]}
                    disabled={isViewMode || isSubmitting}
                  />
                </Grid>
                <Grid sx={{ xs:12, sm:6 }}>
                  <TextField
                    label="Date of Birth"
                    type="date"
                    fullWidth
                    required
                    value={formData.patient.dateOfBirth}
                    onChange={(e) => handleDobChange(e.target.value)}
                    error={!!errors["patient.dateOfBirth"]}
                    helperText={errors["patient.dateOfBirth"]}
                    InputLabelProps={{ shrink: true }}
                    disabled={isViewMode || isSubmitting}
                  />
                </Grid>
                <Grid sx={{ xs:12, sm:6 }}>
                  <FormControl
                    fullWidth
                    required
                    error={!!errors["patient.gender"]}
                    disabled={isViewMode || isSubmitting}
                  >
                    <FormLabel>Gender</FormLabel>
                    <RadioGroup
                      row
                      value={formData.patient.gender}
                      onChange={(e) =>
                        handlePatientChange("gender", e.target.value)
                      }
                    >
                      <FormControlLabel
                        value="MALE"
                        control={<Radio />}
                        label="Male"
                      />
                      <FormControlLabel
                        value="FEMALE"
                        control={<Radio />}
                        label="Female"
                      />
                      <FormControlLabel
                        value="O"
                        control={<Radio />}
                        label="Other"
                      />
                    </RadioGroup>
                    {errors["patient.gender"] && (
                      <Typography variant="caption" color="error">
                        {errors["patient.gender"]}
                      </Typography>
                    )}
                  </FormControl>
                </Grid>
                <Grid sx={{ xs:12 }}>
                  <TextField
                    label="Address"
                    fullWidth
                    required
                    multiline
                    rows={2}
                    value={formData.patient.address}
                    onChange={(e) =>
                      handlePatientChange("address", e.target.value)
                    }
                    error={!!errors["patient.address"]}
                    helperText={errors["patient.address"]}
                    disabled={isViewMode || isSubmitting}
                  />
                </Grid>
              </Grid>
            </Box>

            {/* Test Order Details Section (Giữ nguyên logic 'disabled') */}
            <Box>
              <Typography
                variant="h6"
                gutterBottom
                sx={{ color: "primary.main" }}
              >
                Test Order Details
              </Typography>
              <Divider sx={{ mb: 3 }} />
              <Grid container spacing={2}>
                <Grid sx={{ xs: 12, sm: 6 }}>
                  <FormControl
                    fullWidth
                    required
                    error={!!errors.testType}
                    disabled={isViewMode || mode === "edit" || isSubmitting}
                  >
                    <InputLabel>Test Type</InputLabel>
                    <Select
                      value={formData.testType}
                      label="Test Type"
                      onChange={(e) => handleChange("testType", e.target.value)}
                    >
                      <MenuItem value="CBC">
                        Complete Blood Count (CBC)
                      </MenuItem>
                      <MenuItem value="LFT">Liver Function Test (LFT)</MenuItem>
                      <MenuItem value="HBA1C">HbA1c</MenuItem>
                    </Select>
                    {errors.testType && (
                      <Typography variant="caption" color="error" sx={{ pl: 2 }}>
                        {errors.testType}
                      </Typography>
                    )}
                  </FormControl>
                </Grid>

                <Grid sx={{ xs: 12 }}>
                  <TextField
                    label="Notes (Optional)"
                    fullWidth
                    multiline
                    rows={3}
                    value={formData.notes}
                    onChange={(e) => handleChange("notes", e.target.value)}
                    disabled={isViewMode || mode === "edit" || isSubmitting}
                    error={!!errors.notes}
                    helperText={
                      errors.notes || "Additional notes or special instructions"
                    }
                    placeholder="Enter any special instructions or notes for this test order..."
                  />
                </Grid>
              </Grid>
            </Box>
            <Box
              sx={{
                mt: 3,
                p: 2,
                bgcolor: "grey.50",
                borderRadius: 1,
                border: 1,
                borderColor: "grey.300",
              }}
            >
              <Typography variant="subtitle2" gutterBottom>
                Order Summary
              </Typography>
              <Grid container spacing={1}>
                <Grid sx={{ xs:6 }}>
                  <Typography variant="caption" color="text.secondary">
                    Patient:
                  </Typography>
                  <Typography variant="body2">
                    {formData.patient.fullName || "—"}
                  </Typography>
                </Grid>
                <Grid sx={{ xs:6 }}>
                  <Typography variant="caption" color="text.secondary">
                    Gender:
                  </Typography>
                  <Typography variant="body2">
                    {formData.patient.gender}
                  </Typography>
                </Grid>
                <Grid sx={{xs:6}}>
                  <Typography variant="caption" color="text.secondary">
                    Test Type:
                  </Typography>
                  <Typography variant="body2">{formData.testType}</Typography>
                </Grid>
              </Grid>
            </Box>
          </>
        )}
      </DialogContent>
      <DialogActions sx={{ p: 2, gap: 1 }}>
        <Button
          onClick={handleClose}
          color="inherit"
          variant="outlined"
          disabled={isSubmitting}
        >
          {isViewMode ? "Close" : "Cancel"}
        </Button>

        {!isViewMode && (
          <Button
            onClick={handleSubmit}
            variant="contained"
            disabled={isSubmitting || isLoading}
            startIcon={
              isSubmitting ? (
                <CircularProgress size={20} color="inherit" />
              ) : mode === "create" ? (
                <AddIcon />
              ) : (
                <EditIcon />
              )
            }
          >
            {isSubmitting
              ? "Saving..."
              : mode === "create"
              ? "Create Test Order"
              : "Save Changes"}
          </Button>
        )}
      </DialogActions>
    </Dialog>
  );
}
