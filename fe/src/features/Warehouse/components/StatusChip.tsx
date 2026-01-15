// src/features/Warehouse/components/StatusChip.tsx

import { Chip, type SxProps, type Theme } from "@mui/material";

interface StatusChipProps {
  label: string;
  status?: string;
  sx?: SxProps<Theme>;
  size?: "small" | "medium";
}

const StatusChip: React.FC<StatusChipProps> = ({
  label,
  status,
  sx,
  size = "small",
}) => {
  // Optional: dynamic color based on status
  let dynamicStyles: SxProps<Theme> = {};
  if (status) {
    const s = status.toLowerCase();
    if (s === "ready") {
      dynamicStyles = {
        background: "linear-gradient(135deg, #a8e6a1 0%, #4caf50 100%)",
        color: "white",
      };
    } else if (s === "maintenance") {
      dynamicStyles = {
        background: "linear-gradient(135deg, #fbc2eb 0%, #a6c1ee 100%)",
        color: "white",
      };
    } else if (s === "offline") {
      dynamicStyles = {
        background: "linear-gradient(135deg, #a1c4fd 0%, #c2e9fb 100%)",
        color: "white",
      };
    }
  }

  return (
    <Chip
      label={label}
      size={size}
      sx={{
        fontWeight: 600,
        ...dynamicStyles,
        ...sx,
      }}
    />
  );
};

export default StatusChip;

{/* <Chip
                          size="small"
                          label={(inst.status || "—").toString().toUpperCase()}
                          sx={{
                            fontWeight: 600,
                            // compare case-insensitively
                            ...(String(inst.status || "").toLowerCase() === "ready" && {
                              background: 'linear-gradient(135deg, #a8e6a1 0%, #4caf50 100%)',
                              color: 'white'
                            }),
                            ...(String(inst.status || "").toLowerCase() === "maintenance" && {
                              background: 'linear-gradient(135deg, #fbc2eb 0%, #a6c1ee 100%)',
                              color: 'white'
                            }),
                            ...(String(inst.status || "").toLowerCase() === "offline" && {
                              background: 'linear-gradient(135deg, #a1c4fd 0%, #c2e9fb 100%)',
                              color: 'white'
                            })
                          }}
                        /> */}