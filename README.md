# HTTPForge

A lightweight HTTP server implementation in pure Java, built from scratch with zero external dependencies. This project explores different server concurrency models and their performance characteristics.

## What is this?

HTTPForge is a learning project that implements a working HTTP/1.1 server using only the Java standard library. It includes three different concurrency strategies so you can compare how they handle load and what trade-offs each approach makes.

If you've ever wondered how web servers like Tomcat or Jetty work under the hood, this is a good place to start.

## Features

**Three Server Strategies:**
- Single-Threaded - handles one request at a time (baseline)
- Thread-per-Request - spawns a new thread for each connection
- Thread Pool - uses a fixed pool of worker threads with a request queue
- planned: NIO-based server for async handling

**HTTP/1.1 Support:**
- GET and POST methods
- Header parsing and handling
- Keep-alive connections
- Request body parsing

**Routing System:**
- Simple path-based routing
- Lambda-based request handlers
- Method-specific routes (GET, POST, etc.)

**Performance Metrics:**
- Request count and active connections
- Latency tracking (min, max, avg, percentiles)
- Real-time metrics endpoint at `/metrics`

**Built-in Benchmarking:**
- Uses `hey` benchmarking tool
- Automated performance tests
- Comparative analysis across strategies

## Quick Start

### Prerequisites
- Java 11 or higher
- Maven 3.6+
- `hey` (optional, for benchmarking)

### Build and Run

```bash
# Clone the repository
git clone <your-repo-url>
cd httpforge

# Build the project
mvn clean package

# Run with thread pool server (recommended)
java -jar target/httpforge-1.0-SNAPSHOT.jar pool

# Or try other strategies
java -jar target/httpforge-1.0-SNAPSHOT.jar single
java -jar target/httpforge-1.0-SNAPSHOT.jar thread
```

The server will start on `http://localhost:8080`

## API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/` | GET | Welcome message |
| `/hello` | GET | Hello world with simulated 20ms delay |
| `/echo` | GET | Returns request information |
| `/data` | POST | Echoes back the POST body |
| `/metrics` | GET | Server performance metrics (JSON) |

### Examples

```bash
# Test the welcome endpoint
curl http://localhost:8080/

# Test the hello endpoint
curl http://localhost:8080/hello

# Test echo endpoint
curl http://localhost:8080/echo

# Send POST data
curl -X POST http://localhost:8080/data -d "Hello from client"

# Check server metrics
curl http://localhost:8080/metrics
```

## Benchmarking

Compare the performance of different server strategies:

```bash
cd benchmarks

# Run benchmarks for all servers
./run.sh

# Test specific server(s)
./run.sh pool
./run.sh single,thread,pool

# View results
cat benchmark-results/pool_c100.txt
```

See [benchmarks/README.md](benchmarks/README.md) for detailed benchmarking guide.

## Architecture

```
HTTPForge
├── http/          - HTTP protocol implementation
│   ├── HttpParser.java
│   ├── HttpRequest.java
│   └── HttpResponse.java
├── routing/       - Request routing system
│   ├── Router.java
│   └── Routes.java
├── server/        - Server implementations
│   ├── ServerStrategy.java (interface)
│   ├── SingleThreadServer.java
│   ├── ThreadPerRequestServer.java
│   └── ThreadPoolServer.java
├── metrics/       - Performance tracking
│   └── Metrics.java
└── Main.java      - Application entry point
```

## Learning Outcomes

By exploring this project, you'll understand:

- How HTTP protocol parsing works
- Different concurrency models and their trade-offs
- Thread management and pooling strategies
- Performance characteristics under load
- How to implement a basic web server from scratch

## Development

```bash
# Run tests
mvn test

# Compile without running
mvn compile

# Clean build artifacts
mvn clean
```

## Performance Comparison

| Strategy | Best For | Throughput | Memory Usage |
|----------|----------|------------|--------------|
| Single-Threaded | Learning, simple apps | Low | Minimal |
| Thread-per-Request | Moderate traffic | Medium | High |
| Thread Pool | Production | High | Moderate |

## Contributing

This is an educational project. Feel free to:
- Experiment with the code
- Add new features
- Implement additional server strategies
- Improve performance

## License

This project is open source and available for educational purposes.

---

Built to understand web server internals and concurrency patterns in Java.

