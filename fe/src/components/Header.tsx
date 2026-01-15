// src/components/Header.tsx

import {
  AppBar,
  Toolbar,
  Box,
  Button,
  TextField,
  IconButton,
  Menu,
  MenuItem,
  Typography,
  InputAdornment,
  Divider,
  useTheme,
  Tooltip,
} from "@mui/material";
import { useLocation, useNavigate } from "react-router-dom";
import { useState, type MouseEvent, useCallback } from "react";

// Icons
import SearchIcon from "@mui/icons-material/Search";
import LightModeIcon from "@mui/icons-material/LightMode";
import DarkModeIcon from "@mui/icons-material/DarkMode";
import AccountCircleIcon from "@mui/icons-material/AccountCircle";
import LogoutIcon from "@mui/icons-material/Logout";
import SettingsIcon from "@mui/icons-material/Settings";
import LoginIcon from "@mui/icons-material/Login";
import KeyboardArrowDownIcon from "@mui/icons-material/KeyboardArrowDown";
import ListItemIcon from "@mui/material/ListItemIcon";

// Types and Data
import {
  ALL_NAV_ITEMS,
  type NavItem,
  type UserData,
} from "../types/HeaderRouting";
import api from "@/api/AxiosInstance";
import { SERVICE_ENDPOINTS } from "@/api/Endpoints";

interface AppHeaderProps extends UserData {
  onToggleTheme?: () => void;
}

