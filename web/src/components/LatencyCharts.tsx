import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from "recharts";
import { CustomTooltip } from "./CustomTooltip";
import { p50LatencyData, p99LatencyData, COLORS } from "../data/benchmarkData";

export function LatencyCharts() {
  return (
    <div className="grid grid-cols-1 lg:grid-cols-2 gap-4 mt-6 mb-6">
      {/* p50 Latency */}
      <div className="bg-white border border-gray-200">
        <div className="mb-6 p-4">
          <h2 className="font-mono text-xl font-medium text-gray-900 mb-2">
            p50 latency
          </h2>
          <p className="font-mono text-xs text-gray-600 leading-relaxed">
            median response time. single-thread shows catastrophic queueing
            delay beyond c=10.
          </p>
        </div>
        <ResponsiveContainer width="100%" height={300}>
          <LineChart data={p50LatencyData}>
            <CartesianGrid strokeDasharray="3 3" stroke="#f3f4f6" />
            <XAxis
              label={{
                value: "concurrent_connections",
                position: "insideBottom",
                offset: -10,
                style: { fontFamily: "monospace", fontSize: 11 },
              }}
              dataKey="concurrency"
              tick={{ fontFamily: "monospace", fontSize: 11 }}
            />
            <YAxis
              tick={{ fontFamily: "monospace", fontSize: 11 }}
              label={{
                value: "latency (ms)",
                angle: -90,
                position: "insideLeft",
                style: { fontFamily: "monospace", fontSize: 11 },
              }}
            />
            <Tooltip content={<CustomTooltip />} />
            <Legend
              wrapperStyle={{
                fontFamily: "monospace",
                fontSize: 12,
                paddingTop: "20px",
              }}
            />
            <Line
              type="monotone"
              dataKey="single"
              stroke={COLORS.single}
              strokeWidth={2}
              name="single-thread"
              dot={{ r: 3 }}
            />
            <Line
              type="monotone"
              dataKey="thread"
              stroke={COLORS.thread}
              strokeWidth={2}
              name="thread-per-request"
              dot={{ r: 3 }}
            />
            <Line
              type="monotone"
              dataKey="pool"
              stroke={COLORS.pool}
              strokeWidth={2}
              name="thread-pool"
              dot={{ r: 3 }}
            />
          </LineChart>
        </ResponsiveContainer>
      </div>

      {/* p99 Latency */}
      <div className="bg-white border border-gray-200">
        <div className="mb-6 p-4">
          <h2 className="font-mono text-xl font-medium text-gray-900 mb-2">
            p99 latency
          </h2>
          <p className="font-mono text-xs text-gray-600 leading-relaxed">
            99th percentile. thread-pool maintains sub-300ms tail latency until
            extreme concurrency.
          </p>
        </div>
        <ResponsiveContainer
          width="100%"
          height={300}
          style={{ padding: 0, margin: 0 }}
        >
          <LineChart
            data={p99LatencyData}
            margin={{ top: 0, right: 0, bottom: 0, left: 0 }}
          >
            <CartesianGrid strokeDasharray="3 3" stroke="#f3f4f6" />
            <XAxis
              label={{
                value: "concurrent_connections",
                position: "insideBottom",
                offset: -10,
                style: { fontFamily: "monospace", fontSize: 11 },
              }}
              dataKey="concurrency"
              tick={{ fontFamily: "monospace", fontSize: 11 }}
            />
            <YAxis
              tick={{ fontFamily: "monospace", fontSize: 11 }}
              label={{
                value: "latency (ms)",
                angle: -90,
                offset: 10,
                position: "insideLeft",
                style: { fontFamily: "monospace", fontSize: 11 },
              }}
            />
            <Tooltip content={<CustomTooltip />} />
            <Legend
              wrapperStyle={{
                fontFamily: "monospace",
                fontSize: 12,
                paddingTop: "20px",
              }}
            />
            <Line
              type="monotone"
              dataKey="single"
              stroke={COLORS.single}
              strokeWidth={2}
              name="single-thread"
              dot={{ r: 3 }}
            />
            <Line
              type="monotone"
              dataKey="thread"
              stroke={COLORS.thread}
              strokeWidth={2}
              name="thread-per-request"
              dot={{ r: 3 }}
            />
            <Line
              type="monotone"
              dataKey="pool"
              stroke={COLORS.pool}
              strokeWidth={2}
              name="thread-pool"
              dot={{ r: 3 }}
            />
          </LineChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}
