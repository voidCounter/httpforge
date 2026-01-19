#!/bin/bash

# HTTPForge Benchmark Script
# Compares performance of different server strategies
# Usage: ./run.sh [server1,server2,...]
#   Examples:
#     ./run.sh              # Test all servers
#     ./run.sh pool         # Test only Pool
#     ./run.sh single,thread # Test single and thread
#     ./run.sh single,thread,pool  # Test all (explicit)

set -e

if [ $# -eq 0 ]; then
    SERVERS=("single" "thread" "pool")
else
    IFS=',' read -ra SERVERS <<< "$1"

    VALID_SERVERS=("single" "thread" "pool")
    for server in "${SERVERS[@]}"; do
        if [[ ! " ${VALID_SERVERS[@]} " =~ " ${server} " ]]; then
            echo -e "${RED}✗ Invalid server name: '$server'${NC}"
            echo ""
            echo "Valid servers: single, thread, pool"
            echo ""
            echo "Usage:"
            echo "  ./run.sh              # Test all servers"
            echo "  ./run.sh pool         # Test only Pool"
            echo "  ./run.sh single,thread # Test single and thread"
            echo ""
            exit 1
        fi
    done
fi

CONCURRENCY_LEVELS=(1 10 50 100 200 1000)
REQUESTS=10000
PORT=8080
RESULTS_DIR="benchmark-results"

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

echo "=========================================="
echo "   HTTPForge Performance Benchmark"
echo "=========================================="
echo ""
echo -e "${BLUE}Testing servers: ${SERVERS[*]}${NC}"
echo ""

# Check if hey is installed
if ! command -v hey &> /dev/null; then
    echo -e "${RED}✗${NC} 'hey' is not installed!"
    echo ""
    echo "Install hey:"
    echo "  • With Go: go install github.com/rakyll/hey@latest"
    echo "  • Or download from: https://github.com/rakyll/hey/releases"
    echo ""
    exit 1
fi

echo -e "${GREEN}✓${NC} Using 'hey' for benchmarking"

# Create results directory
mkdir -p "$RESULTS_DIR"

# Clean up any existing server on port 8080
echo "Cleaning up port $PORT..."
lsof -ti:$PORT | xargs kill -9 2>/dev/null || true
sleep 1

start_server() {
    local server_type=$1
    echo -e "${YELLOW}Starting $server_type server...${NC}"

    # Start server and capture output to a log file for debugging
    local log_file="${RESULTS_DIR}/${server_type}_server.log"
    java -jar ../target/httpforge-1.0-SNAPSHOT.jar "$server_type" > "$log_file" 2>&1 &
    SERVER_PID=$!
    sleep 3

    # Check if server is running
    if ! kill -0 $SERVER_PID 2>/dev/null; then
        echo -e "${RED}✗ Failed to start $server_type server${NC}"
        echo "  Check log: $log_file"
        return 1
    fi

    # Test if server responds
    if ! curl -s --connect-timeout 2 http://localhost:$PORT/ > /dev/null 2>&1; then
        echo -e "${RED}✗ Server not responding${NC}"
        echo "  Check log: $log_file"
        kill $SERVER_PID 2>/dev/null || true
        return 1
    fi

    echo -e "${GREEN}✓ Server started (PID: $SERVER_PID)${NC}"
    return 0
}

stop_server() {
    if [ ! -z "$SERVER_PID" ]; then
        echo -e "${YELLOW}Stopping server (PID: $SERVER_PID)...${NC}"
        kill $SERVER_PID 2>/dev/null || true
        wait $SERVER_PID 2>/dev/null || true
        sleep 2

        # Force kill if still running
        if kill -0 $SERVER_PID 2>/dev/null; then
            kill -9 $SERVER_PID 2>/dev/null || true
        fi
    fi

    # Also clean up any stragglers on the port
    lsof -ti:$PORT | xargs kill -9 2>/dev/null || true
}

run_benchmark() {
    local server_type=$1
    local concurrency=$2
    local output_file="${RESULTS_DIR}/${server_type}_c${concurrency}.txt"

    echo "  Running: $REQUESTS requests, $concurrency concurrent"
    hey -n $REQUESTS -c $concurrency http://localhost:$PORT/hello > "$output_file" 2>&1

    # Extract key metrics
    local rps=$(grep "Requests/sec:" "$output_file" | awk '{print $2}')
    local avg=$(grep "Average:" "$output_file" | awk '{print $2}')
    local p50=$(grep "50%" "$output_file" | awk '{print $2}')
    local p95=$(grep "95%" "$output_file" | awk '{print $2}')
    local p99=$(grep "99%" "$output_file" | awk '{print $2}')

    echo "    → RPS: $rps, Avg: $avg, p50: $p50, p95: $p95, p99: $p99"
}


# Build the project first
echo ""
echo "Building project..."
cd ..
mvn clean package -q
cd benchmarks
echo -e "${GREEN}✓ Build complete${NC}"
echo ""

# Run benchmarks for each server type
for server in "${SERVERS[@]}"; do
    echo ""
    echo "=========================================="
    echo "  Testing: $server server"
    echo "=========================================="

    start_server "$server" || continue

    for concurrency in "${CONCURRENCY_LEVELS[@]}"; do
        run_benchmark "$server" "$concurrency"
    done

    stop_server
    echo ""
done

echo ""
echo "=========================================="
echo "  Benchmark Complete!"
echo "=========================================="

# Final cleanup
lsof -ti:$PORT | xargs kill -9 2>/dev/null || true

echo "Results saved to: $RESULTS_DIR/"
echo ""
echo "To view results:"
echo "  • Raw data: ls $RESULTS_DIR/"
echo "  • Summary: cat $RESULTS_DIR/*.txt | grep 'Requests/sec'"
echo ""

