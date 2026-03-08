import React, { useEffect, useState } from 'react';
import { Box, Grid, Paper, Typography, Button, Card, CardContent } from '@mui/material';
import { LineChart, Line, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import axios from 'axios';

const API_URL = 'http://localhost:8080';

export default function DashboardPage() {
  const [dashboardData, setDashboardData] = useState({
    activeCampaigns: 0,
    totalLeads: 0,
    successfulCalls: 0,
    successRate: 0,
    metrics: []
  });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchDashboardData();
  }, []);

  const fetchDashboardData = async () => {
    try {
      const token = localStorage.getItem('accessToken');
      const headers = { Authorization: `Bearer ${token}` };

      const campaigns = await axios.get(`${API_URL}/api/v1/campaigns`, { headers });
      setDashboardData(prev => ({
        ...prev,
        activeCampaigns: campaigns.data.data.length
      }));
    } catch (error) {
      console.error('Failed to fetch dashboard data:', error);
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return <Typography>Loading dashboard...</Typography>;
  }

  const mockMetrics = [
    { date: 'Jan 1', calls: 45, successful: 32 },
    { date: 'Jan 2', calls: 52, successful: 38 },
    { date: 'Jan 3', calls: 48, successful: 35 },
    { date: 'Jan 4', calls: 61, successful: 45 },
    { date: 'Jan 5', calls: 55, successful: 40 },
    { date: 'Jan 6', calls: 67, successful: 49 },
    { date: 'Jan 7', calls: 72, successful: 53 }
  ];

  return (
    <Box sx={{ padding: 3 }}>
      <Typography variant="h4" sx={{ marginBottom: 3 }}>
        Dashboard
      </Typography>

      <Grid container spacing={3} sx={{ marginBottom: 4 }}>
        <Grid item xs={12} sm={6} md={3}>
          <Paper elevation={2} sx={{ padding: 2, textAlign: 'center' }}>
            <Typography color="textSecondary" gutterBottom>
              Active Campaigns
            </Typography>
            <Typography variant="h4">{dashboardData.activeCampaigns}</Typography>
          </Paper>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Paper elevation={2} sx={{ padding: 2, textAlign: 'center' }}>
            <Typography color="textSecondary" gutterBottom>
              Total Leads
            </Typography>
            <Typography variant="h4">{dashboardData.totalLeads}</Typography>
          </Paper>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Paper elevation={2} sx={{ padding: 2, textAlign: 'center' }}>
            <Typography color="textSecondary" gutterBottom>
              Successful Calls
            </Typography>
            <Typography variant="h4">{dashboardData.successfulCalls}</Typography>
          </Paper>
        </Grid>
        <Grid item xs={12} sm={6} md={3}>
          <Paper elevation={2} sx={{ padding: 2, textAlign: 'center' }}>
            <Typography color="textSecondary" gutterBottom>
              Success Rate
            </Typography>
            <Typography variant="h4">{dashboardData.successRate}%</Typography>
          </Paper>
        </Grid>
      </Grid>

      <Grid container spacing={3}>
        <Grid item xs={12} md={6}>
          <Paper elevation={2} sx={{ padding: 2 }}>
            <Typography variant="h6" gutterBottom>Call Volume</Typography>
            <ResponsiveContainer width="100%" height={300}>
              <LineChart data={mockMetrics}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="date" />
                <YAxis />
                <Tooltip />
                <Legend />
                <Line type="monotone" dataKey="calls" stroke="#8884d8" />
              </LineChart>
            </ResponsiveContainer>
          </Paper>
        </Grid>

        <Grid item xs={12} md={6}>
          <Paper elevation={2} sx={{ padding: 2 }}>
            <Typography variant="h6" gutterBottom>Call Success Rate</Typography>
            <ResponsiveContainer width="100%" height={300}>
              <BarChart data={mockMetrics}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="date" />
                <YAxis />
                <Tooltip />
                <Legend />
                <Bar dataKey="successful" fill="#82ca9d" />
              </BarChart>
            </ResponsiveContainer>
          </Paper>
        </Grid>
      </Grid>

      <Box sx={{ marginTop: 3 }}>
        <Button variant="contained" color="primary" sx={{ marginRight: 1 }}>
          View Campaigns
        </Button>
        <Button variant="outlined" color="primary">
          View Leads
        </Button>
      </Box>
    </Box>
  );
}
