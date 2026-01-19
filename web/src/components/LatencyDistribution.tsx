import {
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from 'recharts';
import { CustomTooltip } from './CustomTooltip';
import { latencyDistribution, CONCURRENCY_COLORS } from '../data/benchmarkData';

export function LatencyDistribution() {
  return (
    <div className="bg-white border border-gray-200 p-8 mb-6">
      <div className="mb-6">
        <h2 className="font-mono text-xl font-medium text-gray-900 mb-2">
          thread_pool_latency_distribution
        </h2>
        <p className="font-mono text-xs text-gray-600 leading-relaxed">
          percentile breakdown showing latency consistency. notice the p95-p99 spike at c=1000
          indicating occasional head-of-line blocking under extreme load.
        </p>
      </div>
      <ResponsiveContainer width="100%" height={350}>
        <BarChart data={latencyDistribution}>
          <CartesianGrid strokeDasharray="3 3" stroke="#f3f4f6" />
          <XAxis dataKey="percentile" tick={{ fontFamily: 'monospace', fontSize: 11 }} />
          <YAxis
            tick={{ fontFamily: 'monospace', fontSize: 11 }}
            label={{
              value: 'latency_ms',
              angle: 90,
              position: 'insideRight',
              style: { fontFamily: 'monospace', fontSize: 11 },
            }}
          />
          <Tooltip content={<CustomTooltip />} />
          <Legend wrapperStyle={{ fontFamily: 'monospace', fontSize: 12, paddingTop: '20px' }} />
          <Bar dataKey="c10" fill={CONCURRENCY_COLORS.c10} name="c=10" radius={[6, 6, 0, 0]} />
          <Bar dataKey="c100" fill={CONCURRENCY_COLORS.c100} name="c=100" radius={[6, 6, 0, 0]} />
          <Bar dataKey="c1000" fill={CONCURRENCY_COLORS.c1000} name="c=1000" radius={[6, 6, 0, 0]} />
        </BarChart>
      </ResponsiveContainer>
    </div>
  );
}
