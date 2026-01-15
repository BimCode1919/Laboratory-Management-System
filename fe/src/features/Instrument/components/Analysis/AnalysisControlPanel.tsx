import React, { useState } from 'react';
import { InstrumentApi } from '../../services/InstrumentAPI';
import type { AnalysisRun, InstrumentMode, StartAnalysisPayload } from '../../types/types';
import SampleAnalysisForm from './SampleAnalysisForm';
import AnalysisSkeleton from './AnalysisSkeleton';

interface Props {
    instrumentId: string;
    instrumentMode: InstrumentMode;
    user?: { id: string; name: string };
    onAnalysisStateChange: () => void;
    selectedTestType?: string;
    availableTestTypes?: string[];
    onAnalysisFinished?: () => void;
    onSelectTestType?: (type: string) => void;
}

const AnalysisControlPanel: React.FC<Props> = ({ instrumentId, instrumentMode, onAnalysisStateChange, selectedTestType, availableTestTypes = [], onAnalysisFinished, onSelectTestType }) => {
    const [analysisRun, setAnalysisRun] = useState<AnalysisRun | null>(null);
    const [status, setStatus] = useState<{ type: 'info' | 'warning' | 'error'; message: string } | null>(null);
    const [isProcessing, setIsProcessing] = useState(false);
    const [isAnalyzing, setIsAnalyzing] = useState(false);
    const [progress, setProgress] = useState(0);

    const isStartNewAnalysisDisabled = isProcessing || !!(analysisRun && analysisRun.status !== 'COMPLETED');

    const handleStartAnalysis = async (formData: StartAnalysisPayload) => {
        if (!formData.barcodes || formData.barcodes.length === 0) {
            setStatus({ type: 'warning', message: 'Vui lòng chọn ít nhất một mẫu để phân tích.' });
            return;
        }

        setIsProcessing(true);
        setIsAnalyzing(true);
        setProgress(0);

        const numSamples = formData.barcodes.length;
        const analysisDuration = numSamples * 5000; // 5s
        const startTime = Date.now();

        setStatus({ type: 'info', message: `Đang bắt đầu phân tích cho ${numSamples} mẫu...` });

        // Progress simulation
        const interval = setInterval(() => {
            const elapsedTime = Date.now() - startTime;
            const currentProgress = Math.min(100, (elapsedTime / analysisDuration) * 100);
            setProgress(currentProgress);
            if (currentProgress >= 100) {
                clearInterval(interval);
            }
        }, 100);

        try {
            await new Promise(resolve => setTimeout(resolve, analysisDuration));

            const runData = await InstrumentApi.startAnalysis(instrumentId, formData);
            const effectiveStatus = 'COMPLETED';
            setAnalysisRun({ ...runData, status: effectiveStatus as typeof runData.status });

            const successMessage = numSamples > 1
                ? `Phiên phân tích cho ${numSamples} mẫu đã hoàn tất với Run ID: ${runData.runId}`
                : `Phiên phân tích cho mẫu đã hoàn tất với Run ID: ${runData.runId}`;

            setStatus({ type: 'info', message: successMessage });
            onAnalysisStateChange();
            // Notify parent to refresh reagents / other UI after analysis finished
            if (onAnalysisFinished) {
                try { onAnalysisFinished(); } catch (e) { console.warn('onAnalysisFinished callback failed', e); }
            }
        } catch (err) {
            const message = err instanceof Error ? err.message : String(err);
            setStatus({ type: 'error', message: message || 'Có lỗi xảy ra khi bắt đầu phân tích.' });
        } finally {
            clearInterval(interval);
            setIsProcessing(false);
            setIsAnalyzing(false);
            setProgress(100);
        }
    };

    const getStatusColor = () => {
        if (!status) return '';
        switch (status.type) {
            case 'error':
                return 'bg-red-100 border-red-400 text-red-700 dark:bg-red-900/20 dark:border-red-700 dark:text-red-300';
            case 'warning':
                return 'bg-yellow-100 border-yellow-400 text-yellow-700 dark:bg-yellow-900/20 dark:border-yellow-700 dark:text-yellow-300';
            default:
                return 'bg-blue-100 border-blue-400 text-blue-700 dark:bg-blue-900/20 dark:border-blue-700 dark:text-blue-300';
        }
    };

    return (
        <div className="mt-6 bg-white dark:bg-gray-800 p-4 rounded-lg shadow-sm border border-gray-200 dark:border-gray-700">
            <h3 className="text-lg font-semibold mb-4 text-gray-900 dark:text-gray-100">Blood Sample Analysis</h3>

            {instrumentMode !== 'READY' && (
                <p className="text-sm text-green-800 dark:text-green-300 mb-4 p-2 bg-green-100 dark:bg-green-900/20 rounded-md">
                    Lưu ý: Thiết bị phải ở chế độ 'Ready' để bắt đầu phân tích.
                </p>
            )}

            {isAnalyzing ? (
                <AnalysisSkeleton progress={progress} />
            ) : !analysisRun ? (
                <SampleAnalysisForm
                    instrumentId={instrumentId}
                    onSubmit={handleStartAnalysis}
                    isLoading={isProcessing || instrumentMode !== 'READY'}
                    selectedTestType={selectedTestType}
                    {...(availableTestTypes ? { availableTestTypes } : {})}
                    onSelectTestType={onSelectTestType}
                />
            ) : (
                <div className="space-y-4">
                    <div>
                        <h4 className="font-medium text-gray-800 dark:text-gray-200">Analysis Result</h4>
                        <p className="text-sm text-gray-600 dark:text-gray-400">Run ID: {analysisRun.runId}</p>
                        <p className="text-sm text-gray-600 dark:text-gray-400">Status: <span className={`font-semibold ${analysisRun.status === 'RUNNING' ? 'text-yellow-500' : 'text-green-500'}`}>{analysisRun.status}</span></p>
                        {analysisRun.startTime && <p className="text-sm text-gray-600 dark:text-gray-400">Start Time: {new Date(analysisRun.startTime).toLocaleString()}</p>}
                    </div>
                    <div className="flex space-x-4">
                        <button
                            onClick={() => setAnalysisRun(null)} // Allow starting a new run
                            disabled={isStartNewAnalysisDisabled}
                            className="px-5 py-2 bg-gray-500 text-white rounded-md shadow-sm hover:bg-gray-600 disabled:bg-gray-400"
                        >
                            {isProcessing ? 'Processing...' : 'Start New Analysis'}
                        </button>
                    </div>
                </div>
            )}

            {status && (
                <div className={`mt-4 p-3 rounded-md border ${getStatusColor()}`}>
                    <p className="font-medium">{status.type.toUpperCase()}: <span className="font-normal">{status.message}</span></p>
                </div>
            )}
        </div>
    );
};

export default AnalysisControlPanel;
