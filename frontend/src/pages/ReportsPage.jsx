import React from 'react';
import { Box, Typography, Paper, Grid } from '@mui/material';

export default function ReportsPage() {
  return (
    <Box sx={{ padding: 3 }}>
      <Typography variant="h4" sx={{ marginBottom: 3 }}>
        Reports
      </Typography>

      <Grid container spacing={3}>
        <Grid item xs={12} md={6}>
          <Paper elevation={2} sx={{ padding: 2 }}>
            <Typography variant="h6" sx={{ marginBottom: 2 }}>Campaign Performance</Typography>
            <Typography color="textSecondary">
              Analytics and performance metrics for your campaigns will appear here.
            </Typography>
          </Paper>
        </Grid>

        <Grid item xs={12} md={6}>
          <Paper elevation={2} sx={{ padding: 2 }}>
            <Typography variant="h6" sx={{ marginBottom: 2 }}>Lead Analytics</Typography>
            <Typography color="textSecondary">
              Detailed insights about your leads and conversion metrics will appear here.
            </Typography>
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
}
