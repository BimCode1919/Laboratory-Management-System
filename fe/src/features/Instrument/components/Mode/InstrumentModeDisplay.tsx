import React from 'react';
import type { Instrument, InstrumentMode } from '../../types/types';

interface Props {
  instrument: Instrument;
  onSwitchModeClick: () => void;
}

const InstrumentModeDisplay: React.FC<Props> = ({ instrument, onSwitchModeClick }) => {
  const { mode, name, model, serialNumber, location, configVersion, lastConfigSyncAt } = instrument;

  const getModeStyles = (mode: InstrumentMode) => {
    switch (mode) {
      case 'READY':
        return { bg: 'bg-green-100 dark:bg-green-900/20', text: 'text-green-800 dark:text-green-300', border: 'border-green-300 dark:border-green-700' };
      case 'MAINTENANCE':
        return { bg: 'bg-yellow-100 dark:bg-yellow-900/20', text: 'text-yellow-800 dark:text-yellow-300', border: 'border-yellow-300 dark:border-yellow-700' };
      case 'INACTIVE':
        return { bg: 'bg-red-100 dark:bg-red-900/20', text: 'text-red-800 dark:text-red-300', border: 'border-red-300 dark:border-red-700' };
      default:
        return { bg: 'bg-gray-100 dark:bg-gray-800', text: 'text-gray-800 dark:text-gray-300', border: 'border-gray-300 dark:border-gray-700' };
    }
  };

  const modeStyles = getModeStyles(mode);

  const formatDateTime = (iso?: string | null) => {
    if (!iso) return 'N/A';
    try {
      const d = new Date(iso);
      if (Number.isNaN(d.getTime())) return iso;
      return d.toLocaleString();
    } catch {
      return iso;
    }
  };

  return (
    <div className={`p-4 rounded-lg shadow-sm border ${modeStyles.border} ${modeStyles.bg}`}>
      <div className="flex justify-between items-start">
        <div>
          <h3 className="text-lg font-semibold text-gray-900 dark:text-gray-100">Trạng thái thiết bị</h3>
          <p className={`text-2xl font-bold ${modeStyles.text}`}>{mode}</p>
        </div>
        <button 
          onClick={onSwitchModeClick}
          className="px-4 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700 shadow-sm"
        >
          Change Mode
        </button>
      </div>
      <div className="mt-3 pt-3 border-t border-gray-200 dark:border-gray-700 grid grid-cols-2 gap-x-4">
        <div>
            <p className="text-sm text-gray-600 dark:text-gray-400"><strong>Name:</strong> {name}</p>
            <p className="text-sm text-gray-600 dark:text-gray-400"><strong>Model:</strong> {model}</p>
            <p className="text-sm text-gray-600 dark:text-gray-400"><strong>S/N:</strong> {serialNumber}</p>
        </div>
        <div>
            <p className="text-sm text-gray-600 dark:text-gray-400"><strong>Location:</strong> {location}</p>
            {/*<p className="text-sm text-gray-600 dark:text-gray-400"><strong>Connection:</strong> {isOnline ? <span className="text-green-600">Online</span> : <span className="text-red-600">Offline</span>}</p>*/}
            <p className="text-sm text-gray-600 dark:text-gray-400"><strong>Config Version:</strong> {configVersion ?? 'N/A'}</p>
            <p className="text-sm text-gray-600 dark:text-gray-400"><strong>Last Config Sync:</strong> {formatDateTime(lastConfigSyncAt)}</p>
        </div>
      </div>
    </div>
  );
};

export default InstrumentModeDisplay;
