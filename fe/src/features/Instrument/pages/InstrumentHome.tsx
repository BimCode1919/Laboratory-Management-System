import React, { useState } from 'react';
import type { AxiosError } from 'axios';
import { InstrumentApi } from '../services/InstrumentAPI';
import { Link } from 'react-router-dom';

const InstrumentHome: React.FC = () => {
  const [instrumentId, setInstrumentId] = useState<string>('');
  const [isSyncingOne, setIsSyncingOne] = useState(false);
  const [isSyncingAll, setIsSyncingAll] = useState(false);
  const [message, setMessage] = useState<{ type: 'success' | 'error' | 'info'; text: string } | null>(null);
  const timeoutRef = React.useRef<number | null>(null);

  // Validate UUID (v1-v5) — the API expects a standard UUID format
  const isValidUUID = (id: string) => {
    if (!id) return false;
    const trimmed = id.trim();
    const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;
    return uuidRegex.test(trimmed);
  };

  const clearMessage = () => {
    if (timeoutRef.current) {
      window.clearTimeout(timeoutRef.current);
    }
    timeoutRef.current = window.setTimeout(() => setMessage(null), 5000);
  };

  React.useEffect(() => {
    return () => {
      if (timeoutRef.current) window.clearTimeout(timeoutRef.current);
    };
  }, []);

  const handleSyncInstrument = async () => {
    const trimmedId = instrumentId.trim();
    if (!trimmedId) {
      setMessage({ type: 'error', text: 'Vui lòng nhập instrument id.' });
      clearMessage();
      return;
    }

    if (!isValidUUID(trimmedId)) {
      setMessage({ type: 'error', text: 'Instrument id không hợp lệ. Vui lòng cung cấp UUID hợp lệ.' });
      clearMessage();
      return;
    }

    setIsSyncingOne(true);
    setMessage(null);
    try {
      // Directly call sync; if the instrument id does not exist backend will return 404
      const resp = await InstrumentApi.syncInstrumentConfiguration(trimmedId);
      if (resp && (resp.code === '200' || resp.code === 'OK')) {
        setMessage({ type: 'success', text: resp.message || 'Đồng bộ cấu hình cho instrument thành công.' });
      } else {
        setMessage({ type: 'error', text: resp?.message || 'Đồng bộ instrument thất bại.' });
      }
    } catch (err: unknown) {
      console.error('Sync instrument failed', err);
      // If axios error, show status and response body when available to help debugging
      const axiosErr = err as AxiosError;
      if (axiosErr && axiosErr.response) {
        const status = axiosErr.response.status;
        const data = axiosErr.response.data as Record<string, unknown> | undefined;

        // Prefer structured errors from backend when present
        const backendCode = data && typeof data['code'] === 'string' ? String(data['code']) : undefined;
        const backendMessage = data && typeof data['message'] === 'string' ? String(data['message']) : undefined;

        // Case A: Instrument not found — explicit backend code or 404
        if (backendCode === 'INSTRUMENT_NOT_FOUND' || (status === 404 && backendMessage)) {
          setMessage({ type: 'error', text: backendMessage || 'Instrument not found.' });
          console.info('Instrument not found response body:', data);
        }
        // Case B: Server side problem — don't show raw JSON to users, show a friendly message and log details
        else if (status >= 500) {
          setMessage({ type: 'error', text: 'Lỗi phía máy chủ khi đồng bộ. Vui lòng thử lại sau hoặc liên hệ bộ phận hỗ trợ.' });
          console.error('Server error response body:', data);
        }
        // Case C: Other client-side error (4xx) or application-level error — show backend message if available
        else {
          const userMessage = backendMessage ? backendMessage : `Sync failed: HTTP ${status}`;
          setMessage({ type: 'error', text: userMessage });
          console.info('Error response body:', data);
        }
      } else {
         const text = err instanceof Error ? err.message : String(err);
         setMessage({ type: 'error', text: `Lỗi khi sync: ${text}` });
       }
     } finally {
      setIsSyncingOne(false);
      clearMessage();
    }
  };

  const handleSyncAll = async () => {
    setIsSyncingAll(true);
    setMessage(null);
    try {
      const resp = await InstrumentApi.syncAllConfigurations();
      if (resp && (resp.code === '200' || resp.code === 'OK')) {
        setMessage({ type: 'success', text: resp.message || 'Đồng bộ tất cả cấu hình thành công.' });
      } else {
        setMessage({ type: 'error', text: resp?.message || 'Đồng bộ tất cả cấu hình thất bại.' });
      }
    } catch (err: unknown) {
      console.error('Sync all failed', err);
      const text = err instanceof Error ? err.message : String(err);
      setMessage({ type: 'error', text: `Lỗi khi sync all: ${text}` });
    } finally {
      setIsSyncingAll(false);
      clearMessage();
    }
  };

  return (
    <div className="p-6 bg-gray-100 dark:bg-gray-900 min-h-screen">
      <div className="max-w-3xl mx-auto">
        <div className="flex items-center justify-between mb-6">
          <div>
            <h1 className="text-2xl font-bold text-gray-900 dark:text-gray-100">Instrument Home</h1>
            <p className="text-sm text-gray-600 dark:text-gray-300">Trang quản lý đồng bộ cấu hình thiết bị.</p>
          </div>
          <div>
            <Link to="/instruments/dashboard" className="inline-flex items-center px-3 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700">Execute Tests</Link>
          </div>
        </div>

        <div className="bg-white dark:bg-gray-800 p-6 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700 space-y-4">
          <div>
            <label className="block text-sm font-medium text-gray-700 dark:text-gray-300">Instrument ID</label>
            <input
              type="text"
              value={instrumentId}
              onChange={(e) => setInstrumentId(e.target.value)}
              placeholder="Enter instrument id to sync"
              className="mt-1 block w-full rounded-md border-gray-300 dark:bg-gray-700 dark:border-gray-600 dark:text-gray-200 p-2"
            />
          </div>

          <div className="flex gap-3">
            <button
              type="button"
              onClick={handleSyncInstrument}
              disabled={isSyncingOne}
              className={`px-4 py-2 rounded-md text-white ${isSyncingOne ? 'bg-indigo-400 cursor-not-allowed' : 'bg-indigo-600 hover:bg-indigo-700'}`}>
              {isSyncingOne ? 'Syncing...' : 'Sync Instrument'}
            </button>

            <button
              type="button"
              onClick={handleSyncAll}
              disabled={isSyncingAll}
              className={`px-4 py-2 rounded-md text-white ${isSyncingAll ? 'bg-green-400 cursor-not-allowed' : 'bg-green-600 hover:bg-green-700'}`}>
              {isSyncingAll ? 'Syncing All...' : 'Sync All Configurations'}
            </button>
          </div>

          {message && (
            <div className={`p-3 rounded-md ${message.type === 'success' ? 'bg-green-50 text-green-700 border border-green-200' : message.type === 'error' ? 'bg-red-50 text-red-700 border border-red-200' : 'bg-yellow-50 text-yellow-700 border border-yellow-200'}`}>
              {message.text}
            </div>
          )}

          <div className="text-sm text-gray-500 dark:text-gray-400">
            Ghi chú: Sử dụng <strong>Sync Instrument</strong> để đồng bộ cấu hình từ backend cho 1 instrument cụ thể (phải có instrument id).
            <br />Sử dụng <strong>Sync All Configurations</strong> để đồng bộ cấu hình cho tất cả instrument trên hệ thống.
          </div>
        </div>
      </div>
    </div>
  );
};

export default InstrumentHome;
