export const throughputData = [
  { concurrency: 1, single: 48.14, thread: 47.78, pool: 48.18 },
  { concurrency: 10, single: 49.17, thread: 477.69, pool: 481.97 },
  { concurrency: 50, single: 49.08, thread: 2383.34, pool: 785.56 },
  { concurrency: 100, single: 50.22, thread: 3205.18, pool: 787.03 },
  { concurrency: 200, single: 50.65, thread: 2987.50, pool: 786.60 },
  { concurrency: 1000, single: 54.74, thread: 1266.70, pool: 3181.15 },
];

export const p50LatencyData = [
  { concurrency: 1, single: 20.8, thread: 20.9, pool: 20.7 },
  { concurrency: 10, single: 203.3, thread: 20.8, pool: 20.7 },
  { concurrency: 50, single: 1019.0, thread: 21.0, pool: 61.1 },
  { concurrency: 100, single: 1059.2, thread: 20.6, pool: 122.4 },
  { concurrency: 200, single: 1060.7, thread: 20.5, pool: 245.1 },
  { concurrency: 1000, single: 1059.2, thread: 21.3, pool: 105.9 },
];

export const p99LatencyData = [
  { concurrency: 1, single: 21.2, thread: 21.6, pool: 21.3 },
  { concurrency: 10, single: 205.1, thread: 21.9, pool: 21.4 },
  { concurrency: 50, single: 1023.0, thread: 22.7, pool: 80.9 },
  { concurrency: 100, single: 1064.8, thread: 23.8, pool: 142.1 },
  { concurrency: 200, single: 1176.3, thread: 1087.4, pool: 266.1 },
  { concurrency: 1000, single: 2318.8, thread: 2759.5, pool: 2318.8 },
];

export const latencyDistribution = [
  { percentile: 'p50', c10: 20.7, c100: 122.4, c1000: 105.9 },
  { percentile: 'p75', c10: 20.9, c100: 123.6, c1000: 118.8 },
  { percentile: 'p90', c10: 21.0, c100: 141.6, c1000: 152.8 },
  { percentile: 'p95', c10: 21.1, c100: 141.8, c1000: 1034.8 },
  { percentile: 'p99', c10: 21.4, c100: 142.1, c1000: 2318.8 },
];

export const successRateData = [
  { concurrency: 1, single: 100, thread: 100, pool: 100 },
  { concurrency: 10, single: 100, thread: 100, pool: 100 },
  { concurrency: 50, single: 100, thread: 100, pool: 100 },
  { concurrency: 100, single: 97.6, thread: 100, pool: 100 },
  { concurrency: 200, single: 92.94, thread: 100, pool: 100 },
  { concurrency: 1000, single: 46.53, thread: 100, pool: 100 },
];

export const COLORS = {
  single: '#3b9ab2',
  thread: '#e1af00',
  pool: '#f21a00',
};

// Concurrency level colors for latency distribution
export const CONCURRENCY_COLORS = {
  c1: "#9986a5",
  c10: '#79402e',    // light gray - low load
  c50: '#ccba72',
  c100: '#0f0d0e',   // medium gray - medium load
  c200: '#d9d0d3',
  c1000: '#8d8680',  // dark gray - high load
};

// Semantic color mappings for metrics
export const METRIC_COLORS = {
  bad: '#ef4444',      // red - poor performance
  warning: '#f59e0b',  // amber - moderate/warning
  good: '#10b981',     // green - good performance
  neutral: '#6b7280',  // gray - neutral/informational
};

// Helper function to get metric color based on performance
export function getMetricColor(value: number, thresholds: { good: number; warning: number }, lowerIsBetter: boolean = false): string {
  if (lowerIsBetter) {
    if (value <= thresholds.good) return METRIC_COLORS.good;
    if (value <= thresholds.warning) return METRIC_COLORS.warning;
    return METRIC_COLORS.bad;
  } else {
    if (value >= thresholds.good) return METRIC_COLORS.good;
    if (value >= thresholds.warning) return METRIC_COLORS.warning;
    return METRIC_COLORS.bad;
  }
}

