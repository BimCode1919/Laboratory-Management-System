// src/features/patients/pages/MedicalRecordDetailPage.tsx

import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import {
  Box, CircularProgress, Paper, Typography, Grid, TextField, Card, CardContent,
  List, ListItem, ListItemText, Button, Table, TableBody, TableCell, TableContainer,
  TableHead, TableRow, Chip, Divider, Stack, Accordion, AccordionSummary, AccordionDetails
} from "@mui/material";
import SearchIcon from '@mui/icons-material/Search';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import ScienceIcon from '@mui/icons-material/Science';
import BiotechIcon from '@mui/icons-material/Biotech';
import AssessmentIcon from '@mui/icons-material/Assessment';
import MemoryIcon from '@mui/icons-material/Memory';
import OpacityIcon from '@mui/icons-material/Opacity';
import PlaceIcon from '@mui/icons-material/Place';
import AssignmentIcon from '@mui/icons-material/Assignment';
import CalendarTodayIcon from '@mui/icons-material/CalendarToday';
import NoteIcon from '@mui/icons-material/Note';

import { getMedicalRecordDetail } from "../services/PatientService";
import type { MedicalRecordDetail } from "../types/Patient";

// --- HELPER FUNCTIONS & COMPONENTS ---

const formatParameterName = (name: string) => {
  if (!name) return "";
  return name
    .split('_')
    .map(word => word.charAt(0).toUpperCase() + word.slice(1).toLowerCase())
    .join(' ');
};

// Hàm lấy màu cho Badge trạng thái của Test Record
const getTestStatusColor = (status: string) => {
  switch (status) {
    case "PENDING":
      return "default"; // Màu xám
    case "PROCESSING":
      return "info";    // Màu xanh dương nhạt
    case "COMPLETED":
      return "success"; // Màu xanh lá
    case "REVIEWED":
      return "primary"; // Màu xanh đậm (hoặc tím tùy theme, biểu thị đã duyệt)
    case "CANCELLED":
      return "error";   // Màu đỏ
    default:
      return "default";
  }
};

// 1. Component: Test Results (Table View)
interface TestResultItem {
  parameterName: string;
  resultValue: number | string;
  unit: string;
  referenceLow?: number | null;
  referenceHigh?: number | null;
  alertLevel: "NORMAL" | "HIGH" | "LOW" | string;
}

const TestResultTable = ({ jsonString }: { jsonString: string | null }) => {
  if (!jsonString) return <Typography variant="body2" color="text.secondary">No result data available.</Typography>;

  let data: TestResultItem[] = [];
  try {
    data = JSON.parse(jsonString);
  } catch (e) {
    return <Typography color="error" variant="body2">Error parsing result data.</Typography>;
  }

  const getAlertColor = (level: string) => {
    switch (level) {
      case "HIGH": return "error";
      case "LOW": return "warning";
      case "NORMAL": return "success";
      default: return "default";
    }
  };

  return (
    <TableContainer component={Paper} variant="outlined" sx={{ mt: 1, mb: 3, border: '1px solid #e0e0e0' }}>
      <Table size="small">
        <TableHead sx={{ bgcolor: '#f1f8e9' }}>
          <TableRow>
            <TableCell><strong>Parameter</strong></TableCell>
            <TableCell align="center"><strong>Result</strong></TableCell>
            <TableCell align="center"><strong>Unit</strong></TableCell>
            <TableCell align="center"><strong>Ref. Range</strong></TableCell>
            <TableCell align="center"><strong>Flag</strong></TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {data.map((row, index) => (
            <TableRow key={index} hover>
              <TableCell sx={{ fontWeight: 'medium' }}>{formatParameterName(row.parameterName)}</TableCell>
              <TableCell align="center" sx={{ fontWeight: 'bold', color: row.alertLevel !== 'NORMAL' ? 'error.main' : 'text.primary' }}>
                {row.resultValue}
              </TableCell>
              <TableCell align="center">{row.unit}</TableCell>
              <TableCell align="center">
                {(row.referenceLow !== null && row.referenceHigh !== null) 
                  ? `${row.referenceLow} - ${row.referenceHigh}`
                  : 'N/A'}
              </TableCell>
              <TableCell align="center">
                <Chip 
                  label={row.alertLevel} 
                  color={getAlertColor(row.alertLevel) as any} 
                  size="small" 
                  variant={row.alertLevel === 'NORMAL' ? 'outlined' : 'filled'}
                  sx={{ minWidth: 80, fontWeight: 'bold' }}
                />
              </TableCell>
            </TableRow>
          ))}
        </TableBody>
      </Table>
    </TableContainer>
  );
};

