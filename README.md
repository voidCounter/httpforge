# HTTPForge

A simple HTTP/1.1 server implementation in pure Java with no external dependencies. Includes three different concurrency models to compare performance characteristics.

## Features

- **Three server strategies**: Single-threaded, thread-per-request, and thread pool
- **HTTP/1.1 basics**: GET/POST methods, headers, keep-alive, request bodies
- **Simple routing**: Path-based routing with lambda handlers
- **Metrics**: Request counts, latency tracking, `/metrics` endpoint
- **Benchmarking**: Built-in performance testing with `hey`

## Quick Start

```bash
# Build
mvn clean package

# Run (choose: single, thread, or pool)
java -jar target/httpforge-1.0-SNAPSHOT.jar pool
```

Server runs on `http://localhost:8080`

## Endpoints

- `GET /` - Welcome message
- `GET /hello` - Hello world (20ms simulated delay)
- `GET /echo` - Request info
- `POST /data` - Echo POST body
- `GET /metrics` - Performance metrics (JSON)

## Benchmarking

Install `hey` first:
```bash
# macOS
brew install hey

# Linux
go install github.com/rakyll/hey@latest

# Or download from: https://github.com/rakyll/hey
```
Some benchmarks: 

<img width="1216" height="565" alt="image" src="https://github.com/user-attachments/assets/2728c22e-0ce9-4989-8d43-fc6bcc258f4c" />
<img width="1237" height="482" alt="image" src="https://github.com/user-attachments/assets/88c06278-0c73-405a-9205-05674b236b2b" />
<img width="1216" height="495" alt="image" src="https://github.com/user-attachments/assets/2936c562-d014-4d72-aa1b-0e006800571a" />
<img width="1216" height="478" alt="image" src="https://github.com/user-attachments/assets/b4e5e097-2193-421a-b4d9-4b2acd243efc" />

See more: https://httpforge.vercel.app/


Run benchmarks:
```bash
cd benchmarks
./run.sh pool  # or: single, thread, or all
cat benchmark-results/pool_c100.txt  # view results
```

## Project Structure

```
http/       - HTTP protocol (parser, request, response)
routing/    - Router and route definitions
server/     - Server implementations (single, thread-per-request, pool)
metrics/    - Performance tracking
```

## Requirements

- Java 11+
- Maven 3.6+
- `hey` (optional, for benchmarks)

## Performance Notes

- **Single-threaded**: Minimal resources, handles one request at a time
- **Thread-per-request**: New thread per connection, higher memory usage
- **Thread pool**: Best for production, fixed threads with request queue
