import React from 'react';
import { AppBar, Toolbar, Typography, Menu, MenuItem, Box, Button } from '@mui/material';
import { Link, useNavigate } from 'react-router-dom';

export default function Navigation() {
  const navigate = useNavigate();
  const [anchorEl, setAnchorEl] = React.useState(null);

  const handleLogout = () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    navigate('/login');
  };

  return (
    <AppBar position="static" sx={{ marginBottom: 3 }}>
      <Toolbar>
        <Typography variant="h6" sx={{ flexGrow: 1, fontWeight: 'bold' }}>
          DialGenie
        </Typography>
        <Box sx={{ display: 'flex', gap: 2 }}>
          <Button color="inherit" component={Link} to="/">Dashboard</Button>
          <Button color="inherit" component={Link} to="/campaigns">Campaigns</Button>
          <Button color="inherit" component={Link} to="/leads">Leads</Button>
          <Button color="inherit" component={Link} to="/reports">Reports</Button>
          <Button color="inherit" onClick={handleLogout}>Logout</Button>
        </Box>
      </Toolbar>
    </AppBar>
  );
}
