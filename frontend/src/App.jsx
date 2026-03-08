import React, { useEffect, useState } from 'react';
import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { Box, Container } from '@mui/material';
import LoginPage from './pages/LoginPage';
import DashboardPage from './pages/DashboardPage';
import CampaignsPage from './pages/CampaignsPage';
import LeadsPage from './pages/LeadsPage';
import ReportsPage from './pages/ReportsPage';
import Navigation from './components/Navigation';

function App() {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Check authentication status
    const token = localStorage.getItem('accessToken');
    if (token) {
      setIsAuthenticated(true);
    }
    setLoading(false);
  }, []);

  if (loading) {
    return <div>Loading...</div>;
  }

  return (
    <Router>
      <Box>
        {isAuthenticated && <Navigation />}
        <Container>
          <Routes>
            <Route path="/login" element={<LoginPage setIsAuthenticated={setIsAuthenticated} />} />
            <Route
              path="/"
              element={isAuthenticated ? <DashboardPage /> : <Navigate to="/login" />}
            />
            <Route
              path="/campaigns"
              element={isAuthenticated ? <CampaignsPage /> : <Navigate to="/login" />}
            />
            <Route
              path="/leads"
              element={isAuthenticated ? <LeadsPage /> : <Navigate to="/login" />}
            />
            <Route
              path="/reports"
              element={isAuthenticated ? <ReportsPage /> : <Navigate to="/login" />}
            />
          </Routes>
        </Container>
      </Box>
    </Router>
  );
}

export default App;
