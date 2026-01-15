import React, { useState } from "react";
import { useLogin } from "../../hooks/useLogin";
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
  InputAdornment,
} from "@mui/material";

import LockOpenIcon from "@mui/icons-material/LockOpen";
import VpnKeyIcon from "@mui/icons-material/VpnKey";
import EmailIcon from "@mui/icons-material/Email";
import PasswordIcon from "@mui/icons-material/Password";
import VisibilityIcon from '@mui/icons-material/Visibility';
import VisibilityOffIcon from '@mui/icons-material/VisibilityOff';

const Login: React.FC = () => {
  const { handleLogin, handleFirstLogin, loading, error, firstLogin } =
    useLogin();

  const [localError, setLocalError] = useState<string | null>(null);

  const [identifyNumber, setIdentifyNumber] = useState("");
  const [password, setPassword] = useState("");
  const [showPassword, setShowPassword] = useState(false);

  const [newPassword, setNewPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");

  const [showForgotModal, setShowForgotModal] = useState(false);
  const [showConfirmModal, setShowConfirmModal] = useState(false);

  const [email, setEmail] = useState("");
  const [confirmationCode, setConfirmationCode] = useState("");
  const [newForgotPassword, setNewForgotPassword] = useState("");
  
  const [forgotPasswordError, setForgotPasswordError] = useState<string | null>(null);

  const onSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setLocalError(null);
    handleLogin({ identifyNumber, password });
  };

  const onSubmitFirstLogin = (e: React.FormEvent) => {
    e.preventDefault();
    setLocalError(null);

    if (newPassword !== confirmPassword) {
      setLocalError("Mật khẩu xác nhận không khớp!");
      return;
    }
    handleFirstLogin(identifyNumber, newPassword);
  };

  const handleForgotPassword = async (e: React.FormEvent) => {
    e.preventDefault();
    setForgotPasswordError(null);

    try {
      const res = await api.post(`${SERVICE_ENDPOINTS.AUTH}/forgot-password`, {
        email: email,
      });

      if (res.status === 200 || res.status === 201) {
        setForgotPasswordError("Mã xác nhận đã được gửi đến email của bạn!");
        setShowForgotModal(false);
        setShowConfirmModal(true);
      } else {
        setForgotPasswordError("Không tìm thấy email hoặc lỗi hệ thống!");
      }
    } catch (err) {
      if (axios.isAxiosError(err) && err.response) {
        setForgotPasswordError(`Lỗi: ${err.response.data?.message || "Lỗi server!"}`);
      } else {
        setForgotPasswordError("Lỗi kết nối đến server!");
      }
    }
  };

  const handleConfirmForgotPassword = async (e: React.FormEvent) => {
    e.preventDefault();
    setForgotPasswordError(null);

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
        setLocalError("Đặt lại mật khẩu thành công! Hãy đăng nhập lại.");
        setShowConfirmModal(false);
        setEmail("");
        setConfirmationCode("");
        setNewForgotPassword("");
      } else {
        setForgotPasswordError("Mã xác nhận không đúng hoặc lỗi hệ thống!");
      }
    } catch (err) {
      if (axios.isAxiosError(err) && err.response) {
        setForgotPasswordError(`Lỗi: ${err.response.data?.message || "Lỗi server!"}`);
      } else {
        setForgotPasswordError("Lỗi kết nối đến server!");
      }
    }
  };

  const handleTogglePasswordVisibility = () => {
    setShowPassword(!showPassword);
  };

  return (
    <Box>
      <Box component="form" onSubmit={onSubmit} sx={{ width: '100%' }}>
        <TextField
          label="ID Number"
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
            endAdornment: (
              <InputAdornment position="end">
                <IconButton
                  aria-label="toggle password visibility"
                  onClick={handleTogglePasswordVisibility}
                  edge="end"
                  size="small"
                >
                  {showPassword ? <VisibilityOffIcon /> : <VisibilityIcon />}
                </IconButton>
              </InputAdornment>
            ),
          }}
        />

        {(error || localError) && (
          <Typography
            color="error"
            variant="body2"
            align="center"
            sx={{ mb: 2, p: 1, border: '1px solid', borderColor: 'error.light', borderRadius: 1 }}
          >
            {error || localError}
          </Typography>
        )}

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

        <Box sx={{ textAlign: "right", mt: 1 }}>
          <Button
            onClick={() => setShowForgotModal(true)}
            sx={{ textTransform: "none", color: 'text.secondary' }}
          >
            Forgot password?
          </Button>
        </Box>
      </Box>

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
            {localError && (
              <Typography color="error" variant="body2" sx={{ mb: 1 }}>
                  {localError}
              </Typography>
            )}
            <TextField
              label="Mật khẩu mới"
              type="password"
              fullWidth
              margin="normal"
              value={newPassword}
              onChange={(e) => { setNewPassword(e.target.value); setLocalError(null); }}
              required
            />
            <TextField
              label="Xác nhận mật khẩu"
              type="password"
              fullWidth
              margin="normal"
              value={confirmPassword}
              onChange={(e) => { setConfirmPassword(e.target.value); setLocalError(null); }}
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
              sx={{ py: 1 }}
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
            {forgotPasswordError && (
                <Typography color="error" variant="body2" sx={{ mb: 1 }}>
                    {forgotPasswordError}
                </Typography>
            )}
            <TextField
              label="Nhập email"
              type="email"
              fullWidth
              margin="normal"
              value={email}
              onChange={(e) => { setEmail(e.target.value); setForgotPasswordError(null); }}
              required
              placeholder="example@email.com"
            />
          </DialogContent>
          <DialogActions>
            <Button type="submit" variant="contained" color="primary" fullWidth sx={{ py: 1 }}>
              Gửi mã xác nhận
            </Button>
          </DialogActions>
        </Box>
      </Dialog>

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
            {forgotPasswordError && (
                <Typography color="error" variant="body2" sx={{ mb: 1 }}>
                    {forgotPasswordError}
                </Typography>
            )}
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
              onChange={(e) => { setConfirmationCode(e.target.value); setForgotPasswordError(null); }}
              required
            />
            <TextField
              label="Mật khẩu mới"
              type="password"
              fullWidth
              margin="normal"
              value={newForgotPassword}
              onChange={(e) => { setNewForgotPassword(e.target.value); setForgotPasswordError(null); }}
              required
            />
          </DialogContent>
          <DialogActions>
            <Button type="submit" variant="contained" color="primary" fullWidth sx={{ py: 1 }}>
              Xác nhận
            </Button>
          </DialogActions>
        </Box>
      </Dialog>
    </Box>
  );
};

export default Login;