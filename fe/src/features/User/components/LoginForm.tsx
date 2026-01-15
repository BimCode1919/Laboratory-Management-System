import React, { useState } from "react";
import { useLogin } from "../hooks/useLogin";
import api from "@/api/AxiosInstance";
import axios from "axios";
import { SERVICE_ENDPOINTS } from "@/api/Endpoints";
import {
  Box,
  Button,
  TextField,
  Typography,
  CircularProgress,
  IconButton,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Paper, // NEW: Using Paper for a card look
  InputAdornment, // NEW: For password visibility
} from "@mui/material";

import CloseIcon from "@mui/icons-material/Close";
import LockOpenIcon from "@mui/icons-material/LockOpen";
import VpnKeyIcon from "@mui/icons-material/VpnKey";
import EmailIcon from "@mui/icons-material/Email";
import PasswordIcon from "@mui/icons-material/Password";
import VisibilityIcon from '@mui/icons-material/Visibility';
import VisibilityOffIcon from '@mui/icons-material/VisibilityOff';


interface LoginFormProps {
  onClose: () => void;
}

const LoginForm: React.FC<LoginFormProps> = ({ onClose }) => {
  const { handleLogin, handleFirstLogin, loading, error, firstLogin } =
    useLogin();

  const [identifyNumber, setIdentifyNumber] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false); // NEW: State for password visibility

  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");

  const [showForgotModal, setShowForgotModal] = useState(false);
  const [showConfirmModal, setShowConfirmModal] = useState(false);

  const [email, setEmail] = useState("");
  const [confirmationCode, setConfirmationCode] = useState("");
  const [newForgotPassword, setNewForgotPassword] = useState("");

  const onSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    handleLogin({ identifyNumber, password });
  };

  const onSubmitFirstLogin = (e: React.FormEvent) => {
    e.preventDefault();
    if (newPassword !== confirmPassword) {
      alert("Mật khẩu xác nhận không khớp!");
      return;
    }
    handleFirstLogin(identifyNumber, newPassword);
  };

  const handleForgotPassword = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const res = await api.post(`${SERVICE_ENDPOINTS.AUTH}/forgot-password`, {
        email: email,
      });

      if (res.status === 200 || res.status === 201) {
        alert("Mã xác nhận đã được gửi đến email của bạn!");
        setShowForgotModal(false);
        setShowConfirmModal(true);
      } else {
        alert("Không tìm thấy email hoặc lỗi hệ thống!");
      }
    } catch (err) {
      if (axios.isAxiosError(err) && err.response) {
        alert(`Lỗi: ${err.response.data?.message || "Lỗi server!"}`);
      } else {
        alert("Lỗi kết nối đến server!");
      }
    }
  };

  const handleConfirmForgotPassword = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      const res = await api.post(
        `${SERVICE_ENDPOINTS.AUTH}/forgot-password-confirm`,
        {
          email: email,
          confirmationCode: confirmationCode,
          newPassword: newForgotPassword,
        }
      );

      if (res.status === 200 || res.status === 201) {
        alert("Đặt lại mật khẩu thành công! Hãy đăng nhập lại.");
        setShowConfirmModal(false);
        setEmail("");
        setConfirmationCode("");
        setNewForgotPassword("");
      } else {
        alert("Mã xác nhận không đúng hoặc lỗi hệ thống!");
      }
    } catch (err) {
      if (axios.isAxiosError(err) && err.response) {
        alert(`Lỗi: ${err.response.data?.message || "Lỗi server!"}`);
      } else {
        alert("Lỗi kết nối đến server!");
      }
    }
  };

  const handleTogglePasswordVisibility = () => {
    setShowPassword(!showPassword);
  };

  return (
    <div className="fixed inset-0 flex items-center justify-center bg-black/50 backdrop-blur-sm overflow-hidden z-50">
      {/* Main Login Form - Wrapped in Paper */}
      <Paper
        elevation={10} // Higher elevation for a modern feel
        sx={{
          p: 5,
          width: '90%',
          maxWidth: 400, // Slightly wider max-width for better balance
          borderRadius: 3,
          position: 'relative',
        }}
      >
        <IconButton
          aria-label="close"
          onClick={onClose}
          sx={{
            position: "absolute",
            right: 12,
            top: 12,
            color: (theme) => theme.palette.grey[600],
            '&:hover': { color: 'primary.main' }
          }}
        >
          <CloseIcon />
        </IconButton>

        <Box sx={{ textAlign: 'center', mb: 4 }}>
          <Typography
            variant="h4"
            component="h1"
            sx={{ fontWeight: "bold", color: "primary.main", mb: 0.5 }}
          >
            🧪 Lab System
          </Typography>
          <Typography
            variant="subtitle2"
            color="text.secondary"
          >
            Sign in to access your dashboard
          </Typography>
        </Box>

        <Box component="form" onSubmit={onSubmit} sx={{ width: '100%' }}>
          {/* ID Number Field */}
          <TextField
            label="ID Number / User Code"
            fullWidth
            margin="normal"
            value={identifyNumber}
            onChange={(e) => setIdentifyNumber(e.target.value)}
            required
            autoFocus
            size="medium"
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <VpnKeyIcon sx={{ color: "action.active" }} />
                </InputAdornment>
              ),
            }}
          />

          {/* Password Field (with visibility toggle) */}
          <TextField
            label="Password"
            type={showPassword ? 'text' : 'password'}
            fullWidth
            margin="normal"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            size="medium"
            sx={{ mb: 3 }}
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <LockOpenIcon sx={{ color: "action.active" }} />
                </InputAdornment>
              ),
              endAdornment: ( // Added password visibility toggle
                <InputAdornment position="end">
                  <IconButton
                    aria-label="toggle password visibility"
                    onClick={handleTogglePasswordVisibility}
                    edge="end"
                  >
                    {showPassword ? <VisibilityOffIcon /> : <VisibilityIcon />}
                  </IconButton>
                </InputAdornment>
              ),
            }}
          />

          {error && (
            <Typography
              color="error"
              variant="body2"
              align="center"
              sx={{ mb: 2 }}
            >
              {error}
            </Typography>
          )}

          {/* Login Button */}
          <Button
            type="submit"
            fullWidth
            variant="contained"
            size="large"
            color="primary"
            disabled={loading}
            sx={{ py: 1.5, mb: 1, boxShadow: '0 4px 10px rgba(25, 118, 210, 0.3)' }}
          >
            {loading ? <CircularProgress size={24} color="inherit" /> : "SIGN IN"}
          </Button>

          {/* Forgot Password Link */}
          <Box sx={{ textAlign: "right", mt: 1 }}>
            <Button
              onClick={() => setShowForgotModal(true)}
              sx={{ textTransform: "none", color: 'text.secondary' }}
            >
              Forgot password?
            </Button>
          </Box>
        </Box>
      </Paper>

      {/* --- Modals (Keep Existing Logic) --- */}
      {/* Modal: First Login Password Change */}
      <Dialog
        open={firstLogin}
        onClose={() => window.location.reload()}
        aria-labelledby="first-login-dialog-title"
        PaperProps={{ sx: { maxWidth: 450 } }}
      >
        <DialogTitle
          id="first-login-dialog-title"
          sx={{ textAlign: "center", pb: 1 }}
        >
          <PasswordIcon
            color="primary"
            sx={{ mr: 1, verticalAlign: "middle" }}
          />
          Đổi mật khẩu lần đầu
        </DialogTitle>
        <Box component="form" onSubmit={onSubmitFirstLogin}>
          <DialogContent dividers>
            <TextField
              label="Mật khẩu mới"
              type="password"
              fullWidth
              margin="normal"
              value={newPassword}
              onChange={(e) => setNewPassword(e.target.value)}
              required
            />
            <TextField
              label="Xác nhận mật khẩu"
              type="password"
              fullWidth
              margin="normal"
              value={confirmPassword}
              onChange={(e) => setConfirmPassword(e.target.value)}
              required
            />
          </DialogContent>
          <DialogActions>
            <Button
              type="submit"
              variant="contained"
              color="primary"
              disabled={loading}
              fullWidth
            >
              {loading ? (
                <CircularProgress size={24} color="inherit" />
              ) : (
                "Xác nhận đổi mật khẩu"
              )}
            </Button>
          </DialogActions>
        </Box>
      </Dialog>

      {/* Modal: Forgot Password (Step 1) */}
      <Dialog
        open={showForgotModal}
        onClose={() => setShowForgotModal(false)}
        aria-labelledby="forgot-password-dialog-title"
        PaperProps={{ sx: { maxWidth: 450 } }}
      >
        <DialogTitle
          id="forgot-password-dialog-title"
          sx={{ textAlign: "center", pb: 1 }}
        >
          <VpnKeyIcon color="primary" sx={{ mr: 1, verticalAlign: "middle" }} />
          🔑 Quên mật khẩu
        </DialogTitle>
        <Box component="form" onSubmit={handleForgotPassword}>
          <DialogContent dividers>
            <TextField
              label="Nhập email"
              type="email"
              fullWidth
              margin="normal"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              placeholder="example@email.com"
            />
          </DialogContent>
          <DialogActions>
            <Button type="submit" variant="contained" color="primary" fullWidth>
              Gửi mã xác nhận
            </Button>
          </DialogActions>
        </Box>
      </Dialog>

      {/* Modal: Forgot Password Confirmation (Step 2) */}
      <Dialog
        open={showConfirmModal}
        onClose={() => setShowConfirmModal(false)}
        aria-labelledby="confirm-forgot-password-dialog-title"
        PaperProps={{ sx: { maxWidth: 450 } }}
      >
        <DialogTitle
          id="confirm-forgot-password-dialog-title"
          sx={{ textAlign: "center", pb: 1 }}
        >
          <EmailIcon color="primary" sx={{ mr: 1, verticalAlign: "middle" }} />
          Xác nhận mã
        </DialogTitle>
        <Box component="form" onSubmit={handleConfirmForgotPassword}>
          <DialogContent dividers>
            <TextField
              label="Email"
              type="email"
              fullWidth
              margin="normal"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              required
              disabled 
            />
            <TextField
              label="Mã xác nhận"
              type="text"
              fullWidth
              margin="normal"
              value={confirmationCode}
              onChange={(e) => setConfirmationCode(e.target.value)}
              required
            />
            <TextField
              label="Mật khẩu mới"
              type="password"
              fullWidth
              margin="normal"
              value={newForgotPassword}
              onChange={(e) => setNewForgotPassword(e.target.value)}
              required
            />
          </DialogContent>
          <DialogActions>
            <Button type="submit" variant="contained" color="primary" fullWidth>
              Xác nhận
            </Button>
          </DialogActions>
        </Box>
      </Dialog>
    </div>
  );
};

export default LoginForm;