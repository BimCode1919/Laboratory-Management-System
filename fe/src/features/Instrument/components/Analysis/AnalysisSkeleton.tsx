import React from 'react';

interface AnalysisSkeletonProps {
  progress: number; // 0 to 100
}

const AnalysisSkeleton: React.FC<AnalysisSkeletonProps> = ({ progress }) => {
  return (
    <div className="p-6 border border-gray-200 rounded-lg bg-gray-50 dark:bg-gray-800/50 dark:border-gray-700">
      <div className="animate-pulse">
        <div className="h-4 bg-gray-300 rounded-full dark:bg-gray-600 w-3/4 mb-4"></div>
        <div className="h-3 bg-gray-300 rounded-full dark:bg-gray-600 w-1/2 mb-6"></div>
      </div>

      <div className="space-y-4">
        <p className="text-sm font-medium text-center text-gray-700 dark:text-gray-300">
          Phân tích đang được tiến hành... {progress.toFixed(0)}%
        </p>
        <div className="w-full bg-gray-200 rounded-full h-2.5 dark:bg-gray-700">
          <div
            className="bg-blue-600 h-2.5 rounded-full transition-all duration-150 ease-linear"
            style={{ width: `${progress}%` }}
          ></div>
        </div>
      </div>

      <div className="animate-pulse mt-6">
        <div className="flex justify-between items-center">
            <div className="h-3 bg-gray-300 rounded-full dark:bg-gray-600 w-1/4"></div>
            <div className="h-8 bg-gray-300 rounded-md dark:bg-gray-600 w-1/3"></div>
        </div>
      </div>
    </div>
  );
};

export default AnalysisSkeleton;
