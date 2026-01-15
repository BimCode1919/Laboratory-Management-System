// src/features/patients/pages/EditRecordPage.tsx

import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import { useForm, Controller, useFieldArray } from "react-hook-form";
import { 
  Box, Button, CircularProgress, Paper, TextField, Typography, Grid, 
  Select, MenuItem, FormControl, InputLabel, List, ListItem, ListItemText, IconButton 
} from "@mui/material";
import AddCircleIcon from '@mui/icons-material/AddCircle';
import { getMedicalRecordDetail, updateMedicalRecord } from "../services/PatientService";
import type { MedicalRecordDetail, MedicalRecordUpdate, TestRecordStatus, PatientUpdateDTO, InterpretationUpdateDTO } from "../types/Patient";

const testStatuses: TestRecordStatus[] = ["PENDING", "PROCESSING", "COMPLETED", "CANCELLED", "REVIEWED"];

interface EditFormState {
  patientDTO: PatientUpdateDTO;
  interpretation: InterpretationUpdateDTO[];
  newClinicalNote: string;
  clinicalNotes: string[];
}

export default function EditRecordPage() {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [record, setRecord] = useState<MedicalRecordDetail | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const { 
    control, 
    handleSubmit, 
    reset, 
    watch,
    setValue,
    register, // <--- 1. CẦN LẤY HÀM REGISTER Ở ĐÂY
    formState: { isSubmitting, dirtyFields, isDirty } 
  } = useForm<EditFormState>({
    defaultValues: {
      patientDTO: { fullName: '', phoneNumber: '', email: '', address: '' },
      interpretation: [],
      newClinicalNote: '',
      clinicalNotes: [],
    }
  });

  const { fields: interpretationFields } = useFieldArray({
    control,
    name: "interpretation",
  });

  const newClinicalNoteValue = watch('newClinicalNote');
  const newClinicalNotesList = watch('clinicalNotes');

  useEffect(() => {
    if (!id) {
      setError("ID hồ sơ không hợp lệ.");
      setIsLoading(false);
      return;
    }
    setIsLoading(true);
    getMedicalRecordDetail(id)
      .then(data => {
        setRecord(data);
        setError(null);
        reset({
          patientDTO: {
            fullName: data.fullName,
            phoneNumber: data.phone,
            email: data.email,
            address: data.address,
          },
          interpretation: data.testRecords.map(tr => ({
            testRecordId: tr.testOrderId,
            interpretation: tr.interpretation || '',
            status: tr.status,
          })),
          newClinicalNote: "",
          clinicalNotes: [],
        });
      })
      .catch(err => setError(err.message))
      .finally(() => setIsLoading(false));
  }, [id, reset]);

  const handleAddNote = () => {
    if (newClinicalNoteValue.trim()) {
      setValue('clinicalNotes', [...newClinicalNotesList, newClinicalNoteValue.trim()], { shouldDirty: true });
      setValue('newClinicalNote', '');
    }
  };

  const onSubmit = async (data: EditFormState) => {
    if (!id) return;
    try {
      const payload: MedicalRecordUpdate = {};

      if (dirtyFields.patientDTO) {
        const patientPayload: PatientUpdateDTO = {};
        if (dirtyFields.patientDTO.fullName) patientPayload.fullName = data.patientDTO.fullName;
        if (dirtyFields.patientDTO.phoneNumber) patientPayload.phoneNumber = data.patientDTO.phoneNumber;
        if (dirtyFields.patientDTO.email) patientPayload.email = data.patientDTO.email;
        if (dirtyFields.patientDTO.address) patientPayload.address = data.patientDTO.address;
        if (Object.keys(patientPayload).length > 0) {
          payload.patientDTO = patientPayload;
        }
      }

      // Logic cũ: if (dirtyFields.interpretation) { payload.interpretation = data.interpretation; }
      // Logic mới: Luôn gửi kèm testRecordId chắc chắn
      if (dirtyFields.interpretation) {
         // Lọc và map để đảm bảo dữ liệu sạch
         payload.interpretation = data.interpretation.map(item => ({
            testRecordId: item.testRecordId,
            interpretation: item.interpretation,
            status: item.status
         }));
      }

      if (data.clinicalNotes && data.clinicalNotes.length > 0) {
        payload.clinicalNotes = data.clinicalNotes;
      }
      
      if (Object.keys(payload).length === 0) {
        alert("Không có thay đổi nào để lưu.");
        return;
      }
      
      console.log("Payload sending:", payload); // Debug payload để kiểm tra

      await updateMedicalRecord(id, payload);
      alert("Cập nhật hồ sơ thành công!");
      navigate(-1);
    } catch (err: any) {
      console.error("Update error:", err);
      // Hiển thị lỗi chi tiết hơn nếu có
      const msg = err.response?.data?.message || err.message;
      alert(`Lỗi: ${msg}`);
    }
  };

  if (isLoading) return <Box display="flex" justifyContent="center" mt={4}><CircularProgress /></Box>;
  if (error) return <Typography color="error" align="center" mt={4}>{error}</Typography>;
  if (!record) return <Typography align="center" mt={4}>Không tìm thấy hồ sơ.</Typography>;
  
  return (
    <Box component="form" onSubmit={handleSubmit(onSubmit)} noValidate>
      <Typography variant="h4" gutterBottom>Chỉnh sửa Hồ sơ: {record?.fullName}</Typography>

      <Paper sx={{ p: 2, mb: 3 }}>
        <Typography variant="h6" gutterBottom>Thông tin bệnh nhân</Typography>
        <Grid container spacing={2}>
          <Grid item xs={12} sm={6}>
            <Controller name="patientDTO.fullName" control={control} render={({ field }) => <TextField {...field} label="Họ và Tên" fullWidth />} />
          </Grid>
          <Grid item xs={12} sm={6}>
            <Controller name="patientDTO.phoneNumber" control={control} render={({ field }) => <TextField {...field} label="Số Điện Thoại" fullWidth />} />
          </Grid>
          <Grid item xs={12} sm={6}>
            <Controller name="patientDTO.email" control={control} render={({ field }) => <TextField {...field} label="Email" fullWidth />} />
          </Grid>
          <Grid item xs={12} sm={6}>
            <Controller name="patientDTO.address" control={control} render={({ field }) => <TextField {...field} label="Địa chỉ" fullWidth />} />
          </Grid>
        </Grid>
      </Paper>

      <Paper sx={{ p: 2, mb: 3 }}>
        <Typography variant="h6" gutterBottom>Kết quả & Diễn giải Xét nghiệm</Typography>
        {interpretationFields.map((field, index) => (
          <Box key={field.id} sx={{ mb: 2, p: 2, border: '1px solid #ddd', borderRadius: 1 }}>
            
            {/* 2. QUAN TRỌNG: Thêm Input ẩn để bind testRecordId vào form data */}
            <input 
                type="hidden" 
                {...register(`interpretation.${index}.testRecordId`)} 
            />

            <Typography variant="subtitle1">Mã Xét nghiệm (Order ID): {field.testRecordId}</Typography>
            <Grid container spacing={2} sx={{ mt: 1 }}>
              <Grid item xs={12} md={8}>
                <Controller
                  name={`interpretation.${index}.interpretation`}
                  control={control}
                  render={({ field }) => <TextField {...field} label="Diễn giải" fullWidth multiline rows={2} />}
                />
              </Grid>
              <Grid item xs={12} md={4}>
                <Controller
                  name={`interpretation.${index}.status`}
                  control={control}
                  render={({ field }) => (
                    <FormControl fullWidth>
                      <InputLabel>Trạng thái</InputLabel>
                      <Select {...field} label="Trạng thái">
                        {testStatuses.map(status => (
                          <MenuItem key={status} value={status}>{status}</MenuItem>
                        ))}
                      </Select>
                    </FormControl>
                  )}
                />
              </Grid>
            </Grid>
          </Box>
        ))}
      </Paper>

      <Paper sx={{ p: 2, mb: 3 }}>
        <Typography variant="h6" gutterBottom>Ghi chú Lâm sàng</Typography>
        
        <Box mb={2}>
          <Typography variant="subtitle2">Ghi chú đã có:</Typography>
          {record.clinicalNotes.length > 0 ? (
            <List dense>
              {record.clinicalNotes.map((note, index) => (
                <ListItem key={`${index}-${note.createdAt}`}>
                  <ListItemText primary={note.note} secondary={`Ngày: ${new Date(note.createdAt).toLocaleDateString()}`} />
                </ListItem>
              ))}
            </List>
          ) : <Typography variant="body2">Chưa có ghi chú nào.</Typography>}
        </Box>
        
        {newClinicalNotesList.length > 0 && (
          <Box mb={2}>
            <Typography variant="subtitle2">Ghi chú mới sẽ được thêm:</Typography>
            <List dense>
              {newClinicalNotesList.map((note, index) => (
                <ListItem key={index}>
                  <ListItemText primary={note} />
                </ListItem>
              ))}
            </List>
          </Box>
        )}

        <Box sx={{ display: 'flex', alignItems: 'center' }}>
          <Controller 
            name="newClinicalNote" 
            control={control} 
            render={({ field }) => 
              <TextField {...field} label="Nhập ghi chú mới..." fullWidth multiline rows={2} variant="outlined" />
            }
          />
          <IconButton color="primary" onClick={handleAddNote} disabled={!newClinicalNoteValue || !newClinicalNoteValue.trim()} sx={{ ml: 1 }}>
            <AddCircleIcon fontSize="large" />
          </IconButton>
        </Box>
      </Paper>
      
      <Box sx={{ display: 'flex', mt: 3, gap: 2 }}>
        <Button 
          type="submit" 
          variant="contained" 
          disabled={!isDirty || isSubmitting}
        >
          {isSubmitting ? <CircularProgress size={24} /> : "Lưu thay đổi"}
        </Button>

        <Button
          variant="outlined"
          color="secondary"
          onClick={() => navigate(-1)}
        >
          Hủy
        </Button>
      </Box>
    </Box>
  );
}