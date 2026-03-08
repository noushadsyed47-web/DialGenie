import React, { useState } from 'react';
import { Box, Typography, TextField, Button, Paper, List, ListItem, ListItemText, Chip, Grid } from '@mui/material';

export default function CampaignsPage() {
  const [campaigns, setCampaigns] = useState([]);
  const [newCampaign, setNewCampaign] = useState({ name: '', description: '' });

  const handleCreateCampaign = () => {
    if (newCampaign.name) {
      setCampaigns([...campaigns, { id: Date.now(), ...newCampaign, status: 'DRAFT', createdAt: new Date() }]);
      setNewCampaign({ name: '', description: '' });
    }
  };

  return (
    <Box sx={{ padding: 3 }}>
      <Typography variant="h4" sx={{ marginBottom: 3 }}>
        Campaigns
      </Typography>

      <Grid container spacing={3}>
        <Grid item xs={12} md={4}>
          <Paper elevation={2} sx={{ padding: 2 }}>
            <Typography variant="h6" sx={{ marginBottom: 2 }}>Create New Campaign</Typography>
            <TextField
              fullWidth
              label="Campaign Name"
              value={newCampaign.name}
              onChange={(e) => setNewCampaign({ ...newCampaign, name: e.target.value })}
              margin="normal"
            />
            <TextField
              fullWidth
              label="Description"
              value={newCampaign.description}
              onChange={(e) => setNewCampaign({ ...newCampaign, description: e.target.value })}
              margin="normal"
              multiline
              rows={3}
            />
            <Button
              fullWidth
              variant="contained"
              color="primary"
              sx={{ marginTop: 2 }}
              onClick={handleCreateCampaign}
            >
              Create Campaign
            </Button>
          </Paper>
        </Grid>

        <Grid item xs={12} md={8}>
          <Paper elevation={2} sx={{ padding: 2 }}>
            <Typography variant="h6" sx={{ marginBottom: 2 }}>Your Campaigns</Typography>
            {campaigns.length === 0 ? (
              <Typography color="textSecondary">No campaigns yet. Create one to get started!</Typography>
            ) : (
              <List>
                {campaigns.map(campaign => (
                  <ListItem key={campaign.id} sx={{ borderBottom: '1px solid #eee' }}>
                    <ListItemText
                      primary={campaign.name}
                      secondary={campaign.description}
                    />
                    <Chip label={campaign.status} color="primary" size="small" />
                  </ListItem>
                ))}
              </List>
            )}
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
}
