// src/layouts/FilterLayout.tsx
// Intended to be used for Lab User, Doctors where they need filters to search for stuffs (Different from AdminLayout/DashboardLayout)

import { Box, Divider, Grid, Paper, Typography } from "@mui/material";
import { Outlet } from "react-router-dom";

interface FilterLayoutProps {
  filterPanel: React.ReactNode;
  pageTitle?: string;
  resultCount?: number | string;
  minContentHeight?: string;
}

export default function FilterLayout({
  filterPanel,
  pageTitle = "Search Results",
  resultCount = 0,
  minContentHeight = "80vh",
}: FilterLayoutProps) {
  return (
    <Box sx={{ flexGrow: 1, p: 3 }}>
      <Grid container spacing={3}>
        {/* Filter Panel */}
        <Grid size={{ xs:12, sm:12, md:4, lg:3 }}>
          <Paper
            sx={{
              p: 2,
              height: "100%",
              minHeight: minContentHeight,
              bgcolor: "background.paper",
              boxShadow: 2,
            }}
          >
            <Typography variant="h6" sx={{ fontWeight: 600 }}>
              Filter Options
            </Typography>
            <Divider sx={{ my: 2 }} />

            <Box>{filterPanel}</Box>
          </Paper>
        </Grid>

        {/* Results */}
        <Grid size={{ xs:12, sm:12, md:8, lg:9 }}>
          <Box
            display="flex"
            justifyContent="space-between"
            alignItems="center"
            mb={3}
            p={1}
            sx={{ borderBottom: 1, borderColor: "divider" }}
          >
            <Typography variant="h5" sx={{ fontWeight: 500 }}>
              {pageTitle}
              <Typography
                component="span"
                variant="h5"
                color="primary"
                sx={{ ml: 1, fontWeight: 600 }}
              >
                ({resultCount} results)
              </Typography>
            </Typography>

            <Box display="flex" gap={1}>
              <Typography
                variant="body2"
                color="text.secondary"
                sx={{ alignSelf: "center" }}
              >
                Sort/Display Options
              </Typography>
            </Box>
          </Box>

          <Box sx={{ minHeight: minContentHeight }}>
            <Outlet />
          </Box>
        </Grid>
      </Grid>
    </Box>
  );
}
