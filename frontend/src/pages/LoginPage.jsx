import React, { useState } from 'react';
import { Box, Button, TextField, Container, Paper, Typography, Alert } from '@mui/material';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';

export default function LoginPage({ setIsAuthenticated }) {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handleLogin = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      const response = await axios.post('http://localhost:8080/api/v1/auth/login', {
        email,
        password
      });

      if (response.data.success) {
        localStorage.setItem('accessToken', response.data.data.accessToken);
        localStorage.setItem('refreshToken', response.data.data.refreshToken);
        setIsAuthenticated(true);
        navigate('/');
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Login failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <Container maxWidth="sm">
      <Box sx={{ display: 'flex', justifyContent: 'center', minHeight: '100vh', alignItems: 'center' }}>
        <Paper elevation={3} sx={{ padding: 4, width: '100%' }}>
          <Typography variant="h4" component="h1" sx={{ marginBottom: 3, textAlign: 'center' }}>
            DialGenie
          </Typography>
          <Typography variant="body1" sx={{ marginBottom: 3, textAlign: 'center', color: 'gray' }}>
            AI-Powered Telecalling Platform
          </Typography>

          {error && <Alert severity="error" sx={{ marginBottom: 2 }}>{error}</Alert>}

          <form onSubmit={handleLogin}>
            <TextField
              fullWidth
              label="Email"
              type="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              margin="normal"
              required
            />
            <TextField
              fullWidth
              label="Password"
              type="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              margin="normal"
              required
            />
            <Button
              fullWidth
              variant="contained"
              color="primary"
              sx={{ marginTop: 3 }}
              type="submit"
              disabled={loading}
            >
              {loading ? 'Logging in...' : 'Log In'}
            </Button>
          </form>

          <Typography variant="body2" sx={{ marginTop: 2, textAlign: 'center' }}>
            Demo credentials: admin@dialgenie.com / password123
          </Typography>
        </Paper>
      </Box>
    </Container>
  );
}
