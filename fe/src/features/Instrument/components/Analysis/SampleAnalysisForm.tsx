import React, { useState, useEffect, useCallback, useRef } from 'react';
import { useForm } from 'react-hook-form';
import type { StartAnalysisPayload } from '../../types/types';
import { InstrumentApi, type PendingTestOrder } from '../../services/InstrumentAPI';
import EstimateModal from './EstimateModal';
import Alert from '../../../../components/ui/alert/Alert';
import type { EstimateResult } from '../../types/types';

interface SampleAnalysisFormProps {
  onSubmit: (data: StartAnalysisPayload) => void;
  isLoading: boolean;
  defaultValues?: Partial<StartAnalysisPayload>;
  instrumentId: string;
  selectedTestType?: string;
  availableTestTypes?: string[];
  onSelectTestType?: (type: string) => void;
}

const defaultTestTypes = ['CBC', 'HBA1C', 'LFT'];
const statuses = ['PENDING', 'TESTED'];

const SampleAnalysisForm: React.FC<SampleAnalysisFormProps> = ({
  onSubmit,
  isLoading,
  defaultValues,
  instrumentId,
  selectedTestType = '',
  availableTestTypes = [],
  onSelectTestType,
}) => {
  const { handleSubmit, setValue, formState: { errors, isValid } } = useForm<StartAnalysisPayload>({
    mode: 'onChange', // Enable validation on change
    defaultValues: {
      testType: '',
      barcodes: [],
      autoCreateTestOrder: false,
      expectedSamples: 0,
      batchCode: '',
      note: '',
      ...defaultValues,
    },
  });

  const [pendingTestOrders, setPendingTestOrders] = useState<PendingTestOrder[]>([]);
  const [pendingPageData, setPendingPageData] = useState<import('../../types/types').PaginatedApiResponse<PendingTestOrder> | null>(null);
  // Pagination & sorting for pending orders
  const [page, setPage] = useState<number>(0);
  const [size, setSize] = useState<number>(10); // default 10 as requested
  const [sortBy] = useState<string>('createdAt');
  const [sortDir, setSortDir] = useState<'asc' | 'desc'>('asc'); // default old -> new
  const [selectedOrders, setSelectedOrders] = useState<string[]>([]);
  // Keep a ref in sync with selectedOrders to avoid stale closures in async callbacks
  const selectedOrdersRef = useRef<string[]>([]);
   const [barcodeInput, setBarcodeInput] = useState('');
   const [isCheckingBarcode, setIsCheckingBarcode] = useState(false);
   const [isScanning, setIsScanning] = useState(false);
   const videoRef = useRef<HTMLVideoElement | null>(null);
   const codeReaderRef = useRef<import('@zxing/browser').BrowserMultiFormatReader | null>(null);
   const scannerControlsRef = useRef<import('@zxing/browser').IScannerControls | null>(null);
   // Flag used to indicate a selection change initiated by scanning (so effects don't clear it)
   const scanSelectingRef = useRef(false);
   // typeFilter will be controlled by selectedTestType (from ConfigurationDisplay) when provided
   // Initialize from prop so the UI reflects the selected config on first render
   const [typeFilter, setTypeFilter] = useState<string>(selectedTestType || '');
   // Use appliedType as the source-of-truth for list fetching and selection logic.
   // Parent `selectedTestType` takes precedence — this avoids visual flicker when parent updates.
   const appliedType = selectedTestType || typeFilter;
   const [statusFilter, setStatusFilter] = useState('PENDING');
   const [priorityFilter, setPriorityFilter] = useState('');
   const [isRefreshingPending, setIsRefreshingPending] = useState(false);
   const [estimate, setEstimate] = useState<EstimateResult | null>(null);
   const [isEstimateLoading, setIsEstimateLoading] = useState(false);
   const [showEstimateModal, setShowEstimateModal] = useState(false);
   const [confirmingStart, setConfirmingStart] = useState(false);
   const [lastSubmissionData, setLastSubmissionData] = useState<StartAnalysisPayload | null>(null);
  // Alert UI for inline notifications about barcode checks
  const [alertData, setAlertData] = useState<{ variant: 'success' | 'error' | 'warning' | 'info'; title: string; message: string } | null>(null);
  // Auto-dismiss timer ref for alerts (auto-close after ~1s)
  const alertTimeoutRef = useRef<ReturnType<typeof window.setTimeout> | null>(null);

  // Helper to show alert with reliable auto-dismiss scheduling
  const showAlert = (data: { variant: 'success' | 'error' | 'warning' | 'info'; title: string; message: string }) => {
    // clear any existing timer
    if (alertTimeoutRef.current) {
      console.debug('[SampleAnalysisForm] clearing previous alert timeout');
      clearTimeout(alertTimeoutRef.current);
      alertTimeoutRef.current = null;
    }
    // set the alert immediately
    console.debug('[SampleAnalysisForm] showing alert:', data);
    setAlertData(data);
    // schedule auto-dismiss after 1s
    // use window.setTimeout so type is consistent with our ref
    console.debug('[SampleAnalysisForm] scheduling auto-dismiss in 1000ms');
    alertTimeoutRef.current = window.setTimeout(() => {
      console.debug('[SampleAnalysisForm] auto-dismissing alert now');
      setAlertData(null);
      alertTimeoutRef.current = null;
    }, 3000);
  };

  const closeAlert = () => {
    console.debug('[SampleAnalysisForm] closeAlert called');
    if (alertTimeoutRef.current) {
      clearTimeout(alertTimeoutRef.current);
      alertTimeoutRef.current = null;
    }
    setAlertData(null);
  };

   // Memoize setValue to safely use it in useEffect dependency array
   const memoizedSetValue = useCallback(setValue, [setValue]);

   // Derived test types: if a specific selectedTestType is provided (non-empty), expose only that in the select
   const testTypes = selectedTestType ? [selectedTestType] : (availableTestTypes.length > 0 ? availableTestTypes : defaultTestTypes);

   // Keep local typeFilter in sync with selectedTestType prop
   useEffect(() => {
     setTypeFilter(selectedTestType || '');
     // If the selection change came from our scanning flow, preserve selections.
     if (!scanSelectingRef.current) {
       setSelectedOrders([]);
       selectedOrdersRef.current = [];
       memoizedSetValue('barcodes', [], { shouldValidate: true });
     } else {
       // Clear the flag shortly after to resume normal behavior for subsequent manual changes
       window.setTimeout(() => { scanSelectingRef.current = false; }, 500);
     }
   }, [selectedTestType, memoizedSetValue]);

   // Ensure that when viewing All (appliedType === ''), selections remain cleared
   useEffect(() => {
     if (appliedType === '') {
       setSelectedOrders([]);
       selectedOrdersRef.current = [];
       memoizedSetValue('barcodes', [], { shouldValidate: true });
     }
   }, [appliedType, memoizedSetValue]);

   // Track previous filter set so we only clear selections when filters meaningfully change
   const prevFiltersRef = useRef({ appliedType, statusFilter, priorityFilter });

  // Extract fetch into callback so other controls (Refresh) can re-use it
  const fetchPendingTestOrders = useCallback(async (pageToFetch: number = page) => {
    try {
      const response = await InstrumentApi.getPendingTestOrders(
        pageToFetch,
        size,
        sortBy,
        sortDir,
        appliedType,
        statusFilter,
        priorityFilter
      );
      setPendingTestOrders(response.content);
      setPendingPageData(response);
      // If current page is out of range after a filter/size change, clamp it
      if (response.totalPages > 0 && pageToFetch >= response.totalPages) {
        setPage(Math.max(0, response.totalPages - 1));
      }
    } catch (error) {
      console.error('Error fetching pending test orders:', error);
    }
    // Note: do not alter refresh UI here; caller manages isRefreshingPending when appropriate
   }, [size, sortBy, sortDir, appliedType, statusFilter, priorityFilter, page]);

   const handleRefreshPending = async () => {
     // Clear selections per request when user explicitly refreshes
     setSelectedOrders([]);
     selectedOrdersRef.current = [];
     memoizedSetValue('barcodes', [], { shouldValidate: true });
     setIsRefreshingPending(true);
     try {
       await fetchPendingTestOrders(page);
     } finally {
       // small delay to make spinner visible briefly even on fast networks
       setTimeout(() => setIsRefreshingPending(false), 200);
     }
   };

   // Fetch pending test orders when filters or pagination state changes
   useEffect(() => {
     // Only clear selections when filters (appliedType/status/priority) actually change.
     const filtersChanged = prevFiltersRef.current.appliedType !== appliedType
       || prevFiltersRef.current.statusFilter !== statusFilter
       || prevFiltersRef.current.priorityFilter !== priorityFilter;

     if (filtersChanged) {
       setSelectedOrders([]);
       selectedOrdersRef.current = [];
       memoizedSetValue('barcodes', [], { shouldValidate: true });
       // When filters change, reset to first page to avoid out-of-range pages
       setPage(0);
     }

     // Use the extracted callback
     fetchPendingTestOrders(page);
     // update prev filters snapshot after fetch kicked off
     prevFiltersRef.current = { appliedType, statusFilter, priorityFilter };
   // include selectedTestType so parent-provided selection forces a refetch
   }, [selectedTestType, appliedType, statusFilter, priorityFilter, page, size, sortDir, sortBy, memoizedSetValue, fetchPendingTestOrders]);

   const handleToggleOrderSelection = (barcode: string) => {
     // When viewing "All", selection is disabled — do nothing
     if (appliedType === '') {
       return;
     }

     // Use the ref to avoid stale closures
     const current = selectedOrdersRef.current;
     const newSelectedOrders: string[] = current.includes(barcode)
       ? current.filter(b => b !== barcode)
       : [...current, barcode];

     setSelectedOrders(newSelectedOrders);
     selectedOrdersRef.current = newSelectedOrders;
     memoizedSetValue('barcodes', newSelectedOrders, { shouldValidate: true });
   };

   const handleFormSubmit = (data: StartAnalysisPayload) => {
     const submissionData = { ...data };

     if (appliedType === '' && selectedOrders.length === 1) {
         const selectedOrderDetails = pendingTestOrders.find(order => order.barCode === selectedOrders[0]);
         if (selectedOrderDetails) {
             submissionData.testType = selectedOrderDetails.testType;
         } else {
             // Fallback or error handling if selected order details not found
             console.error("Selected order details not found for single selection with 'All' type filter.");
             return; // Prevent submission if testType cannot be determined
         }
     } else if (typeFilter !== '') {
         submissionData.testType = typeFilter;
     } else {
         console.error("Cannot determine testType for submission. Please select a specific type or a single order.");
         return;
     }

     // Instead of directly starting analysis, call estimate endpoint first
     (async () => {
       try {
         setIsEstimateLoading(true);
         setLastSubmissionData(submissionData);
         const estimateRes = await InstrumentApi.estimateAnalysis(instrumentId, submissionData);
         // The API might wrap data in an ApiResponse; guard for that carefully without using `any`
         const asUnknown = estimateRes as unknown;
         let estimateData: EstimateResult;
         if (asUnknown && typeof asUnknown === 'object' && 'data' in (asUnknown as Record<string, unknown>)) {
           estimateData = (asUnknown as { data: EstimateResult }).data;
         } else {
           estimateData = asUnknown as EstimateResult;
         }
         setEstimate(estimateData);
         setShowEstimateModal(true);
       } catch (err) {
         console.error('Estimate API failed', err);
         // Fallback: open modal with null and allow cancel; do not auto-start
         setEstimate(null);
         setShowEstimateModal(false);
       } finally {
         setIsEstimateLoading(false);
       }
     })();
   };

   const handleConfirmStart = async () => {
     if (!estimate || !lastSubmissionData) return;
     // Only allow start if estimate.sufficient
     if (!estimate.sufficient) return;
     setConfirmingStart(true);
     try {
       await onSubmit(lastSubmissionData);
       setShowEstimateModal(false);
     } catch (err) {
       console.error('Start analysis failed', err);
     } finally {
       setConfirmingStart(false);
     }
   };

   const handleCancelEstimate = () => {
     setShowEstimateModal(false);
     setEstimate(null);
   };

   // Handler to check barcode against backend endpoint and auto-select
   const handleCheckBarcode = async () => {
     if (!barcodeInput) return;
     setIsCheckingBarcode(true);
     try {
       const res = await InstrumentApi.checkPendingTestOrder(barcodeInput, instrumentId);

      // Aggregate semantic reasons why barcode is not acceptable
      const reasons: string[] = [];
      if (!res.exists) reasons.push('Barcode không tồn tại');
      if (!res.instrumentExists) reasons.push('Mẫu không thuộc máy này');
      if (!res.hasMatchingConfig) {
        if (res.testType) reasons.push(`Không có cấu hình phù hợp cho test type '${res.testType}'`);
        else reasons.push('Không có cấu hình phù hợp cho mẫu này');
      }
      if (!res.pending) reasons.push('Mẫu không ở trạng thái Pending');

      if (reasons.length > 0) {
        // Show aggregated alert to user with actionable message
        const title = res.exists && res.testType ? `Barcode: ${barcodeInput}` : 'Barcode không hợp lệ';
        const message = reasons.join('. ') + '.';
        // Use warning when some conditions fail but the barcode exists; use error when it doesn't exist
        const variant: 'success' | 'error' | 'warning' | 'info' = !res.exists ? 'error' : 'warning';
        showAlert({ variant, title, message });
        // Keep barcode input so user can correct or press Scan again; do not proceed with selection
        return;
      }

       // If all four conditions are true, set or update the type filter/testType and select the barcode row
       if (res.exists && res.instrumentExists && res.hasMatchingConfig && res.pending && res.testType) {
         const testType: string = res.testType as string;

         // Determine the current selected type (prop takes precedence)
         const currentSelectedType = selectedTestType || typeFilter;

         // If no type is currently selected, or it's different, we should (re)select the config
         const isDifferentType = !!currentSelectedType && currentSelectedType !== testType;

         // Mark that we're performing a scan-initiated selection to prevent effects clearing selections
         scanSelectingRef.current = true;

         // Notify parent first (if provided) so config UI highlights, otherwise set locally
         if (onSelectTestType) {
           try { onSelectTestType(testType); } catch { console.warn('onSelectTestType callback failed'); }
         } else {
           setTypeFilter(testType);
         }

         // Fetch pending orders for this testType to ensure the barcode exists in the list
         try {
           // For a barcode quick-check, search a generous page size but keep sorting asc (old -> new)
           const pendingRes = await InstrumentApi.getPendingTestOrders(0, 100, sortBy, 'asc', testType, statusFilter, priorityFilter);
           setPendingTestOrders(pendingRes.content);

            // Compute new selected orders based on whether the scanned barcode's type matches current
             // Use ref to get latest selected orders (may have changed while async op was running)
             const currentSelections = selectedOrdersRef.current;
             let newSelectedOrders: string[];
             if (!currentSelectedType) {
               // No previous type — start fresh with the scanned barcode
               newSelectedOrders = [barcodeInput];
             } else if (isDifferentType) {
               // Different type scanned: reset selection to only the new barcode
               newSelectedOrders = [barcodeInput];
             } else {
               // Same type scanned: append to existing selections if not already present
               newSelectedOrders = Array.from(new Set([...currentSelections, barcodeInput]));
             }

          setSelectedOrders(newSelectedOrders);
          selectedOrdersRef.current = newSelectedOrders;
          memoizedSetValue('barcodes', newSelectedOrders, { shouldValidate: true });
          memoizedSetValue('testType', testType, { shouldValidate: true });

           // After successful check, stop scanner and clear input to prepare for next scan
           try { stopScanner(); } catch (e) { void e; }
           setBarcodeInput('');
         } catch (err) {
           console.error('Error fetching pending orders after barcode check', err);
             // On error, still update selection/testType conservatively
             const currentSelections = selectedOrdersRef.current;
             const newSelectedOrders = (!currentSelections.length || selectedTestType || (selectedTestType === testType) || !typeFilter)
               ? [barcodeInput]
               : Array.from(new Set([...currentSelections, barcodeInput]));
             setSelectedOrders(newSelectedOrders);
             selectedOrdersRef.current = newSelectedOrders;
             memoizedSetValue('barcodes', newSelectedOrders, { shouldValidate: true });
             memoizedSetValue('testType', testType, { shouldValidate: true });
           try { stopScanner(); } catch (e) { void e; }
           setBarcodeInput('');
         }
       } else {
         // If we reach here it's unexpected because we handled failures above; log for debugging
         console.warn('Barcode check failed or missing conditions (unexpected path)', res);
       }
     } catch (err) {
       console.error('Error checking barcode', err);
      showAlert({ variant: 'error', title: 'Lỗi kiểm tra barcode', message: 'Có lỗi xảy ra khi kiểm tra barcode. Vui lòng thử lại.' });
     } finally {
       setIsCheckingBarcode(false);
     }
   };

   // Start scanning using camera
   const startScanner = async () => {
     if (isScanning) return;
     setIsScanning(true);
     try {
       const zx = await import('@zxing/browser');
       const codeReader = new zx.BrowserMultiFormatReader();
       codeReaderRef.current = codeReader as unknown as import('@zxing/browser').BrowserMultiFormatReader;
       // decodeFromVideoDevice will pick the default camera (deviceId undefined)
       if (!videoRef.current) {
         console.warn('No video element available for scanning');
         return;
       }
       scannerControlsRef.current = await codeReader.decodeFromVideoDevice(undefined, videoRef.current, (result, err) => {
         if (result) {
           const text = result.getText();
           // Stop scanner and populate input only; user must press Check to validate
           try { stopScanner(); } catch (e) { void e; }
           setTimeout(() => setBarcodeInput(text), 100);
         } else if (err) {
           // log notable errors
           console.warn('Barcode scan error', err);
         }
       });
     } catch (err) {
       console.error('Failed to start scanner', err);
       setIsScanning(false);
     }
   };

   // Stop scanner and release camera
   const stopScanner = () => {
     try {
       if (scannerControlsRef.current) {
         try { scannerControlsRef.current.stop(); } catch (e) { console.debug('scannerControls.stop failed', e); }
         scannerControlsRef.current = null;
       }
       if (codeReaderRef.current) {
         try { (codeReaderRef.current as unknown as { releaseAllStreams?: () => void }).releaseAllStreams?.(); } catch (e) { console.debug('releaseAllStreams failed', e); }
         codeReaderRef.current = null;
       }
     } catch (err) {
       console.warn('Error stopping scanner', err);
     } finally {
       setIsScanning(false);
     }
   };

   // Ensure scanner is stopped when component unmounts
   useEffect(() => {
     return () => {
       try { stopScanner(); } catch (e) { void e; }
     };
   }, []);

   // If typeFilter changes (e.g., user selects config), stop scanning to avoid camera holding
   useEffect(() => {
     if (isScanning) {
       stopScanner();
     }
     // eslint-disable-next-line react-hooks/exhaustive-deps
   }, [typeFilter]);

   // Helper: format ISO date/time strings into a human-friendly US format with seconds and AM/PM
   const formatDate = (iso?: string | null) => {
     if (!iso) return '';
     try {
       const d = new Date(iso);
       if (Number.isNaN(d.getTime())) return iso; // fallback to raw if invalid
       // Use en-US locale to get MM/DD/YYYY and 12-hour clock with seconds
       return new Intl.DateTimeFormat('en-US', {
         year: 'numeric',
         month: '2-digit',
         day: '2-digit',
         hour: 'numeric',
         minute: '2-digit',
         second: '2-digit',
         hour12: true,
       }).format(d);
     } catch (err) {
       console.debug('formatDate parse error', err);
       return iso;
     }
   };

    return (
      <form onSubmit={handleSubmit(handleFormSubmit)} className="space-y-4">
       {showEstimateModal && (
         <EstimateModal
           estimate={estimate}
           onConfirm={handleConfirmStart}
           onCancel={handleCancelEstimate}
           confirming={confirmingStart}
         />
       )}
       {/* userId is handled server-side now; do not send from client */}

       {/* Barcode quick-check row */}
       <div className="flex gap-2 items-end">
         <div className="flex-1">
           <label htmlFor="barcodeInput" className="block text-sm font-medium text-gray-700 dark:text-gray-300">Barcode</label>
           <input
             id="barcodeInput"
             type="text"
             value={barcodeInput}
             onChange={(e) => setBarcodeInput(e.target.value)}
             placeholder="Nhập barcode và nhấn Check"
             className="p-2 mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 dark:bg-gray-700 dark:border-gray-600 dark:text-gray-200"
           />
         </div>
         <div>
           <div className="flex flex-col gap-2">
             <button
               type="button"
               onClick={handleCheckBarcode}
               disabled={isCheckingBarcode || !barcodeInput}
               className="inline-flex items-center px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 disabled:bg-gray-400 disabled:cursor-not-allowed"
             >
               {isCheckingBarcode ? 'Checking...' : 'Check'}
             </button>
             <button
               type="button"
               onClick={() => { if (isScanning) { stopScanner(); } else { startScanner(); } }}
               className={`inline-flex items-center px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white ${isScanning ? 'bg-red-600 hover:bg-red-700 focus:ring-red-500' : 'bg-indigo-600 hover:bg-indigo-700 focus:ring-indigo-500'}`}
             >
               {isScanning ? 'Stop Scan' : 'Scan'}
             </button>
           </div>
         </div>
       </div>

       {/* Video preview for scanning (hidden when not scanning) */}
       {isScanning && (
         <div className="mt-2">
           <label className="block text-sm font-medium text-gray-700 dark:text-gray-300">Camera Preview</label>
           <div className="mt-1 rounded-md overflow-hidden border border-gray-300 dark:border-gray-600">
             <video ref={videoRef} className="w-full h-48 object-cover bg-black" muted playsInline />
           </div>
         </div>
       )}

       <div className="grid grid-cols-3 gap-4">
         <div>
           <label htmlFor="typeFilter" className="block text-sm font-medium text-gray-700 dark:text-gray-300">Test Type</label>
           <select
             id="typeFilter"
             value={typeFilter}
             onChange={(e) => setTypeFilter(e.target.value)}
             className="p-2 mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 dark:bg-gray-700 dark:border-gray-600 dark:text-gray-200"
             disabled={true} // permanently disabled (read-only) as requested
           >
             <option value="">All</option>
             {testTypes.map(type => <option key={type} value={type}>{type}</option>)}
           </select>
         </div>
         <div>
           <label htmlFor="priorityFilter" className="block text-sm font-medium text-gray-700 dark:text-gray-300">Priority</label>
           <select
             id="priorityFilter"
             value={priorityFilter}
             onChange={(e) => setPriorityFilter(e.target.value)}
             className="p-2 mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 dark:bg-gray-700 dark:border-gray-600 dark:text-gray-200"
           >
             <option value="">All</option>
             {['LOW','MEDIUM','HIGH'].map(prio => <option key={prio} value={prio}>{prio}</option>)}
           </select>
         </div>
         <div>
           <label htmlFor="statusFilter" className="block text-sm font-medium text-gray-700 dark:text-gray-300">Status</label>
           <select
             id="statusFilter"
             value={statusFilter}
             onChange={(e) => setStatusFilter(e.target.value)}
             className="p-2 mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 dark:bg-gray-700 dark:border-gray-600 dark:text-gray-200"
           >
             <option value="">All</option>
             {statuses.map(status => <option key={status} value={status}>{status}</option>)}
           </select>
         </div>
       </div>

       <div>
         {typeFilter === '' && (
           <p className="mb-2 text-sm text-gray-600 dark:text-gray-400">Hiện đang xem tất cả loại xét nghiệm — không thể chọn mẫu. Vui lòng chọn một loại cấu hình (hoặc một test type cụ thể) để bật chức năng chọn mẫu.</p>
         )}
       <div className="max-h-60 overflow-y-auto border border-gray-300 rounded-md dark:border-gray-600">
          <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
           <thead className="bg-gray-50 dark:bg-gray-800">
             <tr>
               <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider dark:text-gray-300">Select</th>
                 <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider dark:text-gray-300">Create Date</th>
                 <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider dark:text-gray-300">Patient</th>
               <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider dark:text-gray-300">Test Type</th>
               {/*<th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider dark:text-gray-300">Priority</th>*/}
             </tr>
           </thead>
           <tbody className="bg-white divide-y divide-gray-200 dark:bg-gray-900 dark:divide-gray-700">
             {pendingTestOrders.map((order) => (
               <tr key={order.id}>
                 <td className="px-6 py-4 whitespace-nowrap">
                   <input
                     type="checkbox"
                     checked={selectedOrders.includes(order.barCode)}
                     onChange={() => handleToggleOrderSelection(order.barCode)}
                     disabled={typeFilter === ''}
                     title={typeFilter === '' ? 'Không thể chọn mẫu khi đang xem All' : undefined}
                     className="h-4 w-4 text-indigo-600 border-gray-300 rounded focus:ring-indigo-500 dark:bg-gray-700 dark:border-gray-600 dark:checked:bg-indigo-600"
                   />
                 </td>
                   <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-gray-200">{formatDate(order.createdAt)}</td>
                   <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900 dark:text-gray-200">{order.patientName}</td>
                 <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">{order.testType}</td>
                 {/*<td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">{order.priority}</td>*/}
               </tr>
             ))}
           </tbody>
         </table>
        </div>
       {/* Pagination controls */}
       <div className="mt-2 flex items-center justify-between">
         <div className="flex items-center gap-2">
           <button
             type="button"
             onClick={() => setPage(p => Math.max(0, p - 1))}
             disabled={page <= 0}
             className="inline-flex items-center px-3 py-1 border rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 dark:focus:ring-indigo-400 bg-white text-gray-700 border-gray-300 hover:bg-gray-50 dark:bg-gray-700 dark:text-gray-200 dark:border-gray-600 dark:hover:bg-gray-600 disabled:opacity-50"
           >
             Previous
           </button>
           <button
             type="button"
             onClick={handleRefreshPending}
             disabled={isRefreshingPending}
             className={`px-3 py-1 border rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 dark:focus:ring-indigo-400 ${isRefreshingPending ? 'opacity-70 cursor-not-allowed bg-white text-gray-700 border-gray-300 dark:bg-gray-700 dark:text-gray-200 dark:border-gray-600' : 'hover:bg-gray-50 bg-white text-gray-700 border-gray-300 dark:bg-gray-700 dark:text-gray-200 dark:border-gray-600 dark:hover:bg-gray-600'}`}
             title="Refresh pending orders (clears selections)"
           >
             {isRefreshingPending ? 'Refreshing...' : 'Refresh'}
           </button>
           <button
             type="button"
             onClick={() => setPage(p => p + 1)}
             disabled={!!pendingPageData && pendingPageData.last}
             className="inline-flex items-center px-3 py-1 border rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-indigo-500 dark:focus:ring-indigo-400 bg-white text-gray-700 border-gray-300 hover:bg-gray-50 dark:bg-gray-700 dark:text-gray-200 dark:border-gray-600 dark:hover:bg-gray-600 disabled:opacity-50"
           >
             Next
           </button>
           <div className="text-sm text-gray-600 dark:text-gray-400 ml-3">
             Page {pendingPageData ? (pendingPageData.number + 1) : (page + 1)}
             {pendingPageData ? ` of ${pendingPageData.totalPages}` : ''}
           </div>
         </div>

         <div className="flex items-center gap-2">
           <label className="text-sm text-gray-600 dark:text-gray-400">Per page:</label>
           <select
             value={size}
             onChange={(e) => { setSize(Number(e.target.value)); setPage(0); }}
             className="p-1 rounded-md border border-gray-300 bg-white text-sm dark:bg-gray-700 dark:text-gray-200 dark:border-gray-600"
           >
             <option value={10}>10</option>
             <option value={25}>25</option>
             <option value={50}>50</option>
           </select>
           <label className="text-sm text-gray-600 dark:text-gray-400 ml-3">Sort:</label>
           <select
             value={sortDir}
             onChange={(e) => { setSortDir(e.target.value as 'asc' | 'desc'); setPage(0); }}
             className="p-1 rounded-md border border-gray-300 bg-white text-sm dark:bg-gray-700 dark:text-gray-200 dark:border-gray-600"
           >
             <option value="asc">Oldest → Newest</option>
             <option value="desc">Newest → Oldest</option>
           </select>
         </div>
       </div>
       </div>
        {errors.barcodes && <p className="mt-1 text-sm text-red-600 dark:text-red-400">{errors.barcodes.message}</p>}

       {/* If viewing All, ensure submit stays disabled (selectedOrders length will be 0) */}
       <button
         type="submit"
         disabled={isLoading || isEstimateLoading || !isValid || selectedOrders.length === 0}
         className="w-full px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-green-600 hover:bg-green-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-green-500 disabled:bg-gray-400 disabled:cursor-not-allowed"
       >
         {isEstimateLoading ? 'Checking resources...' : isLoading ? `Starting Analysis for ${selectedOrders.length} sample(s)...` : `Start Analysis for ${selectedOrders.length} sample(s)`}
       </button>

       {/* Render inline alert similar to other components in the app */}
       {alertData && (
         <div className="mt-4 flex items-start gap-3">
           <div className="flex-1">
             <Alert
               variant={alertData.variant}
               title={alertData.title}
               message={alertData.message}
             />
           </div>
           <div className="ml-2">
             <button
               aria-label="Close alert"
               onClick={closeAlert}
               className="text-gray-500 hover:text-gray-700 dark:text-gray-300 dark:hover:text-white p-1"
             >
               ×
             </button>
           </div>
         </div>
       )}
     </form>
   );
};

export default SampleAnalysisForm;

