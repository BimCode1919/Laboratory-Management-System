import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  Box,
} from "@mui/material";
import { useState } from "react";
import { useThemeContext } from "@/utils/ThemeContext";

export const ThemeCustomizerModal = ({
  open,
  onClose,
}: {
  open: boolean;
  onClose: () => void;
}) => {
  const { setCustomColors } = useThemeContext();
  const [primary, setPrimary] = useState("#0d68c4");
  const [background, setBackground] = useState("#f5f7fa");

  const handleApply = () => {
    setCustomColors({ primary, background });
    onClose();
  };

  return (
    <Dialog open={open} onClose={onClose}>
      <DialogTitle>Customize Theme</DialogTitle>
      <DialogContent>
        <Box sx={{ display: "flex", flexDirection: "column", gap: 2, mt: 1 }}>
          <TextField
            label="Primary Color"
            type="color"
            value={primary}
            onChange={(e) => setPrimary(e.target.value)}
            fullWidth
          />
          <TextField
            label="Background Color"
            type="color"
            value={background}
            onChange={(e) => setBackground(e.target.value)}
            fullWidth
          />
        </Box>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Cancel</Button>
        <Button onClick={handleApply} variant="contained">
          Apply
        </Button>
      </DialogActions>
    </Dialog>
  );
};
