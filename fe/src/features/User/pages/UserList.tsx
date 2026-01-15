import React, { useEffect, useState } from "react";
import {
  getAllUsersApi,
  createUserApi,
  getUserByIdApi,
  updateUserApi,
  deleteUserApi,
  restoreUserApi,
} from "../services/user.api";
import type { UserProfileDTO } from "../types/user.types";
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
  Select,
  MenuItem,
  FormControl,
  InputLabel,
  CircularProgress,
} from "@mui/material";

import EditIcon from "@mui/icons-material/Edit";
import DeleteIcon from "@mui/icons-material/Delete";
import ReplayIcon from "@mui/icons-material/Replay";
import AddIcon from "@mui/icons-material/Add";
import { RoleDTO } from "../types/role.types";
import { getAllRoles } from "../services/role.api";

// const roleOptions = [
//   { code: "ADMIN", name: "Admin" },
//   { code: "LAB_MANAGER", name: "Lab Manager" },
//   { code: "SERVICE_USER", name: "Service User" },
//   { code: "LAB_USER", name: "Lab User" },
//   { code: "PATIENT", name: "Patient" },
// ];

const UserList: React.FC = () => {
  const [users, setUsers] = useState<UserProfileDTO[]>([]);
  const [openModal, setOpenModal] = useState(false);
  const [openEditModal, setOpenEditModal] = useState(false);
  const [selectedRole, setSelectedRole] = useState("SERVICE_USER");
  const [selectedUserId, setSelectedUserId] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);
  const [isFetching, setIsFetching] = useState(false);
  const [roleOptions, setRoleOptions] = useState<RoleDTO[]>([]);

  const [newUser, setNewUser] = useState<UserProfileDTO>({
    id: "",
    identifyNumber: "",
    email: "",
    password: "",
    fullName: "",
    age: 0,
    dob: "",
    phoneNumber: "",
    gender: "",
    address: "",
    status: "ACTIVE",
  });

  const [editUser, setEditUser] = useState<UserProfileDTO | null>(null);

  useEffect(() => {
    fetchUsers();
    fetchRoleOptions();
  }, []);

  const fetchUsers = async () => {
    setIsFetching(true);
    try {
      const data = await getAllUsersApi();
      setUsers(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error("Error fetching users:", err);
    } finally {
      setIsFetching(false);
    }
  };

  const fetchRoleOptions = async () => {
    setIsFetching(true);
    try {
      const data = await getAllRoles();
      setRoleOptions(data);
    } catch(error){
      console.error("Error fetching role options: ", error);
    } finally {
      setIsFetching(false);
    }
  }

  const handleCreate = async () => {
    setLoading(true);
    try {
      if (!newUser.password) {
        alert("Mật khẩu là bắt buộc!");
        return;
      }
      await createUserApi(newUser, selectedRole);
      alert("Tạo user thành công!");
      handleCloseModal();
      fetchUsers();
    } catch (err) {
      console.error("Error creating user:", err);
      alert("Lỗi khi tạo user!");
    } finally {
      setLoading(false);
    }
  };

  const handleCloseModal = () => {
    setOpenModal(false);
    setNewUser({
      id: "",
      identifyNumber: "",
      email: "",
      password: "",
      fullName: "",
      age: 0,
      dob: "",
      phoneNumber: "",
      gender: "",
      address: "",
      status: "ACTIVE",
    });
    setSelectedRole("SERVICE_USER");
  };

  const handleOpenEdit = async (id: string) => {
    try {
      const user = await getUserByIdApi(id);
      setEditUser(user);
      setSelectedUserId(id);
      setOpenEditModal(true);
    } catch (err) {
      console.error("Error fetching user:", err);
    }
  };

  const handleUpdate = async () => {
    if (!selectedUserId || !editUser) return;
    setLoading(true);
    try {
      await updateUserApi(selectedUserId, editUser);
      alert("Cập nhật user thành công!");
      setOpenEditModal(false);
      fetchUsers();
    } catch (err) {
      console.error("Error updating user:", err);
      alert("Lỗi khi cập nhật user!");
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (id: string) => {
    if (!window.confirm("Bạn có chắc chắn muốn vô hiệu hóa người dùng này?"))
      return;
    try {
      await deleteUserApi(id);
      alert("Đã vô hiệu hóa người dùng!");
      fetchUsers();
    } catch (err) {
      console.error("Error deleting user:", err);
    }
  };

  const handleRestore = async (id: string) => {
    if (!window.confirm("Bạn có muốn khôi phục người dùng này?")) return;
    try {
      await restoreUserApi(id);
      alert("Khôi phục thành công!");
      fetchUsers();
    } catch (err) {
      console.error("Error restoring user:", err);
    }
  };

  const handleNewUserChange = (
    field: keyof UserProfileDTO,
    value: string | number
  ) => {
    setNewUser((prev) => ({ ...prev, [field]: value }));
  };

  const handleEditUserChange = (
    field: keyof UserProfileDTO,
    value: string | number
  ) => {
    if (editUser) {
      setEditUser((prev) => (prev ? { ...prev, [field]: value } : null));
    }
  };

  const userFields: {
    key: keyof UserProfileDTO;
    label: string;
    type?: string;
    required?: boolean;
  }[] = [
    { key: "fullName", label: "Họ tên", required: true },
    { key: "email", label: "Email", required: true },
    { key: "identifyNumber", label: "CCCD/CMND", required: true },
    { key: "phoneNumber", label: "Số điện thoại" },
    { key: "gender", label: "Giới tính" },
    { key: "address", label: "Địa chỉ" },
  ];

  return (
    <Box sx={{ p: 3 }}>
      <Box
        sx={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          mb: 4,
        }}
      >
        <Typography variant="h5" component="h1" fontWeight="bold">
          Danh sách người dùng
        </Typography>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => setOpenModal(true)}
          sx={{ py: 1 }}
        >
          Tạo mới
        </Button>
      </Box>

      {/* User Table */}
      <TableContainer component={Paper} elevation={3}>
        {isFetching ? (
          <Box
            sx={{
              display: "flex",
              justifyContent: "center",
              alignItems: "center",
              height: 200,
            }}
          >
            <CircularProgress />
          </Box>
        ) : (
          <Table sx={{ minWidth: 1000 }} size="medium">
            <TableHead>
              <TableRow sx={{ backgroundColor: "grey.100" }}>
                <TableCell>Họ tên</TableCell>
                <TableCell>Email</TableCell>
                <TableCell>CCCD/CMND</TableCell>
                <TableCell>Ngày sinh</TableCell>
                <TableCell>Giới tính</TableCell>
                <TableCell>Tuổi</TableCell>
                <TableCell>SĐT</TableCell>
                <TableCell>Địa chỉ</TableCell>
                <TableCell>Trạng thái</TableCell>
                <TableCell align="center">Thao tác</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {users.map((u) => (
                <TableRow
                  key={u.id}
                  hover
                  sx={{ "&:last-child td, &:last-child th": { border: 0 } }}
                >
                  <TableCell>{u.fullName}</TableCell>
                  <TableCell>{u.email}</TableCell>
                  <TableCell>{u.identifyNumber}</TableCell>
                  <TableCell>
                    {u.dob ? new Date(u.dob).toLocaleDateString("vi-VN") : ""}
                  </TableCell>
                  <TableCell>{u.gender}</TableCell>
                  <TableCell>{u.age}</TableCell>
                  <TableCell>{u.phoneNumber}</TableCell>
                  <TableCell>{u.address}</TableCell>
                  <TableCell>
                    <Typography
                      variant="caption"
                      sx={{
                        px: 1,
                        py: 0.5,
                        borderRadius: 1,
                        color:
                          u.status === "ACTIVE" ? "success.main" : "error.main",
                        // backgroundColor:
                        //   u.status === "ACTIVE"
                        //     ? "success.light"
                        //     : "error.light",
                        fontWeight: "bold",
                      }}
                    >
                      {u.status}
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
                        onClick={() => handleOpenEdit(u.id!)}
                        aria-label="edit user"
                      >
                        <EditIcon />
                      </IconButton>
                      {u.status === "ACTIVE" ? (
                        <IconButton
                          color="error"
                          onClick={() => handleDelete(u.id!)}
                          aria-label="deactivate user"
                        >
                          <DeleteIcon />
                        </IconButton>
                      ) : (
                        <IconButton
                          color="success"
                          onClick={() => handleRestore(u.id!)}
                          aria-label="restore user"
                        >
                          <ReplayIcon />
                        </IconButton>
                      )}
                    </Box>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        )}
      </TableContainer>

      {/* Modal: Create User */}
      <Dialog
        open={openModal}
        onClose={handleCloseModal}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>Create new User</DialogTitle>
        <DialogContent dividers>
          <Box
            component="form"
            sx={{ display: "flex", flexDirection: "column", gap: 2 }}
          >
            {userFields.map((field) => (
              <TextField
                key={field.key}
                label={field.label}
                type={field.type || "text"}
                value={(newUser as any)[field.key]}
                onChange={(e) => handleNewUserChange(field.key, e.target.value)}
                required={field.required}
                fullWidth
                margin="dense"
              />
            ))}

            {/* Password Field (only for create) */}
            <TextField
              label="Mật khẩu"
              type="password"
              value={newUser.password}
              onChange={(e) => handleNewUserChange("password", e.target.value)}
              required
              fullWidth
              margin="dense"
            />

            <Box sx={{ display: "flex", gap: 2 }}>
              <TextField
                label="Tuổi"
                type="number"
                value={newUser.age}
                onChange={(e) =>
                  handleNewUserChange("age", Number(e.target.value))
                }
                fullWidth
                margin="dense"
              />
              <TextField
                label="Ngày sinh"
                type="date"
                value={newUser.dob}
                onChange={(e) => handleNewUserChange("dob", e.target.value)}
                InputLabelProps={{ shrink: true }}
                fullWidth
                margin="dense"
              />
            </Box>

            <FormControl fullWidth margin="dense">
              <InputLabel id="select-role-label">Vai trò</InputLabel>
              <Select
                labelId="select-role-label"
                value={selectedRole}
                label="Vai trò"
                onChange={(e) => setSelectedRole(e.target.value)}
              >
                {roleOptions.map((r) => (
                  <MenuItem key={r.code} value={r.code}>
                    {r.name}
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseModal} color="inherit" disabled={loading}>
            Hủy
          </Button>
          <Button
            onClick={handleCreate}
            variant="contained"
            color="primary"
            disabled={loading}
            startIcon={
              loading && <CircularProgress size={20} color="inherit" />
            }
          >
            {loading ? "Đang tạo..." : "Lưu"}
          </Button>
        </DialogActions>
      </Dialog>

      {/* Modal: Edit User */}
      <Dialog
        open={openEditModal}
        onClose={() => setOpenEditModal(false)}
        maxWidth="sm"
        fullWidth
      >
        <DialogTitle>Cập nhật thông tin người dùng</DialogTitle>
        <DialogContent dividers>
          {editUser ? (
            <Box
              component="form"
              sx={{ display: "flex", flexDirection: "column", gap: 2 }}
            >
              {userFields.map((field) => (
                <TextField
                  key={field.key}
                  label={field.label}
                  type={field.type || "text"}
                  value={(editUser as any)[field.key]}
                  onChange={(e) =>
                    handleEditUserChange(field.key, e.target.value)
                  }
                  required={field.required}
                  fullWidth
                  margin="dense"
                />
              ))}

              <Box sx={{ display: "flex", gap: 2 }}>
                <TextField
                  label="Tuổi"
                  type="number"
                  value={editUser.age}
                  onChange={(e) =>
                    handleEditUserChange("age", Number(e.target.value))
                  }
                  fullWidth
                  margin="dense"
                />
                <TextField
                  label="Ngày sinh"
                  type="date"
                  value={editUser.dob}
                  onChange={(e) => handleEditUserChange("dob", e.target.value)}
                  InputLabelProps={{ shrink: true }}
                  fullWidth
                  margin="dense"
                />
              </Box>
            </Box>
          ) : (
            <Box sx={{ display: "flex", justifyContent: "center", p: 3 }}>
              <CircularProgress />
            </Box>
          )}
        </DialogContent>
        <DialogActions>
          <Button
            onClick={() => setOpenEditModal(false)}
            color="inherit"
            disabled={loading}
          >
            Hủy
          </Button>
          <Button
            onClick={handleUpdate}
            variant="contained"
            color="primary"
            disabled={loading || !editUser}
            startIcon={
              loading && <CircularProgress size={20} color="inherit" />
            }
          >
            {loading ? "Đang cập nhật..." : "Cập nhật"}
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default UserList;
