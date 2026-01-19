import {
  AreaChart,
  Area,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from "recharts";
import { CustomTooltip } from "./CustomTooltip";
import { throughputData, COLORS } from "../data/benchmarkData";

export function ThroughputChart() {
  return (
    <div className="bg-white border border-gray-200">
      <div className="mb-6 p-4">
        <h2 className="font-mono text-xl font-medium text-gray-900 mb-2">
          Throughput Performance
        </h2>
        <p className="font-mono text-xs text-gray-600 leading-relaxed">
          thread-pool demonstrates consistent scalability, achieving peak
          performance at <span className="font-semibold">3,181 req/s</span> with
          1,000 concurrent connections. thread-per-request peaks early but
          degrades due to context switching overhead.
        </p>
      </div>
      <ResponsiveContainer
        width="100%"
        height={400}
        style={{ padding: 0, margin: 0 }}
      >
        <AreaChart
          data={throughputData}
          margin={{ top: 0, right: 0, bottom: 0, left: 0 }}
        >
          <defs>
            <linearGradient id="colorSingle" x1="0" y1="0" x2="0" y2="1">
              <stop offset="5%" stopColor={COLORS.single} stopOpacity={0.15} />
              <stop offset="95%" stopColor={COLORS.single} stopOpacity={0} />
            </linearGradient>
            <linearGradient id="colorThread" x1="0" y1="0" x2="0" y2="1">
              <stop offset="5%" stopColor={COLORS.thread} stopOpacity={0.15} />
              <stop offset="95%" stopColor={COLORS.thread} stopOpacity={0} />
            </linearGradient>
            <linearGradient id="colorPool" x1="0" y1="0" x2="0" y2="1">
              <stop offset="5%" stopColor={COLORS.pool} stopOpacity={0.15} />
              <stop offset="95%" stopColor={COLORS.pool} stopOpacity={0} />
            </linearGradient>
          </defs>
          <CartesianGrid strokeDasharray="3 3" stroke="#f3f4f6" />
          <XAxis
            dataKey="concurrency"
            tick={{ fontFamily: "monospace", fontSize: 11 }}
            label={{
              value: "concurrent_connections",
              position: "insideBottom",
              offset: -10,
              style: { fontFamily: "monospace", fontSize: 11 },
            }}
          />
          <YAxis
            tick={{ fontFamily: "monospace", fontSize: 11 }}
            label={{
              value: "req/sec",
              angle: -90,
              position: "insideRight",
              offset: 50,
              style: { fontFamily: "monospace", fontSize: 11 },
            }}
          />
          <Tooltip content={<CustomTooltip />} />
          <Legend
            wrapperStyle={{
              fontFamily: "monospace",
              fontSize: 12,
              paddingTop: "30px",
            }}
          />
          <Area
            type="monotone"
            dataKey="single"
            stroke={COLORS.single}
            strokeWidth={2}
            fillOpacity={1}
            fill="url(#colorSingle)"
            name="single-thread"
          />
          <Area
            type="monotone"
            dataKey="thread"
            stroke={COLORS.thread}
            strokeWidth={2}
            fillOpacity={1}
            fill="url(#colorThread)"
            name="thread-per-request"
          />
          <Area
            type="monotone"
            dataKey="pool"
            stroke={COLORS.pool}
            strokeWidth={2}
            fillOpacity={1}
            fill="url(#colorPool)"
            name="thread-pool"
          />
        </AreaChart>
      </ResponsiveContainer>
    </div>
  );
}
