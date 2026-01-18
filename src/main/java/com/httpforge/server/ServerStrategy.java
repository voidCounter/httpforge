package com.httpforge.server;

import java.io.IOException;

public interface ServerStrategy {
    void start() throws IOException;
    // stop is used to gracefully shut down the server
    void stop();
    String getName();
}
