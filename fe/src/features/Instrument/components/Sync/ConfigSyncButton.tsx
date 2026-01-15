import React, { useState } from 'react';
import { InstrumentApi } from '../../services/InstrumentAPI';

interface Props {
  instrumentId: string;
  onSyncComplete: () => void; // Callback to notify parent
}

const ConfigReagentSyncButton: React.FC<Props> = ({ instrumentId, onSyncComplete }) => {
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSync = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const response = await InstrumentApi.syncReagentsConfiguration(instrumentId);
      if (response.code === 'OK' || response.code === '200') {
        // Notify parent component to trigger a refresh
        onSyncComplete();
      } else {
        throw new Error(response.message || 'Sync failed with an unknown status.');
      }
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : String(err);
      setError(msg || "An unknown error occurred during reagent sync.");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="mt-6 bg-white dark:bg-gray-800 p-4 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700">
      <div className="flex justify-between items-center">
        <div>
            <h3 className="text-lg font-semibold text-gray-900 dark:text-gray-100">Reagent Configuration</h3>
            <p className="text-sm text-gray-500 dark:text-gray-400">Sync installed reagent configurations.</p>
        </div>
        <button
          onClick={handleSync}
          disabled={isLoading}
          className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:bg-blue-400 disabled:cursor-not-allowed"
        >
          {isLoading ? 'Syncing...' : 'Sync Reagents'}
        </button>
      </div>
      {error && (
        <p className="mt-3 text-sm text-red-600 dark:text-red-400">
          Error: {error}
        </p>
      )}
    </div>
  );
};

export default ConfigReagentSyncButton;
