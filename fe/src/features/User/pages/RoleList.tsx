import React, { useEffect, useState } from "react";
import {
  getAllRoles,
  createRole,
  updateRole,
  deleteRole,
  getAllPrivilege,
} from "../services/role.api";
import type { RoleListDTO, RoleDTO, RoleUpdateDTO, PrivilegesDTO } from "../types/role.types";
import {
  Box,
  Typography,
  Button,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  CircularProgress,
  FormControlLabel,
  Checkbox,
  Grid,
} from "@mui/material";

import AddIcon from "@mui/icons-material/Add";
import DeleteIcon from "@mui/icons-material/Delete";
import EditIcon from "@mui/icons-material/Edit";
import SearchIcon from "@mui/icons-material/Search";

const RoleList: React.FC = () => {
  const [roles, setRoles] = useState<RoleListDTO[]>([]);
  const [keyword, setKeyword] = useState("");
  const [loading, setLoading] = useState(false);
  const [isModalOpen, setModalOpen] = useState(false);
  const [editingRole, setEditingRole] = useState<RoleListDTO | null>(null);
  const [isSaving, setIsSaving] = useState(false);

  const [formData, setFormData] = useState<RoleDTO | RoleUpdateDTO>({
    code: "",
    name: "",
    description: "",
    privilegeIds: [],
  });

  const [privilegeMap, setPrivilegeMap] = useState<PrivilegesDTO[]>([]);
  const [isLoadingPrivileges, setIsLoadingPrivileges] = useState<boolean>(true);
  const [errorPrivileges, setErrorPrivileges] = useState<string | null>(null);

  useEffect(() => {
    const fetchPrivileges = async () => {
      setIsLoadingPrivileges(true);
      setErrorPrivileges(null);
      try {
        const data: PrivilegesDTO[] = await getAllPrivilege();
        setPrivilegeMap(data);
      } catch (err) {
        console.error("Lỗi khi lấy dữ liệu đặc quyền:", err);
        setErrorPrivileges("Không thể tải danh sách đặc quyền. Vui lòng kiểm tra kết nối mạng hoặc phiên đăng nhập.");
      } finally {
        setIsLoadingPrivileges(false);
      }
    };

    // Chỉ tải đặc quyền một lần khi component mount
    fetchPrivileges();
  }, []);

  useEffect(() => {
    fetchRoles();
  }, []);

  const fetchRoles = async () => {
    setLoading(true);
    try {
      const res = await getAllRoles(keyword);
      setRoles(Array.isArray(res) ? res : []);
    } catch (e) {
      console.error("Failed to load roles:", e);
      setRoles([]);
    } finally {
      setLoading(false);
    }
  };

  const handleOpenCreate = () => {
    setEditingRole(null);
    setFormData({ code: "", name: "", description: "", privilegeIds: [] });
    setModalOpen(true);
  };

  const handleOpenEdit = (role: RoleListDTO) => {
    setEditingRole(role);
    setFormData({
      name: role.name,
      description: role.description,
      privilegeIds: role.privileges?.map((p) => p.id) || [],
    });
    setModalOpen(true);
  };

  const handleDelete = async (id: string) => {
    if (!window.confirm("Are you sure you want to delete this role?")) return;
    try {
      await deleteRole(id);
      alert("Role deleted successfully.");
      fetchRoles();
    } catch (e) {
      console.error("Error deleting role:", e);
      alert("Failed to delete role.");
    }
  };

  const handleSubmit = async () => {
    setIsSaving(true);
    try {
      if (editingRole) {
        await updateRole(editingRole.id, formData as RoleUpdateDTO);
        alert("Role updated successfully!");
      } else {
        const roleData = formData as RoleDTO;
        if (!roleData.code || !roleData.name) {
          alert("Code and Name are required for a new role.");
          setIsSaving(false);
          return;
        }
        await createRole(roleData);
        alert("Role created successfully!");
      }
      setModalOpen(false);
      fetchRoles();
    } catch (err) {
      console.error("Error saving role:", err);
      alert("Failed to save role. Check console for details.");
    } finally {
      setIsSaving(false);
    }
  };

  const togglePrivilege = (id: number) => {
    setFormData((prev) => {
      const selected = prev.privilegeIds || [];
      const exists = selected.includes(id);
      const newList = exists
        ? selected.filter((pid) => pid !== id)
        : [...selected, id];
      return { ...prev, privilegeIds: newList };
    });
  };

  return (
    <Box sx={{ p: 3 }}>
      <Box
        sx={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          mb: 4,
          gap: 2,
        }}
      >
        <Typography variant="h5" component="h1" fontWeight="bold">
          Role Management
        </Typography>
        <Box sx={{ display: "flex", gap: 1 }}>
          <TextField
            label="Search by name..."
            variant="outlined"
            size="small"
            value={keyword}
            onChange={(e) => setKeyword(e.target.value)}
            sx={{ minWidth: 250 }}
          />
          <Button
            onClick={fetchRoles}
            disabled={loading}
            variant="contained"
            color="secondary"
            startIcon={<SearchIcon />}
          >
            {loading ? (
              <CircularProgress size={24} color="inherit" />
            ) : (
              "Search"
            )}
          </Button>
          <Button
            onClick={handleOpenCreate}
            variant="contained"
            color="primary"
            startIcon={<AddIcon />}
          >
            New Role
          </Button>
        </Box>
      </Box>

      <TableContainer component={Paper} elevation={3}>
        <Table sx={{ minWidth: 800 }} size="medium">
          <TableHead>
            <TableRow sx={{ backgroundColor: "grey.100" }}>
              <TableCell sx={{ fontWeight: "bold" }}>Code</TableCell>
              <TableCell sx={{ fontWeight: "bold" }}>Name</TableCell>
              <TableCell sx={{ fontWeight: "bold" }}>Description</TableCell>
              <TableCell sx={{ fontWeight: "bold" }}>Privileges</TableCell>
              <TableCell align="center" sx={{ fontWeight: "bold" }}>
                Actions
              </TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {loading ? (
              <TableRow>
                <TableCell colSpan={5} align="center">
                  <CircularProgress size={30} sx={{ my: 2 }} />
                </TableCell>
              </TableRow>
            ) : roles.length > 0 ? (
              roles.map((role) => (
                <TableRow key={role.id} hover>
                  <TableCell>{role.code}</TableCell>
                  <TableCell>{role.name}</TableCell>
                  <TableCell>{role.description}</TableCell>
                  <TableCell>
                    <Typography
                      variant="caption"
                      sx={{
                        display: "block",
                        maxWidth: 400,
                        whiteSpace: "nowrap",
                        overflow: "hidden",
                        textOverflow: "ellipsis",
                      }}
                    >
                      {role.privileges?.length
                        ? role.privileges.map((p) => p.name).join(", ")
                        : "—"}
                    </Typography>
                  </TableCell>
                  <TableCell align="center">
                    <Box
                      sx={{
                        display: "flex",
                        justifyContent: "center",
                        gap: 0.5,
                      }}
                    >
                      <IconButton
                        color="primary"
                        onClick={() => handleOpenEdit(role)}
                        aria-label="edit role"
                      >
                        <EditIcon />
                      </IconButton>
                      <IconButton
                        color="error"
                        onClick={() => handleDelete(role.id)}
                        aria-label="delete role"
                      >
                        <DeleteIcon />
                      </IconButton>
                    </Box>
                  </TableCell>
                </TableRow>
              ))
            ) : (
              <TableRow>
                <TableCell
                  colSpan={5}
                  align="center"
                  sx={{ color: "text.secondary", p: 4 }}
                >
                  No roles found
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Modal Create / Update */}
      <Dialog
        open={isModalOpen}
        onClose={() => setModalOpen(false)}
        maxWidth="md"
        fullWidth
      >
        <DialogTitle>{editingRole ? "Update Role" : "Create Role"}</DialogTitle>
        <DialogContent dividers>
          <Box
            component="form"
            sx={{ display: "flex", flexDirection: "column", gap: 2 }}
          >
            {!editingRole && (
              <TextField
                label="Code"
                value={(formData as RoleDTO).code || ""}
                onChange={(e) =>
                  setFormData({ ...formData, code: e.target.value })
                }
                required
                fullWidth
                margin="dense"
              />
            )}
            <TextField
              label="Name"
              value={formData.name || ""}
              onChange={(e) =>
                setFormData({ ...formData, name: e.target.value })
              }
              required
              fullWidth
              margin="dense"
            />
            <TextField
              label="Description"
              value={formData.description || ""}
              onChange={(e) =>
                setFormData({ ...formData, description: e.target.value })
              }
              fullWidth
              margin="dense"
              multiline
              rows={2}
            />

            <Typography variant="subtitle1" sx={{ mt: 2, mb: 1, fontWeight: "bold" }}>
              Privileges
            </Typography>

            {isLoadingPrivileges && (
              <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', p: 4, minHeight: 400, border: "1px solid #ddd", borderRadius: 1 }}>
                <CircularProgress size={24} />
                <Typography sx={{ ml: 2, color: 'text.secondary' }}>Đang tải danh sách đặc quyền...</Typography>
              </Box>
            )}

            {errorPrivileges && (
              <Box sx={{ p: 2, border: "1px solid", borderColor: 'error.main', borderRadius: 1 }}>
                <Typography color="error">{errorPrivileges}</Typography>
              </Box>
            )}

            <Grid
              container
              spacing={1}
              sx={{
                border: "1px solid",
                borderColor: "grey.300",
                p: 2,
                borderRadius: 1,
                maxHeight: 400,
                overflowY: "auto",
              }}
            >
              {privilegeMap.map((p) => (
                <Grid item xs={12} sm={6} md={4} key={p.id}>
                  <FormControlLabel
                    control={
                      <Checkbox
                        checked={formData.privilegeIds?.includes(p.id)}
                        onChange={() => togglePrivilege(p.id)}
                        size="small"
                      />
                    }
                    label={<Typography variant="body2">{p.name}</Typography>}
                  />
                </Grid>
              ))}
            </Grid>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button
            onClick={() => setModalOpen(false)}
            color="inherit"
            disabled={isSaving}
          >
            Cancel
          </Button>
          <Button
            onClick={handleSubmit}
            variant="contained"
            color="primary"
            disabled={
              isSaving ||
              !formData.name ||
              (!editingRole && !(formData as RoleDTO).code)
            }
            startIcon={
              isSaving && <CircularProgress size={20} color="inherit" />
            }
          >
            {isSaving
              ? editingRole
                ? "Updating..."
                : "Creating..."
              : editingRole
                ? "Update"
                : "Create"}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default RoleList;
