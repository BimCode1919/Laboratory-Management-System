import React, { useEffect, useState, useMemo } from "react";
import {
  getAllConfigurations,
  deleteConfiguration,
  createConfiguration,
  updateConfiguration,
} from "@/features/Warehouse/services/ConfigurationServices";
import type { ConfigurationDTO, CreateConfigurationRequest } from "@/features/Warehouse/types/configuration";
import { Box, Button, TextField, Card, CardContent, CardActions, Typography, Dialog, DialogTitle, DialogContent, DialogActions, Switch, FormControlLabel, IconButton, InputAdornment, Paper, Stack, Avatar, TablePagination, Skeleton, Alert } from "@mui/material";
import SearchIcon from "@mui/icons-material/Search";
import EditIcon from "@mui/icons-material/Edit";
import DeleteIcon from "@mui/icons-material/Delete";
import AddIcon from "@mui/icons-material/Add";
import SettingsSuggestIcon from '@mui/icons-material/SettingsSuggest';
import { toast } from "react-toastify";

const formInitial = {
  configName: "",
  configKey: "",
  configValue: "",
  defaultValue: "",
  description: "",
  global: true,
  instrumentId: "",
} as CreateConfigurationRequest;

const ConfigurationPage: React.FC = () => {
  const [configs, setConfigs] = useState<ConfigurationDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [actionLoading, setActionLoading] = useState(false);

  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [form, setForm] = useState<CreateConfigurationRequest>(formInitial);
  const [search, setSearch] = useState("");
  const [confirmDelete, setConfirmDelete] = useState<{ open: boolean; id?: string }>(
    { open: false }
  );
  const [page, setPage] = useState(0);
  const [rowsPerPage, setRowsPerPage] = useState(8);

  const load = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await getAllConfigurations();
      setConfigs(data ?? []);
    } catch (e) {
      setError("Failed to load configurations.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    let mounted = true;
    (async () => {
      await load();
      if (!mounted) return;
    })();
    return () => {
      mounted = false;
    };
  }, []);

  const refresh = async () => await load();

  const startCreate = () => {
    setEditingId(null);
    setForm(formInitial);
    setShowForm(true);
  };

  const startEdit = (cfg: ConfigurationDTO) => {
    const idVar = (cfg as any).id || (cfg as any).configId || (cfg as any).configurationId || null;
    setEditingId(idVar);
    setForm({
      configName: cfg.configName ?? "",
      configKey: cfg.configKey ?? "",
      configValue: cfg.configValue ?? "",
      defaultValue: cfg.defaultValue ?? "",
      description: cfg.description ?? "",
      // support both `global` and `isGlobal` from API responses
      global: !!(cfg as any).global || !!(cfg as any).isGlobal,
      instrumentId: cfg.instrumentId ?? "",
    });
    setShowForm(true);
  };

  const handleSave = async () => {
    if (!form.configName || !form.configKey) {
      toast.error("Name and key are required");
      return;
    }
    setActionLoading(true);
    try {
      // Ensure we don't send an invalid or empty instrumentId to the server
      const isValidUUID = (v?: string) => !!v && /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i.test(v);
      const payload: any = { ...form };
      // Backend expects `isGlobal` — map our `global` form field to it
      const isGlobal = !!form.global;
      payload.isGlobal = isGlobal;
      // If config is global we must not send instrumentId
      if (isGlobal) delete payload.instrumentId;
      else {
        // if present but not a valid UUID, omit it (backend will treat as not provided)
        if (!isValidUUID(payload.instrumentId)) delete payload.instrumentId;
      }
      // remove local-only form field
      delete payload.global;

      if (editingId) {
        await updateConfiguration(editingId, payload);
        toast.success("Configuration updated.");
      } else {
        await createConfiguration(payload);
        toast.success("Configuration created.");
      }
      setShowForm(false);
      await refresh();
    } catch (e) {
      console.error(e);
      toast.error("Save failed.");
    } finally {
      setActionLoading(false);
    }
  };

  const handleDelete = async (id?: string) => {
    if (!id) return;
    setActionLoading(true);
    try {
      const ok = await deleteConfiguration(String(id));
      if (ok) {
        toast.success("Configuration deleted.");
        await refresh();
      } else {
        toast.error("Failed to delete configuration.");
      }
    } catch (e) {
      toast.error("Delete failed.");
    } finally {
      setActionLoading(false);
      setConfirmDelete({ open: false });
    }
  };



  // Keep hooks order stable: don't return early based on `loading` or `error`.
  // We'll show skeletons when loading and an Alert when there's an error.

  const filtered = useMemo(() => {
    const q = search.trim().toLowerCase();
    if (!q) return configs;
    return configs.filter((c) => ((c.configName || "").toString().toLowerCase().includes(q)));
  }, [configs, search]);

  const paged = useMemo(() => {
    const start = page * rowsPerPage;
    return filtered.slice(start, start + rowsPerPage);
  }, [filtered, page, rowsPerPage]);

  // (template selector removed per design request)

  return (
    <div style={{ padding: 16 }}>
      <Paper elevation={0} sx={{ p: 3, mb: 2, borderRadius: 2, background: '#ffffff', border: '1px solid #e0e0e0' }}>
        <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', gap: 2, flexWrap: 'wrap' }}>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
            <Box sx={{ width: 48, height: 48, borderRadius: 2, background: '#1976d2', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
              <SettingsSuggestIcon sx={{ color: 'white' }} />
            </Box>
            <Box>
              <Typography variant="h5" sx={{ fontWeight: 700, color: '#1976d2', mb: 0.5 }}>Warehouse Configurations</Typography>
              <Typography color="text.secondary" sx={{ fontWeight: 500 }}>Manage global and instrument configurations</Typography>
            </Box>
          </Box>

          <Box sx={{ display: 'flex', gap: 2 }}>
            <TextField
              size="small"
                placeholder="Search by name"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <SearchIcon fontSize="small" />
                  </InputAdornment>
                ),
              }}
              sx={{ '& .MuiOutlinedInput-root': { '&:hover fieldset': { borderColor: '#1976d2' }, '&.Mui-focused fieldset': { borderColor: '#1976d2' } }, minWidth: 240 }}
            />
            <Button variant="outlined" onClick={refresh} disabled={loading || actionLoading}  sx={{ borderRadius: 1, fontWeight: 600 }}>Refresh</Button>
            <Button variant="contained" onClick={startCreate} startIcon={<AddIcon />} sx={{ borderRadius: 1, fontWeight: 600, px: 3 }}>Add</Button>
          </Box>
        </Box>
      </Paper>

      <Box display="flex" alignItems="center" marginTop={2}>
        <Box flex={1} textAlign="right">Showing {filtered.length} configuration(s).</Box>
      </Box>

      {/* configuration template selector removed - only search retained */}

      {error && (
        <Box mt={2}>
          <Alert severity="error">{error}</Alert>
        </Box>
      )}

      {/* Form */}
      <Dialog open={showForm} onClose={() => setShowForm(false)} fullWidth maxWidth="sm">
        <DialogTitle>{editingId ? "Edit configuration" : "Create configuration"}</DialogTitle>
        <DialogContent>
          <Box display="grid" gridTemplateColumns="1fr 1fr" gap={2} marginTop={1}>
            <TextField label="Name" value={form.configName} onChange={(e) => setForm({ ...form, configName: e.target.value })} fullWidth />
            <TextField label="Key" value={form.configKey} onChange={(e) => setForm({ ...form, configKey: e.target.value })} fullWidth />
            <TextField label="Value" value={form.configValue} onChange={(e) => setForm({ ...form, configValue: e.target.value })} fullWidth />
            <TextField label="Default value" value={form.defaultValue} onChange={(e) => setForm({ ...form, defaultValue: e.target.value })} fullWidth />
            <FormControlLabel control={<Switch checked={form.global} onChange={(e) => setForm({ ...form, global: e.target.checked })} />} label="Global" />
            <TextField label="Description" value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} fullWidth multiline rows={3} style={{ gridColumn: "1 / -1" }} />
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setShowForm(false)}>Cancel</Button>
          <Button onClick={handleSave} variant="contained" disabled={actionLoading}>{actionLoading ? "Saving..." : "Save"}</Button>
        </DialogActions>
      </Dialog>

      {/* Grid cards - force two cards per row using CSS grid */}
      <Paper elevation={0} sx={{ padding: 1, borderRadius: 2, background: 'white', border: '1px solid #e0e0e0' }}>
        <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', md: 'repeat(2, 1fr)', lg: 'repeat(3, 1fr)' }, gap: 2 }}>
          {loading && Array.from({ length: rowsPerPage }).map((_, i) => (
            <Box key={`skeleton-${i}`}>
              <Card variant="outlined">
                <CardContent>
                  <Stack direction="row" spacing={2} alignItems="center">
                    <Skeleton variant="circular" width={40} height={40} />
                    <Box sx={{ flex: 1 }}>
                      <Skeleton width="60%" />
                      <Skeleton width="40%" />
                    </Box>
                  </Stack>
                  <Skeleton sx={{ mt: 2 }} />
                  <Skeleton width="30%" sx={{ mt: 1 }} />
                </CardContent>
              </Card>
            </Box>
          ))}

          {!loading && paged.map((cfg, idx) => {
            const cfgId = (cfg as any).id || (cfg as any).configId || (cfg as any).configurationId || String(idx);
            return (
            <Box key={cfgId}>
              <Card variant="outlined" sx={{ height: "100%", display: "flex", flexDirection: "column" }}>
                <CardContent sx={{ flex: 1 }}>
                  <Stack direction="row" spacing={2} alignItems="flex-start">
                    <Avatar sx={{ bgcolor: 'transparent', width: 44, height: 44, p: 0, border: '1px solid rgba(0,0,0,0.04)' }}>
                      <SettingsSuggestIcon sx={{ color: '#1976d2' }} />
                    </Avatar>
                    <Box sx={{ flex: 1 }}>
                      <Box display="flex" justifyContent="space-between">
                        <Box>
                          <Typography variant="subtitle1" fontWeight={700} color="primary.main">{cfg.configName}</Typography>
                          <Typography variant="caption" color="text.secondary">{cfg.configKey}</Typography>
                        </Box>
                        <Box textAlign="right">
                          <Typography variant="subtitle2" fontWeight={700}>{cfg.configValue}</Typography>
                          <Typography variant="caption" color="text.secondary">{(cfg as any).global || (cfg as any).isGlobal ? "Global" : "Local"}</Typography>
                        </Box>
                      </Box>
                      <Box mt={1}>
                        <Typography variant="body2" color="text.primary">{cfg.description ?? "No description"}</Typography>
                      </Box>
                      <Box mt={2}>
                        <Typography variant="caption" color="text.secondary">Default: <strong>{cfg.defaultValue ?? "-"}</strong></Typography>
                      </Box>
                    </Box>
                  </Stack>
                </CardContent>
                <CardActions sx={{ justifyContent: 'flex-end' }}>
                  <IconButton size="small" onClick={() => startEdit(cfg)} aria-label="edit" sx={{ color: '#fbc2eb' }}><EditIcon fontSize="small" /></IconButton>
                  <IconButton size="small" onClick={() => setConfirmDelete({ open: true, id: cfgId })} aria-label="delete" sx={{ color: 'error.main' }}><DeleteIcon fontSize="small" /></IconButton>
                </CardActions>
              </Card>
            </Box>
            );
          })}
        </Box>
        <Box mt={2} display="flex" justifyContent="center">
          <TablePagination
            component="div"
            count={filtered.length}
            page={page}
            onPageChange={(_, p) => setPage(p)}
            rowsPerPage={rowsPerPage}
            onRowsPerPageChange={(e) => { setRowsPerPage(Number(e.target.value)); setPage(0); }}
            rowsPerPageOptions={[4, 8, 12, 24]}
            sx={{ borderTop: '2px solid', borderColor: 'divider', '& .MuiTablePagination-select': { fontWeight: 600 } }}
          />
        </Box>
      </Paper>

      <Dialog open={confirmDelete.open} onClose={() => setConfirmDelete({ open: false })}>
        <DialogTitle>Delete configuration</DialogTitle>
        <DialogContent>Are you sure you want to delete this configuration?</DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirmDelete({ open: false })}>Cancel</Button>
          <Button color="error" variant="contained" onClick={() => handleDelete(confirmDelete.id)} disabled={actionLoading}>Delete</Button>
        </DialogActions>
      </Dialog>
    </div>
  );
};

export default ConfigurationPage;