// 2. Component: Instrument Information
const InstrumentInfo = ({ jsonString }: { jsonString: string | null }) => {
  if (!jsonString) return <Typography variant="caption">N/A</Typography>;
  let info: any = {};
  try { info = JSON.parse(jsonString); } catch (e) { return null; }

  return (
    <Paper variant="outlined" sx={{ p: 2, bgcolor: '#fafafa', height: '100%' }}>
        <Stack direction="row" alignItems="center" spacing={1} mb={1.5}>
            <MemoryIcon fontSize="small" color="info" />
            <Typography variant="subtitle2" fontWeight="bold" color="info.main">
                INSTRUMENT DETAILS
            </Typography>
        </Stack>
        <Divider sx={{ mb: 1.5 }} />
        <Stack spacing={1}>
            <Typography variant="body2">
                <strong>Model:</strong> {info.name} ({info.model})
            </Typography>
            <Typography variant="body2" component="div" sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
                <strong>Code:</strong> 
                <Chip label={info.instrumentCode} size="small" sx={{ height: 20, fontSize: '0.75rem' }} />
            </Typography>
            <Typography variant="body2">
                <strong>Serial:</strong> {info.serialNumber || 'N/A'}
            </Typography>
            <Typography variant="body2" component="div" sx={{ display: 'flex', alignItems: 'center', gap: 0.5 }}>
                <PlaceIcon fontSize="inherit" color="action"/> 
                <strong>Location:</strong> {info.location || 'N/A'}
            </Typography>
        </Stack>
    </Paper>
  );
};

// 3. Component: Reagent Information
const ReagentInfo = ({ jsonString }: { jsonString: string | null }) => {
    if (!jsonString) return <Typography variant="caption">N/A</Typography>;
    let data: any = {};
    try { data = JSON.parse(jsonString); } catch (e) { return null; }
  
    const reagents = Object.entries(data)
        .filter(([key, value]: [string, any]) => key !== 'count' && typeof value === 'object' && value?.name)
        .map(([_, value]) => value as any);
  
    return (
      <Paper variant="outlined" sx={{ p: 2, bgcolor: '#fffde7', borderColor: '#fff59d', height: '100%' }}>
         <Stack direction="row" alignItems="center" spacing={1} mb={1.5}>
            <OpacityIcon fontSize="small" color="warning" />
            <Typography variant="subtitle2" fontWeight="bold" color="warning.dark">
                REAGENTS USED
            </Typography>
        </Stack>
        <Divider sx={{ mb: 1.5, borderColor: '#fff59d' }} />
        
        {reagents.length > 0 ? (
            <List dense disablePadding>
                {reagents.map((r: any, idx: number) => (
                     <ListItem key={idx} disablePadding sx={{ mb: 1, display: 'block' }}>
                        <Typography variant="body2" fontWeight="bold" color="text.primary">
                             {r.name}
                        </Typography>
                        <Typography variant="caption" display="block" color="text.secondary">
                             Lot: {r.lot} | Remaining: {r.remaining} {r.unit}
                        </Typography>
                     </ListItem>
                ))}
            </List>
        ) : (
            <Typography variant="caption">No specific reagent info.</Typography>
        )}
      </Paper>
    );
  };

const InfoItem = ({ label, value }: { label: string; value: string | undefined | null }) => (
  <ListItem>
    <ListItemText 
      primary={value || 'N/A'} 
      secondary={label} 
      primaryTypographyProps={{ fontWeight: 'medium', color: 'text.primary' }}
      secondaryTypographyProps={{ color: 'text.secondary' }}
    />
  </ListItem>
);

// --- MAIN PAGE COMPONENT ---

