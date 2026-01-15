import React, { useState, useEffect, useCallback } from "react";
import type {
  Instrument,
  RawTestResult,
  ChangeModeResult,
  PaginatedApiResponse,
} from "../types/types";
import { InstrumentApi } from "../services/InstrumentAPI";
import InstrumentModeDisplay from "../components/Mode/InstrumentModeDisplay";
import AnalysisControlPanel from "../components/Analysis/AnalysisControlPanel";
import RawResultsTable, {
  type RawTestResultView,
} from "../components/Analysis/RawResultsTable";
import ModeSwitchModal from "../components/Mode/ModeSwitchModal";
import ReagentManagement from "../components/Reagent/ReagentManagement";
import ConfigReagentSyncButton from "../components/Sync/ConfigSyncButton"; // Renamed import
import ConfigurationDisplay from "../components/Configuration/ConfigurationDisplay";

import { ThemeToggleButton } from "../../../context/ThemeButtonToggle";

const PAGE_SIZE = 10;

const InstrumentDashboard: React.FC = () => {
  const [instruments, setInstruments] = useState<Instrument[]>([]);
  const [selectedInstrument, setSelectedInstrument] =
    useState<Instrument | null>(null);
  const [isSyncingInstrument, setIsSyncingInstrument] = useState(false);
  const [resultsData, setResultsData] =
    useState<PaginatedApiResponse<RawTestResult> | null>(null);
  const [currentPage, setCurrentPage] = useState(0);
  const [rawTestResultView, setRawTestResultView] =
    useState<RawTestResultView>("all");
  const [reagentManagementKey, setReagentManagementKey] = useState(Date.now()); // Add key state for ReagentManagement
  const [isLoading, setIsLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [isSyncingAll, setIsSyncingAll] = useState(false);
  const [syncAllStatus, setSyncAllStatus] = useState<{
    type: "success" | "error";
    message: string;
  } | null>(null);
  const [selectedTestType, setSelectedTestType] = useState<string>("");

  // Helper to pick preferred test type from instrument configurations
  const pickPreferredTestType = (
    instrument: Instrument | null | undefined
  ): string => {
    if (!instrument) return "";
    const configs = instrument.configurations || [];
    const globalConfig = configs.find((c) => c.isGlobal);
    if (globalConfig) {
      return (
        globalConfig.configKey ||
        globalConfig.configValue ||
        globalConfig.configId ||
        ""
      );
    }
    return "";
  };

  // derive available test types from selectedInstrument configurations
  const availableTestTypes: string[] = selectedInstrument?.configurations
    ? Array.from(
        new Set(
          selectedInstrument.configurations
            .map((c) => c.configKey || c.configValue)
            .filter(Boolean)
        )
      )
    : [];

  // Choose initial selectedTestType from instrument configurations when instrument changes.
  // Prefer the first configuration that has isGlobal === true. If none, default to '' (All).
  React.useEffect(() => {
    if (!selectedInstrument) {
      setSelectedTestType("");
      return;
    }

    const configs = selectedInstrument.configurations || [];
    const globalConfig = configs.find((c) => c.isGlobal);
    if (globalConfig) {
      const key =
        globalConfig.configKey ||
        globalConfig.configValue ||
        globalConfig.configId ||
        "";
      setSelectedTestType(key);
    } else {
      setSelectedTestType("");
    }
  }, [selectedInstrument, selectedInstrument?.id]);

  const sleep = (ms: number) =>
    new Promise((resolve) => setTimeout(resolve, ms));

  const fetchInstruments = useCallback(
    async (keepSelectedId?: string | null) => {
      try {
        setIsLoading(true);
        const data = await InstrumentApi.getAllInstruments();
        setInstruments(data);
        if (data.length > 0) {
          if (keepSelectedId) {
            const found = data.find((d) => d.id === keepSelectedId);
            const chosen = found || data[0];
            setSelectedInstrument(chosen);
            // Immediately set selectedTestType so children receive it on first render
            setSelectedTestType(pickPreferredTestType(chosen));
          } else {
            setSelectedInstrument((prev) => {
              const sel = prev ? data.find((d) => d.id === prev.id) || data[0] : data[0];
              // Also set selectedTestType synchronously
              setSelectedTestType(pickPreferredTestType(sel));
              return sel;
            });
          }
        } else {
          setSelectedInstrument(null);
          setSelectedTestType("");
        }
      } catch (err) {
        console.error("Failed to fetch instruments:", err);
        setError("Failed to fetch instruments.");
      } finally {
        setIsLoading(false);
      }
    },
    []
  );

  const fetchRawTestResults = useCallback(
    async (instrumentId: string, page: number, filter: RawTestResultView) => {
      setError(null);
      try {
        let response;
        switch (filter) {
          case "backedUp":
            response = await InstrumentApi.getRawTestResultsBackupTrue(
              instrumentId,
              page,
              PAGE_SIZE
            );
            break;
          case "notBackedUp":
            response = await InstrumentApi.getRawTestResultsBackupFalse(
              instrumentId,
              page,
              PAGE_SIZE
            );
            break;
          case "all":
          default:
            response = await InstrumentApi.getRawTestResults(
              instrumentId,
              page,
              PAGE_SIZE
            );
            break;
        }
        setResultsData(response);
      } catch (err) {
        console.error("Failed to fetch raw test results:", err);
        setError("Failed to fetch raw test results.");
      }
    },
    []
  );

  useEffect(() => {
    fetchInstruments();
  }, [fetchInstruments]);

  useEffect(() => {
    if (selectedInstrument) {
      fetchRawTestResults(
        selectedInstrument.id,
        currentPage,
        rawTestResultView
      );
    }
  }, [selectedInstrument, currentPage, rawTestResultView, fetchRawTestResults]);

  const handleInstrumentChange = (instrumentId: string) => {
    const instrument = instruments.find((inst) => inst.id === instrumentId);
    if (instrument) {
      setSelectedInstrument(instrument);
      // Also set the preferred test type immediately to avoid flashing 'All' in children
      setSelectedTestType(pickPreferredTestType(instrument));
      setCurrentPage(0);
    }
  };

  const handleFilterChange = (filter: RawTestResultView) => {
    setRawTestResultView(filter);
    setCurrentPage(0);
  };

  const handleModeChangeSuccess = (result: ChangeModeResult) => {
    setInstruments((prev) =>
      prev.map((inst) =>
        inst.id === result.instrumentId
          ? { ...inst, mode: result.newMode }
          : inst
      )
    );
    if (selectedInstrument && result.instrumentId === selectedInstrument.id) {
      setSelectedInstrument((prev) =>
        prev ? { ...prev, mode: result.newMode } : null
      );
    }
  };

  const handleDataChange = () => {
    if (selectedInstrument) {
      fetchRawTestResults(
        selectedInstrument.id,
        currentPage,
        rawTestResultView
      );
    }
  };

  const handleReagentSyncComplete = () => {
    setReagentManagementKey(Date.now()); // Update the key to force re-render
  };

  const handleSyncAllConfigurations = async () => {
    setIsSyncingAll(true);
    setSyncAllStatus(null);
    try {
      const apiCall = InstrumentApi.syncAllConfigurations();
      const [response] = await Promise.all([apiCall, sleep(3000)]);
      if (response.code === "200") {
        setSyncAllStatus({
          type: "success",
          message:
            response.message || "All configurations synced successfully.",
        });
        await fetchInstruments();
      } else {
        setSyncAllStatus({
          type: "error",
          message: response.message || "Failed to sync all configurations.",
        });
      }
    } catch (err) {
      console.error("Failed to sync all configurations:", err);
      setSyncAllStatus({
        type: "error",
        message: "An unexpected error occurred during sync.",
      });
    } finally {
      setIsSyncingAll(false);
    }
  };

  const handleSyncSelectedInstrument = async () => {
    if (!selectedInstrument) return;
    setIsSyncingInstrument(true);
    try {
      const apiCall = InstrumentApi.syncInstrumentConfiguration(
        selectedInstrument.id
      );
      const [res] = await Promise.all([apiCall, sleep(3000)]);
      if (res && (res.code === "200" || res.code === "OK")) {
        await fetchInstruments(selectedInstrument.id);
        try {
          const updated = await InstrumentApi.getInstrumentById(
            selectedInstrument.id
          );
          setSelectedInstrument(updated);
        } catch (err) {
          console.warn(
            "Unable to fetch updated instrument by id after sync:",
            err
          );
        }
      } else {
        // Handle error
      }
    } catch (err) {
      console.error("Failed to sync selected instrument:", err);
    } finally {
      setIsSyncingInstrument(false);
    }
  };

  if (isLoading && instruments.length === 0) {
    return <div className="p-6">Loading instruments...</div>;
  }

  if (error && !resultsData) {
    return <div className="p-6 text-red-500">{error}</div>;
  }

  return (
    <div className="p-6 bg-gray-100 dark:bg-gray-900 min-h-screen">
      <div className="max-w-screen-xl mx-auto">
        <div className="flex justify-between items-center mb-6">
          <h1 className="text-3xl font-bold text-gray-900 dark:text-gray-100">
            Instrument Service Dashboard
          </h1>
          {instruments.length > 0 && (
            <div className="w-1/3 flex items-start space-x-3">
              <div className="flex-1">
                <select
                  onChange={(e) => handleInstrumentChange(e.target.value)}
                  value={selectedInstrument?.id || ""}
                  disabled={isSyncingInstrument || isSyncingAll}
                  className={`block w-full p-2 border border-gray-300 rounded-md shadow-sm dark:bg-gray-700 dark:border-gray-600 dark:text-gray-200 ${
                    isSyncingInstrument || isSyncingAll
                      ? "opacity-60 cursor-not-allowed"
                      : ""
                  }`}
                >
                  {instruments.map((inst) => (
                    <option key={inst.id} value={inst.id}>
                      {inst.name} ({inst.instrumentCode})
                    </option>
                  ))}
                </select>
              </div>
              <div className="flex-shrink-0">
                <button
                  onClick={handleSyncSelectedInstrument}
                  disabled={
                    isSyncingInstrument || isLoading || !selectedInstrument
                  }
                  className="px-3 py-2 bg-indigo-600 text-white rounded-md shadow-sm hover:bg-indigo-700 disabled:bg-indigo-400 disabled:cursor-not-allowed"
                >
                  {isSyncingInstrument ? "Syncing..." : "Sync Selected"}
                </button>
              </div>
            </div>
          )}
        </div>

        {instruments.length === 0 ? (
          <div className="flex flex-col items-center justify-center p-10 bg-white dark:bg-gray-800 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700">
            <p className="text-lg text-gray-700 dark:text-gray-300 mb-4">
              No instrument selected or available.
            </p>
            <button
              onClick={handleSyncAllConfigurations}
              disabled={isSyncingAll}
              className="px-6 py-3 bg-blue-600 text-white rounded-md shadow-sm hover:bg-blue-700 disabled:bg-gray-400 disabled:cursor-not-allowed"
            >
              {isSyncingAll ? "Syncing All..." : "Sync All Configurations"}
            </button>
            {syncAllStatus && (
              <div
                className={`mt-4 p-3 rounded-md border w-full text-center ${
                  syncAllStatus.type === "success"
                    ? "bg-green-100 border-green-400 text-green-700 dark:bg-green-900/20 dark:border-green-700 dark:text-green-300"
                    : "bg-red-100 border-red-400 text-red-700 dark:bg-red-900/20 dark:border-red-700 dark:text-red-300"
                }`}
              >
                <p>{syncAllStatus.message}</p>
              </div>
            )}
          </div>
        ) : isSyncingAll ? (
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            <div className="lg:col-span-2 space-y-6">
              <div className="p-6 bg-white rounded-lg shadow-sm animate-pulse h-64 dark:bg-gray-800" />
              <div className="p-6 bg-white rounded-lg shadow-sm animate-pulse h-48 dark:bg-gray-800" />
              <div className="p-6 bg-white rounded-lg shadow-sm animate-pulse h-48 dark:bg-gray-800" />
            </div>
            <div className="lg:col-span-1 space-y-6">
              <div className="p-6 bg-white rounded-lg shadow-sm animate-pulse h-40 dark:bg-gray-800" />
              <div className="p-6 bg-white rounded-lg shadow-sm animate-pulse h-40 dark:bg-gray-800" />
              <div className="p-6 bg-white rounded-lg shadow-sm animate-pulse h-24 dark:bg-gray-800" />
            </div>
          </div>
        ) : (
          selectedInstrument && (
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
              <div className="lg:col-span-2 space-y-6">
                <InstrumentModeDisplay
                  instrument={selectedInstrument}
                  onSwitchModeClick={() => setIsModalOpen(true)}
                />
                <AnalysisControlPanel
                  instrumentId={selectedInstrument.id}
                  instrumentMode={selectedInstrument.mode}
                  onAnalysisStateChange={handleDataChange}
                  selectedTestType={selectedTestType}
                  availableTestTypes={availableTestTypes}
                  onAnalysisFinished={() => setReagentManagementKey(Date.now())}
                  onSelectTestType={(t) => setSelectedTestType(t)}
                />
                <RawResultsTable
                  results={resultsData?.content || []}
                  paginationData={resultsData}
                  onPageChange={setCurrentPage}
                  onDataChange={handleDataChange}
                  currentFilter={rawTestResultView}
                  onFilterChange={handleFilterChange}
                />
              </div>

              <div className="lg:col-span-1 space-y-6">
                <ConfigurationDisplay
                  configurations={selectedInstrument.configurations || []}
                  selectedTestType={selectedTestType}
                  onSelectTestType={(t) => setSelectedTestType(t)}
                />
                <ReagentManagement
                  key={reagentManagementKey}
                  instrumentId={selectedInstrument.id}
                  refreshListTrigger={reagentManagementKey}
                />
                <ConfigReagentSyncButton
                  instrumentId={selectedInstrument.id}
                  onSyncComplete={handleReagentSyncComplete}
                />

                <div className="fixed z-50 hidden bottom-6 right-6 sm:block">
                  <ThemeToggleButton />
                </div>
              </div>
            </div>
          )
        )}

        {isModalOpen && selectedInstrument && (
          <ModeSwitchModal
            currentMode={selectedInstrument.mode}
            instrumentId={selectedInstrument.id}
            onClose={() => setIsModalOpen(false)}
            onSuccess={handleModeChangeSuccess}
          />
        )}
      </div>
    </div>
  );
};

export default InstrumentDashboard;