export default function AppHeader({
  isLoggedIn,
  username,
  userRole,
  onToggleTheme,
}: AppHeaderProps) {
  const navigate = useNavigate();
  const location = useLocation();
  const theme = useTheme();

  const [userMenuAnchor, setUserMenuAnchor] = useState<null | HTMLElement>(
    null
  );

  const [navMenuAnchor, setNavMenuAnchor] = useState<null | HTMLElement>(null);
  const [navMenuId, setNavMenuId] = useState<string | null>(null);

  const isDark = theme.palette.mode === "dark";

  const handleUserMenuOpen = (event: MouseEvent<HTMLButtonElement>) => {
    setUserMenuAnchor(event.currentTarget);
  };

  const handleUserMenuClose = () => {
    setUserMenuAnchor(null);
  };

  const handleLogout = async () => {
    try {
      await api.post(`${SERVICE_ENDPOINTS.AUTH}/logout`);
      localStorage.clear();
      navigate("/");
    } catch (error: unknown) {
      console.log("Failed logging out.");
      console.error("Error: ", error);
    }
  };

  const handleNavMenuOpen = (
    event: MouseEvent<HTMLElement>,
    menuId: string
  ) => {
    setNavMenuAnchor(event.currentTarget);
    setNavMenuId(menuId);
  };

  const handleNavMenuClose = () => {
    setNavMenuAnchor(null);
    setNavMenuId(null);
  };

  const isActive = (path?: string) => {
    if (!path) return false;
    return path === "/"
      ? location.pathname === "/"
      : location.pathname.startsWith(path);
  };

  const isNavGroupActive = (item: NavItem) => {
    if (item.children) {
      return item.children.some((subItem) => isActive(subItem.path));
    }
    return false;
  };

  return (
    <AppBar
      position="sticky"
      color="primary"
      elevation={2}
      sx={{ height: 64, justifyContent: "center" }}
    >
      <Toolbar
        sx={{
          justifyContent: "space-between",
          gap: 2,
        }}
      >
        <Box sx={{ display: "flex", alignItems: "center", gap: 1, mr: 4 }}>
          <img
            src="/HemaLabManager - Logo.png"
            alt="HemaLab Logo"
            style={{
              width: "48px",
              height: "48px",
              objectFit: "contain",
              filter: "drop-shadow(0 0 2px #fff)",
            }}
          />
          <Typography
            variant="h6"
            component={"div"}
            sx={{ cursor: "pointer", fontWeight: "bold" }}
            onClick={() => navigate("/")}
            noWrap
          >
            HemaLab
          </Typography>
        </Box>

        {isLoggedIn && (
          <Box
            sx={{ flexGrow: 1, display: "flex", alignItems: "center", gap: 2 }}
          >
            <Box sx={{ display: "flex", gap: 1 }}>
              {ALL_NAV_ITEMS.map((item) => {
                const Icon = item.icon;
                const isGroupActive = isNavGroupActive(item);
                const active = isActive(item.path) || isGroupActive;

                if (item.children) {
                  return (
                    <Box key={item.id} onMouseLeave={handleNavMenuClose}>
                      <Button
                        startIcon={<Icon />}
                        endIcon={<KeyboardArrowDownIcon />}
                        onMouseEnter={(e) => handleNavMenuOpen(e, item.id)}
                        onClick={() => navigate(item.path || "#")}
                        variant={active ? "contained" : "text"}
                        color={active ? "secondary" : "inherit"}
                        sx={{ textTransform: "none", px: 1.5 }}
                      >
                        {item.label}
                      </Button>

                      <Menu
                        anchorEl={navMenuAnchor}
                        open={navMenuId === item.id}
                        onClose={handleNavMenuClose}
                        MenuListProps={{
                          onMouseLeave: handleNavMenuClose,
                        }}
                        anchorOrigin={{
                          vertical: "bottom",
                          horizontal: "left",
                        }}
                        transformOrigin={{
                          vertical: "top",
                          horizontal: "left",
                        }}
                      >
                        {item.children.map((subItem) => {
                          const SubIcon = subItem.icon;
                          return (
                            <MenuItem
                              key={subItem.path}
                              onClick={() => {
                                navigate(subItem.path);
                                handleNavMenuClose();
                              }}
                              selected={isActive(subItem.path)}
                            >
                              <ListItemIcon>
                                <SubIcon fontSize="small" />
                              </ListItemIcon>
                              <Typography variant="body2">
                                {subItem.label}
                              </Typography>
                            </MenuItem>
                          );
                        })}
                      </Menu>
                    </Box>
                  );
                }

                return (
                  <Button
                    key={item.path}
                    startIcon={<Icon />}
                    onClick={() => navigate(item.path!)}
                    variant={active ? "contained" : "text"}
                    color={active ? "secondary" : "inherit"}
                    sx={{ textTransform: "none", px: 1.5 }}
                  >
                    {item.label}
                  </Button>
                );
              })}
            </Box>

            <TextField
              placeholder="Search patients, orders..."
              variant="outlined"
              size="small"
              sx={{
                width: 300,
                ml: "auto",
                bgcolor: "rgba(255, 255, 255, 0.2)",
              }}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <SearchIcon sx={{ color: "white" }} />
                  </InputAdornment>
                ),
                sx: {
                  borderRadius: 1,
                  height: 40,
                  color: "white",
                  "& .MuiOutlinedInput-notchedOutline": {
                    borderColor: "rgba(255, 255, 255, 0.5)",
                  },
                  "&:hover .MuiOutlinedInput-notchedOutline": {
                    borderColor: "white",
                  },
                },
              }}
            />
          </Box>
        )}

        <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
          {onToggleTheme && (
            <Tooltip title={`Toggle ${isDark ? "Light" : "Dark"} Mode`}>
              <IconButton
                onClick={onToggleTheme}
                color="inherit"
                sx={{ border: 1, borderColor: "rgba(255, 255, 255, 0.5)" }}
                aria-label="Toggle theme"
              >
                {isDark ? <LightModeIcon /> : <DarkModeIcon />}
              </IconButton>
            </Tooltip>
          )}

          {isLoggedIn ? (
            <>
              <Button
                onClick={handleUserMenuOpen}
                variant="outlined"
                color="inherit"
                endIcon={<AccountCircleIcon />}
                sx={{ textTransform: "none", gap: 1, ml: 1 }}
              >
                <Box
                  sx={{
                    textAlign: "right",
                    display: { xs: "none", sm: "block" },
                  }}
                >
                  <Typography
                    variant="body2"
                    sx={{ fontWeight: 500, lineHeight: 1.2 }}
                  >
                    {username}
                  </Typography>
                  <Typography
                    variant="caption"
                    sx={{ color: "rgba(255,255,255, 0.7)", lineHeight: 1 }}
                  >
                    {userRole}
                  </Typography>
                </Box>
              </Button>

              <Menu
                anchorEl={userMenuAnchor}
                open={Boolean(userMenuAnchor)}
                onClose={handleUserMenuClose}
                anchorOrigin={{ vertical: "bottom", horizontal: "right" }}
                transformOrigin={{ vertical: "top", horizontal: "right" }}
                slotProps={{ paper: { sx: { minWidth: 200, mt: 1 } } }}
              >
                <Box sx={{ p: 1 }}>
                  <Typography
                    variant="subtitle1"
                    sx={{ px: 1, fontWeight: 600 }}
                  >
                    {username}
                  </Typography>
                  <Typography
                    variant="caption"
                    color="text.secondary"
                    sx={{ px: 1, display: "block", mb: 1 }}
                  >
                    {userRole}
                  </Typography>
                </Box>
                <Divider sx={{ my: 0.5 }} />
                {/* <MenuItem onClick={handleProfileClick}>
                  <SettingsIcon fontSize="small" sx={{ mr: 1 }} />
                  Profile Settings
                </MenuItem>
                <MenuItem onClick={handlePreferencesClick}>
                  <SettingsIcon fontSize="small" sx={{ mr: 1 }} />
                  Preferences
                </MenuItem> */}
                {/* <Divider sx={{ my: 0.5 }} /> */}
                <MenuItem
                  onClick={handleLogout}
                  sx={{ color: theme.palette.error.main }}
                >
                  <LogoutIcon fontSize="small" sx={{ mr: 1 }} />
                  Logout
                </MenuItem>
              </Menu>
            </>
          ) : (
            <Button
              variant="outlined"
              color="inherit"
              onClick={() => navigate("/login")}
              startIcon={<LoginIcon />}
            >
              Login
            </Button>
          )}
        </Box>
      </Toolbar>
    </AppBar>
  );
}
