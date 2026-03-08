import React, { useState } from 'react';
import { Box, Typography, Paper, Button, Table, TableBody, TableCell, TableContainer, TableHead, TableRow } from '@mui/material';
import { useDropzone } from 'react-dropzone';

export default function LeadsPage() {
  const [leads, setLeads] = useState([]);
  const [uploadedFile, setUploadedFile] = useState(null);

  const onDrop = (acceptedFiles) => {
    if (acceptedFiles.length > 0) {
      const file = acceptedFiles[0];
      setUploadedFile(file);
      // Parse Excel file using xlsx library
      const reader = new FileReader();
      reader.onload = (e) => {
        const data = new Uint8Array(e.target.result);
        // In production, use xlsx library to parse
        console.log("File ready for processing:", file.name);
      };
      reader.readAsArrayBuffer(file);
    }
  };

  const { getRootProps, getInputProps, isDragActive } = useDropzone({ onDrop });

  return (
    <Box sx={{ padding: 3 }}>
      <Typography variant="h4" sx={{ marginBottom: 3 }}>
        Leads
      </Typography>

      <Paper elevation={2} sx={{ padding: 3, marginBottom: 3 }}>
        <Typography variant="h6" sx={{ marginBottom: 2 }}>Upload Leads from Excel</Typography>
        <Box
          {...getRootProps()}
          sx={{
            border: '2px dashed #ccc',
            borderRadius: 1,
            padding: 3,
            textAlign: 'center',
            cursor: 'pointer',
            backgroundColor: isDragActive ? '#f0f0f0' : 'white'
          }}
        >
          <input {...getInputProps()} />
          {isDragActive ? (
            <Typography>Drop the Excel file here...</Typography>
          ) : (
            <>
              <Typography>Drag and drop an Excel file here, or click to select</Typography>
              <Typography color="textSecondary" variant="body2">
                Expected columns: Name, Phone Number, Email, Concern
              </Typography>
            </>
          )}
        </Box>
        {uploadedFile && (
          <Box sx={{ marginTop: 2 }}>
            <Typography variant="body2">Selected file: {uploadedFile.name}</Typography>
            <Button variant="contained" color="primary" sx={{ marginTop: 1 }}>
              Upload &amp; Process
            </Button>
          </Box>
        )}
      </Paper>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow sx={{ backgroundColor: '#f5f5f5' }}>
              <TableCell>Name</TableCell>
              <TableCell>Phone</TableCell>
              <TableCell>Email</TableCell>
              <TableCell>Concern</TableCell>
              <TableCell>Status</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {leads.length === 0 ? (
              <TableRow>
                <TableCell colSpan={5} sx={{ textAlign: 'center', padding: 3 }}>
                  No leads uploaded yet
                </TableCell>
              </TableRow>
            ) : (
              leads.map((lead) => (
                <TableRow key={lead.id}>
                  <TableCell>{lead.name}</TableCell>
                  <TableCell>{lead.phone}</TableCell>
                  <TableCell>{lead.email}</TableCell>
                  <TableCell>{lead.concern}</TableCell>
                  <TableCell>{lead.status}</TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>
    </Box>
  );
}
