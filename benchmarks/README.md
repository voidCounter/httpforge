# HTTPForge Benchmarks

Automated performance testing for comparing different server strategies.

## Prerequisites

Install `hey`:
```bash
# With Go
go install github.com/rakyll/hey@latest

# Or download binary from releases
# https://github.com/rakyll/hey/releases
```

Verify installation:
```bash
hey -version
```

## Running Benchmarks

### Test All Servers
```bash
cd benchmarks
./run.sh
```

### Test Specific Servers
```bash
# Test only NIO server
./run.sh nio

# Test NIO and Thread Pool
./run.sh nio,pool

# Test multiple servers (comma-separated, no spaces)
./run.sh single,thread,pool,nio
```

**Available servers**: `single`, `thread`, `pool`, `nio`

This will:
1. Build the project (`mvn clean package`)
2. Test each specified server type
3. Run at different concurrency levels (1, 10, 50, 100, 200)
4. Generate 10,000 requests per test
5. Save results to `benchmark-results/`

## Reading Results

Each test creates a file like: `benchmark-results/nio_c100.txt`

Key metrics:
- **Requests/sec** - Throughput (higher is better)
- **Average** - Mean latency (lower is better)
- **p50/p95/p99** - Latency percentiles (lower is better)

Example output:
```
Testing: nio server
  Running: 10000 requests, 100 concurrent
    → RPS: 12543.32, Avg: 0.0079, p50: 0.0072, p95: 0.0145, p99: 0.0234
```

## Understanding the Metrics

### Throughput (RPS - Requests Per Second)
- How many requests the server can handle per second
- **Higher is better**
- Example: 12,543 RPS means ~12K requests/sec

### Latency Percentiles
- **p50 (median)**: 50% of requests complete in this time or less
- **p95**: 95% of requests complete in this time or less
- **p99**: 99% of requests complete in this time or less
- **Lower is better**
- Times are in seconds (0.0072 = 7.2ms)

### Why p99 Matters
If p99 = 0.05s (50ms), that means 1% of users experience 50ms+ latency.
For 10,000 requests, 100 users see slow responses.

## Comparing Server Strategies

Expected performance characteristics:

| Server Type | Throughput | Latency | Memory | Use Case |
|------------|------------|---------|---------|----------|
| Single     | Low        | Low     | Minimal | Learning/Simple |
| Thread     | Medium     | Medium  | High    | Traditional |
| Pool       | High       | Low     | Medium  | Production |
| NIO        | Very High  | Very Low| Low     | High Performance |

## Troubleshooting

### Port already in use
```bash
# Find and kill process on port 8080
lsof -ti:8080 | xargs kill -9
```

### hey not found
```bash
# Make sure Go bin is in PATH
export PATH=$PATH:$(go env GOPATH)/bin
```

### Server won't start
Check logs and ensure JAR is built:
```bash
mvn clean package
java -jar target/httpforge.jar nio
```

