import { useEffect, useState, useMemo, useCallback } from "react";
import { Box, Button, Paper, Card, CardContent, CardActions, Avatar, Stack, IconButton, Typography, Chip, Tooltip, Fade, TablePagination, Skeleton, Dialog, DialogTitle, DialogContent, TextField, DialogContentText, DialogActions, Switch, FormControlLabel, Alert, MenuItem, Select, InputLabel, FormControl, Divider, InputAdornment } from "@mui/material";
import RefreshIcon from "@mui/icons-material/Refresh";
import AddIcon from "@mui/icons-material/Add";
import DeleteIcon from "@mui/icons-material/Delete";
import SearchIcon from "@mui/icons-material/Search";
import ScienceIcon from "@mui/icons-material/Science";
import machineIcon from '@/assets/medical-laboratory (1).svg';
import VisibilityIcon from "@mui/icons-material/Visibility";
import LabelOutlinedIcon from '@mui/icons-material/LabelOutlined';
import VpnKeyIcon from '@mui/icons-material/VpnKey';
import DescriptionIcon from '@mui/icons-material/Description';
import RestoreIcon from '@mui/icons-material/Restore';
import CloseIcon from '@mui/icons-material/Close';
import { toast } from "react-toastify";
import { createConfiguration } from "../../services/ConfigurationServices";
import { getAllConfigurations } from "../../services/ConfigurationServices";
import type { CreateConfigurationRequest } from "../../types/configuration";
import { getAllReagents } from "../../services/ReagentServices";
import type { Reagent } from "../../types/Reagent";
import StatusChip from "../../components/StatusChip";

import { type Instrument } from "../../types/Instrument";
import {
  getAllInstruments,
  activateInstrument,
  deactivateInstrument,
  saveInstrument,
  cloneConfigsToInstrument,
  updateInstrumentStatus,
} from "../../services/InstrumentServices";


