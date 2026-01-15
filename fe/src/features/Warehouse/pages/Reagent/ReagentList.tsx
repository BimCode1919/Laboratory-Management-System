import { useEffect, useState, useMemo } from "react";
import {
    Button,
    Dialog,
    DialogContent,
    DialogTitle,
    TextField,
    FormControl,
    InputLabel,
    Select,
    MenuItem,
    CircularProgress,
    Box,
    Paper,
    TableContainer,
    Table,
    TableHead,
    TableRow,
    TableCell,
    TableBody,
    Avatar,
    Stack,
    Chip,
    Pagination,
    Typography,
    Tooltip,
    IconButton,
    DialogActions,
    Grid,
} from "@mui/material";
import ScienceIcon from "@mui/icons-material/Science";
import AddIcon from "@mui/icons-material/Add";
import RefreshIcon from "@mui/icons-material/Refresh";
import DeleteIcon from "@mui/icons-material/Delete";
import HistoryIcon from '@mui/icons-material/History';
import StorageIcon from '@mui/icons-material/Storage';
import VisibilityIcon from "@mui/icons-material/Visibility";
import Inventory2Icon from '@mui/icons-material/Inventory2';
import EditIcon from '@mui/icons-material/Edit';
import CloseIcon from "@mui/icons-material/Close";
import { toast } from "react-toastify";
import type { Reagent } from "../../types/Reagent";
import {
    getAllReagents,
    createReagent,
    deleteReagent,
    getUsageHistory,
    getInventory,
    importReagent,
    formatDate,
    formatDateTime,
} from "../../services/ReagentServices";
import VendorSelect from "../../components/VendorSelect";

