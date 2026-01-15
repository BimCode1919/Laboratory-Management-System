import React, { useState } from 'react';
import { InstrumentApi } from '../../services/InstrumentAPI';
import type { InstrumentMode, ChangeModeResult } from '../../types/types';

interface Props {
  currentMode: InstrumentMode;
  instrumentId: string;
  onClose: () => void;
  onSuccess: (result: ChangeModeResult) => void;
}

const ModeSwitchModal: React.FC<Props> = ({ currentMode, instrumentId, onClose, onSuccess }) => {
  const [newMode, setNewMode] = useState<InstrumentMode>(currentMode);
  const [reason, setReason] = useState('');
  const [qcConfirmed, setQcConfirmed] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const [error, setError] = useState('');
  const [fieldErrors, setFieldErrors] = useState<{ reason?: string, qcCheck?: string }>({});

  const handleSave = async () => {
    setError('');
    setFieldErrors({});
    setIsSaving(true);

    // Guard: don't save if mode hasn't changed
    if (newMode === currentMode) {
      setError('Chưa có thay đổi chế độ — không thể lưu.');
      setIsSaving(false);
      return;
    }

    // Guard: disallow direct switch between MAINTENANCE <-> INACTIVE
    if ((currentMode === 'MAINTENANCE' && newMode === 'INACTIVE') || (currentMode === 'INACTIVE' && newMode === 'MAINTENANCE')) {
      setError('Không được chuyển trực tiếp giữa MAINTENANCE và INACTIVE. Vui lòng chọn trạng thái trung gian (ví dụ READY).');
      setIsSaving(false);
      return;
    }

    const errors: { reason?: string, qcCheck?: string } = {};

    if ((newMode === 'MAINTENANCE' || newMode === 'INACTIVE') && !reason) {
      errors.reason = "Lý do là bắt buộc.";
    }

    if (newMode === 'READY' && !qcConfirmed) {
      errors.qcCheck = 'Bạn phải xác nhận thiết bị đã đạt kiểm tra QC.';
    }

    if (Object.keys(errors).length > 0) {
      setFieldErrors(errors);
      setIsSaving(false);
      return;
    }

    try {
      const payload: { mode: InstrumentMode; reason?: string } = {
        mode: newMode,
        reason: newMode === 'READY' ? 'QC Check Confirmed' : reason,
      };

      const result = await InstrumentApi.changeMode(instrumentId, payload);

      onSuccess(result);
      onClose();

    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : String(err);
      setError(msg || 'Đã có lỗi xảy ra. Vui lòng thử lại.');
    } finally {
      setIsSaving(false);
    }
  };

  // Determine if save should be disabled (used for button)
  const isProhibitedTransition = (currentMode === 'MAINTENANCE' && newMode === 'INACTIVE') || (currentMode === 'INACTIVE' && newMode === 'MAINTENANCE');
  const noChange = newMode === currentMode;

  return (
    <div className="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity">
      <div className="fixed inset-0 z-10 overflow-y-auto">
        <div className="flex min-h-full items-end justify-center p-4 text-center sm:items-center sm:p-0">
          <div className="relative transform overflow-hidden rounded-lg bg-white dark:bg-gray-800 text-left shadow-xl transition-all sm:my-8 sm:w-full sm:max-w-lg">
            <div className="bg-white dark:bg-gray-800 px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
              <h3 className="text-lg font-medium leading-6 text-gray-900 dark:text-gray-100">Thay đổi Chế độ Thiết bị</h3>
              <div className="mt-4">
                <div className="mb-4">
                  <label className="block text-sm font-medium text-gray-700 dark:text-gray-300">Chế độ mới</label>
                  <select
                    value={newMode}
                    onChange={(e) => setNewMode(e.target.value as InstrumentMode)}
                    className="mt-1 block w-full px-3 py-2 bg-white dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm text-gray-900 dark:text-gray-100"
                  >
                    <option value="READY">Ready</option>
                    <option value="MAINTENANCE">Maintenance</option>
                    <option value="INACTIVE">Inactive</option>
                  </select>
                  {noChange && <p className="text-sm text-gray-500 dark:text-gray-400 mt-2">Bạn đang chọn chế độ hiện tại — không có gì để lưu.</p>}
                  {isProhibitedTransition && <p className="text-sm text-red-600 dark:text-red-400 mt-2">Không được chuyển trực tiếp giữa Maintenance và Inactive.</p>}
                </div>

                {(newMode === 'MAINTENANCE' || newMode === 'INACTIVE') && (
                  <div className="mb-4">
                    <label className="block text-sm font-medium text-gray-700 dark:text-gray-300">Lý do thay đổi</label>
                    <textarea
                      value={reason}
                      onChange={(e) => setReason(e.target.value)}
                      rows={4}
                      className={`mt-1 block w-full px-3 py-2 bg-white dark:bg-gray-700 border dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm text-gray-900 dark:text-gray-100 ${fieldErrors.reason ? 'border-red-500' : 'border-gray-300'}`}
                      placeholder="Nhập lý do thay đổi..."
                    />
                    {fieldErrors.reason && <p className="text-red-500 dark:text-red-400 text-xs mt-1">{fieldErrors.reason}</p>}
                  </div>
                )}

                {newMode === 'READY' && (
                  <div className="mb-4">
                    <div className="flex items-start">
                      <div className="flex items-center h-5">
                        <input
                          id="qc-check"
                          type="checkbox"
                          checked={qcConfirmed}
                          onChange={(e) => setQcConfirmed(e.target.checked)}
                          className="focus:ring-indigo-500 h-4 w-4 text-indigo-600 border-gray-300 rounded"
                        />
                      </div>
                      <div className="ml-3 text-sm">
                        <label htmlFor="qc-check" className="font-medium text-gray-700 dark:text-gray-300">
                          Xác nhận thiết bị đã đạt kiểm tra QC
                        </label>
                        <p className="text-gray-500 dark:text-gray-400">QC Check Confirmed</p>
                      </div>
                    </div>
                    {fieldErrors.qcCheck && <p className="text-red-500 dark:text-red-400 text-xs mt-1">{fieldErrors.qcCheck}</p>}
                  </div>
                )}
              </div>
              {error && <p className="text-red-500 dark:text-red-400 text-sm mb-4 bg-red-100 dark:bg-red-900/20 p-3 rounded-md">{error}</p>}
            </div>
            <div className="bg-gray-50 dark:bg-gray-700 px-4 py-3 sm:flex sm:flex-row-reverse sm:px-6">
              <button
                type="button"
                onClick={handleSave}
                disabled={isSaving || noChange || isProhibitedTransition}
                className={`inline-flex w-full justify-center rounded-md border border-transparent bg-indigo-600 px-4 py-2 text-base font-medium text-white shadow-sm hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 sm:ml-3 sm:w-auto sm:text-sm ${isSaving || noChange || isProhibitedTransition ? 'opacity-50 cursor-not-allowed' : ''}`}
              >
                {isSaving ? 'Đang lưu...' : 'Lưu thay đổi'}
              </button>
              <button
                type="button"
                onClick={onClose}
                disabled={isSaving}
                className="mt-3 inline-flex w-full justify-center rounded-md border border-gray-300 bg-white px-4 py-2 text-base font-medium text-gray-700 shadow-sm hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 sm:mt-0 sm:ml-3 sm:w-auto sm:text-sm dark:bg-gray-600 dark:text-gray-200 dark:hover:bg-gray-500"
              >
                Hủy
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ModeSwitchModal;
