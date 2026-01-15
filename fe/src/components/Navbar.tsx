// src/components/Navbar.tsx

import { Box, Button, Menu, MenuItem } from "@mui/material";
import { useLocation, useNavigate } from "react-router-dom";
import { useState } from "react";
import type { MouseEvent } from "react";
import ArrowDropDownIcon from "@mui/icons-material/ArrowDropDown";

const NAV_ITEMS = [
  { label: "Home", path: "/" },
  {
    label: "Test Orders",
    path: "/test-orders",
    children: [
      { label: "List", path: "/test-orders" },
      { label: "Dashboard", path: "/test-orders/dashboard" },
    ],
  },
  {
    label: "Patients",
    path: "/patients",
    children: [
      { label: "List", path: "/patients" },
      { label: "Dashboard", path: "/patients/dashboard" },
    ],
  },
  {
    label: "Instruments",
    path: "/instruments",
    children: [
      { label: "List", path: "/instruments" },
      { label: "Dashboard", path: "/instruments/dashboard" },
    ],
  },
  {
    label: "Warehouse",
    path: "/warehouse",
      children: [
        { label: "Instrument", path: "/warehouse/instruments" },
        { label: "Reagent", path: "/warehouse/reagents" },
        { label: "Configs", path: "/warehouse/configs" },
      ],
  },
];

export default function NavBar() {
  const navigate = useNavigate();
  const location = useLocation();

  const [openMenuIndex, setOpenMenuIndex] = useState<number | null>(null);
  const [anchors, setAnchors] = useState<(HTMLElement | null)[]>([]);
  const [hoverTimeout] = useState<number | null>(null);

  const handleMouseEnter = (index: number, event: MouseEvent<HTMLElement>) => {
    if (hoverTimeout) clearTimeout(hoverTimeout);
    setAnchors((prev) => {
      const copy = [...prev];
      copy[index] = event.currentTarget;
      return copy;
    });
    setOpenMenuIndex(index);
  };

  return (
      <Box
        sx={{
          width: "100%",
          display: "flex",
          gap: 2,
          px: 3,
          py: 1.5,
          backgroundColor: "#fff",borderBottom: "1px solid #dd",
        }}
      >
        {NAV_ITEMS.map((item, index) => {
          const isActive =
            item.path === "/"
              ? location.pathname === "/"
              : location.pathname.startsWith(item.path);

          if (!item.children) {
            return (
              <Button
                key={item.path}
                onClick={() => navigate(item.path)}
                sx={{
                  fontWeight: isActive ? "bold" : "normal",
                  color: isActive ? "primary.main" : "text.primary",
                  borderBottom: isActive
                    ? "2px solid #181818ff"
                    : "2px solid transparent",
                  borderRadius: 0,
                  "&:hover": {
                    backgroundColor: "transparent",
                    borderBottom: "2px solid #111",
                    borderColor: "primary.main",
                  },
                }}
              >
                {item.label}
              </Button>
            );
          }

          return (
            <Box
              key={item.path}
              sx={{ position: "relative" }}
              onMouseEnter={(e) => handleMouseEnter(index, e)}
              onMouseLeave={() => setOpenMenuIndex(null)}
            >
              <Button
                onClick={() => navigate(item.path)}
                endIcon={<ArrowDropDownIcon fontSize="small" />}
                sx={{
                  fontWeight: isActive ? "bold" : "normal",
                  color: isActive ? "primary.main" : "text.primary",
                  borderBottom: isActive
                    ? "2px solid #181818ff"
                    : "2px solid transparent",
                  borderRadius: 0,
                  "&:hover": {
                    backgroundColor: "transparent",
                    borderBottom: "2px solid #111",
                    borderColor: "primary.main",
                  },
                }}
              >
                {item.label}
              </Button>

              <Menu
                anchorEl={anchors[index]}
                open={openMenuIndex === index}
                onClose={() => setOpenMenuIndex(null)}
                MenuListProps={{
                  onMouseLeave: () => setOpenMenuIndex(null),
                }}
                anchorOrigin={{
                  vertical: "bottom",
                  horizontal: "left",
                }}
                transformOrigin={{
                  vertical: "top",
                  horizontal: "left",
                }}
                sx={{ mt: 0.5 }}
              >
                {item.children.map((child) => (
                  <MenuItem
                    key={child.path}
                    onClick={() => {
                      navigate(child.path);
                      setOpenMenuIndex(null);
                    }}
                    sx={{
                      "&:hover": { backgroundColor: "grey.100" },
                    }}
                  >
                    {child.label}
                  </MenuItem>
                ))}
              </Menu>
            </Box>
          );
        })}
      </Box>
  );
}
