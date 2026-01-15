import React from 'react';
import type { Configuration } from '../../types/types';

interface ConfigurationDisplayProps {
  configurations: Configuration[];
  selectedTestType?: string;
  onSelectTestType?: (type: string) => void;
}

const ConfigurationDisplay: React.FC<ConfigurationDisplayProps> = ({ configurations, selectedTestType = '', onSelectTestType }) => {
  if (!configurations || configurations.length === 0) {
    return (
      <div className="p-4 bg-white dark:bg-gray-800 rounded-lg shadow-sm">
        <h3 className="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-2">Configurations</h3>
        <p className="text-gray-600 dark:text-gray-400">No configuration data available.</p>
      </div>
    );
  }

  // Extract unique test types from configurations (prefer configKey but fallback to configValue)
  const testTypes = Array.from(new Set(configurations.map(c => c.configKey || c.configValue).filter(Boolean))) as string[];

  const handleCardClick = (configKey?: string) => {
    if (!onSelectTestType) return;
    const newValue = configKey && configKey === selectedTestType ? '' : (configKey || '');
    onSelectTestType(newValue);
  };

  return (
    <div className="p-4 bg-white dark:bg-gray-800 rounded-lg shadow-sm">
      <h3 className="text-lg font-semibold text-gray-900 dark:text-gray-100 mb-4">Instrument Configurations</h3>

      <div className="mb-3">
        <label htmlFor="configType" className="block text-sm font-medium text-gray-700 dark:text-gray-300">Select Config Type</label>
        <select
          id="configType"
          value={selectedTestType}
          onChange={(e) => onSelectTestType && onSelectTestType(e.target.value)}
          className="p-2 mt-1 block w-full rounded-md border-gray-300 shadow-sm focus:border-indigo-500 focus:ring-indigo-500 dark:bg-gray-700 dark:border-gray-600 dark:text-gray-200"
        >
          <option value="">All</option>
          {testTypes.map(t => (
            <option key={t} value={t}>{t}</option>
          ))}
        </select>
      </div>

      <div className="space-y-3">
        {configurations.map((config) => {
          const key = config.configKey || config.configValue || config.configId;
          const isSelected = key === selectedTestType;

          return (
            <div
              key={config.configId}
              role={onSelectTestType ? 'button' : undefined}
              tabIndex={onSelectTestType ? 0 : undefined}
              aria-pressed={onSelectTestType ? isSelected : undefined}
              onClick={onSelectTestType ? () => handleCardClick(key) : undefined}
              onKeyDown={onSelectTestType ? (e) => { if (e.key === 'Enter' || e.key === ' ') { e.preventDefault(); handleCardClick(key); } } : undefined}
              className={`p-3 rounded-md cursor-pointer focus:outline-none focus:ring-2 focus:ring-indigo-500 ${isSelected ? 'bg-indigo-50 dark:bg-indigo-900/30 border border-indigo-400 dark:border-indigo-700' : 'bg-gray-50 dark:bg-gray-700'} h-40`}
            >
              <div className="h-full flex flex-col">
                <div className="flex-1 overflow-auto">
                  <p className="font-medium text-gray-800 dark:text-gray-200">{config.configName}</p>
                  <p className="text-sm text-gray-600 dark:text-gray-400 mt-2">{config.description}</p>
                </div>
                <div className="mt-2">
                  <p className="text-sm text-gray-600 dark:text-gray-400">
                    Value: <span className="font-mono bg-gray-200 dark:bg-gray-600 px-1 rounded">{config.configValue}</span>
                  </p>
                </div>
              </div>
            </div>
         );
       })}
      </div>
    </div>
  );
};

export default ConfigurationDisplay;
