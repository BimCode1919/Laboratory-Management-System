import { useEffect, useState } from 'react';
import { Autocomplete, TextField, Dialog, DialogTitle, DialogContent, DialogActions, Button, IconButton, Tooltip } from '@mui/material';
import AddIcon from '@mui/icons-material/Add';
import type { Vendor } from '../services/VendorServices';
import { getAllVendors, createVendor } from '../services/VendorServices';

type Props = {
  value?: string; // vendor id
  onChange: (id: string | null) => void;
  label?: string;
};

export default function VendorSelect({ value, onChange, label = 'Nhà cung cấp' }: Props) {
  const [options, setOptions] = useState<Vendor[]>([]);
  const [openCreate, setOpenCreate] = useState(false);
  const [newName, setNewName] = useState('');
  const [loading, setLoading] = useState(false);
  const [inputValue, setInputValue] = useState('');

  useEffect(() => {
    let mounted = true;
    (async () => {
      setLoading(true);
      try {
        const data = await getAllVendors();
        if (!mounted) return;
        const list = data ?? [];
        const normalized = list.map((v: any) => ({
          ...(v || {}),
          id: v?.id ?? v?.vendor_id ?? v?.vendorId ?? v?.vendorIdString ?? String(v?.id ?? v?.vendor_id ?? ''),
          name: v?.name ?? v?.vendor_name ?? v?.vendorName ?? '',
        } as Vendor));
        setOptions(normalized);
      } finally {
        setLoading(false);
      }
    })();
    return () => { mounted = false; };
  }, []);

  const selected = options.find(o => o.id === value) || null;

  const handleCreate = async () => {
    if (!newName.trim()) return;
    try {
      const created = await createVendor({ name: newName.trim() });
      if (created) {
        const norm = {
          ...(created as any),
          id: (created as any).id ?? (created as any).vendor_id ?? (created as any).vendorId ?? String((created as any).id ?? (created as any).vendor_id ?? ''),
          name: (created as any).name ?? (created as any).vendor_name ?? (created as any).vendorName ?? newName.trim(),
        } as Vendor;
        setOptions(prev => [norm, ...prev]);
        onChange(norm.id || null);
        setOpenCreate(false);
        setNewName('');
      }
    } catch (err) {
      console.error('Failed to create vendor', err);
    }
  };

  return (
    <>
      <Autocomplete
        options={options}
        getOptionLabel={(opt: Vendor) => opt.name || ''}
        value={selected}
        onChange={(_, val) => {
          if (!val) {
            onChange(null);
            return;
          }
          if ((val as any).id === '__create__') {
            setNewName(inputValue);
            setOpenCreate(true);
            return;
          }
          onChange(val.id || (val as any).vendorId || (val as any).id || null);
        }}
        loading={loading}
        filterOptions={(opts) => {
          const filtered = opts.filter(o => o.name?.toLowerCase().includes(inputValue.toLowerCase()));
          if (inputValue && !opts.some(o => o.name?.toLowerCase() === inputValue.toLowerCase())) {
            filtered.push({ id: '__create__', name: `Create "${inputValue}"` } as unknown as Vendor);
          }
          return filtered;
        }}
        renderInput={(params) => (
          <TextField
            {...params}
            label={label}
            size="small"
            fullWidth
            InputProps={{
              ...params.InputProps,
              endAdornment: (
                <>
                  <Tooltip title="Add vendor">
                    <span>
                      <IconButton
                        size="small"
                        aria-label="add-vendor"
                        onMouseDown={(e) => e.stopPropagation()}
                        onClick={() => { setNewName(''); setOpenCreate(true); }}
                        sx={{
                          mr: 0.5,
                          height: 30,
                          width: 30,
                          minWidth: 30,
                          borderRadius: 1,
                          border: '1px solid rgba(0,0,0,0.08)',
                          bgcolor: 'background.paper',
                          color: 'text.primary',
                          boxShadow: 'none',
                          '&:hover': { bgcolor: 'grey.100' },
                          padding: '4px'
                        }}
                      >
                        <AddIcon fontSize="small" />
                      </IconButton>
                    </span>
                  </Tooltip>
                  {params.InputProps.endAdornment}
                </>
              ),
            }}
          />
        )}
        inputValue={inputValue}
        onInputChange={(_, iv) => setInputValue(iv)}
        renderOption={(props, option) => (
          <li {...props} key={(option as any).id}>{option.name}</li>
        )}
      />

      <Dialog open={openCreate} onClose={() => setOpenCreate(false)}>
        <DialogTitle>Create Vendor</DialogTitle>
        <DialogContent>
          <TextField autoFocus fullWidth value={newName} onChange={e => setNewName(e.target.value)} label="Vendor name" />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenCreate(false)}>Cancel</Button>
          <Button onClick={handleCreate} variant="contained">Create</Button>
        </DialogActions>
      </Dialog>
    </>
  );
}