export default function MedicalRecordDetailPage() {
  const { id } = useParams<{ id: string }>();
  const [record, setRecord] = useState<MedicalRecordDetail | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  
  const [instrumentInput, setInstrumentInput] = useState('');
  const [startDateInput, setStartDateInput] = useState('');
  const [endDateInput, setEndDateInput] = useState('');

  const [appliedInstrumentFilter, setAppliedInstrumentFilter] = useState('');
  const [appliedStartDate, setAppliedStartDate] = useState<string | undefined>(undefined);
  const [appliedEndDate, setAppliedEndDate] = useState<string | undefined>(undefined);

  useEffect(() => {
    if (!id) {
      setError("Invalid Record ID.");
      setIsLoading(false);
      return;
    }
    setIsLoading(true);
    
    const params: any = {
      instrumentUsed: appliedInstrumentFilter || undefined,
    };

    if (appliedStartDate) {
      params.startDate = new Date(appliedStartDate).toISOString();
    }

    if (appliedEndDate) {
      const end = new Date(appliedEndDate);
      end.setHours(23, 59, 59, 999);
      params.endDate = end.toISOString();
    }
    
    getMedicalRecordDetail(id, params)
      .then(data => {
        setRecord(data);
        setError(null);
      })
      .catch(err => setError(err.message))
      .finally(() => setIsLoading(false));
      
  }, [id, appliedInstrumentFilter, appliedStartDate, appliedEndDate]);

  const handleFilter = () => {
    setAppliedInstrumentFilter(instrumentInput);
    setAppliedStartDate(startDateInput);
    setAppliedEndDate(endDateInput);
  };

  if (isLoading) return <Box display="flex" justifyContent="center" mt={4}><CircularProgress /></Box>;
  if (error) return <Typography color="error" align="center" mt={4}>{error}</Typography>;
  if (!record) return <Typography align="center" mt={4}>Record not found.</Typography>;

  return (
    <Box>
      <Typography variant="h4" gutterBottom sx={{ fontWeight: 'bold', color: '#1565c0' }}>
        Medical Record Details
      </Typography>

      {/* Section: Patient Information */}
      <Paper elevation={1} sx={{ p: 2, mb: 3, borderRadius: 2, borderLeft: '6px solid #1565c0' }}>
        <Typography variant="h6" gutterBottom sx={{ pl: 1, color: '#1565c0' }}>
           Patient Info: <strong>{record.patient.fullName.toUpperCase()}</strong>
        </Typography>
        <Grid container>
          <Grid item xs={12} md={6}>
            <List dense>
              <InfoItem label="Patient ID" value={record.patient.patientCode} />
              <InfoItem label="Date of Birth" value={new Date(record.patient.dateOfBirth).toLocaleDateString('en-US')} />
              <InfoItem label="Email" value={record.patient.email} />
            </List>
          </Grid>
          <Grid item xs={12} md={6}>
            <List dense>
              <InfoItem label="Gender" value={record.patient.gender} />
              <InfoItem label="Phone" value={record.patient.phone} />
              <InfoItem label="Address" value={record.patient.address} />
            </List>
          </Grid>
        </Grid>
      </Paper>

      {/* Section: Clinical Notes */}
      <Paper elevation={1} sx={{ p: 3, mb: 3, borderRadius: 2 }}>
        <Typography variant="h6" gutterBottom sx={{ display: 'flex', alignItems: 'center', gap: 1, color: '#1565c0' }}>
            <NoteIcon /> Clinical Notes
        </Typography>
        
        {record.clinicalNotes && record.clinicalNotes.length > 0 ? (
           <List>
             {record.clinicalNotes.map((note, index) => (
               <ListItem key={index} divider={index < record.clinicalNotes.length - 1}>
                 <ListItemText 
                   primary={note.note}
                   secondary={`Created at: ${new Date(note.createdAt).toLocaleString('en-US')}`}
                   primaryTypographyProps={{ fontWeight: 'medium' }}
                 />
               </ListItem>
             ))}
           </List>
        ) : (
           <Typography variant="body2" color="text.secondary" sx={{ fontStyle: 'italic', mt: 1 }}>
             No clinical notes available.
           </Typography>
        )}
      </Paper>

      {/* Section: Test History */}
      <Paper elevation={1} sx={{ p: 3, mb: 3, borderRadius: 2 }}>
        <Stack direction="row" justifyContent="space-between" alignItems="center" mb={2}>
             <Typography variant="h6" sx={{ display: 'flex', alignItems: 'center', gap: 1, color: '#1565c0' }}>
                <AssessmentIcon /> Laboratory Test History
            </Typography>
        </Stack>
        
        {/* Filters */}
        <Grid container spacing={2} mb={3} alignItems="center" sx={{ bgcolor: '#f5f5f5', p: 2, borderRadius: 1 }}>
          <Grid item xs={12} sm={3}>
             <TextField
                fullWidth
                size="small"
                label="From Date"
                type="date"
                value={startDateInput}
                onChange={(e) => setStartDateInput(e.target.value)}
                InputLabelProps={{ shrink: true }}
             />
          </Grid>
          <Grid item xs={12} sm={3}>
             <TextField
                fullWidth
                size="small"
                label="To Date"
                type="date"
                value={endDateInput}
                onChange={(e) => setEndDateInput(e.target.value)}
                InputLabelProps={{ shrink: true }}
             />
          </Grid>
          <Grid item xs={12} sm={4}>
            <TextField
              fullWidth
              size="small"
              label="Filter by Instrument"
              variant="outlined"
              value={instrumentInput}
              onChange={e => setInstrumentInput(e.target.value)}
            />
          </Grid>
          <Grid item xs={12} sm={2}>
            <Button
              fullWidth
              variant="contained"
              startIcon={<SearchIcon />}
              onClick={handleFilter}
              sx={{ height: '40px' }} 
            >
              Filter
            </Button>
          </Grid>
        </Grid>

        {/* Test Records List */}
        {record && record.testRecords.length > 0 ? (
          <Stack spacing={3}>
            {record.testRecords.map((test) => (
              <Card key={test.testOrderId} variant="outlined" sx={{ borderRadius: 2, border: '1px solid #cfd8dc', boxShadow: 1 }}>
                <CardContent sx={{ pb: '16px !important' }}>
                    
                    {/* HEADER SECTION */}
                    <Grid container alignItems="flex-start" justifyContent="space-between" mb={1}>
                        <Grid item xs={12} sm={9}>
                             
                             <Stack direction="row" alignItems="center" spacing={1} mb={1}>
                                <AssignmentIcon fontSize="small" color="action" />
                                <Typography variant="h6" fontWeight="bold" color="primary.dark">
                                    Interpretation: {test.interpretation || "N/A"}
                                </Typography>
                             </Stack>

                             <Stack direction="row" spacing={2} mt={0.5} divider={<Divider orientation="vertical" flexItem />}>
                                <Typography variant="body2" color="text.secondary">
                                    Order ID: <strong>{test.testOrderId}</strong>
                                </Typography>
                                <Typography variant="body2" color="text.secondary" display="flex" alignItems="center" gap={0.5}>
                                    <CalendarTodayIcon fontSize="inherit"/>
                                    Completed: {test.testCompletedAt ? new Date(test.testCompletedAt).toLocaleString('en-US') : "Pending"}
                                </Typography>
                             </Stack>
                        </Grid>
                        <Grid item xs={12} sm={3} sx={{ textAlign: 'right' }}>
                            <Chip 
                                label={test.status} 
                                color={getTestStatusColor(test.status) as any} // Sử dụng hàm lấy màu mới
                                variant="filled"
                                size="medium"
                                sx={{ fontWeight: 'bold', minWidth: 100 }}
                            />
                        </Grid>
                    </Grid>

                    <Divider sx={{ my: 1.5 }} />

                    {/* ACCORDION DETAILS (Mặc định đóng - Đã xóa defaultExpanded) */}
                    <Accordion 
                        disableGutters 
                        elevation={0}
                        sx={{ '&:before': { display: 'none' } }}
                    >
                        <AccordionSummary expandIcon={<ExpandMoreIcon />} sx={{ bgcolor: '#f1f3f4', borderRadius: 1, minHeight: 48 }}>
                            <Typography variant="subtitle2" sx={{ display: 'flex', alignItems: 'center', gap: 1, fontWeight: 'bold', color: '#455a64' }}>
                                <ScienceIcon fontSize="small"/> Analysis Details
                            </Typography>
                        </AccordionSummary>
                        
                        <AccordionDetails sx={{ px: 0, py: 2 }}>
                            <TestResultTable jsonString={test.testResultsJson} />

                            <Box sx={{ mt: 3 }}>
                                <Typography variant="subtitle2" gutterBottom sx={{ color: 'text.secondary', mb: 1, ml: 1 }}>
                                    Technical Information:
                                </Typography>
                                <Grid container spacing={2}>
                                    <Grid item xs={12} md={5}>
                                        <InstrumentInfo jsonString={test.instrumentDetailsJson} />
                                    </Grid>
                                    <Grid item xs={12} md={7}>
                                        <ReagentInfo jsonString={test.reagentDetailsJson} />
                                    </Grid>
                                </Grid>
                            </Box>

                        </AccordionDetails>
                    </Accordion>

                </CardContent>
              </Card>
            ))}
          </Stack>
        ) : (
          <Box textAlign="center" py={5} bgcolor="#f9f9f9" borderRadius={2}>
            <BiotechIcon sx={{ fontSize: 60, color: '#bdbdbd', mb: 2 }} />
            <Typography color="text.secondary">No test records found matching your criteria.</Typography>
          </Box>
        )}
      </Paper>
    </Box>
  );
}