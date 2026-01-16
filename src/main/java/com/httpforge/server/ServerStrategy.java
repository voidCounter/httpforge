package com.httpforge.server;

import java.io.IOException;

public interface ServerStrategy {
    void start() throws IOException;
    void stop();
    String getName();
}
