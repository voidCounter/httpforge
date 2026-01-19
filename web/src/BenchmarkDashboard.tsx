import { MetricCard } from "./components/MetricCard";
import { ThroughputChart } from "./components/ThroughputChart";
import { LatencyCharts } from "./components/LatencyCharts";
import { LatencyDistribution } from "./components/LatencyDistribution";
import { SuccessRateChart } from "./components/SuccessRateChart";
import { ComparisonTable } from "./components/ComparisonTable";
import { COLORS } from "./data/benchmarkData";
import { Github } from "griddy-icons";

export default function BenchmarkDashboard() {
  return (
    <div className="min-h-screen bg-white">
      {/* GitHub Link */}
      <div className="absolute top-8 right-8">
        <a
          href="https://github.com/voidCounter/httpforge"
          target="_blank"
          rel="noopener noreferrer"
          className="flex items-center gap-2 text-gray-600 hover:text-gray-900 transition-colors font-mono text-xs"
        >
          <Github className="w-5 h-5" strokeWidth={1.5} />
          <span className="hidden sm:inline">github</span>
        </a>
      </div>

      {/* Hero Section */}
      <div className="max-w-7xl mx-auto px-4 py-16">
        <div className="text-left mb-6">
          <h1 className="font-mono text-4xl font-semibold text-gray-900 mb-2">
            httpforge
          </h1>
          <p className="font-mono text-sm text-gray-500 max-w-2xl">
            performance analysis of blocking i/o http server concurrency
            strategies
          </p>
        </div>

        {/* Key Metrics */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 mb-12">
          <MetricCard
            title="peak throughput"
            value="3,181"
            unit="req/s"
            subtitle="thread-pool @ c=1000"
            tooltip="Maximum requests per second achieved by thread-pool architecture under high concurrency (1,000 concurrent connections). This demonstrates the upper limit of the server's request handling capacity."
          />
          <MetricCard
            title="reliability"
            value="100"
            unit="%"
            subtitle="vs 46.5% single-thread"
            tooltip="Thread-pool maintains 100% success rate across all load levels. Single-thread catastrophically fails with 53.5% error rate (5,347 failed requests) at c=1000 due to timeout and connection issues."
          />
          <MetricCard
            title="throughput gain"
            value="+151"
            unit="%"
            subtitle="vs thread-per-request"
            tooltip="Thread-pool achieves 151% higher throughput than thread-per-request at c=1000 (3,181 vs 1,267 req/s). This gain comes from efficient thread reuse and avoiding context-switching overhead."
          />
          <MetricCard
            title="thread degradation"
            value="-60"
            unit="%"
            subtitle="thread: c100 → c1000"
            tooltip="Thread-per-request performance collapses from 3,205 req/s at c=100 to 1,267 req/s at c=1000—a 60% drop. This degradation is caused by excessive context switching between 1,000+ threads."
          />
        </div>

        {/* Charts */}
        <ThroughputChart />
        <LatencyCharts />
        <LatencyDistribution />
        <SuccessRateChart />

        {/* Architecture Bottlenecks */}
        <div className="mb-6">
          <h2 className="font-mono text-xl font-medium text-gray-900 mb-4">
            Architecture Bottlenecks
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
            <div className="bg-white border border-gray-200 p-6">
              <div className="flex items-center gap-2 mb-3">
                <div
                  className="w-2 h-2 rounded-full"
                  style={{ backgroundColor: COLORS.single }}
                ></div>
                <h3 className="font-mono text-sm font-medium text-gray-900">
                  single-thread
                </h3>
              </div>
              <div className="mb-3">
                <span className="font-mono text-xs text-gray-400 uppercase">
                  bottleneck
                </span>
                <p className="font-mono text-xs text-gray-900 mt-1">
                  event_loop_saturation
                </p>
              </div>
              <p className="font-mono text-xs text-gray-500 leading-relaxed">
                cannot utilize multiple cpu cores. request queueing causes
                latency to spike from 20ms to 1000ms+ beyond c=10. catastrophic
                failure rate at high concurrency.
              </p>
            </div>

            <div className="bg-white border border-gray-200 p-6">
              <div className="flex items-center gap-2 mb-3">
                <div
                  className="w-2 h-2 rounded-full"
                  style={{ backgroundColor: COLORS.thread }}
                ></div>
                <h3 className="font-mono text-sm font-medium text-gray-900">
                  thread-per-request
                </h3>
              </div>
              <div className="mb-3">
                <span className="font-mono text-xs text-gray-400 uppercase">
                  bottleneck
                </span>
                <p className="font-mono text-xs text-gray-900 mt-1">
                  context_switching_overhead
                </p>
              </div>
              <p className="font-mono text-xs text-gray-500 leading-relaxed">
                peaks at c=100 (3,205 req/s) but degrades 60% by c=1000. thread
                creation/destruction and context switching between 1000+ threads
                crushes performance.
              </p>
            </div>

            <div className="bg-white border border-gray-200 p-6">
              <div className="flex items-center gap-2 mb-3">
                <div
                  className="w-2 h-2 rounded-full"
                  style={{ backgroundColor: COLORS.pool }}
                ></div>
                <h3 className="font-mono text-sm font-medium text-gray-900">
                  thread-pool ✓
                </h3>
              </div>
              <div className="mb-3">
                <span className="font-mono text-xs text-gray-400 uppercase">
                  bottleneck
                </span>
                <p className="font-mono text-xs text-gray-900 mt-1">
                  none (recommended)
                </p>
              </div>
              <p className="font-mono text-xs text-gray-500 leading-relaxed">
                fixed thread pool (10 threads) efficiently handles all loads.
                maintains 100% reliability across all concurrency levels.
                optimal balance of concurrency and resource management.
              </p>
            </div>
          </div>
        </div>

        {/* Comparison Table */}
        <ComparisonTable />

        {/* Test Methodology */}
        <div className="bg-white border border-gray-200 mb-6 p-4">
          <h2 className="font-mono text-xl font-medium text-gray-900 mb-4">
            Test Methodology
          </h2>

          {/* Test Parameters */}
          <div className="mb-6">
            <h3 className="font-mono text-sm font-medium mb-4 text-gray-500">
              test_parameters
            </h3>
            <div className="grid grid-cols-2 md:grid-cols-5 gap-4">
              <div className="border border-gray-200 p-4">
                <div className="font-mono text-xs text-gray-400 mb-1">tool</div>
                <div className="font-mono text-sm text-gray-900">hey</div>
              </div>
              <div className="border border-gray-200 p-4">
                <div className="font-mono text-xs text-gray-400 mb-1">
                  endpoint
                </div>
                <div className="font-mono text-sm text-gray-900">
                  GET /hello
                </div>
              </div>
              <div className="border border-gray-200 p-4">
                <div className="font-mono text-xs text-gray-400 mb-1">
                  i/o_simulation
                </div>
                <div className="font-mono text-sm text-gray-900">
                  20ms sleep
                </div>
              </div>
              <div className="border border-gray-200 p-4">
                <div className="font-mono text-xs text-gray-400 mb-1">
                  runtime
                </div>
                <div className="font-mono text-sm text-gray-900">java_17+</div>
              </div>
            </div>
            <p className="font-mono text-xs text-gray-500 mt-4">
              each request includes a 2ms sleep to simulate blocking i/o
              operations (database queries, file operations, network calls).
              this highlights how different concurrency strategies handle
              i/o-bound workloads.
            </p>
          </div>

          {/* Architecture Implementations */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            <div className="border border-gray-200 p-6">
              <h3 className="font-mono text-sm font-medium mb-4 text-gray-900">
                single-thread
              </h3>
              <ul className="space-y-2 font-mono text-xs text-gray-600">
                <li>• sequential on main thread</li>
                <li>• no concurrency</li>
              </ul>
            </div>

            <div className="border border-gray-200 p-6">
              <h3 className="font-mono text-sm font-medium mb-4 text-gray-900">
                thread-per-request
              </h3>
              <ul className="space-y-2 font-mono text-xs text-gray-600">
                <li>• new thread per connection</li>
                <li>• unlimited thread creation</li>
                <li>• context switching overhead</li>
                <li>• no queue management</li>
              </ul>
            </div>

            <div className="border border-gray-200 p-6">
              <h3 className="font-mono text-sm font-medium mb-4 text-gray-900">
                thread-pool
              </h3>
              <ul className="space-y-2 font-mono text-xs text-gray-600">
                <li>• ThreadPoolExecutor</li>
                <li>• elastic: min=cores×2, max=cores×20</li>
                <li>
                  • rejection: CALLER_RUNS(when maxed, run on caller thread &gt;
                  backpressure)
                </li>
                <li>• 503 when overloaded</li>
              </ul>
            </div>
          </div>
        </div>
      </div>

      {/* Footer */}
      <div className="border-t border-gray-500">
        <div className="max-w-7xl mx-auto px-8 py-8 text-center font-mono text-xs">
          <p>
            built by{" "}
            <a
              href="https://github.com/voidCounter"
              className={"hover:underline"}
            >
              @voidCounter
            </a>
          </p>
        </div>
      </div>
    </div>
  );
}
