import React from 'react';
import type { EstimateResult } from '../../types/types';

interface Props {
  estimate: EstimateResult | null;
  onConfirm: () => void;
  onCancel: () => void;
  confirming: boolean;
}

const formatNumber = (n: number | undefined | null) => {
  if (n === undefined || n === null || Number.isNaN(n)) return '-';
  // Show integer if whole number, otherwise two decimals
  return Number(n) % 1 === 0 ? String(n) : Number(n).toFixed(2);
};

const EstimateModal: React.FC<Props> = ({ estimate, onConfirm, onCancel, confirming }) => {
  if (!estimate) return null;

  const reagentKeys = Array.from(new Set([
    ...Object.keys(estimate.requiredPerReagent || {}),
    ...Object.keys(estimate.availablePerReagent || {}),
    ...Object.keys(estimate.shortfallPerReagent || {}),
    ...Object.keys(estimate.runsLeftPerReagent || {}),
    ...Object.keys(estimate.installedDetailsPerReagent || {}),
  ]));

  // Compute usable (non-expired) installed quantities per reagent
  const usablePerReagent: Record<string, number> = {};
  const totalInstalledPerReagent: Record<string, number> = {};
  if (estimate.installedDetailsPerReagent) {
    Object.entries(estimate.installedDetailsPerReagent).forEach(([key, lots]) => {
      let usable = 0;
      let total = 0;
      lots.forEach(l => {
        const qty = Number(l.quantityRemaining) || 0;
        total += qty;
        if (!l.expired) {
          usable += qty;
        }
      });
      usablePerReagent[key] = usable;
      totalInstalledPerReagent[key] = total;
    });
  }

  return (
    <div className="fixed inset-0 z-40 flex items-center justify-center p-4 mt-22">
      <div className="fixed inset-0 bg-black/40 backdrop-blur-sm" onClick={onCancel} />

      <div className="relative z-50 mx-auto w-full max-w-5xl max-h-[88vh] overflow-y-auto rounded-lg bg-white dark:bg-gray-800 shadow-xl ring-1 ring-black/5">
        <div className="px-5 py-3 border-b border-gray-100 dark:border-gray-700 flex items-center justify-between">
          <div className="flex items-center gap-4">
            <div>
              <h3 className="text-lg font-semibold text-gray-900 dark:text-gray-100">Ước lượng phân tích</h3>
              <p className="text-sm text-gray-500 dark:text-gray-300">Xem ước lượng tài nguyên trước khi bắt đầu phiên phân tích.</p>
            </div>
            <div className="ml-2">
              {estimate.sufficient ? (
                <span className="inline-flex items-center rounded-full bg-green-50 px-3 py-1 text-sm font-medium text-green-700 ring-1 ring-inset ring-green-600/10">✓ Đủ điều kiện</span>
              ) : (
                <span className="inline-flex items-center rounded-full bg-red-50 px-3 py-1 text-sm font-medium text-red-700 ring-1 ring-inset ring-red-600/10">⚠️ Không đủ</span>
              )}
            </div>
          </div>

          <div className="text-right">
            <div className="text-sm text-gray-500 dark:text-gray-300">Số mẫu yêu cầu</div>
            <div className="mt-1 text-xl font-semibold text-gray-900 dark:text-gray-100">{estimate.samplesRequested} mẫu</div>
            <div className="mt-1 text-xs text-gray-500 dark:text-gray-400">Lần chạy ước tính: <span className="font-medium text-gray-800 dark:text-gray-200">{estimate.estimatedRunsPossible}</span></div>
          </div>
        </div>

        <div className="px-5 py-4">
          <div className="mb-4 text-sm text-gray-600 dark:text-gray-300">
            Bảng dưới đây liệt kê lượng thuốc thử cần thiết và số lượng hiện có. Nếu có thiếu, bạn sẽ thấy phần bị thiếu và số lần chạy còn lại. Bên dưới mỗi dòng reagent có danh sách các lot đã cài đặt (nếu có) với trạng thái "in use".
          </div>

          <div className="overflow-x-auto rounded-md border border-gray-100 dark:border-gray-700">
            <table className="min-w-full text-left text-sm">
              <thead className="bg-gray-50 dark:bg-gray-900">
                <tr>
                  <th className="px-3 py-2 text-xs font-medium text-gray-500 uppercase tracking-wide">Thuốc thử</th>
                  <th className="px-3 py-2 text-xs font-medium text-gray-500 uppercase tracking-wide">Yêu cầu</th>
                  <th className="px-3 py-2 text-xs font-medium text-gray-500 uppercase tracking-wide">Sẵn có</th>
                  <th className="px-3 py-2 text-xs font-medium text-gray-500 uppercase tracking-wide">Thiếu</th>
                  <th className="px-3 py-2 text-xs font-medium text-gray-500 uppercase tracking-wide">Lần chạy còn lại</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100 dark:divide-gray-700">
                {reagentKeys.length === 0 ? (
                  <tr>
                    <td colSpan={5} className="px-3 py-4 text-center text-sm text-gray-500">Không có dữ liệu thuốc thử</td>
                  </tr>
                ) : (
                  reagentKeys.map((r) => {
                    const required = estimate.requiredPerReagent?.[r] ?? 0;
                    const available = estimate.availablePerReagent?.[r] ?? 0;
                    const shortfall = estimate.shortfallPerReagent?.[r] ?? 0;
                    const runsLeft = estimate.runsLeftPerReagent?.[r] ?? 0;

                    const shortfallNum = Number(shortfall);

                    return (
                      <React.Fragment key={r}>
                        <tr className="align-top">
                          <td className="px-3 py-2 align-middle">
                            <div className="font-medium text-gray-800 dark:text-gray-100">{r}</div>
                          </td>
                          <td className="px-3 py-2 align-middle text-gray-700 dark:text-gray-200">{formatNumber(required)}</td>
                          <td className="px-3 py-2 align-middle text-gray-700 dark:text-gray-200">
                            {/* Show usable vs total installed if we have details */}
                            {estimate.installedDetailsPerReagent && estimate.installedDetailsPerReagent[r] ? (
                              <div className="text-sm">
                                <div className="font-medium">{formatNumber(usablePerReagent[r] ?? available)} usable</div>
                                <div className="text-xs text-gray-500">total: {formatNumber(totalInstalledPerReagent[r] ?? available)}</div>
                              </div>
                            ) : (
                              formatNumber(available)
                            )}
                          </td>
                          <td className="px-3 py-2 align-middle">
                            {shortfallNum > 0 ? (
                              <span className="inline-flex items-center rounded-full bg-red-100 px-2 py-0.5 text-xs font-medium text-red-700">-{formatNumber(shortfallNum)}</span>
                            ) : (
                              <span className="text-sm text-gray-500">—</span>
                            )}
                          </td>
                          <td className="px-3 py-2 align-middle">
                            <div className={`text-sm font-medium ${shortfallNum > 0 ? 'text-red-600' : 'text-gray-800 dark:text-gray-100'}`}>{formatNumber(runsLeft)}</div>
                          </td>
                        </tr>

                        {/* Render installed lots for this reagent if available */}
                        {(estimate.installedDetailsPerReagent && estimate.installedDetailsPerReagent[r] && estimate.installedDetailsPerReagent[r].length > 0) && (
                          <tr className="bg-gray-50 dark:bg-gray-900">
                            <td colSpan={5} className="px-3 py-2">
                              <div className="grid grid-cols-4 gap-3">
                                {estimate.installedDetailsPerReagent![r].map((lot, idx) => (
                                  <div key={`${r}-${lot.lotNumber}-${idx}`} className={`p-2 rounded-md border ${lot.inUse ? 'border-indigo-500 bg-indigo-50 dark:bg-indigo-900/20' : 'border-gray-200 dark:border-gray-700 bg-white dark:bg-gray-800'}`}>
                                    <div className="flex items-center justify-between">
                                      <div className="text-sm">
                                        <div className="font-medium text-gray-800 dark:text-gray-100">Lot: {lot.lotNumber}</div>
                                        <div className="text-xs text-gray-500 dark:text-gray-400">{formatNumber(lot.quantityRemaining)} {lot.unit}</div>
                                        {lot.expirationDate && (
                                          <div className="text-xs text-gray-500 dark:text-gray-400">Expires: {lot.expirationDate}</div>
                                        )}
                                      </div>
                                      <div className="text-xs">
                                        {lot.inUse ? (
                                          <span className="inline-flex items-center rounded-full bg-indigo-600 text-white px-2 py-0.5">In use</span>
                                        ) : (
                                          <span className="inline-flex items-center rounded-full bg-gray-100 text-gray-700 px-2 py-0.5">Available</span>
                                        )}
                                        {lot.expired ? (
                                          <div className="mt-1 text-xs text-red-600 font-medium">Expired</div>
                                        ) : null}
                                      </div>
                                    </div>
                                  </div>
                                ))}
                              </div>
                            </td>
                          </tr>
                        )}
                      </React.Fragment>
                    );
                  })
                )}
              </tbody>
            </table>
          </div>

          {/* If any reagent's required amount exceeds usable available, we should mark as insufficient */}
          {(() => {
            const lacking = reagentKeys.some(key => {
              const required = Number(estimate.requiredPerReagent?.[key] ?? 0);
              const usable = Number(usablePerReagent[key] ?? estimate.availablePerReagent?.[key] ?? 0);
              return required > usable;
            });
            return (!estimate.sufficient || lacking) ? (
            <div className="mt-4 rounded-md bg-red-50 p-3 text-sm text-red-700">
              <strong>Không đủ thuốc thử để thực hiện số mẫu đã yêu cầu.</strong>
              <div className="mt-1 text-red-700/90">Vui lòng bổ sung thuốc thử hoặc giảm số mẫu. Lưu ý: các lot quá hạn không được tính vào lượng sẵn có.</div>
            </div>
            ) : null;
          })()
           }

          {(() => {
            const lacking = reagentKeys.some(key => {
              const required = Number(estimate.requiredPerReagent?.[key] ?? 0);
              const usable = Number(usablePerReagent[key] ?? estimate.availablePerReagent?.[key] ?? 0);
              return required > usable;
            });
            if (!lacking) {
              return (
                <div className="mt-4 rounded-md bg-green-50 p-3 text-sm text-green-700">
                  Sẵn sàng bắt đầu: thiết bị có đủ thuốc thử (không tính lot quá hạn) cho số mẫu yêu cầu.
                </div>
              );
            }
            return null;
          })()}
         </div>

         <div className="flex items-center justify-end gap-3 border-t border-gray-100 dark:border-gray-700 px-5 py-3">
            <button
                type="button"
                onClick={onCancel}
                disabled={confirming}
                aria-label="Hủy"
                className="group flex items-center rounded-md bg-white dark:bg-gray-700 border border-gray-200 dark:border-gray-600 px-4 py-2 text-sm text-gray-700 dark:text-gray-200 hover:bg-gray-100 dark:hover:bg-gray-600 transition-colors duration-200"
            >
                <svg
                    xmlns="http://www.w3.org/2000/svg"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    strokeWidth={2}
                    className="h-4 w-4 text-gray-600 dark:text-gray-300"
                >
                    <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
                </svg>
                <span className="ml-2 transition-colors duration-200 group-hover:text-red-500">Hủy</span>
            </button>


            <button
            type="button"
            onClick={onConfirm}
            disabled={confirming || reagentKeys.some(key => {
              const required = Number(estimate.requiredPerReagent?.[key] ?? 0);
              const usable = Number(usablePerReagent[key] ?? estimate.availablePerReagent?.[key] ?? 0);
              return required > usable;
            })}
            className={`inline-flex items-center gap-2 rounded-md px-4 py-2 text-sm font-medium text-white ${reagentKeys.some(key => {
              const required = Number(estimate.requiredPerReagent?.[key] ?? 0);
              const usable = Number(usablePerReagent[key] ?? estimate.availablePerReagent?.[key] ?? 0);
              return required > usable;
            }) ? 'bg-gray-400 cursor-not-allowed' : 'bg-green-600 hover:bg-green-700'} disabled:opacity-60`}
          >
            {confirming ? 'Đang bắt đầu...' : 'Bắt đầu phân tích'}
          </button>
          </div>
        </div>
      </div>
    );
  };

  export default EstimateModal;
