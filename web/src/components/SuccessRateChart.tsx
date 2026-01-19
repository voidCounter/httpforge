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
import { successRateData, COLORS } from "../data/benchmarkData";

export function SuccessRateChart() {
  return (
    <div className="bg-white border border-gray-200 mb-6">
      <div className="mb-6 p-4">
        <h2 className="font-mono text-xl font-medium text-gray-900 mb-2">
          Reliability Success Rate
        </h2>
        <p className="font-mono text-xs text-gray-600 leading-relaxed">
          single-thread architecture catastrophically fails with a{" "}
          <span className="font-semibold text-gray-900">53.5%</span> failure
          rate at c=1000. thread-pool maintains{" "}
          <span className="font-semibold text-gray-900">100%</span> success
          across all loads.
        </p>
      </div>
      <ResponsiveContainer
        width="100%"
        height={350}
        style={{ padding: 0, margin: 0 }}
      >
        <AreaChart
          data={successRateData}
          margin={{ top: 0, right: 0, bottom: 0, left: 0 }}
        >
          <defs>
            <linearGradient id="successSingle" x1="0" y1="0" x2="0" y2="1">
              <stop offset="5%" stopColor={COLORS.single} stopOpacity={0.15} />
              <stop offset="95%" stopColor={COLORS.single} stopOpacity={0} />
            </linearGradient>
            <linearGradient id="successThread" x1="0" y1="0" x2="0" y2="1">
              <stop offset="5%" stopColor={COLORS.thread} stopOpacity={0.15} />
              <stop offset="95%" stopColor={COLORS.thread} stopOpacity={0} />
            </linearGradient>
            <linearGradient id="successPool" x1="0" y1="0" x2="0" y2="1">
              <stop offset="5%" stopColor={COLORS.pool} stopOpacity={0.15} />
              <stop offset="95%" stopColor={COLORS.pool} stopOpacity={0} />
            </linearGradient>
          </defs>
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
            domain={[0, 100]}
            tick={{ fontFamily: "monospace", fontSize: 11 }}
            label={{
              value: "success_rate %",
              angle: -90,
              offset: 45,
              position: "insideRight",
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
          <Area
            type="monotone"
            dataKey="single"
            stroke={COLORS.single}
            strokeWidth={2}
            fillOpacity={1}
            fill="url(#successSingle)"
            name="single-thread"
          />
          <Area
            type="monotone"
            dataKey="thread"
            stroke={COLORS.thread}
            strokeWidth={2}
            fillOpacity={1}
            fill="url(#successThread)"
            name="thread-per-request"
          />
          <Area
            type="monotone"
            dataKey="pool"
            stroke={COLORS.pool}
            strokeWidth={2}
            fillOpacity={1}
            fill="url(#successPool)"
            name="thread-pool"
          />
        </AreaChart>
      </ResponsiveContainer>
    </div>
  );
}
