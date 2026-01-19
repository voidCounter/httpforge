import { METRIC_COLORS, COLORS } from "../data/benchmarkData";

export function ComparisonTable() {
  return (
    <div className="bg-white border border-gray-200 mb-6">
      <div className="mb-4 p-4">
        <h2 className="font-mono text-xl font-medium text-gray-900 mb-2">
          Architecture Comparison @ c=1000
        </h2>
        <p className="font-mono text-xs text-gray-500">
          high load scenario with 1,000 concurrent connections
        </p>
      </div>
      <div className="overflow-x-auto">
        <table className="w-full">
          <thead>
            <tr className="border-b-2 border-gray-300">
              <th className="text-left py-4 px-4 font-mono text-xs font-semibold text-gray-700">
                metric
              </th>
              <th className="text-center py-4 px-4 font-mono text-xs font-semibold text-gray-700">
                <div className="flex items-center justify-center gap-2">
                  <div
                    className="w-2 h-2 rounded-full"
                    style={{ backgroundColor: COLORS.single }}
                  ></div>
                  <span>single-thread</span>
                </div>
              </th>
              <th className="text-center py-4 px-4 font-mono text-xs font-semibold text-gray-700">
                <div className="flex items-center justify-center gap-2">
                  <div
                    className="w-2 h-2 rounded-full"
                    style={{ backgroundColor: COLORS.thread }}
                  ></div>
                  <span>thread-per-request</span>
                </div>
              </th>
              <th className="text-center py-4 px-4 font-mono text-xs font-semibold text-gray-900">
                <div className="flex items-center justify-center gap-2">
                  <div
                    className="w-2 h-2 rounded-full"
                    style={{ backgroundColor: COLORS.pool }}
                  ></div>
                  <span>thread-pool ✓</span>
                </div>
              </th>
            </tr>
          </thead>
          <tbody className="font-mono text-xs">
            <tr className="border-b border-gray-100 hover:bg-gray-50">
              <td className="py-4 px-4 text-gray-900">throughput</td>
              <td
                className="py-4 px-4 text-center"
                style={{ color: METRIC_COLORS.bad }}
              >
                54.7 req/s
              </td>
              <td
                className="py-4 px-4 text-center"
                style={{ color: METRIC_COLORS.warning }}
              >
                1,267 req/s
              </td>
              <td
                className="py-4 px-4 text-center font-bold"
                style={{ color: METRIC_COLORS.good }}
              >
                3,181 req/s
              </td>
            </tr>
            <tr className="border-b border-gray-100 hover:bg-gray-50">
              <td className="py-4 px-4 text-gray-900">p50_latency</td>
              <td
                className="py-4 px-4 text-center"
                style={{ color: METRIC_COLORS.bad }}
              >
                1,059 ms
              </td>
              <td
                className="py-4 px-4 text-center font-bold"
                style={{ color: METRIC_COLORS.good }}
              >
                21.3 ms
              </td>
              <td
                className="py-4 px-4 text-center"
                style={{ color: METRIC_COLORS.warning }}
              >
                106 ms
              </td>
            </tr>
            <tr className="border-b border-gray-100 hover:bg-gray-50">
              <td className="py-4 px-4 text-gray-900">p99_latency</td>
              <td
                className="py-4 px-4 text-center"
                style={{ color: METRIC_COLORS.bad }}
              >
                2,319 ms
              </td>
              <td
                className="py-4 px-4 text-center font-semibold"
                style={{ color: METRIC_COLORS.bad }}
              >
                2,760 ms
              </td>
              <td
                className="py-4 px-4 text-center"
                style={{ color: METRIC_COLORS.warning }}
              >
                2,319 ms
              </td>
            </tr>
            <tr className="border-b border-gray-100 hover:bg-gray-50">
              <td className="py-4 px-4 text-gray-900">success_rate</td>
              <td
                className="py-4 px-4 text-center"
                style={{ color: METRIC_COLORS.bad }}
              >
                46.5%
              </td>
              <td
                className="py-4 px-4 text-center"
                style={{ color: METRIC_COLORS.good }}
              >
                100%
              </td>
              <td
                className="py-4 px-4 text-center font-bold"
                style={{ color: METRIC_COLORS.good }}
              >
                100%
              </td>
            </tr>
            <tr className="border-b border-gray-100 hover:bg-gray-50">
              <td className="py-4 px-4 text-gray-900">memory_usage</td>
              <td
                className="py-4 px-4 text-center"
                style={{ color: METRIC_COLORS.good }}
              >
                1MB
              </td>
              <td
                className="py-4 px-4 text-center"
                style={{ color: METRIC_COLORS.bad }}
              >
                ~1,000 MB
              </td>
              <td
                className="py-4 px-4 text-center font-bold"
                style={{ color: METRIC_COLORS.good }}
              >
                ~160 MB
              </td>
            </tr>
            <tr className="border-b border-gray-100 hover:bg-gray-50">
              <td className="py-4 px-4 text-gray-900">thread_count</td>
              <td
                className="py-4 px-4 text-center"
                style={{ color: METRIC_COLORS.neutral }}
              >
                1
              </td>
              <td
                className="py-4 px-4 text-center"
                style={{ color: METRIC_COLORS.bad }}
              >
                1,000
              </td>
              <td
                className="py-4 px-4 text-center font-bold"
                style={{ color: METRIC_COLORS.good }}
              >
                16-160
              </td>
            </tr>
            {/*cpu efficiency needs some testing*/}
            {/*<tr className="hover:bg-gray-50">*/}
            {/*  <td className="py-4 px-4 text-gray-900">cpu_efficiency</td>*/}
            {/*  <td className="py-4 px-4 text-center" style={{ color: METRIC_COLORS.bad }}>13%</td>*/}
            {/*  <td className="py-4 px-4 text-center" style={{ color: METRIC_COLORS.warning }}>75%</td>*/}
            {/*  <td className="py-4 px-4 text-center font-bold" style={{ color: METRIC_COLORS.good }}>96%</td>*/}
            {/*</tr>*/}
          </tbody>
        </table>
      </div>
    </div>
  );
}