export default function ReagentList() {
    const [reagents, setReagents] = useState<Reagent[]>([]);
    const [loading, setLoading] = useState(true);
    const [isAddOpen, setIsAddOpen] = useState(false);
    const [isEditMode, setIsEditMode] = useState(false);
    const [editId, setEditId] = useState<string | null>(null);
    const [deleteId, setDeleteId] = useState<string | null>(null);
    const [detailReagent, setDetailReagent] = useState<Reagent | null>(null);

    const [name, setName] = useState("");
    const [catalogNumber, setCatalogNumber] = useState("");
    const [manufacturer, setManufacturer] = useState("");
    const [casNumber, setCasNumber] = useState("");

    const [importReagentData, setImportReagentData] = useState<Reagent | null>(null);
    const [importQuantity, setImportQuantity] = useState("");
    const [importNote, setImportNote] = useState("");

    const [importVendorId, setImportVendorId] = useState("");
    const [importPONumber, setImportPONumber] = useState("");
    const [importOrderDate, setImportOrderDate] = useState("");
    const [importReceiptDate, setImportReceiptDate] = useState("");
    const [importUnit, setImportUnit] = useState("");
    const [importLot, setImportLot] = useState("");
    const [importExpire, setImportExpire] = useState("");
    const [importStorage, setImportStorage] = useState("");
    const [importStatus, setImportStatus] = useState("");
    // new UI states: per-field filters
    const [filterName, setFilterName] = useState("");
    const [filterCatalog, setFilterCatalog] = useState("");
    const [filterManufacturer, setFilterManufacturer] = useState("");
    const [filterCas, setFilterCas] = useState("");
    const [page, setPage] = useState(1);
    const rowsPerPage = 8;
    // usage history states
    const [usageHistory, setUsageHistory] = useState<any[]>([]);
    const [usageOpen, setUsageOpen] = useState(false);
    const [usageLoading, setUsageLoading] = useState(false);
    // inventory states
    const [inventoryList, setInventoryList] = useState<any[]>([]);
    const [inventoryOpen, setInventoryOpen] = useState(false);
    const [inventoryLoading, setInventoryLoading] = useState(false);
    const [inventoryFilter, setInventoryFilter] = useState("");
    const [inventoryDetail, setInventoryDetail] = useState<any | null>(null);

    const fetchData = async () => {
        try {
            setLoading(true);
            const data = await getAllReagents();
            if (data) {
                setReagents(data);
            } else {
                toast.error("Failed to load reagents");
                setReagents([]);
            }
        } catch (e) {
            console.error('Error details:', e);
            toast.error("Failed to load reagents");
            setReagents([]);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchData();
    }, []);

    const resetForm = () => {
        setName("");
        setCatalogNumber("");
        setManufacturer("");
        setCasNumber("");
        setIsEditMode(false);
        setEditId(null);
    };

    const handleSave = async () => {
        try {
            if (isEditMode && editId) {
                // update
                // import updateReagent lazily to avoid circular issues if any
                const { updateReagent } = await import("../../services/ReagentServices");
                await updateReagent(editId, {
                    name,
                    catalogNumber,
                    manufacturer,
                    casNumber,
                });
                toast.success("Reagent updated successfully");
            } else {
                await createReagent({
                    name,
                    catalogNumber,
                    manufacturer,
                    casNumber,
                });
                toast.success("Reagent created successfully");
            }

            setIsAddOpen(false);
            resetForm();
            fetchData();
        } catch (e) {
            toast.error(isEditMode ? "Failed to update reagent" : "Failed to create reagent");
        }
    };

    const handleDelete = async (id: string) => {
        try {
            await deleteReagent(id);
            toast.success("Reagent deleted successfully");
            setDeleteId(null);
            fetchData();
        } catch {
            toast.error("Failed to delete reagent");
        }
    };

    const handleImport = async () => {
        if (!importReagentData || !importQuantity || isNaN(Number(importQuantity)) || Number(importQuantity) <= 0) return;
        try {
            await importReagent({
                reagentId: importReagentData.reagentId,
                vendorID: importVendorId || null,
                poNumber: importPONumber || null,
                orderDate: importOrderDate || null,
                receiptDate: importReceiptDate || null,
                quantity: Number(importQuantity),
                unitOfMeasure: importUnit || null,
                lotNumber: importLot || null,
                expirationDate: importExpire || null,
                storageLocation: importStorage || null,
                status: importStatus || null,
                note: importNote || undefined
            });
            toast.success("Import successful!");
            setImportReagentData(null);
            setImportQuantity("");
            setImportNote("");
            fetchData();
        } catch {
            toast.error("Import failed");
        }
    };

    const fetchUsageHistory = async (reagentId: string) => {
        try {
            setUsageLoading(true);
            const data = await getUsageHistory(reagentId);
            if (data && Array.isArray(data)) {
                // Normalize different possible response shapes to what the UI expects
                const normalized = data.map((d: any) => ({
                    usageId: d.usageId || d.id || d.id,
                    reagentId: d.reagentId,
                    reagentName: d.reagentName || d.name || '',
                    quantity: d.quantityUsed ?? d.quantity ?? 0,
                    action: d.action || d.note || '',
                    usedBy: d.usedBy || d.user || '',
                    usedAt: d.usageDate || d.timestamp || d.usedAt || null,
                }));
                setUsageHistory(normalized);
            } else {
                toast.error("Failed to load usage history");
                setUsageHistory([]);
            }
            setUsageOpen(true);
        } catch (e) {
            toast.error("Failed to load usage history");
            setUsageHistory([]);
        } finally {
            setUsageLoading(false);
        }
    };

    const fetchInventory = async () => {
        setInventoryList([]);
        setInventoryOpen(true);
        try {
            setInventoryLoading(true);
            const data = await getInventory();
            if (data) {
                setInventoryList(data);
            } else {
                toast.error("Failed to load inventory");
                setInventoryList([]);
            }
        } catch (e) {
            toast.error("Failed to load inventory");
            setInventoryList([]);
        } finally {
            setInventoryLoading(false);
        }
    };

    // derived lists for search & pagination - uses per-field filters
    const filtered = useMemo(() => {
        // if no filters are set, return all
        const n = filterName.trim().toLowerCase();
        const c = filterCatalog.trim().toLowerCase();
        const m = filterManufacturer.trim().toLowerCase();
        const cas = filterCas.trim().toLowerCase();

    if (!n && !c && !m && !cas) return reagents;

        return reagents.filter(r => {
            const rn = (r.name || "").toLowerCase();
            const rc = (r.catalogNumber || "").toLowerCase();
            const rm = (r.manufacturer || "").toLowerCase();
            const rcas = (r.casNumber || "").toLowerCase();

            // per-field matching (all non-empty filters must match)
            if (n && !rn.includes(n)) return false;
            if (c && !rc.includes(c)) return false;
            if (m && !rm.includes(m)) return false;
            if (cas && !rcas.includes(cas)) return false;

            return true;
        });
    }, [reagents, filterName, filterCatalog, filterManufacturer, filterCas]);

    const totalPages = Math.max(1, Math.ceil(filtered.length / rowsPerPage));
    const pageItems = filtered.slice((page - 1) * rowsPerPage, page * rowsPerPage);

    return (
        <Box className="p-8 min-h-screen" sx={{ background: "linear-gradient(180deg,#f8fafc 0%,#ecfdf5 100%)" }}>
            <Paper elevation={3} sx={{ p: 3, borderRadius: 3, mb: 4 }}>
                <Stack direction="row" alignItems="center" justifyContent="space-between" spacing={2}>
                    <Stack direction="row" spacing={2} alignItems="center">
                        <Avatar sx={{
                            bgcolor: "transparent",
                            width: 64,
                            height: 64,
                            boxShadow: "0 6px 18px rgba(5,150,105,0.18)",
                            background: "linear-gradient(135deg,#059669 0%, #10b981 100%)"
                        }}>
                            <ScienceIcon sx={{ color: "white", fontSize: 32 }} />
                        </Avatar>
                        <div>
                            <Typography variant="h4" sx={{ fontWeight: 700, color: "text.primary" }}>Reagents</Typography>
                            <Typography variant="body2" sx={{ color: "text.secondary" }}>Manage and track chemical reagents in your warehouse</Typography>
                        </div>
                    </Stack>

                    <Stack direction="row" spacing={1} alignItems="center">
                        <Button
                            variant="outlined"
                            startIcon={<RefreshIcon />}
                            onClick={() => { fetchData(); }}
                            sx={{ borderColor: "#059669", color: "#059669" }}
                        >
                            Refresh
                        </Button>
                        <Button
                            variant="contained"
                            startIcon={<AddIcon />}
                            onClick={() => { resetForm(); setIsAddOpen(true); }}
                            sx={{ backgroundColor: "#059669", "&:hover": { backgroundColor: "#047857" }, borderRadius: 2 }}
                        >
                            Add Reagent
                        </Button>
                        <Button
                            variant="outlined"
                            startIcon={<StorageIcon />}
                            onClick={() => { fetchInventory(); }}
                            sx={{ borderColor: "#059669", color: "#059669", borderRadius: 2 }}
                        >
                            Inventory
                        </Button>
                    </Stack>
                </Stack>
            </Paper>

            {/* Filters panel - separate section */}
            <Paper elevation={1} sx={{ p: 2, borderRadius: 2, mb: 4 }}>
                <Stack direction={{ xs: 'column', md: 'row' }} spacing={2} alignItems="center">
                    <TextField
                        size="small"
                        placeholder="Name"
                        value={filterName}
                        onChange={e => { setFilterName(e.target.value); setPage(1); }}
                        InputProps={{ sx: { borderRadius: "8px", backgroundColor: "white" } }}
                        sx={{ minWidth: 180 }}
                    />
                    <TextField
                        size="small"
                        placeholder="Catalog No"
                        value={filterCatalog}
                        onChange={e => { setFilterCatalog(e.target.value); setPage(1); }}
                        InputProps={{ sx: { borderRadius: "8px", backgroundColor: "white" } }}
                        sx={{ minWidth: 160 }}
                    />
                    <TextField
                        size="small"
                        placeholder="Manufacturer"
                        value={filterManufacturer}
                        onChange={e => { setFilterManufacturer(e.target.value); setPage(1); }}
                        InputProps={{ sx: { borderRadius: "8px", backgroundColor: "white" } }}
                        sx={{ minWidth: 180 }}
                    />
                    <TextField
                        size="small"
                        placeholder="CAS"
                        value={filterCas}
                        onChange={e => { setFilterCas(e.target.value); setPage(1); }}
                        InputProps={{ sx: { borderRadius: "8px", backgroundColor: "white" } }}
                        sx={{ minWidth: 140 }}
                    />
                    <Button
                        variant="outlined"
                        startIcon={<RefreshIcon />}
                        onClick={() => { setFilterName(""); setFilterCatalog(""); setFilterManufacturer(""); setFilterCas(""); fetchData(); }}
                        sx={{ borderColor: "#059669", color: "#059669", ml: 'auto' }}
                    >
                        Reset
                    </Button>
                </Stack>
            </Paper>

            <Paper elevation={1} sx={{ borderRadius: 3, overflow: "hidden" }}>
                <TableContainer>
                    <Table size="small">
                        <TableHead>
                            <TableRow sx={{ backgroundColor: "#f3f4f6" }}>
                                <TableCell align="center" sx={{ fontWeight: 700 }}>#</TableCell>
                                <TableCell sx={{ fontWeight: 700 }}>Name</TableCell>
                                <TableCell sx={{ fontWeight: 700 }}>Catalog No</TableCell>
                                <TableCell sx={{ fontWeight: 700 }}>Manufacturer</TableCell>
                                <TableCell sx={{ fontWeight: 700 }}>CAS</TableCell>
                                <TableCell align="center" sx={{ fontWeight: 700 }}>Actions</TableCell>
                                <TableCell align="center" sx={{ fontWeight: 700 }}>Import</TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                            {loading ? (
                                <TableRow>
                                    <TableCell colSpan={7} align="center" sx={{ py: 6 }}>
                                        <Stack direction="row" alignItems="center" justifyContent="center" spacing={2}>
                                            <CircularProgress size={28} color="success" />
                                            <Typography color="text.secondary">Loading reagents…</Typography>
                                        </Stack>
                                    </TableCell>
                                </TableRow>
                            ) : pageItems.length === 0 ? (
                                <TableRow>
                                    <TableCell colSpan={7} align="center" sx={{ py: 6 }}>
                                        <Typography color="text.secondary">No reagents found.</Typography>
                                    </TableCell>
                                </TableRow>
                            ) : (
                                pageItems.map((r, i) => (
                                    <TableRow key={r.reagentId} hover>
                                        <TableCell align="center">{(page - 1) * rowsPerPage + i + 1}</TableCell>
                                        <TableCell>
                                            <Stack direction="row" spacing={2} alignItems="center">
                                                <Avatar sx={{ bgcolor: "#ecfdf5", color: "#059669", width: 36, height: 36 }}>
                                                    <ScienceIcon />
                                                </Avatar>
                                                <div>
                                                    <Typography sx={{ fontWeight: 600 }}>{r.name}</Typography>
                                                    <Stack direction="row" spacing={1} sx={{ mt: 0.5 }}>
                                                        {r.manufacturer && <Chip size="small" label={r.manufacturer} variant="outlined" />}
                                                        {r.quantity !== undefined && <Chip size="small" label={`Qty: ${r.quantity}`} color="success" />}
                                                    </Stack>
                                                </div>
                                            </Stack>
                                        </TableCell>
                                        <TableCell>{r.catalogNumber || "-"}</TableCell>
                                        <TableCell>{r.manufacturer || "-"}</TableCell>
                                        <TableCell>{r.casNumber || "-"}</TableCell>
                                        <TableCell align="center">
                                            <Stack direction="row" spacing={0.5} justifyContent="center">
                                                <Tooltip title="View details" arrow>
                                                    <IconButton color="primary" onClick={() => setDetailReagent(r)}>
                                                        <VisibilityIcon />
                                                    </IconButton>
                                                </Tooltip>
                                                <Tooltip title="Edit reagent" arrow>
                                                    <IconButton color="secondary" onClick={() => {
                                                        // open modal in edit mode and prefill
                                                        setIsEditMode(true);
                                                        setEditId(r.reagentId);
                                                        setName(r.name || "");
                                                        setCatalogNumber(r.catalogNumber || "");
                                                        setManufacturer(r.manufacturer || "");
                                                        setCasNumber(r.casNumber || "");
                                                        setIsAddOpen(true);
                                                    }}>
                                                        <EditIcon />
                                                    </IconButton>
                                                </Tooltip>
                                                <Tooltip title="Usage history" arrow>
                                                    <IconButton color="info" onClick={() => fetchUsageHistory(r.reagentId)}>
                                                        <HistoryIcon />
                                                    </IconButton>
                                                </Tooltip>
                                                <Tooltip title="Delete reagent" arrow>
                                                    <IconButton color="error" onClick={() => setDeleteId(r.reagentId)}>
                                                        <DeleteIcon />
                                                    </IconButton>
                                                </Tooltip>
                                            </Stack>
                                        </TableCell>
                                        <TableCell align="center">
                                            <Tooltip title="Import">
                                                <IconButton color="success" onClick={() => setImportReagentData(r)}>
                                                    <Inventory2Icon />
                                                </IconButton>
                                            </Tooltip>
                                        </TableCell>
                                    </TableRow>
                                ))
                            )}
                        </TableBody>
                    </Table>
                </TableContainer>

                <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", p: 2 }}>
                    <Typography variant="body2" color="text.secondary">
                        Showing {filtered.length === 0 ? 0 : (page - 1) * rowsPerPage + 1} - {Math.min(page * rowsPerPage, filtered.length)} of {filtered.length}
                    </Typography>
                    <Pagination
                        count={totalPages}
                        page={page}
                        onChange={(_, v) => setPage(v)}
                        color="primary"
                        shape="rounded"
                        sx={{ '& .MuiPaginationItem-root': { color: '#065f46' }, '& .Mui-selected': { backgroundColor: '#059669' } }}
                    />
                </Box>
            </Paper>

            <Dialog
                open={isAddOpen}
                onClose={() => setIsAddOpen(false)}
                maxWidth="sm"
                fullWidth
                PaperProps={{
                    sx: {
                        borderRadius: "16px",
                        paddingY: 2,
                        boxShadow: "0 8px 30px rgba(0,0,0,0.15)",
                    },
                }}
            >
                <DialogTitle>
                    <div className="text-2xl font-bold text-emerald-700 mb-1">
                        {isEditMode ? 'Edit Reagent' : 'Add New Reagent'}
                    </div>
                    <p className="text-sm text-gray-500">
                        {isEditMode ? 'Update the reagent details below.' : 'Please fill in the details below to add a new reagent.'}
                    </p>
                </DialogTitle>

                <DialogContent>
                    <div className="mt-3 flex flex-col gap-5">
                        <TextField
                            label="Name *"
                            value={name}
                            onChange={(e) => setName(e.target.value)}
                            fullWidth
                            variant="outlined"
                            InputProps={{
                                sx: { borderRadius: "10px" },
                            }}
                            sx={{ "& .MuiInputBase-root": { marginTop: "8px" } }}
                        />
                        <TextField
                            label="Catalog Number"
                            value={catalogNumber}
                            onChange={(e) => setCatalogNumber(e.target.value)}
                            fullWidth
                            variant="outlined"
                            InputProps={{
                                sx: { borderRadius: "10px" },
                            }}
                            sx={{ "& .MuiInputBase-root": { marginTop: "8px" } }}
                        />
                        <TextField
                            label="Manufacturer"
                            value={manufacturer}
                            onChange={(e) => setManufacturer(e.target.value)}
                            fullWidth
                            variant="outlined"
                            InputProps={{
                                sx: { borderRadius: "10px" },
                            }}
                            sx={{ "& .MuiInputBase-root": { marginTop: "8px" } }}
                        />
                        <TextField
                            label="CAS Number"
                            value={casNumber}
                            onChange={(e) => setCasNumber(e.target.value)}
                            fullWidth
                            variant="outlined"
                            InputProps={{
                                sx: { borderRadius: "10px" },
                            }}
                            sx={{ "& .MuiInputBase-root": { marginTop: "8px" } }}
                        />

                            <div className="flex justify-end gap-3 pt-4">
                            <Button
                                variant="outlined"
                                    onClick={() => { setIsAddOpen(false); resetForm(); }}
                                sx={{
                                    textTransform: "none",
                                    borderRadius: "8px",
                                    borderColor: "#d1d5db",
                                    color: "#374151",
                                    "&:hover": {
                                        backgroundColor: "#f3f4f6",
                                        borderColor: "#9ca3af",
                                    },
                                }}
                            >
                                Cancel
                            </Button>
                            <Button
                                variant="contained"
                                    onClick={handleSave}
                                    disabled={!name}
                                sx={{
                                    textTransform: "none",
                                    borderRadius: "8px",
                                    backgroundColor: "#059669",
                                    "&:hover": { backgroundColor: "#047857" },
                                    "&.Mui-disabled": { backgroundColor: "#9ca3af" },
                                }}
                            >
                                    {isEditMode ? 'Save' : 'Create'}
                            </Button>
                        </div>
                    </div>
                </DialogContent>
            </Dialog>

            {/* Delete Dialog */}
            <Dialog open={!!deleteId} onClose={() => setDeleteId(null)}>
                <DialogTitle>Confirm delete reagent?</DialogTitle>
                <DialogContent>Are you sure you want to delete this reagent?</DialogContent>
                <DialogActions>
                    <Button onClick={() => setDeleteId(null)}>Cancel</Button>
                    <Button color="error" variant="contained" onClick={() => deleteId && handleDelete(deleteId)}>Delete</Button>
                </DialogActions>
            </Dialog>

            {/* Detail Dialog */}
            <Dialog open={!!detailReagent} onClose={() => setDetailReagent(null)} maxWidth="xs" fullWidth>
                <DialogTitle className="text-emerald-700 font-bold text-xl">Reagent Details</DialogTitle>
                <DialogContent dividers>
                    {detailReagent && (
                        <div className="space-y-4 py-2">
                            <div><span className="font-semibold mr-2">Name:</span> {detailReagent.name}</div>
                            <div><span className="font-semibold mr-2">Catalog No:</span> {detailReagent.catalogNumber || '-'}</div>
                            <div><span className="font-semibold mr-2">Manufacturer:</span> {detailReagent.manufacturer || '-'}</div>
                            <div><span className="font-semibold mr-2">CAS Number:</span> {detailReagent.casNumber || '-'}</div>
                            <div><span className="font-semibold mr-2">Created By:</span> {(detailReagent as any).createdBy || '-'}</div>
                            <div><span className="font-semibold mr-2">Updated By:</span> {(detailReagent as any).updatedBy || '-'}</div>
                            <div><span className="font-semibold mr-2">Created At:</span> {formatDateTime((detailReagent as any).createdAt)}</div>
                            <div><span className="font-semibold mr-2">Updated At:</span> {formatDateTime((detailReagent as any).updatedAt)}</div>
                        </div>
                    )}
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setDetailReagent(null)} variant="outlined">Close</Button>
                </DialogActions>
            </Dialog>

            {/* Inventory Dialog */}
            <Dialog open={inventoryOpen} onClose={() => setInventoryOpen(false)} maxWidth="lg" fullWidth>
                <DialogTitle className="text-blue-700 font-bold text-lg">Inventory</DialogTitle>
                <DialogContent dividers>
                    {inventoryLoading ? (
                        <Stack direction="row" alignItems="center" justifyContent="center" spacing={2} sx={{ py: 4 }}>
                            <CircularProgress />
                            <div>Loading inventory…</div>
                        </Stack>
                    ) : inventoryList.length === 0 ? (
                        <div className="p-4 text-center text-gray-500">No inventory data found.</div>
                    ) : (
                        <>
                            <Stack direction="row" spacing={2} alignItems="center" sx={{ mb: 2 }}>
                                <TextField
                                    size="small"
                                    placeholder="Filter by Chemical, PO..."
                                    value={inventoryFilter}
                                    onChange={e => setInventoryFilter(e.target.value)}
                                    sx={{ flex: 1, backgroundColor: 'white', borderRadius: 1 }}
                                />

                            </Stack>

                            <TableContainer sx={{ maxHeight: 420 }}>
                                <Table stickyHeader size="small">
                                    <TableHead>
                                        <TableRow sx={{ backgroundColor: '#f3f4f6' }}>
                                                    <TableCell sx={{ fontWeight: 700 }}>Chemical</TableCell>
                                                    <TableCell sx={{ fontWeight: 700, textAlign: 'right' }}>Quantity</TableCell>
                                                    <TableCell sx={{ fontWeight: 700 }}>Expiration</TableCell>
                                                    <TableCell sx={{ fontWeight: 700 }}>Location</TableCell>
                                                    <TableCell sx={{ fontWeight: 700 }}>Status</TableCell>
                                                    <TableCell sx={{ fontWeight: 700, textAlign: 'center' }}>Details</TableCell>
                                        </TableRow>
                                    </TableHead>
                                    <TableBody>
                                        {inventoryList
                                            .filter((item: any) => {
                                                const q = inventoryFilter.trim().toLowerCase();
                                                if (!q) return true;
                                                return (
                                                    (item.reagentName || '').toString().toLowerCase().includes(q) ||
                                                    (item.poNumber || '').toString().toLowerCase().includes(q) ||
                                                    (item.storageLocation || '').toString().toLowerCase().includes(q)
                                                );
                                            })
                                            .map((item: any, idx: number) => (
                                                <TableRow key={idx} hover>
                                                    <TableCell>{item.reagentName || '-'}</TableCell>
                                                    <TableCell align="right">{item.quantity !== undefined && item.quantity !== null ? `${Number(item.quantity).toLocaleString()} ${item.unitOfMeasure || 'ml'}` : '-'}</TableCell>
                                                    <TableCell>{formatDate(item.expirationDate)}</TableCell>
                                                    <TableCell>{item.storageLocation || '-'}</TableCell>
                                                    <TableCell>
                                                        <Chip size="small" label={item.status || '-'} color={
                                                            (item.status || '').toString().toLowerCase() === 'in stock' || (item.status || '').toString().toLowerCase() === 'còn hàng' ? 'success' :
                                                            (item.status || '').toString().toLowerCase() === 'low' || (item.status || '').toString().toLowerCase() === 'sắp hết' ? 'warning' :
                                                            (item.status || '').toString().toLowerCase() === 'expired' || (item.status || '').toString().toLowerCase() === 'hết hạn' ? 'default' : 'default'
                                                        } />
                                                    </TableCell>
                                                    <TableCell align="center">
                                                        <Tooltip title="Details"><IconButton size="small" onClick={() => setInventoryDetail(item)}><VisibilityIcon /></IconButton></Tooltip>
                                                    </TableCell>
                                                </TableRow>
                                            ))}
                                    </TableBody>
                                </Table>
                            </TableContainer>
                        </>
                    )}
                </DialogContent>
                <DialogActions sx={{ justifyContent: 'flex-end' }}>
                    <Button variant="outlined" onClick={() => setInventoryOpen(false)}>CLOSE</Button>
                </DialogActions>
            </Dialog>

            {/* Inventory Detail Dialog */}
            <Dialog open={!!inventoryDetail} onClose={() => setInventoryDetail(null)} maxWidth="sm" fullWidth>
                <DialogTitle>Inventory Detail</DialogTitle>
                <DialogContent dividers>
                    {inventoryDetail ? (
                        <Box sx={{ display: 'grid', gridTemplateColumns: '1fr 2fr', gap: 1, rowGap: 1 }}>
                            <Typography variant="body2" sx={{ fontWeight: 600 }}>Chemical</Typography>
                            <Typography variant="body2">{inventoryDetail.reagentName || '-'}</Typography>

                            <Typography variant="body2" sx={{ fontWeight: 600 }}>Vendor</Typography>
                            <Typography variant="body2">{inventoryDetail.vendorName || '-'}</Typography>

                            <Typography variant="body2" sx={{ fontWeight: 600 }}>PO Number</Typography>
                            <Typography variant="body2">{inventoryDetail.poNumber || '-'}</Typography>

                            <Typography variant="body2" sx={{ fontWeight: 600 }}>Order Date</Typography>
                            <Typography variant="body2">{formatDate(inventoryDetail.orderDate)}</Typography>

                            <Typography variant="body2" sx={{ fontWeight: 600 }}>Receipt Date</Typography>
                            <Typography variant="body2">{formatDate(inventoryDetail.receiptDate)}</Typography>

                            <Typography variant="body2" sx={{ fontWeight: 600 }}>Quantity</Typography>
                            <Typography variant="body2">{inventoryDetail.quantity !== undefined ? `${Number(inventoryDetail.quantity).toLocaleString()} ${inventoryDetail.unitOfMeasure || ''}` : '-'}</Typography>

                            <Typography variant="body2" sx={{ fontWeight: 600 }}>Lot Number</Typography>
                            <Typography variant="body2">{inventoryDetail.lotNumber || '-'}</Typography>

                            <Typography variant="body2" sx={{ fontWeight: 600 }}>Expiration Date</Typography>
                            <Typography variant="body2">{formatDate(inventoryDetail.expirationDate)}</Typography>

                            <Typography variant="body2" sx={{ fontWeight: 600 }}>Storage Location</Typography>
                            <Typography variant="body2">{inventoryDetail.storageLocation || '-'}</Typography>

                            <Typography variant="body2" sx={{ fontWeight: 600 }}>Status</Typography>
                            <Typography variant="body2">{inventoryDetail.status || '-'}</Typography>

                            <Typography variant="body2" sx={{ fontWeight: 600 }}>Note</Typography>
                            <Typography variant="body2">{inventoryDetail.note || '-'}</Typography>
                        </Box>
                    ) : (
                        <Typography color="text.secondary">No details available.</Typography>
                    )}
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setInventoryDetail(null)} variant="outlined">Close</Button>
                </DialogActions>
            </Dialog>
 {/* Import Dialog */}
            <Dialog
                open={!!importReagentData}
                onClose={() => setImportReagentData(null)}
                maxWidth="md"
                fullWidth
                PaperProps={{
                    sx: {
                        borderRadius: 3,
                        px: { xs: 2, sm: 4 },
                        py: { xs: 2, sm: 3 },
                        boxShadow: '0 12px 36px rgba(0,0,0,0.15)'
                    }
                }}
            >
                {/* Title with close button */}
                <DialogTitle sx={{ position: 'relative', pb: 1 }}>
                    <Box sx={{ fontWeight: 800, fontSize: 20, color: 'primary.main' }}>📦 Import Reagent – {importReagentData?.name}</Box>
                    <IconButton
                        aria-label="close"
                        onClick={() => setImportReagentData(null)}
                        sx={{ position: 'absolute', right: 8, top: 8 }}
                        size="large"
                    >
                        <CloseIcon />
                    </IconButton>
                </DialogTitle>

                <DialogContent dividers sx={{ py: 2 }}>
                    <div className="overflow-y-auto max-h-[calc(90vh-180px)] p-6">
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">

                {/* Quantity */}
                <div className="space-y-2">
                <label className="block text-sm font-semibold text-gray-700">
                    Quantity
                </label>
                <input
                    type="number"
                    value={importQuantity}
                    onChange={(e) => setImportQuantity(e.target.value)}
                    className="w-full px-4 py-2.5 border border-gray-300 rounded-lg 
                            focus:ring-2 focus:ring-blue-500 focus:border-transparent 
                            transition-all outline-none"
                />
                </div>

                {/* Unit */}
                <div className="space-y-2">
                <label className="block text-sm font-semibold text-gray-700">
                    Unit
                </label>
                <input
                    type="text"
                    value={importUnit}
                    onChange={(e) => setImportUnit(e.target.value)}
                    className="w-full px-4 py-2.5 border border-gray-300 rounded-lg 
                            focus:ring-2 focus:ring-blue-500 focus:border-transparent 
                            transition-all outline-none"
                />
                </div>

                {/* Vendor */}
                <div className="space-y-2">
                <label className="block text-sm font-semibold text-gray-700">
                    Vendor
                </label>
                <VendorSelect
                    value={importVendorId}
                    onChange={(id) => setImportVendorId(id ?? "")}
                    style={{ width: "100%" }}
                />
                </div>
            </div>

            {/* ORDER INFORMATION */}
            <div className="mt-6 pt-6 border-t border-gray-200">
                <h3 className="text-sm font-bold text-gray-700 uppercase tracking-wide mb-4">
                Order Information
                </h3>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                
                {/* PO Number */}
                <div className="space-y-2">
                    <label className="block text-sm font-semibold text-gray-700">
                    PO Number
                    </label>
                    <input
                    type="text"
                    value={importPONumber}
                    onChange={(e) => setImportPONumber(e.target.value)}
                    className="w-full px-4 py-2.5 border border-gray-300 rounded-lg 
                                focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                </div>

                {/* Order Date */}
                <div className="space-y-2">
                    <label className="block text-sm font-semibold text-gray-700">
                    Order Date
                    </label>
                    <input
                    type="date"
                    value={importOrderDate}
                    onChange={(e) => setImportOrderDate(e.target.value)}
                    className="w-full px-4 py-2.5 border border-gray-300 rounded-lg 
                                focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                </div>

                {/* Receipt Date */}
                <div className="space-y-2">
                    <label className="block text-sm font-semibold text-gray-700">
                    Receipt Date
                    </label>
                    <input
                    type="date"
                    value={importReceiptDate}
                    onChange={(e) => setImportReceiptDate(e.target.value)}
                    className="w-full px-4 py-2.5 border border-gray-300 rounded-lg 
                                focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                </div>
                </div>
            </div>

            {/* BATCH DETAILS */}
            <div className="mt-6 pt-6 border-t border-gray-200">
                <h3 className="text-sm font-bold text-gray-700 uppercase tracking-wide mb-4">
                Batch Details
                </h3>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4">

                {/* Lot Number */}
                <div className="space-y-2">
                    <label className="block text-sm font-semibold text-gray-700">
                    Lot Number
                    </label>
                    <input
                    type="text"
                    value={importLot}
                    onChange={(e) => setImportLot(e.target.value)}
                    className="w-full px-4 py-2.5 border border-gray-300 rounded-lg 
                                focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                </div>

                {/* Expiration Date */}
                <div className="space-y-2">
                    <label className="block text-sm font-semibold text-gray-700">
                    Expiration Date
                    </label>
                    <input
                    type="date"
                    value={importExpire}
                    onChange={(e) => setImportExpire(e.target.value)}
                    className="w-full px-4 py-2.5 border border-gray-300 rounded-lg 
                                focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                </div>

                {/* Status */}
                <div className="space-y-2">
                    <label className="block text-sm font-semibold text-gray-700">
                    Status
                    </label>
                    <select
                    value={importStatus}
                    onChange={(e) => setImportStatus(e.target.value)}
                    className="w-full px-4 py-2.5 border border-gray-300 rounded-lg 
                                focus:ring-2 focus:ring-blue-500 focus:border-transparent bg-white"
                    >
                    <option value="RECEIVED">Received</option>
                    <option value="PARTIAL_SHIPMENT">Partial Shipment</option>
                    <option value="RETURNED">Returned</option>
                    </select>
                </div>
                </div>
            </div>

            {/* STORAGE + NOTES */}
            <div className="mt-6 pt-6 border-t border-gray-200 space-y-4">

                {/* Storage */}
                <div className="space-y-2">
                <label className="block text-sm font-semibold text-gray-700">
                    Storage Location
                </label>
                <input
                    type="text"
                    value={importStorage}
                    onChange={(e) => setImportStorage(e.target.value)}
                    className="w-full px-4 py-2.5 border border-gray-300 rounded-lg 
                            focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                />
                </div>

                {/* Notes */}
                <div className="space-y-2">
                <label className="block text-sm font-semibold text-gray-700">
                    Notes
                </label>
                <textarea
                    value={importNote}
                    onChange={(e) => setImportNote(e.target.value)}
                    rows={4}
                    className="w-full px-4 py-2.5 border border-gray-300 rounded-lg 
                            focus:ring-2 focus:ring-blue-500 focus:border-transparent 
                            resize-none"
                />
                </div>
            </div>
            </div>

                </DialogContent>

                <DialogActions sx={{ px: 3, pb: 2, justifyContent: 'flex-end' }}>
                    <Button variant="outlined" onClick={() => setImportReagentData(null)} sx={{ textTransform: 'none' }}>
                        Cancel
                    </Button>
                    <Button
                        variant="contained"
                        onClick={handleImport}
                        sx={{
                            px: 3,
                            fontWeight: 700,
                            borderRadius: 2,
                            ml: 1
                        }}
                    >
                        IMPORT
                    </Button>
                </DialogActions>
            </Dialog>


            {/* Usage History Dialog */}
            <Dialog open={usageOpen} onClose={() => setUsageOpen(false)} maxWidth="md" fullWidth>
                <DialogTitle>Usage History</DialogTitle>
                <DialogContent dividers>
                    {usageLoading ? (
                        <Stack direction="row" alignItems="center" justifyContent="center" spacing={2} sx={{ py: 4 }}>
                            <CircularProgress />
                            <div>Loading usage history…</div>
                        </Stack>
                    ) : usageHistory.length === 0 ? (
                        <div className="p-4 text-center text-gray-500">No usage history found.</div>
                    ) : (
                        <Table size="small">
                            <TableHead>
                                <TableRow>
                                    <TableCell>Reagent</TableCell>
                                    <TableCell>Used By</TableCell>
                                    <TableCell>Quantity</TableCell>
                                    <TableCell>Note</TableCell>
                                    <TableCell>Used At</TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {usageHistory.map((u, idx) => (
                                    <TableRow key={u.usageId || idx}>
                                        <TableCell>{u.reagentName || '-'}</TableCell>
                                        <TableCell>{u.usedBy || '-'}</TableCell>
                                        <TableCell>{u.quantity ?? '-'}</TableCell>
                                        <TableCell>{u.action || '-'}</TableCell>
                                        <TableCell>{formatDateTime(u.usedAt)}</TableCell>
                                    </TableRow>
                                ))}
                            </TableBody>
                        </Table>
                    )}
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setUsageOpen(false)}>Close</Button>
                </DialogActions>
            </Dialog>
        </Box>
    );
}
