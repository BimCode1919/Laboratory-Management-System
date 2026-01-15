// src/components/DashboardSidebar.tsx

import React, { useState } from "react";
import {
  Box,
  Drawer,
  Typography,
  List,
  ListItem,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Tooltip,
  Collapse,
} from "@mui/material";
import LocalHospitalIcon from "@mui/icons-material/LocalHospital";
import {
  ADMIN_SIDEBAR_ITEMS,
  type NavItemChild,
  type SidebarItem,
} from "../types/HeaderRouting";
import ExpandLess from "@mui/icons-material/ExpandLess";
import ExpandMore from "@mui/icons-material/ExpandMore";

const DRAWER_WIDTH = 260;

interface DashboardSidebarProps {
  currentPage: string;
  onNavigate: (path: string) => void;
}

export default function DashboardSidebar({
  currentPage,
  onNavigate,
}: DashboardSidebarProps) {
  const [open, setOpen] = useState<{ [key: string]: boolean }>({});

  const isActive = (path: string) => currentPage === path;

  const handleClick = (label: string) => {
    setOpen((prev) => ({ ...prev, [label]: !prev[label] }));
  };

  const renderListItem = (
    item: SidebarItem | NavItemChild,
    isChild = false
  ) => {
    const Icon = item.icon;
    const active = isActive(item.path || "");

    const isCollapsible =
      "children" in item && item.children && item.children.length > 0;
    const itemKey = item.label;

    const handleAction = () => {
      if (isCollapsible) {
        handleClick(itemKey);
      } else if (item.path) {
        onNavigate(item.path);
      }
    };

    const sxProps = isChild
      ? {
          pl: 5,
          "&.Mui-selected": {
            bgcolor: "primary.light",
            borderLeft: "4px solid #0d68c4ff",
          },
          "&:hover": { bgcolor: "action.hover" },
        }
      : {
          borderRadius: 1,
          borderLeft: active ? "4px solid #0d68c4ff" : "4px solid transparent",
          transition: "border-left 0.2s ease, background-color 0.2s ease",

          "&.Mui-selected": {
            bgcolor: "primary.main",
            color: "primary.contrastText",
            "& .MuiListItemIcon-root": { color: "primary.contrastText" },
          },
          "&:hover": {
            bgcolor: active ? "primary.dark" : "action.hover",
            borderLeft: active ? "4px solid #0d63b9ff" : "4px solid #6bb1ebff",
          },
          "& .MuiListItemIcon-root": {
            color: active && !isChild ? "primary.main" : "text.secondary",
          },
        };

    return (
      <React.Fragment key={item.label}>
        <ListItem disablePadding sx={{ mb: isChild ? 0 : 0.5 }}>
          <Tooltip title={item.label} placement="right">
            <ListItemButton
              selected={active && !isCollapsible}
              onClick={handleAction}
              sx={sxProps}
            >
              <ListItemIcon
                sx={{
                  minWidth: 40,
                  color: active && !isChild ? "primary.main" : "text.secondary",
                }}
              >
                <Icon />
              </ListItemIcon>
              <ListItemText
                primary={item.label}
                primaryTypographyProps={{
                  fontSize: "0.875rem",
                  fontWeight: active && !isChild ? 600 : 400,
                }}
              />
              {isCollapsible ? (
                open[itemKey] ? (
                  <ExpandLess />
                ) : (
                  <ExpandMore />
                )
              ) : null}
            </ListItemButton>
          </Tooltip>
        </ListItem>

        {isCollapsible && (
          <Collapse in={open[itemKey]} timeout="auto" unmountOnExit>
            <List component="div" disablePadding>
              {item.children?.map((child) => renderListItem(child, true))}
            </List>
          </Collapse>
        )}
      </React.Fragment>
    );
  };

  return (
    <Drawer
      variant="permanent"
      anchor="left"
      sx={{
        width: DRAWER_WIDTH,
        flexShrink: 0,
        "& .MuiDrawer-paper": {
          width: DRAWER_WIDTH,
          boxSizing: "border-box",
          backgroundColor: "#f2f2f7ff",
          display: "flex",
          flexDirection: "column",
          height: "100vh",
          borderRight: 1,
          borderColor: "divider",
          boxShadow: "0px 2px 8px rgba(0, 0, 0, 0.05)",
        },
      }}
    >
      <Box
        sx={{
          p: 3,
          borderBottom: "1px solid",
          borderColor: "divider",
          display: "flex",
          alignItems: "center",
          bgcolor: "#0d68c4ff",
          gap: 1,
          minHeight: 32,
          color: "#ffffff",
        }}
      >
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

        <Typography variant="h6" fontWeight={700} noWrap>
          HemaLab
        </Typography>
      </Box>

      <Box sx={{ flexGrow: 1, overflowY: "auto", p: 2 }}>
        <List disablePadding>
          {ADMIN_SIDEBAR_ITEMS.map((item) => renderListItem(item))}
        </List>
      </Box>

      <Box sx={{ p: 2, borderTop: 1, borderColor: "divider" }}>
        <Typography variant="caption" color="text.secondary">
          v2.0.0 - Admin Panel
        </Typography>
      </Box>
    </Drawer>
  );
}
