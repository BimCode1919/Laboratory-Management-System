// src/types/HeaderRouting.ts

import type { SvgIconComponent } from "@mui/icons-material";
import AssignmentIcon from "@mui/icons-material/Assignment";
import PeopleIcon from "@mui/icons-material/People";
import ScienceIcon from "@mui/icons-material/Science";
import InventoryIcon from "@mui/icons-material/Inventory";
import BiotechIcon from "@mui/icons-material/Biotech";
import ListIcon from "@mui/icons-material/List";
import BarChartIcon from "@mui/icons-material/BarChart";
import SettingsIcon from "@mui/icons-material/Settings";
import DashboardIcon from "@mui/icons-material/Dashboard";
import AnalyticsIcon from "@mui/icons-material/Analytics";
import PrecisionManufacturingIcon from "@mui/icons-material/PrecisionManufacturing";
import SyncIcon from "@mui/icons-material/Sync";
import PersonIcon from "@mui/icons-material/Person";

export interface NavItem {
  id: string;
  label: string;
  path?: string;
  icon: SvgIconComponent;
  children?: NavItemChild[];
}

export interface NavItemChild {
  label: string;
  path: string;
  icon: SvgIconComponent;
}

export interface SidebarItem {
  label: string;
  path?: string;
  icon: SvgIconComponent;
  children?: NavItemChild[];
}

export interface UserData {
  isLoggedIn: boolean;
  username: string;
  userRole: string;
}

export const ALL_NAV_ITEMS: NavItem[] = [
  {
    id: "test-orders",
    label: "Test Orders",
    icon: AssignmentIcon,
    children: [{ path: "/test-orders", label: "Order List", icon: ListIcon }],
  },
  {
    id: "patients",
    label: "Patients",
    icon: PeopleIcon,
    children: [
      { path: "/patients/list", label: "Patient List", icon: ListIcon },
      { path: "/patients/me", label: "Personal Profile", icon: PersonIcon },
    ],
  },
  {
    id: "instruments",
    label: "Test Execution",
    icon: PrecisionManufacturingIcon,
    children: [
      { path: "/instruments", label: "Sync Instruments", icon: SyncIcon },
      {
        path: "/instruments/dashboard",
        label: "Test Execution",
        icon: BarChartIcon,
      },
    ],
  },
  {
    id: "warehouse",
    label: "Warehouse",
    icon: InventoryIcon,
    children: [
      { path: "/warehouse/reagents", label: "Reagents", icon: ScienceIcon },
      {
        path: "/warehouse/instruments",
        label: "Instruments",
        icon: BiotechIcon,
      },
      { path: "/warehouse/configs", label: "Configs", icon: SettingsIcon },
    ],
  },
];

export const ADMIN_SIDEBAR_ITEMS: SidebarItem[] = [
  { path: "/admin", label: "Dashboard", icon: DashboardIcon },
  { path: "/admin/users", label: "User Management", icon: PeopleIcon },
  { path: "/admin/roles", label: "Role Management", icon: AssignmentIcon },

  { path: "/test-orders", label: "Test Orders", icon: AssignmentIcon },
  { path: "/patients/list", label: "Patient Records", icon: PeopleIcon },

  {
    label: "Test Execution",
    icon: PrecisionManufacturingIcon,
    children: [
      { path: "/instruments/", label: "Sync Instrument", icon: SyncIcon },
      {
        path: "/instruments/dashboard",
        label: "Test Execution",
        icon: BarChartIcon,
      },
    ],
  },

  {
    label: "Inventory Management",
    icon: InventoryIcon,
    children: [
      {
        path: "/warehouse/instruments",
        label: "Instrument Inventory",
        icon: BiotechIcon,
      },
      {
        path: "/warehouse/reagents",
        label: "Reagent Inventory",
        icon: ScienceIcon,
      },
      {
        path: "/warehouse/configs",
        label: "Instrument Configs",
        icon: SettingsIcon,
      },
    ],
  },

  {
    label: "Monitoring & Logs",
    path: "/monitoring",
    icon: AnalyticsIcon,
    children: [
      { path: "/monitoring/event-logs", label: "Event Logs", icon: ListIcon },
      {
        path: "/monitoring/message-broker-health",
        label: "Broker Health",
        icon: BarChartIcon,
      },
      // {
      //   path: "/monitoring/sync-up-requests",
      //   label: "Sync-Up Requests",
      //   icon: ListIcon,
      // },
      { path: "/monitoring/hl7-backups", label: "HL7 Backups", icon: ListIcon },
      {
        path: "/monitoring/health-event-logs",
        label: "Health Event Logs",
        icon: ListIcon,
      },
    ],
  },

  // { path: "/admin/configuration", label: "System Configuration", icon: SettingsIcon },
];
