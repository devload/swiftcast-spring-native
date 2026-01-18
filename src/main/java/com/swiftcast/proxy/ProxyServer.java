package com.swiftcast.proxy;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ProxyServer {

    private boolean running = false;
    private int port = 8080;

    public void start(int port) {
        if (running) {
            throw new IllegalStateException("Proxy server is already running");
        }

        this.port = port;
        this.running = true;

        log.info("Proxy server started on port {}", port);
    }

    public void stop() {
        if (running) {
            running = false;
            log.info("Proxy server stopped");
        }
    }

    public boolean isRunning() {
        return running;
    }

    public int getPort() {
        return port;
    }
}
