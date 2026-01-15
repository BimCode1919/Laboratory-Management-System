import React, { useState, useEffect } from 'react';
import type { RawTestResult, PaginatedApiResponse } from '../../types/types';
import { InstrumentApi } from '../../services/InstrumentAPI';
import RawHl7DataViewerModal from './RawHl7DataViewerModal';
import DeleteRawDataModal from './DeleteRawDataModal';

export type RawTestResultView = 'all' | 'backedUp' | 'notBackedUp';

interface Props {
  results: RawTestResult[];
  paginationData: PaginatedApiResponse<RawTestResult> | null;
  onPageChange: (page: number) => void;
  onDataChange: () => void;
  onFilterChange: (filter: RawTestResultView) => void;
  currentFilter: RawTestResultView;
}

const RawResultsTable: React.FC<Props> = ({ 
  results, 
  paginationData, 
  onPageChange, 
  onDataChange,
  onFilterChange,
  currentFilter 
}) => {
  const [isRefreshing, setIsRefreshing] = useState(false);
  const [itemToView, setItemToView] = useState<RawTestResult | null>(null);
  const [itemToDelete, setItemToDelete] = useState<RawTestResult | null>(null);
  const [selectedItems, setSelectedItems] = useState<RawTestResult[]>([]);
  const [isDeleting, setIsDeleting] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [isBulkDeleteConfirmOpen, setBulkDeleteConfirmOpen] = useState(false);

  const canDelete = currentFilter === 'backedUp';

  useEffect(() => {
    setSelectedItems([]);
  }, [results, currentFilter]);

  const getStatusChip = (status: RawTestResult['status']) => {
    switch (status) {
      case 'SENT':
      case 'ACKNOWLEDGED':
        return <span className="px-2 py-1 text-xs font-semibold rounded-full bg-green-100 text-green-800 dark:bg-green-900/30 dark:text-green-300">{status}</span>;
      case 'QUEUED':
        return <span className="px-2 py-1 text-xs font-semibold rounded-full bg-yellow-100 text-yellow-800 dark:bg-yellow-900/30 dark:text-yellow-300">{status}</span>;
      case 'FAILED':
        return <span className="px-2 py-1 text-xs font-semibold rounded-full bg-red-100 text-red-800 dark:bg-red-900/30 dark:text-red-300">{status}</span>;
      default:
        return <span className="px-2 py-1 text-xs font-semibold rounded-full bg-gray-100 text-gray-800 dark:bg-gray-900/30 dark:text-gray-300">{status}</span>;
    }
  };

  const handleSelectAll = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.checked) {
      setSelectedItems(results);
    } else {
      setSelectedItems([]);
    }
  };

  const handleSelectItem = (item: RawTestResult, checked: boolean) => {
    if (checked) {
      setSelectedItems(prev => [...prev, item]);
    } else {
      setSelectedItems(prev => prev.filter(i => i.id !== item.id));
    }
  };

  const handlePrevPage = () => {
    if (paginationData && !paginationData.first) {
      onPageChange(paginationData.number - 1);
    }
  };

  const handleNextPage = () => {
    if (paginationData && !paginationData.last) {
      onPageChange(paginationData.number + 1);
    }
  };

  const handleDeleteRequest = (result: RawTestResult) => {
    setError(null);
    setItemToDelete(result);
  };

  const confirmDelete = async () => {
    if (!itemToDelete || !canDelete) return;

    setIsDeleting(true);
    setError(null);
    try {
      await InstrumentApi.deleteRawTestResult(itemToDelete.runId, itemToDelete.barcode);
      setItemToDelete(null);
      onDataChange();
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : String(err);
      setError(msg);
      setItemToDelete(null);
    } finally {
      setIsDeleting(false);
    }
  };

  const confirmBulkDelete = async () => {
    if (selectedItems.length === 0 || !canDelete) return;

    setIsDeleting(true);
    setError(null);
    const failedDeletes: string[] = [];
    try {
      for (const item of selectedItems) {
        try {
          await InstrumentApi.deleteRawTestResult(item.runId, item.barcode);
        } catch {
          // record failed barcode without referencing the caught error variable
          failedDeletes.push(item.barcode);
        }
      }

      if (failedDeletes.length > 0) {
        const msg = `Failed to delete barcodes: ${failedDeletes.join(', ')}`;
        setError(msg);
      } else {
        setSelectedItems([]);
        onDataChange();
      }
    } catch (err: unknown) {
      const msg = err instanceof Error ? err.message : String(err);
      setError(msg);
    } finally {
      setIsDeleting(false);
      setBulkDeleteConfirmOpen(false);
    }
  };

  return (
    <div className="mt-6 bg-white dark:bg-gray-800 p-4 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700">
      <div className="flex justify-between items-center mb-4">
        <h3 className="text-lg font-semibold text-gray-900 dark:text-gray-100">Raw Test Results</h3>
        <div className="flex items-center space-x-4">
          <button
            onClick={async () => { setError(null); setIsRefreshing(true); try { await onDataChange(); } finally { setTimeout(() => setIsRefreshing(false), 300); } }}
            className={`px-3 py-2 bg-gray-200 dark:bg-gray-700 text-sm rounded-md ${isRefreshing ? 'opacity-70 cursor-not-allowed' : 'hover:bg-gray-300 dark:hover:bg-gray-600'}`}
            title="Refresh results"
            disabled={isRefreshing}
          >
            {isRefreshing ? 'Refreshing...' : 'Refresh'}
          </button>
          <select
            value={currentFilter}
            onChange={(e) => onFilterChange(e.target.value as RawTestResultView)}
            className="block w-full pl-3 pr-10 py-2 text-base border-gray-300 focus:outline-none focus:ring-indigo-500 focus:border-indigo-500 sm:text-sm rounded-md dark:bg-gray-700 dark:border-gray-600 dark:placeholder-gray-400 dark:text-white"
          >
            <option value="all">All Results</option>
            <option value="backedUp">Backed Up</option>
            <option value="notBackedUp">Not Backed Up</option>
          </select>
          {canDelete && (
            <button
              onClick={() => setBulkDeleteConfirmOpen(true)}
              disabled={selectedItems.length === 0 || isDeleting}
              className="px-4 py-2 text-sm font-medium text-white bg-red-600 border border-transparent rounded-md hover:bg-red-700 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {isDeleting ? 'Deleting...' : `Delete Selected (${selectedItems.length})`}
            </button>
          )}
        </div>
      </div>

      {error && <p className="p-3 bg-red-100 dark:bg-red-900/20 text-red-700 dark:text-red-300 rounded-md mb-4">Error: {error}</p>}

      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200 dark:divide-gray-700">
          <thead className="bg-gray-50 dark:bg-gray-700">
            <tr>
              {canDelete && (
                <th className="p-4">
                  <input 
                    type="checkbox"
                    className="h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300 rounded"
                    onChange={handleSelectAll}
                    checked={results.length > 0 && selectedItems.length === results.length}
                  />
                </th>
              )}
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Barcode</th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Test Type</th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Status</th>
              <th className="px-4 py-3 text-left text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Created At</th>
              <th className="px-4 py-3 text-center text-xs font-medium text-gray-500 dark:text-gray-300 uppercase tracking-wider">Actions</th>
            </tr>
          </thead>
          <tbody className="bg-white dark:bg-gray-800 divide-y divide-gray-200 dark:divide-gray-700">
            {results.map((result) => (
              <tr key={result.id} className={selectedItems.some(i => i.id === result.id) ? 'bg-blue-50 dark:bg-gray-900' : ''}>
                {canDelete && (
                  <td className="p-4">
                    <input 
                      type="checkbox"
                      className="h-4 w-4 text-indigo-600 focus:ring-indigo-500 border-gray-300 rounded"
                      checked={selectedItems.some(i => i.id === result.id)}
                      onChange={(e) => handleSelectItem(result, e.target.checked)}
                    />
                  </td>
                )}
                <td className="px-4 py-4 whitespace-nowrap font-medium text-gray-800 dark:text-gray-200">{result.barcode}</td>
                <td className="px-4 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">{result.testType}</td>
                <td className="px-4 py-4 whitespace-nowrap">{getStatusChip(result.status)}</td>
                <td className="px-4 py-4 whitespace-nowrap text-sm text-gray-500 dark:text-gray-400">{new Date(result.createdAt).toLocaleString()}</td>
                <td className="px-4 py-4 text-center whitespace-nowrap text-sm font-medium space-x-4">
                  <button 
                    onClick={() => setItemToView(result)}
                    disabled={!result.hl7Message}
                    className="text-blue-600 hover:text-blue-900 disabled:text-gray-400 disabled:cursor-not-allowed dark:text-blue-500 dark:hover:text-blue-400"
                  >
                    View HL7
                  </button>
                  {canDelete && (
                    <button 
                      onClick={() => handleDeleteRequest(result)}
                      className="text-red-600 hover:text-red-900 dark:text-red-500 dark:hover:text-red-400"
                    >
                      Delete
                    </button>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {paginationData && !paginationData.empty && (
        <div className="flex justify-between items-center mt-4">
           <button
            onClick={handlePrevPage}
            disabled={paginationData.first}
            className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50 disabled:opacity-50 dark:bg-gray-700 dark:text-gray-300 dark:border-gray-600 dark:hover:bg-gray-600"
          >
            Previous
          </button>
          <span className="text-sm text-gray-700 dark:text-gray-300">
            Page {paginationData.number + 1} of {paginationData.totalPages}
          </span>
          <button
            onClick={handleNextPage}
            disabled={paginationData.last}
            className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-md hover:bg-gray-50 disabled:opacity-50 dark:bg-gray-700 dark:text-gray-300 dark:border-gray-600 dark:hover:bg-gray-600"
          >
            Next
          </button>
        </div>
      )}

      {itemToDelete && canDelete && (
        <DeleteRawDataModal
          barcode={itemToDelete.barcode}
          onConfirm={confirmDelete}
          onCancel={() => setItemToDelete(null)}
          isDeleting={isDeleting}
        />
      )}
      
      {isBulkDeleteConfirmOpen && canDelete && (
        <DeleteRawDataModal
          barcode={`the selected ${selectedItems.length} items`}
          onConfirm={confirmBulkDelete}
          onCancel={() => setBulkDeleteConfirmOpen(false)}
          isDeleting={isDeleting}
        />
      )}

      {itemToView && (
        <RawHl7DataViewerModal
          barcode={itemToView.barcode}
          rawData={itemToView.hl7Message || 'No HL7 data available.'}
          onClose={() => setItemToView(null)}
        />
      )}
    </div>
  );
};

export default RawResultsTable;