export default function InstrumentList() {
  const [instruments, setInstruments] = useState<Instrument[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(false);

  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(8);
  const [query, setQuery] = useState("");

  

  // Modal state for create form
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);

  // Form states for create
  const [name, setName] = useState("");
  const [model, setModel] = useState("");
  const [status, setStatus] = useState("Ready");
  const [serialNumber, setSerialNumber] = useState("");
  const [editingInstrumentId, setEditingInstrumentId] = useState<string | number | null>(null);
  const [cloneFrom, setCloneFrom] = useState("none");
  const [saving, setSaving] = useState(false);
  // Clone dialog state
  const [isCloneDialogOpen, setIsCloneDialogOpen] = useState(false);
  const [selectedInstrumentForClone, setSelectedInstrumentForClone] =
    useState<string>("");
  const [cloning, setCloning] = useState(false);
  // Create Configuration dialog state
  const [isCreateConfigOpen, setIsCreateConfigOpen] = useState(false);
  const [cfgName, setCfgName] = useState("");
  const [cfgKey, setCfgKey] = useState("");
  const [cfgValue, setCfgValue] = useState("");
  const [cfgDefault, setCfgDefault] = useState("");
  const [cfgDescription, setCfgDescription] = useState("");
  
  const [creatingConfig, setCreatingConfig] = useState(false);
  // Status change dialog state
  const [isStatusDialogOpen, setIsStatusDialogOpen] = useState(false);
  const [statusTargetInstrument, setStatusTargetInstrument] = useState<Instrument | null>(null);
  const [statusNewValue, setStatusNewValue] = useState("READY");
  const [statusReason, setStatusReason] = useState("");
  const [statusUpdating, setStatusUpdating] = useState(false);

  // Detail dialog state
  const [instrumentDetail, setInstrumentDetail] = useState<Instrument | null>(null);
  

  // Reagents and Configurations state
  const [reagents, setReagents] = useState<
    Array<{ id: number; reagentId: string; quantity: number }>
  >([]);
  const [reagentOptions, setReagentOptions] = useState<Reagent[]>([]);
  const [configurationMode, setConfigurationMode] = useState<
    "default" | "clone" | "select"
  >("default");
  const [selectedConfigIds, setSelectedConfigIds] = useState<string[]>([]);
  const [reagentCounter, setReagentCounter] = useState(1);
  const [templateOptions, setTemplateOptions] = useState<any[]>([]);

  const loadTemplates = async () => {
    try {
      const list = await getAllConfigurations();
      if (Array.isArray(list)) {
        // keep only global templates
        const globals = list.filter((c: any) => !!(c.isGlobal || c.global));
        setTemplateOptions(globals);
      } else {
        setTemplateOptions([]);
      }
    } catch (err) {
      console.error('Failed to load configuration templates', err);
      setTemplateOptions([]);
    }
  };

  // Fetch instruments
  const loadInstruments = useCallback(async () => {
    console.log("Loading instruments...");
    setLoading(true);
    setError(false);

    try {
      const data = await getAllInstruments();
      console.log("Instruments data received:", data);

      if (data === null) {
        console.warn("getAllInstruments returned null — treating as empty list");
        setInstruments([]);
        setError(false);
      } else {
        console.log(`Successfully loaded ${data.length} instruments`);
        setInstruments(data);
      }
    } catch (err) {
      console.error("Exception in loadInstruments:", err);
      // real exception — show error banner
      setError(true);
      setInstruments([]);
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadInstruments();
  }, [loadInstruments]);

  useEffect(() => {
    loadTemplates();
  }, []);

  // Load reagent options for selection in create form
  const loadReagents = async () => {
    try {
      const list = await getAllReagents();
      if (list && Array.isArray(list)) setReagentOptions(list as Reagent[]);
      else setReagentOptions([]);
    } catch (err) {
      console.error("Failed to load reagent options:", err);
      setReagentOptions([]);
    }
  };

  useEffect(() => {
    // initial load of reagent options (non-blocking)
    loadReagents();
  }, []);

  const handleAdd = () => {
    // Reset form
    setName("");
    setModel("");
    setStatus("Ready");
    setSerialNumber("");
    setCloneFrom("none");
    setReagents([]);
    setEditingInstrumentId(null);
  // make sure reagent options are loaded when opening the create modal
  loadReagents();
    setConfigurationMode("default");
    setSelectedConfigIds([]);
    setReagentCounter(1);
    setIsCreateModalOpen(true);
  };

  const handleOpenEdit = (inst: Instrument) => {
    // populate form with instrument data and open modal in edit mode
    console.debug("Opening edit modal for instrument id:", inst.id);
    setEditingInstrumentId(inst.id !== undefined && inst.id !== null ? String(inst.id) : null);
    setName(inst.name || "");
    setModel(inst.model || "");
   
    const reverseStatusMap: Record<string, string> = {
      READY: "Ready",
      MAINTENANCE: "Maintenance",
      INACTIVE: "Inactive",
      PROCESSING: "Processing",
      ERROR: "Error",
    };
    const raw = String(inst.status || "");
    setStatus(reverseStatusMap[raw.toUpperCase()] || raw || "Ready");
    setSerialNumber(inst.serialNumber || "");
    // For simplicity reuse the selectedConfigIds and reagents state — attempt to read from instrument
    setSelectedConfigIds((inst as any).configurationIds || []);
    setReagents((inst as any).reagents || []);
    setConfigurationMode("default");
    loadReagents();
    setIsCreateModalOpen(true);
  };

  const handleCloneConfigs = async (item: Instrument) => {
    if (!item.id) return;
    try {
      const cloned = await cloneConfigsToInstrument(String(item.id));
      if (cloned && Array.isArray(cloned)) {
        toast.success(`Cloned ${cloned.length} configurations to ${item.name}`);
      } else {
        toast.success(`Configurations cloned to ${item.name}`);
      }
      await loadInstruments();
    } catch (err) {
      console.error("Failed to clone configs:", err);
      toast.error("Failed to clone configurations");
    }
  };

  const handleAddReagent = () => {
    setReagents([
      ...reagents,
      { id: reagentCounter, reagentId: "", quantity: 1 },
    ]);
    setReagentCounter(reagentCounter + 1);
  };

  const handleRemoveReagent = (id: number) => {
    setReagents(reagents.filter((r) => r.id !== id));
  };

  const handleCancelCreate = () => {
    setIsCreateModalOpen(false);
    setEditingInstrumentId(null);
  };

  const handleAddConfigId = () => {
    // default to first available template id if exists
    const defaultId = templateOptions.length > 0 ? (templateOptions[0].configId || templateOptions[0].id) : "";
    setSelectedConfigIds([...selectedConfigIds, defaultId]);
  };

  const handleRemoveConfigId = (index: number) => {
    setSelectedConfigIds(selectedConfigIds.filter((_, i) => i !== index));
  };

  // Dialog handlers (header-level Add Configs)
  const handleOpenCloneDialog = () => setIsCloneDialogOpen(true);
  const handleCloseCloneDialog = () => {
    setIsCloneDialogOpen(false);
    setSelectedInstrumentForClone("");
  };

  const handleCloseStatusDialog = () => {
    setIsStatusDialogOpen(false);
    setStatusTargetInstrument(null);
    setStatusReason("");
  };

  const handleSubmitStatusChange = async () => {
    if (!statusTargetInstrument || statusTargetInstrument.id === undefined) {
      console.error('No instrument id available for status update', { statusTargetInstrument });
      toast.error('Cannot update status: instrument id is missing');
      return;
    }
    setStatusUpdating(true);
    try {
      const statusMap: Record<string, string> = {
        Ready: "READY",
        Maintenance: "MAINTENANCE",
        Inactive: "INACTIVE",
        Processing: "PROCESSING",
        Error: "ERROR",
      };
      const normalized = statusMap[statusNewValue] || statusNewValue.toUpperCase();
      console.debug("Submitting status change", { id: statusTargetInstrument.id, normalized, updatedBy: "admin", reason: statusReason, target: statusTargetInstrument });
      const res = await updateInstrumentStatus(String(statusTargetInstrument.id), normalized, "admin", statusReason || undefined);
      console.debug("Status change response", { res });
      if (res) {
        toast.success("Instrument status updated");
        await loadInstruments();
      } else {
        toast.error("Failed to update instrument status");
      }
    } catch (err) {
      console.error("Failed to update instrument status:", err);
      toast.error("Failed to update instrument status");
    } finally {
      setStatusUpdating(false);
      handleCloseStatusDialog();
    }
  };
  

  

  // When user clicks "Add Instrument" from the Clone dialog, close the dialog first
  // and open the Create modal a short moment later so the new modal receives focus
  // and inputs are interactive (avoids nested dialog focus issues).
  const handleAddFromClone = () => {
    handleCloseCloneDialog();
    // small delay to let the dialog close animation finish
    setTimeout(() => {
      handleAdd();
    }, 160);
  };

 

  const handleConfirmClone = async () => {
    const id = selectedInstrumentForClone;
    if (!id) {
      toast.error("Please select an instrument to clone configs into.");
      return;
    }
    setCloning(true);
    try {
      const cloned = await cloneConfigsToInstrument(id);
      if (cloned && Array.isArray(cloned)) {
        toast.success(`Cloned ${cloned.length} configurations`);
      } else {
        toast.success("Configurations cloned");
      }
      await loadInstruments();
      handleCloseCloneDialog();
    } catch (err) {
      console.error("Failed to clone configs:", err);
      toast.error("Failed to clone configurations");
    } finally {
      setCloning(false);
    }
  };

  const handleSubmitCreate = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!name.trim()) {
      toast.error("Instrument name is required");
      return;
    }

    setSaving(true);

    try {
      // Build payload matching API example: uppercase status, cloneFromInstrumentId, configurationIds, reagents
      // Map UI labels to backend enum values. Server expects enums like: ERROR, READY, MAINTENANCE, INACTIVE, PROCESSING
      // We provide an explicit mapping for known UI labels; fall back to uppercased value for others.
      const statusMap: Record<string, string> = {
        Ready: "READY",
        Maintenance: "MAINTENANCE",
        Offline: "INACTIVE", // backend uses INACTIVE for offline
        Calibrating: "PROCESSING",
      };

      const normalizedStatus = statusMap[(status || "").toString()] || (status || "").toString().toUpperCase();

      const payload: any = {
        // include id when editing so saveInstrument will perform PUT
  ...(editingInstrumentId !== null ? { id: editingInstrumentId } : {}),
        name: name.trim(),
        serialNumber: serialNumber.trim() || undefined,
        model: model.trim() || undefined,
        status: normalizedStatus,
        cloneFromInstrumentId: cloneFrom && cloneFrom !== 'none' ? cloneFrom : undefined,
        configurationIds: selectedConfigIds ? selectedConfigIds.filter(Boolean) : [],
        reagents: reagents.map(r => ({ reagentId: r.reagentId, quantity: Number(r.quantity) }))
      };

      // Useful debug log for request/response troubleshooting (remove in production if sensitive)
      console.debug("Saving instrument payload:", payload);

  // call service - allow service to accept the payload (cast to any to avoid strict type mismatch)
  await saveInstrument(payload as any);
  toast.success(editingInstrumentId !== null ? "Instrument updated successfully!" : "Instrument created successfully!");
  setIsCreateModalOpen(false);
  setEditingInstrumentId(null);
  await loadInstruments();
    } catch (error) {
      console.error("Failed to create instrument:", error);
      toast.error(
        error instanceof Error ? error.message : "Failed to create instrument"
      );
    } finally {
      setSaving(false);
    }
  };

  

  

  // Search/filter
  const filtered = useMemo(() => {
    if (!query.trim()) return instruments;
    const q = query.trim().toLowerCase();
    return instruments.filter(
      (it) =>
        (it.name || "").toLowerCase().includes(q) ||
        (it.model || "").toLowerCase().includes(q) ||
        (it.serialNumber || "").toLowerCase().includes(q)
    );
  }, [instruments, query]);

  const handleChangePage = (_: any, newPage: number) => setPage(newPage);
  const handleChangeRowsPerPage = (
    e: React.ChangeEvent<HTMLInputElement | HTMLTextAreaElement>
  ) => {
    setRowsPerPage(parseInt(e.target.value, 10));
    setPage(0);
  };

  const visible = filtered.slice(
    page * rowsPerPage,
    page * rowsPerPage + rowsPerPage
  );

  // Don't block UI - show error as banner instead

  return (
    <Fade in>
      <Box>
        {/* Error Banner */}
        {error && (
          <Alert
            severity="error"
            sx={{ mb: 3, borderRadius: 2, boxShadow: 2 }}
            action={
              <Button color="inherit" size="small" onClick={loadInstruments}>
                Retry
              </Button>
            }
          >
            Failed to load instruments. Check console for details.
          </Alert>
        )}

          {/* Header Card */}
          <Paper 
            elevation={0}
            sx={{ 
              p: 3, 
              mb: 2, 
              borderRadius: 2,
              background: '#ffffff',
              border: '1px solid #e0e0e0'
            }}
          >
            <Box
              sx={{
                display: "flex",
                alignItems: "center",
                justifyContent: "space-between",
                flexWrap: 'wrap',
                gap: 2
              }}
            >
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                <Box
                  sx={{
                    width: 48,
                    height: 48,
                    borderRadius: 2,
                    background: '#1976d2',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                  }}
                >
                  <Box component="img" src={machineIcon} alt="instrument" sx={{ width: 28, height: 28 }} />
                </Box>
                <Box>
                  <Typography 
                    variant="h5" 
                    sx={{ 
                      fontWeight: 700,
                      color: '#1976d2',
                      mb: 0.5
                    }}
                  >
                    Laboratory Instruments
                  </Typography>
                  <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                    <Typography color="text.secondary" sx={{ fontWeight: 500 }}>
                      Manage and monitor all laboratory instruments
                    </Typography>
                    <Chip 
                      label={`${instruments.length} Total`}
                      size="small"
                      sx={{ 
                        fontWeight: 600,
                        background: '#1976d2',
                        color: 'white'
                      }}
                    />
                  </Box>
                </Box>
              </Box>

              <Box sx={{ display: "flex", gap: 2 }}>
                <Button
                  variant="outlined"
                  startIcon={<RefreshIcon />}
                  onClick={loadInstruments}
                  disabled={loading}
                  sx={{
                    borderRadius: 1,
                    fontWeight: 600,
                  }}
                >
                  Refresh
                </Button>
                <Button 
                  variant="contained" 
                  startIcon={<AddIcon />} 
                  onClick={handleAdd}
                  sx={{
                    borderRadius: 1,
                    fontWeight: 600,
                    px: 3,
                  }}
                >
                  Add Instrument
                </Button>
                
              </Box>
            </Box>
          </Paper>

          {/* Search Bar */}
          <Paper 
            elevation={0}
            sx={{ 
              p: 2, 
              mb: 2, 
              borderRadius: 2,
              background: 'white',
              border: '1px solid #e0e0e0'
            }}
          >
            <Box sx={{ display: "flex", gap: 2, alignItems: "center" }}>
              <SearchIcon sx={{ color: 'text.secondary' }} />
                <TextField
                size="small"
                placeholder="Search by name, model or serial number..."
                value={query}
                onChange={(e) => setQuery(e.target.value)}
                sx={{ 
                  flex: 1,
                  '& .MuiOutlinedInput-root': {
                    borderRadius: 1,
                    '&:hover fieldset': {
                      borderColor: '#1976d2',
                    },
                    '&.Mui-focused fieldset': {
                      borderColor: '#1976d2',
                      borderWidth: 1
                    }
                  }
                }}
              />
              {query && (
                <Chip 
                  label={`${filtered.length} results`}
                  color="primary"
                  size="small"
                  sx={{ fontWeight: 600 }}
                />
              )}
            </Box>
          </Paper>

          {/* Grid of cards */}
          <Paper
            elevation={0}
            sx={{
              borderRadius: 2,
              overflow: "hidden",
              background: 'white',
              border: '1px solid #e0e0e0'
            }}
          >
            {loading ? (
              <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr', md: 'repeat(3, 1fr)', lg: 'repeat(3, 1fr)' }, gap: 2, p: 2 }}>
                {[...Array(rowsPerPage)].map((_, i) => (
                  <Box key={i}>
                    <Card
                      variant="outlined"
                      sx={{
                        transition: 'transform .18s ease, box-shadow .18s ease',
                        '&:hover': {
                          transform: 'translateY(-6px)',
                          boxShadow: '0 12px 30px rgba(2,6,23,0.12)',
                        },
                        cursor: 'pointer',
                      }}
                    >
                      <CardContent>
                        <Stack direction="row" spacing={2} alignItems="center">
                          <Skeleton variant="circular" width={48} height={48} />
                          <Box sx={{ flex: 1 }}>
                            <Skeleton width="60%" />
                            <Skeleton width="40%" />
                          </Box>
                        </Stack>
                      </CardContent>
                    </Card>
                  </Box>
                ))}
              </Box>
            ) : visible.length === 0 ? (
              <Box sx={{ py: 8 }}>
                <Box
                  sx={{
                    display: 'flex',
                    flexDirection: 'column',
                    alignItems: 'center',
                    gap: 2
                  }}
                >
                  <Box
                    sx={{
                      width: 80,
                      height: 80,
                      borderRadius: '50%',
                      background: 'linear-gradient(135deg, #a8c0ff 0%, #c8b6ff 100%)',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'center',
                      opacity: 0.8
                    }}
                  >
                    <ScienceIcon sx={{ fontSize: 48, color: 'white' }} />
                  </Box>
                  <Typography variant="h6" color="text.secondary" fontWeight={600}>
                    No instruments found
                  </Typography>
                  <Typography color="text.secondary">
                    Add a new instrument or adjust filters to see results.
                  </Typography>
                </Box>
              </Box>
            ) : (
              <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr', md: 'repeat(3, 1fr)', lg: 'repeat(3, 1fr)' }, gap: 2, p: 2 }}>
                {visible.map((inst) => (
                  <Box key={inst.id}>
                    <Card
                      sx={{
                        height: '100%',
                        display: 'flex',
                        flexDirection: 'column',
                        transition: 'transform .18s ease, box-shadow .18s ease',
                        '&:hover': {
                          transform: 'translateY(-6px)',
                          boxShadow: '0 12px 30px rgba(2,6,23,0.12)',
                        },
                        cursor: 'pointer',
                      }}
                    >
                      <CardContent sx={{ flex: 1 }}>
                        <Stack direction="row" spacing={2} alignItems="center">
                          <Avatar sx={{ bgcolor: 'transparent', width: 44, height: 44, p: 0 }}>
                            <Box component="img" src={machineIcon} alt="instrument" sx={{ width: 36, height: 36 }} />
                          </Avatar>
                          <Box sx={{ flex: 1 }}>
                            <Typography fontWeight={700} color="primary.main">{inst.name}</Typography>
                            <Typography variant="body2" color="text.secondary">{inst.model || '—'}</Typography>
                          </Box>
                        </Stack>

                        <Box sx={{ mt: 2, display: 'flex', gap: 1, alignItems: 'center', justifyContent: 'space-between' }}>
                          <Typography variant="body2" sx={{ fontFamily: 'monospace', bgcolor: 'grey.100', px: 1, py: 0.4, borderRadius: 1 }}>
                            {inst.serialNumber || '—'}
                          </Typography>
                          <Box>
                            {/* status chip */}
                            {(() => {
                              const raw = String(inst.status || "");
                              const s = raw.trim().toLowerCase();
                              const label = raw ? raw.toUpperCase() : "—";
                              const baseSx = {
                                fontWeight: 700,
                                textTransform: 'uppercase' as const,
                                px: 1.2,
                                py: 0.4,
                                borderRadius: '999px',
                                fontSize: '0.75rem',
                              };

                        if (s === "ready") {
                          return (
                            <StatusChip
                              size="small"
                              label={label}
                              sx={{
                                ...baseSx,
                                background:
                                  "linear-gradient(135deg, #e6faea 0%, #4caf50 100%)",
                                color: "#0b3d12",
                                border: "1px solid rgba(76,175,80,0.12)",
                              }}
                            />
                          );
                        }

                        if (s === "maintenance") {
                          return (
                            <StatusChip
                              size="small"
                              label={label}
                              sx={{
                                ...baseSx,
                                background:
                                  "linear-gradient(135deg, #fbc2eb 0%, #a6c1ee 100%)",
                                color: "white",
                              }}
                            />
                          );
                        }

                        if (s === "offline") {
                          return (
                            <StatusChip
                              size="small"
                              label={label}
                              sx={{
                                ...baseSx,
                                background:
                                  "linear-gradient(135deg, #f0f4f8 0%, #c2e9fb 100%)",
                                color: "#213547",
                              }}
                            />
                          );
                        }

                              return <Chip size="small" label={label} sx={{ ...baseSx, background: 'rgba(0,0,0,0.04)' }} />;
                            })()}
                          </Box>
                        </Box>

                        <Box sx={{ mt: 1 }}>
                          <Typography variant="caption" color="text.secondary">
                            {inst.createdAt ? new Date(inst.createdAt).toLocaleDateString() : '—'}
                          </Typography>
                        </Box>
                      </CardContent>

                      <CardActions sx={{ justifyContent: 'flex-end' }}>
                        <Tooltip title="View Details" arrow>
                          <IconButton size="small" sx={{ color: '#a8c0ff' }} onClick={() => setInstrumentDetail(inst)}>
                            <VisibilityIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                        
                        
                        <Tooltip title="Change Status" arrow>
                          <IconButton size="small" onClick={() => {
                            // normalize id from common field names (id, _id, instrumentId)
                            const possibleId = (inst as any).id ?? (inst as any)._id ?? (inst as any).instrumentId ?? undefined;
                            const normalizedInst = { ...inst, id: possibleId } as Instrument & { id?: string | number };
                            setStatusTargetInstrument(normalizedInst as Instrument);

                            // map raw status to UI label when opening
                            const reverseStatusMap: Record<string, string> = {
                              READY: "Ready",
                              MAINTENANCE: "Maintenance",
                              INACTIVE: "Offline",
                              PROCESSING: "Calibrating",
                              ERROR: "Error",
                            };
                            const raw = String(inst.status || "");
                            setStatusNewValue(reverseStatusMap[raw.toUpperCase()] || (raw || "Ready"));
                            setStatusReason("");
                            setIsStatusDialogOpen(true);
                          }} sx={{ color: '#a8a8a8' }}>
                            <DescriptionIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                      </CardActions>
                    </Card>
                  </Box>
                ))}
              </Box>
            )}

            <Box sx={{ px: 2, py: 1 }}>
              <TablePagination
                component="div"
                count={filtered.length}
                page={page}
                onPageChange={handleChangePage}
                rowsPerPage={rowsPerPage}
                onRowsPerPageChange={handleChangeRowsPerPage}
                rowsPerPageOptions={[5, 8, 10, 20]}
                sx={{
                  borderTop: '2px solid',
                  borderColor: 'divider',
                  '& .MuiTablePagination-select': {
                    fontWeight: 600
                  }
                }}
              />
            </Box>
          </Paper>

          {/* Delete action removed; confirmation dialog omitted */}

        {/* Clone Configurations Dialog */}
        <Dialog
          open={isCloneDialogOpen}
          onClose={handleCloseCloneDialog}
          PaperProps={{
            sx: {
              borderRadius: 3,
              boxShadow: "0 8px 32px rgba(0,0,0,0.12)",
            },
          }}
        >
          <DialogTitle>Clone Configurations</DialogTitle>
          <DialogContent>
            {instruments.length === 0 ? (
              <Box
                sx={{
                  display: "flex",
                  flexDirection: "column",
                  gap: 2,
                  minWidth: 320,
                }}
              >
                <Alert severity="info">
                  No instruments available. Create an instrument first to clone
                  configurations into.
                </Alert>
                <Box
                  sx={{ display: "flex", justifyContent: "flex-end", gap: 1 }}
                >
                  <Button variant="outlined" onClick={handleAddFromClone}>
                    Add Instrument
                  </Button>
                </Box>
              </Box>
            ) : (
              <>
                <FormControl fullWidth size="small">
                  <InputLabel id="clone-select-label">
                    Select Instrument
                  </InputLabel>
                  <Select
                    labelId="clone-select-label"
                    value={selectedInstrumentForClone}
                    label="Select Instrument"
                    onChange={(e) =>
                      setSelectedInstrumentForClone(e.target.value as string)
                    }
                  >
                    {instruments.map((it) => (
                      <MenuItem value={String(it.id)} key={it.id}>
                        {it.name}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
                <DialogContentText sx={{ mt: 2 }}>
                  Select an instrument to copy global configurations into.
                </DialogContentText>
              </>
            )}
          </DialogContent>
          <DialogActions sx={{ px: 3, pb: 3 }}>
            <Button onClick={handleCloseCloneDialog} variant="outlined">
              Cancel
            </Button>
            <Button
              onClick={handleConfirmClone}
              variant="contained"
              disabled={
                instruments.length === 0 ||
                !selectedInstrumentForClone ||
                cloning
              }
            >
              {cloning ? "Cloning..." : "Clone"}
            </Button>
          </DialogActions>
        </Dialog>

            

          {/* Change Status Dialog */}
          <Dialog
            open={isStatusDialogOpen}
            onClose={handleCloseStatusDialog}
            maxWidth="sm"
            fullWidth
            PaperProps={{ sx: { borderRadius: 3 } }}
          >
            <DialogTitle>Change Instrument Status</DialogTitle>
            <DialogContent>
              <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                {statusTargetInstrument ? `Instrument: ${statusTargetInstrument.name}` : ''}
              </Typography>
              <FormControl fullWidth size="small" sx={{ mb: 2 }}>
                <InputLabel id="status-change-label">New Status</InputLabel>
                <Select
                  labelId="status-change-label"
                  label="New Status"
                  value={statusNewValue}
                  onChange={(e) => setStatusNewValue(e.target.value as string)}
                >
                  <MenuItem value="Ready">Ready</MenuItem>
                  <MenuItem value="Maintenance">Maintenance</MenuItem>
                  <MenuItem value="Inactive">Inactive</MenuItem>
                  <MenuItem value="Processing">Processing</MenuItem>
                  <MenuItem value="Error">Error</MenuItem>
                </Select>
              </FormControl>

              <TextField
                label="Reason (optional)"
                placeholder="Provide a reason for the status change"
                fullWidth
                size="small"
                value={statusReason}
                onChange={(e) => setStatusReason(e.target.value)}
                multiline
                rows={3}
              />
            </DialogContent>
            <DialogActions sx={{ px: 3, pb: 3 }}>
              <Button onClick={handleCloseStatusDialog} variant="outlined">Cancel</Button>
              <Button onClick={handleSubmitStatusChange} variant="contained" disabled={statusUpdating}>
                {statusUpdating ? 'Updating...' : 'Update Status'}
              </Button>
            </DialogActions>
          </Dialog>

          {/* Instrument Detail Dialog */}
          <Dialog
            open={!!instrumentDetail}
            onClose={() => setInstrumentDetail(null)}
            maxWidth="md"
            fullWidth
            PaperProps={{ sx: { borderRadius: 3 } }}
          >
            <DialogTitle>Instrument Details</DialogTitle>
            <DialogContent dividers>
              {instrumentDetail ? (
                <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: '1fr 1fr' }, gap: 2 }}>
                  <Box>
                    <Typography variant="h6" sx={{ fontWeight: 700 }}>{instrumentDetail.name}</Typography>
                    <Typography color="text.secondary">{instrumentDetail.model || '-'}</Typography>
                    <Stack direction="row" spacing={1} sx={{ mt: 1 }}>
                      <Chip label={instrumentDetail.serialNumber || '-'} />
                      <Chip label={instrumentDetail.status || '-'} color={instrumentDetail.status === 'MAINTENANCE' ? 'warning' : 'default'} />
                    </Stack>

                    <Box sx={{ mt: 2 }}>
                      <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>Location</Typography>
                      <Typography>{(instrumentDetail as any).location || '-'}</Typography>
                    </Box>

                    <Box sx={{ mt: 1 }}>
                      <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>Config Version</Typography>
                      <Typography>{(instrumentDetail as any).configVersion || '-'}</Typography>
                    </Box>

                    <Box sx={{ mt: 1 }}>
                      <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>Instrument Code</Typography>
                      <Typography>{(instrumentDetail as any).instrumentCode || '-'}</Typography>
                    </Box>
                  </Box>

                  <Box>
                    <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>Timestamps</Typography>
                    <Typography>Created: {instrumentDetail.createdAt ? new Date(instrumentDetail.createdAt).toLocaleString() : '-'}</Typography>
                    <Typography>Updated: {instrumentDetail.updatedAt ? new Date(instrumentDetail.updatedAt).toLocaleString() : '-'}</Typography>

                    <Box sx={{ mt: 2 }}>
                      <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>Configurations</Typography>
                      {(instrumentDetail as any).configurations && (instrumentDetail as any).configurations.length > 0 ? (
                        <Box>
                          {(instrumentDetail as any).configurations.map((c: any) => (
                            <Box key={c.configId} sx={{ mb: 1 }}>
                              <Typography sx={{ fontWeight: 600 }}>{c.configName}</Typography>
                              <Typography variant="body2" color="text.secondary">{c.configKey}: {c.configValue}</Typography>
                            </Box>
                          ))}
                        </Box>
                      ) : (
                        <Typography color="text.secondary">No configurations</Typography>
                      )}
                    </Box>

                    <Box sx={{ mt: 2 }}>
                      <Typography variant="subtitle2" sx={{ fontWeight: 600 }}>Assigned Reagents</Typography>
                      {(instrumentDetail as any).reagent && (instrumentDetail as any).reagent.length > 0 ? (
                        <Box>
                          {(instrumentDetail as any).reagent.map((r: any, idx: number) => {
                            const rid = r.id?.reagentId || r.reagentId;
                            const found = reagentOptions.find(ro => String(ro.reagentId) === String(rid));
                            const label = found ? found.name : 'Unknown reagent';
                            return (
                              <Typography key={idx} variant="body2">{label} — Qty: {r.quantity}</Typography>
                            );
                          })}
                        </Box>
                      ) : (
                        <Typography color="text.secondary">No reagents assigned</Typography>
                      )}
                    </Box>
                  </Box>
                </Box>
              ) : (
                <Typography color="text.secondary">No instrument selected.</Typography>
              )}
            </DialogContent>
            <DialogActions>
              <Button onClick={() => setInstrumentDetail(null)} variant="outlined">Close</Button>
            </DialogActions>
          </Dialog>

          

          {/* Create Instrument Modal */}
          <Dialog 
            open={isCreateModalOpen} 
            onClose={handleCancelCreate}
            maxWidth="md"
            fullWidth
            PaperProps={{
              sx: {
                borderRadius: 3,
                boxShadow: '0 8px 32px rgba(0,0,0,0.12)',
                maxHeight: '85vh'
              }
            }}
          >
            <DialogTitle sx={{ pb: 2, borderBottom: '2px solid', borderColor: 'divider' }}>
              <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                <Box
                  sx={{
                    width: 48,
                    height: 48,
                    borderRadius: 2,
                    background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center'
                  }}
                >
                  <ScienceIcon sx={{ color: 'white' }} />
                </Box>
                <Box>
                  <Typography variant="h6" sx={{ fontWeight: 700 }}>
                    {editingInstrumentId !== null ? 'Edit Instrument' : 'Add New Instrument'}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    {editingInstrumentId !== null ? 'Update instrument details' : 'Create a new instrument record'}
                  </Typography>
                  {editingInstrumentId !== null && (
                    <Typography variant="caption" color="text.secondary" sx={{ mt: 0.5 }}>
                      Editing id: {editingInstrumentId}
                    </Typography>
                  )}
                </Box>
              </Box>
            </DialogTitle>
          <DialogContent dividers sx={{ overflowY: 'auto', maxHeight: '65vh' }}>
            <form id="create-instrument-form" onSubmit={handleSubmitCreate}>
              {/* ===== BASIC INFO ===== */}
              <Box sx={{ mb: 4 }}>
                <Typography variant="h6" sx={{ fontWeight: 600, mb: 2 }}>
                  Instrument Information
                </Typography>
                <Box
                  sx={{
                    display: "grid",
                    gridTemplateColumns: { xs: "1fr", md: "1fr 1fr" },
                    gap: 2,
                  }}
                >
                  <TextField
                    label="Instrument Name"
                    placeholder="e.g., Microscope X200"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    fullWidth
                    required
                    disabled={saving}
                    size="small"
                  />
                  <TextField
                    label="Model"
                    placeholder="e.g., X200"
                    value={model}
                    onChange={(e) => setModel(e.target.value)}
                    fullWidth
                    disabled={saving}
                    size="small"
                  />
                  <TextField
                    label="Serial Number"
                    placeholder="e.g., SN123456"
                    value={serialNumber}
                    onChange={(e) => setSerialNumber(e.target.value)}
                    fullWidth
                    disabled={saving}
                    size="small"
                  />
                  <FormControl fullWidth disabled={saving} size="small">
                    <InputLabel id="status-label">Initial Status</InputLabel>
                    <Select
                      labelId="status-label"
                      label="Initial Status"
                      value={status}
                      onChange={(e) => setStatus(e.target.value as string)}
                    >
                      <MenuItem value="Ready">Ready</MenuItem>
                      <MenuItem value="Maintenance">Maintenance</MenuItem>
                      <MenuItem value="Offline">Offline</MenuItem>
                    </Select>
                  </FormControl>
                </Box>
              </Box>

              <Divider sx={{ my: 3 }} />

              {/* ===== CLONE SECTION ===== */}
              <Box sx={{ mb: 3 }}>
                <Typography variant="h6" sx={{ fontWeight: 600, mb: 2 }}>
                  Clone Existing Instrument
                </Typography>
                <FormControl fullWidth size="small">
                  <InputLabel id="clone-label">Select Instrument</InputLabel>
                  <Select
                    labelId="clone-label"
                    value={cloneFrom}
                    label="Select Instrument"
                    onChange={(e) => setCloneFrom(e.target.value as string)}
                  >
                    <MenuItem value="none">None – Start Fresh</MenuItem>
                    {instruments.map((it) => (
                      <MenuItem key={it.id} value={String(it.id)}>
                        {it.name}
                      </MenuItem>
                    ))}
                  </Select>
                </FormControl>
                <Typography
                  sx={{ color: "text.secondary", fontSize: "0.875rem", mt: 1 }}
                >
                  Choose an instrument to duplicate its reagents &
                  configurations.
                </Typography>
              </Box>

              <Divider sx={{ my: 3 }} />

              {/* ===== OPTIONAL ===== */}
              <Box
                sx={{ mb: 2, display: "flex", flexDirection: "column", gap: 2 }}
              >
                <Box
                  sx={{
                    p: 2,
                    borderRadius: 2,
                    border: "1px solid",
                    borderColor: "grey.200",
                    bgcolor: "grey.50",
                  }}
                >
                  <Box
                    sx={{
                      display: "flex",
                      alignItems: "center",
                      justifyContent: "space-between",
                      mb: reagents.length > 0 ? 2 : 0,
                    }}
                  >
                    <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>
                      Reagents (Optional)
                    </Typography>
                    <Button
                      variant="contained"
                      color="success"
                      startIcon={<AddIcon />}
                      size="small"
                      onClick={handleAddReagent}
                    >
                      Add Reagent
                    </Button>
                  </Box>
                  {reagents.map((reagent) => (
                    <Box
                      key={reagent.id}
                      sx={{
                        display: "flex",
                        alignItems: "center",
                        gap: 1,
                        mb: 1,
                        p: 1.5,
                        bgcolor: "white",
                        borderRadius: 1,
                        border: "1px solid",
                        borderColor: "grey.300",
                      }}
                    >
                      <FormControl size="small" sx={{ flex: 2 }}>
                        <InputLabel id={`reagent-select-label-${reagent.id}`}>
                          Reagent
                        </InputLabel>
                        <Select
                          labelId={`reagent-select-label-${reagent.id}`}
                          value={reagent.reagentId}
                          label="Reagent"
                          onChange={(e) => {
                            const val = String(e.target.value || "");
                            setReagents((prev) =>
                              prev.map((r) =>
                                r.id === reagent.id
                                  ? { ...r, reagentId: val }
                                  : r
                              )
                            );
                          }}
                        >
                          <MenuItem value="">-- Select reagent --</MenuItem>
                          {reagentOptions.map((opt) => (
                            <MenuItem key={opt.reagentId} value={opt.reagentId}>
                              {opt.name}
                            </MenuItem>
                          ))}
                        </Select>
                      </FormControl>
                      <TextField
                        size="small"
                        label="Quantity"
                        type="number"
                        value={reagent.quantity}
                        onChange={(e) => {
                          setReagents((prev) =>
                            prev.map((r) =>
                              r.id === reagent.id
                                ? { ...r, quantity: Number(e.target.value) }
                                : r
                            )
                          );
                        }}
                        sx={{ flex: 1 }}
                        inputProps={{ min: 1 }}
                      />
                      <IconButton
                        size="small"
                        color="error"
                        onClick={() => handleRemoveReagent(reagent.id)}
                      >
                        <DeleteIcon />
                      </IconButton>
                    </Box>
                  ))}
                </Box>

                <Box
                  sx={{
                    p: 2,
                    borderRadius: 2,
                    border: "1px solid",
                    borderColor: "grey.200",
                    bgcolor: "grey.50",
                  }}
                >
                  <Typography
                    variant="subtitle1"
                    sx={{ fontWeight: 600, mb: 2 }}
                  >
                    Configurations
                  </Typography>

                  <FormControl fullWidth size="small" sx={{ mb: 2 }}>
                    <InputLabel>Configuration Mode</InputLabel>
                    <Select
                      value={configurationMode}
                      label="Configuration Mode"
                      onChange={(e) =>
                        setConfigurationMode(
                          e.target.value as "default" | "clone" | "select"
                        )
                      }
                    >
                      <MenuItem value="default">
                        Use Default Global Configurations
                      </MenuItem>
                      <MenuItem value="clone">
                        Clone from Another Instrument
                      </MenuItem>
                      <MenuItem value="select">
                        Select Specific Configuration Templates
                      </MenuItem>
                    </Select>
                  </FormControl>

                  {configurationMode === "clone" && (
                    <Alert severity="info" sx={{ mb: 2 }}>
                      Configurations will be cloned from the instrument selected
                      in "Clone From" field above.
                    </Alert>
                  )}

                  {configurationMode === "select" && (
                    <Box>
                      <Box
                        sx={{
                          display: "flex",
                          alignItems: "center",
                          justifyContent: "space-between",
                          mb: 1,
                        }}
                      >
                        <Typography variant="body2" color="text.secondary">
                          Select configuration template IDs
                        </Typography>
                        <Button
                          variant="outlined"
                          size="small"
                          startIcon={<AddIcon />}
                          onClick={handleAddConfigId}
                        >
                          Add Config ID
                        </Button>
                      </Box>
                      {selectedConfigIds.map((configId, index) => (
                        <Box
                          key={index}
                          sx={{
                            display: "flex",
                            alignItems: "center",
                            gap: 1,
                            mb: 1,
                            p: 1.5,
                            bgcolor: "white",
                            borderRadius: 1,
                            border: "1px solid",
                            borderColor: "grey.300",
                          }}
                        >
                          <TextField
                            select
                            size="small"
                            label="Configuration ID"
                            value={configId}
                            onChange={(e) => {
                              const val = String(e.target.value || "");
                              setSelectedConfigIds(prev => prev.map((id, i) => i === index ? val : id));
                            }}
                            sx={{ flex: 1 }}
                            placeholder="Select configuration template ID"
                          >
                            <MenuItem value="">-- Select template --</MenuItem>
                            {templateOptions.map((opt: any) => (
                              <MenuItem key={opt.configId || opt.id} value={opt.configId || opt.id}>
                                {opt.configName || opt.configKey || (opt.configId || opt.id)}
                              </MenuItem>
                            ))}
                          </TextField>
                          <IconButton
                            size="small"
                            color="error"
                            onClick={() => handleRemoveConfigId(index)}
                          >
                            <DeleteIcon />
                          </IconButton>
                        </Box>
                      ))}
                    </Box>
                  )}

                  {configurationMode === "default" && (
                    <Alert severity="info">
                      All global default configurations will be automatically
                      assigned to this instrument.
                    </Alert>
                  )}
                </Box>
              </Box>
            </form>
          </DialogContent>
          <DialogActions sx={{ px: 3, py: 2.5, borderTop: '2px solid', borderColor: 'divider', position: 'sticky', bottom: 0, background: 'white', zIndex: 10 }}>
            <Button
              variant="outlined"
              onClick={handleCancelCreate}
              disabled={saving}
              sx={{
                borderRadius: 2,
                fontWeight: 600,
                borderWidth: 2,
                "&:hover": { borderWidth: 2 },
              }}
            >
              Cancel
            </Button>
            <Button
              type="submit"
              form="create-instrument-form"
              variant="contained"
              disabled={saving}
              sx={{
                borderRadius: 2,
                fontWeight: 600,
                px: 3,
                background: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)",
                "&:hover": {
                  background:
                    "linear-gradient(135deg, #764ba2 0%, #667eea 100%)",
                },
              }}
            >
              {saving ? (editingInstrumentId !== null ? "Updating..." : "Creating...") : (editingInstrumentId !== null ? "Update Instrument" : "Create Instrument")}
            </Button>
          </DialogActions>
        </Dialog>
      </Box>
    </Fade>
  );
}
