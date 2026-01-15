import React, { useState, useEffect, useCallback } from 'react';
import { InstrumentApi } from '../../services/InstrumentAPI';
import type { InstalledReagent, ReagentInfo, InstallReagentPayload, UninstallReagentPayload } from '../../types/types';
import Alert from '../../../../components/ui/alert/Alert';
import type { AxiosError } from 'axios';

interface ReagentManagementProps {
  instrumentId: string;
  refreshListTrigger?: number;
}

const ReagentManagement: React.FC<ReagentManagementProps> = ({ instrumentId, refreshListTrigger }) => {
  const [installedReagents, setInstalledReagents] = useState<InstalledReagent[]>([]);
  const [warehouseReagents, setWarehouseReagents] = useState<ReagentInfo[]>([]);
  const [activeTab, setActiveTab] = useState('list');
  const [isLoading, setIsLoading] = useState(true);
  const [isWarehouseLoading, setIsWarehouseLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [warehouseError, setWarehouseError] = useState<string | null>(null);
  const [installingReagent, setInstallingReagent] = useState<ReagentInfo | null>(null);
  const [uninstallingReagent, setUninstallingReagent] = useState<InstalledReagent | null>(null);
  const [installQuantity, setInstallQuantity] = useState<number>(0);
  const [quantityToRemain, setQuantityToRemain] = useState<number>(0);
  const [isProcessing, setIsProcessing] = useState<boolean>(false);
  // Local alert state to replace default window.alert usages
  const [alertData, setAlertData] = useState<{
    variant: 'success' | 'error' | 'warning' | 'info';
    title: string;
    message: string;
  } | null>(null);
  const [alertTimeoutId, setAlertTimeoutId] = useState<number | null>(null);

  const showAlert = (variant: 'success' | 'error' | 'warning' | 'info', title: string, message: string, timeout = 5000) => {
    // clear previous timeout if any
    if (alertTimeoutId) {
      window.clearTimeout(alertTimeoutId);
      setAlertTimeoutId(null);
    }
    setAlertData({ variant, title, message });
    const id = window.setTimeout(() => {
      setAlertData(null);
      setAlertTimeoutId(null);
    }, timeout);
    setAlertTimeoutId(id);
  };

  const fetchInstalledReagents = useCallback((): Promise<InstalledReagent[]> => {
    if (!instrumentId) return Promise.resolve([] as InstalledReagent[]);
    setIsLoading(true);
    setError(null);
    return InstrumentApi.getInstalledReagents(instrumentId)
      .then(res => {
        setInstalledReagents(res);
        return res;
      })
      .catch(() => {
        setError("Failed to fetch installed reagents.");
        throw new Error('Failed to fetch installed reagents.');
      })
      .finally(() => setIsLoading(false));
  }, [instrumentId]);

  const fetchWarehouseReagents = useCallback(() => {
    setIsWarehouseLoading(true);
    setWarehouseError(null);
    return InstrumentApi.getReagentsFromWarehouse()
      .then(res => {
        setWarehouseReagents(res);
        return res;
      })
      .catch(() => {
        setWarehouseError("Failed to fetch reagents from instrument-service internal endpoint.");
        throw new Error('Failed to fetch reagents from warehouse.');
      })
      .finally(() => setIsWarehouseLoading(false));
  }, []);

  useEffect(() => {
    if (activeTab === 'list') {
      fetchInstalledReagents();
    } else if (activeTab === 'install') {
      fetchWarehouseReagents();
    }
  }, [activeTab, fetchInstalledReagents, fetchWarehouseReagents]);

  // When parent triggers refreshListTrigger, switch to 'list' and reload installed reagents
  React.useEffect(() => {
    if (typeof refreshListTrigger !== 'undefined') {
      setActiveTab('list');
      // fetchInstalledReagents returns a promise
      fetchInstalledReagents().catch(() => {
        // already handled inside fetchInstalledReagents
      });
    }
  }, [refreshListTrigger, fetchInstalledReagents]);

  // Clear any pending alert timeout on unmount
  useEffect(() => {
    return () => {
      if (alertTimeoutId) {
        window.clearTimeout(alertTimeoutId);
      }
    };
  }, [alertTimeoutId]);

  const handleInstallClick = (reagent: ReagentInfo) => {
    setInstallingReagent(reagent);
    setInstallQuantity(0);
  };

  const handleUninstallClick = (reagent: InstalledReagent) => {
    setUninstallingReagent(reagent);
    setQuantityToRemain(0);
  };

  const handleInstall = async () => {
    if (!installingReagent || !instrumentId) return;

    // Basic UUID validation for instrumentId and reagentId to give faster client-side feedback
    const uuidRegex = /^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$/i;
    if (!uuidRegex.test(instrumentId)) {
      showAlert('error', 'Invalid Instrument ID', 'Instrument ID must be a valid UUID.');
      return;
    }
    if (!uuidRegex.test(String(installingReagent.reagentId))) {
      showAlert('error', 'Invalid Reagent ID', 'Selected reagent has an invalid id.');
      return;
    }

    if (isNaN(installQuantity) || installQuantity <= 0) {
      showAlert('error', 'Invalid Quantity', 'Quantity must be a positive number.');
      return;
    }

    const payload: InstallReagentPayload = {
      reagentId: installingReagent.reagentId,
      quantity: installQuantity,
    };

    setIsProcessing(true);
    try {
      const apiResponse = await InstrumentApi.installReagent(instrumentId, payload);
      // The backend returns an ApiResponse where data contains eventType and payload array
      if (apiResponse && apiResponse.data) {
        type EventResult = { payload?: unknown; reagentName?: string; quantityRemaining?: number; lotNumber?: string; unit?: string; status?: string; reason?: string };
        const event = apiResponse.data as unknown as EventResult;
        const eventPayload = Array.isArray(event.payload) ? (event.payload as unknown[])[0] : event.payload;
        const payloadObj = eventPayload as Record<string, unknown> | undefined;

        if (payloadObj && typeof payloadObj.status === 'string' && payloadObj.status === 'INSTALLED') {
          // Success: Refresh lists and show a friendly message
          setInstallingReagent(null);
          setInstallQuantity(0);
          await Promise.all([fetchInstalledReagents(), fetchWarehouseReagents()]);
          setActiveTab('list');
          // Show a success toast / alert with installed details
          const name = (typeof payloadObj.reagentName === 'string' && payloadObj.reagentName) || installingReagent.name;
          const qty = (typeof payloadObj.quantityRemaining === 'number' && payloadObj.quantityRemaining) ?? installQuantity;
          const lot = typeof payloadObj.lotNumber === 'string' ? payloadObj.lotNumber : '';
          const unit = typeof payloadObj.unit === 'string' ? payloadObj.unit : '';
          showAlert('success', 'Installation Successful', `Installed ${name} (lot: ${lot}) \u2014 remaining: ${qty} ${unit}`);
        } else if (payloadObj && typeof payloadObj.status === 'string' && payloadObj.status === 'FAIL') {
          // Failure: show reason from payload
          const reason = (typeof payloadObj.reason === 'string' && payloadObj.reason) || apiResponse.message || 'Install failed';
          showAlert('error', 'Installation Failed', `Failed to install reagent: ${reason}`);
        } else {
          // Fallback: If the service returned a non-standard response, try to treat data as InstalledReagent
          if (apiResponse.data && typeof apiResponse.data === 'object' && 'installedReagentId' in apiResponse.data) {
            setInstallingReagent(null);
            setInstallQuantity(0);
            await Promise.all([fetchInstalledReagents(), fetchWarehouseReagents()]);
            setActiveTab('list');
            showAlert('success', 'Installation Successful', 'Reagent installed successfully.');
          } else {
            throw new Error(apiResponse.message || 'Unexpected response from install API');
          }
        }
      } else {
        throw new Error('No response data from install API');
      }
    } catch (err: unknown) {
      console.error('Failed to install reagent', err);

      // Try to normalize axios error shapes to produce clearer messages
      const axiosErr = err as AxiosError & { response?: { status?: number; data?: unknown } };
      const resp = axiosErr?.response;
      if (resp && resp.data) {
        const data = resp.data as Record<string, unknown>;
        const status = resp.status;

        // Case: warehouse reports reagent low
        if ((data.code === 'REAGENT_LOW' as unknown) || status === 409) {
          const message = (typeof data.message === 'string' && data.message) || 'Reagent level is low in warehouse.';
          showAlert('error', 'Reagent Low', message);
        }
        // Case: backend internal error (example: invalid reagent id produced INTERNAL_ERROR)
        else if (data.code === 'INTERNAL_ERROR' || status === 500) {
          const message = (typeof data.message === 'string' && data.message) || 'Unexpected internal server error.';
          // If backend doesn't provide helpful detail, provide a pragmatic hint
          const hint = message.toLowerCase().includes('reagent') ? 'Please verify the reagent id.' : '';
          showAlert('error', 'Server Error', `${message}${hint ? ' — ' + hint : ''}`);
        }
        // Case: some services return 200 but embed an event payload with failure (instrument not found)
        else if (data.data && typeof data.data === 'object') {
          const embedded = data.data as Record<string, unknown>;
          const payloadArr = embedded.payload as unknown;
          if (Array.isArray(payloadArr) && payloadArr.length > 0) {
            const first = payloadArr[0] as Record<string, unknown>;
            if (first.reason && typeof first.reason === 'string') {
              showAlert('error', 'Installation Failed', String(first.reason));
            } else {
              showAlert('error', 'Installation Failed', String(embedded.message ?? 'Failed to install reagent.'));
            }
          } else {
            showAlert('error', 'Installation Error', String(data.message ?? 'Failed to install reagent.'));
          }
        }
        // Fallback to any message provided by backend
        else {
          showAlert('error', 'Installation Error', String(data.message ?? 'Failed to install reagent.'));
        }
      } else {
        const message = err instanceof Error ? err.message : String(err);
        showAlert('error', 'Installation Error', `Failed to install reagent: ${message}`);
      }
    } finally {
      setIsProcessing(false);
    }
  };

  const handleUninstall = async () => {
    if (!uninstallingReagent || !instrumentId) return;

    if (isNaN(quantityToRemain) || quantityToRemain < 0 || quantityToRemain > uninstallingReagent.quantityRemaining) {
      showAlert('error', 'Invalid Quantity', `"New Quantity Remaining" must be a valid number, and less than the current quantity (${uninstallingReagent.quantityRemaining}).`);
      return;
    }

    const payload: UninstallReagentPayload = {
      reagentId: uninstallingReagent.reagentId,
      quantityRemaining: quantityToRemain,
    };

    setIsProcessing(true);
    try {
      await InstrumentApi.uninstallReagent(instrumentId, payload);
      setUninstallingReagent(null);
      setQuantityToRemain(0);
      await Promise.all([fetchInstalledReagents(), fetchWarehouseReagents()]);
      showAlert('success', 'Uninstallation Successful', 'Reagent uninstalled successfully.');
    } catch (err: unknown) {
      console.error('Failed to uninstall reagent', err);
      const message = err instanceof Error ? err.message : String(err);
      showAlert('error', 'Uninstallation Error', `Failed to uninstall reagent: ${message}`);
    } finally {
      setIsProcessing(false);
    }
  };

  const handleInUseChange = async (reagent: InstalledReagent, inUse: boolean) => {
    // Optimistic update
    const originalReagents = installedReagents;
    const newReagents = originalReagents.map(r => 
      r.installedReagentId === reagent.installedReagentId ? { ...r, inUse } : r
    );
    setInstalledReagents(newReagents);

    try {
      // Use reagent.reagentId as per API design consistency
      const updatedReagent = await InstrumentApi.updateReagentInUseStatus(instrumentId, reagent.reagentId, inUse);
      // API call was successful, update state with the definitive response
      setInstalledReagents(prevReagents =>
        prevReagents.map(r =>
          r.installedReagentId === updatedReagent.installedReagentId ? updatedReagent : r
        )
      );
    } catch (err) {
      console.error('Failed to update "in use" status', err);
      showAlert('error', 'Status Update Failed', 'Failed to update status. Please try again.');
      // Revert UI change on failure
      setInstalledReagents(originalReagents);
    }
  };

  const ReagentRow: React.FC<{ reagent: ReagentInfo, onInstall: (reagent: ReagentInfo) => void }> = ({ reagent, onInstall }) => (
    <tr className="hover:bg-gray-50 dark:hover:bg-gray-700">
      {/* reagentId intentionally not displayed in UI (kept in types for later use) */}
      <td className="px-4 py-3 whitespace-nowrap text-sm text-gray-500 dark:text-gray-100">{reagent.name}</td>
      <td className="px-4 py-3 whitespace-nowrap text-sm text-gray-500 dark:text-gray-100">{reagent.catalogNumber}</td>
      <td className="px-4 py-3 whitespace-nowrap text-sm text-gray-500 dark:text-gray-100">{reagent.manufacturer}</td>
      <td className="px-4 py-3 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">{reagent.casNumber ?? '-'}</td>
      <td className="px-4 py-3 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">{reagent.quantity}</td>
      <td className="px-4 py-3 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">{reagent.totalQuantity ?? '-'}</td>
      <td className="px-4 py-3 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">{reagent.expirationDate ?? '-'}</td>
      {/*<td className="px-4 py-3 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">{reagent.createdBy ?? '-'}</td>*/}
      {/*<td className="px-4 py-3 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">{reagent.updatedBy ?? '-'}</td>*/}
      {/*<td className="px-4 py-3 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">{reagent.createdAt ?? '-'}</td>*/}
      {/*<td className="px-4 py-3 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">{reagent.updatedAt ?? '-'}</td>*/}
      <td className="px-3 py-2 text-sm">
        <button onClick={() => onInstall(reagent)} className='text-indigo-600 hover:text-indigo-900 dark:text-indigo-400 dark:hover:text-indigo-200'>Install</button>
      </td>
    </tr>
  );

  return (
    <div className="mt-6 bg-white dark:bg-gray-800 p-4 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700">
      <h3 className="text-lg font-semibold mb-4 text-gray-900 dark:text-gray-100">Reagents Management</h3>
      <div className="border-b border-gray-200 dark:border-gray-700">
        <nav className="-mb-px flex space-x-6">
          <button onClick={() => setActiveTab('list')} className={`py-2 px-1 border-b-2 font-medium text-sm ${activeTab === 'list' ? 'border-indigo-500 text-indigo-600 dark:text-indigo-400 dark:border-indigo-400' : 'border-transparent text-gray-500 dark:text-gray-400 hover:text-gray-700 dark:hover:text-gray-300 hover:border-gray-300 dark:hover:border-gray-600'}`}>List</button>
          <button onClick={() => setActiveTab('install')} className={`py-2 px-1 border-b-2 font-medium text-sm ${activeTab === 'install' ? 'border-indigo-500 text-indigo-600 dark:text-indigo-400 dark:border-indigo-400' : 'border-transparent text-gray-500 dark:text-gray-400 hover:text-gamma-700 dark:hover:text-gray-300 hover:border-gray-300 dark:hover:border-gray-600'}`}>Install</button>
        </nav>
      </div>

      {activeTab === 'list' && (
        <div className="mt-4">
          {isLoading ? <p className="text-gray-500 dark:text-gray-400">Loading...</p> : error ? <p className="text-red-500">{error}</p> : (
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
                <thead className="bg-gray-50 dark:bg-gray-700">
                  <tr>
                    <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Name</th>
                    <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Lot Number</th>
                    <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Remaining</th>
                    <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">In Use</th>
                    <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Action</th>
                  </tr>
                </thead>
                <tbody className="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
                  {installedReagents.map(r => (
                    <tr key={r.installedReagentId}>
                      <td className="px-4 py-3 whitespace-nowrap text-sm text-gray-500 dark:text-gray-100">{r.reagentName}</td>
                      <td className="px-4 py-3 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">{r.lotNumber}</td>
                      <td className="px-4 py-3 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">{r.quantityRemaining} {r.unit}</td>
                      <td className="px-4 py-3 whitespace-nowrap">
                        <label className="relative inline-flex items-center cursor-pointer">
                          <input
                            type="checkbox"
                            checked={r.inUse}
                            onChange={(e) => handleInUseChange(r, e.target.checked)}
                            className="sr-only peer"
                          />
                          <div className="w-11 h-6 bg-gray-200 rounded-full peer dark:bg-gray-700 peer-checked:after:translate-x-full peer-checked:after:border-white after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:border-gray-300 after:border after:rounded-full after:h-5 after:w-5 after:transition-all dark:border-gray-600 peer-checked:bg-indigo-600"></div>
                        </label>
                      </td>
                      <td className="px-4 py-3 whitespace-nowrap text-sm font-medium">
                        <button onClick={() => handleUninstallClick(r)} className="text-red-600 hover:text-red-900 dark:text-red-400 dark:hover:text-red-200">Uninstall</button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {activeTab === 'install' && (
        <div className="mt-4">
          {isWarehouseLoading ? <p className="text-gray-500 dark:text-gray-400">Loading available reagents from instrument-service...</p> : warehouseError ? <p className="text-red-500 dark:text-red-400">{warehouseError}</p> : (
            <div className="overflow-x-auto">
              <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
                <thead className="bg-gray-50 dark:bg-gray-700">
                  <tr>
                    <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Name</th>
                    <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Catalog Number</th>
                    <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Manufacturer</th>
                    <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Cas Number</th>
                    <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Quantity</th>
                    <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Total Quantity</th>
                    <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Expiration Date</th>
                    {/*<th className="px-4 py-2 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Created By</th>*/}
                    {/*<th className="px-4 py-2 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Updated By</th>*/}
                    {/*<th className="px-4 py-2 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Created At</th>*/}
                    {/*<th className="px-4 py-2 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Updated At</th>*/}
                    <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Action</th>
                  </tr>
                </thead>
                <tbody className="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
                  {warehouseReagents.map(r => <ReagentRow key={r.reagentId} reagent={r} onInstall={handleInstallClick} />)}
                </tbody>
              </table>
            </div>
          )}
        </div>
      )}

      {/* Install Modal */}
      {installingReagent && (
        <div className="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity">
          <div className="fixed inset-0 z-10 overflow-y-auto">
            <div className="flex min-h-full items-end justify-center p-4 text-center sm:items-center sm:p-0">
              <div className="relative transform overflow-hidden rounded-lg bg-white dark:bg-gray-800 text-left shadow-xl transition-all sm:my-8 sm:w-full sm:max-w-lg">
                <div className="bg-white dark:bg-gray-800 px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
                  <h3 className="text-lg font-medium leading-6 text-gray-900 dark:text-gray-100">Install Reagent: {installingReagent.name}</h3>
                  <div className="mt-2">
                    <p className="text-sm text-gray-500 dark:text-gray-400">Enter the quantity to install.</p>
                    <div className="mt-4">
                      <label htmlFor='quantity' className='block text-sm font-medium text-gray-700 dark:text-gray-300'>Quantity</label>
                      <input 
                        type='number' 
                        id='quantity' 
                        value={installQuantity} 
                        onChange={e => setInstallQuantity(Number(e.target.value))} 
                        className='mt-1 block w-full px-3 py-2 bg-white dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm text-gray-900 dark:text-gray-100' 
                      />
                    </div>
                  </div>
                </div>
                <div className="bg-gray-50 dark:bg-gray-700 px-4 py-3 sm:flex sm:flex-row-reverse sm:px-6">
                  <button
                    type="button"
                    onClick={handleInstall}
                    className="inline-flex w-full justify-center rounded-md border border-transparent bg-indigo-600 px-4 py-2 text-base font-medium text-white shadow-sm hover:bg-indigo-700 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 sm:ml-3 sm:w-auto sm:text-sm"
                    disabled={isProcessing}
                  >
                    {isProcessing ? 'Installing...' : 'Install'}
                  </button>
                  <button
                    type="button"
                    onClick={() => setInstallingReagent(null)}
                    className="mt-3 inline-flex w-full justify-center rounded-md border border-gray-300 bg-white px-4 py-2 text-base font-medium text-gray-700 shadow-sm hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 sm:mt-0 sm:ml-3 sm:w-auto sm:text-sm dark:bg-gray-600 dark:text-gray-200 dark:hover:bg-gray-500"
                  >
                    Cancel
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Uninstall Modal */}
      {uninstallingReagent && (
        <div className="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity">
          <div className="fixed inset-0 z-10 overflow-y-auto">
            <div className="flex min-h-full items-end justify-center p-4 text-center sm:items-center sm:p-0">
              <div className="relative transform overflow-hidden rounded-lg bg-white dark:bg-gray-800 text-left shadow-xl transition-all sm:my-8 sm:w-full sm:max-w-lg">
                <div className="bg-white dark:bg-gray-800 px-4 pt-5 pb-4 sm:p-6 sm:pb-4">
                  <h3 className="text-lg font-medium leading-6 text-gray-900 dark:text-gray-100">Uninstall Reagent</h3>
                  <div className="mt-2">
                    <p className="text-sm text-gray-500 dark:text-gray-400">Current quantity of {uninstallingReagent.reagentName} (Lot: {uninstallingReagent.lotNumber}) is {uninstallingReagent.quantityRemaining}.</p>
                    <div className="mt-4">
                      <label htmlFor="quantity-to-remain" className="block text-sm font-medium text-gray-700 dark:text-gray-300">New Quantity Remaining</label>
                      <input
                        type="number"
                        id="quantity-to-remain"
                        value={quantityToRemain}
                        onChange={e => setQuantityToRemain(Number(e.target.value))}
                        className="mt-1 block w-full px-3 py-2 bg-white dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-md shadow-sm focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm text-gray-900 dark:text-gray-100"
                      />
                    </div>
                  </div>
                </div>
                <div className="bg-gray-50 dark:bg-gray-700 px-4 py-3 sm:flex sm:flex-row-reverse sm:px-6">
                  <button
                    type="button"
                    onClick={handleUninstall}
                    className="inline-flex w-full justify-center rounded-md border border-transparent bg-red-600 px-4 py-2 text-base font-medium text-white shadow-sm hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-red-500 focus:ring-offset-2 sm:ml-3 sm:w-auto sm:text-sm"
                    disabled={isProcessing}
                  >
                    {isProcessing ? 'Uninstalling...' : 'Confirm'}
                  </button>
                  <button
                    type="button"
                    onClick={() => setUninstallingReagent(null)}
                    className="mt-3 inline-flex w-full justify-center rounded-md border border-gray-300 bg-white px-4 py-2 text-base font-medium text-gray-700 shadow-sm hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:ring-offset-2 sm:mt-0 sm:ml-3 sm:w-auto sm:text-sm dark:bg-gray-600 dark:text-gray-200 dark:hover:bg-gray-500"
                  >
                    Cancel
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* Alert Component */}
      {alertData && (
        <div className="fixed top-28 right-3 z-50 w-full max-w-sm">
          <div className="flex items-start gap-3">
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
                onClick={() => setAlertData(null)}
                className="text-gray-500 hover:text-gray-700 dark:text-gray-300 dark:hover:text-white p-1"
              >
                ×
              </button>
            </div>
          </div>
        </div>
      )}

    </div>
  );
};

export default ReagentManagement;
