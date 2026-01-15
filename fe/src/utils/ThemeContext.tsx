// src/utils/ThemeContext.tsx

import { createContext, useContext, useState, useMemo, type ReactNode } from "react";
import { ThemeProvider as MuiThemeProvider, createTheme } from "@mui/material/styles";

type ThemeMode = "light" | "dark" | "custom";

interface ThemeContextType {
  mode: ThemeMode;
  toggleMode: () => void;
  setCustomColors: (colors: { primary: string; background: string }) => void;
}

const ThemeContext = createContext<ThemeContextType | undefined>(undefined);

export const useThemeContext = () => {
  const ctx = useContext(ThemeContext);
  if (!ctx) throw new Error("useThemeContext must be used within ThemeProvider");
  return ctx;
};

export const ThemeProvider = ({ children }: { children: ReactNode }) => {
  const [mode, setMode] = useState<ThemeMode>("light");
  const [customColors, setCustomColorsState] = useState({
    primary: "#0d68c4",
    background: "#f5f7fa",
  });

  const toggleMode = () => {
    setMode((prev) => (prev === "light" ? "dark" : "light"));
  };

  const setCustomColors = (colors: { primary: string; background: string }) => {
    setCustomColorsState(colors);
    setMode("custom");
  };

  const theme = useMemo(() => {
    if (mode === "custom") {
      return createTheme({
        palette: {
          mode: "light",
          primary: { main: customColors.primary },
          background: { default: customColors.background, paper: customColors.background },
        },
      });
    }
    return createTheme({ palette: { mode } });
  }, [mode, customColors]);

  return (
    <ThemeContext.Provider value={{ mode, toggleMode, setCustomColors }}>
      <MuiThemeProvider theme={theme}>{children}</MuiThemeProvider>
    </ThemeContext.Provider>
  );
};
