package com.swiftcast.proxy;

import com.swiftcast.model.Account;
import com.swiftcast.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProxyServer {

    private final AccountService accountService;
    private final WebClient.Builder webClientBuilder;

    private DisposableServer server;
    private int port;

    public void start(int port) {
        if (server != null && !server.isDisposed()) {
            throw new IllegalStateException("Proxy server is already running");
        }

        this.port = port;

        server = HttpServer.create()
                .port(port)
                .route(routes -> routes
                        .post("/**", this::handleProxy)
                        .get("/**", this::handleProxy)
                        .put("/**", this::handleProxy)
                        .delete("/**", this::handleProxy)
                )
                .bindNow();

        log.info("Proxy server started on port {}", port);
    }

    public void stop() {
        if (server != null && !server.isDisposed()) {
            server.disposeNow();
            log.info("Proxy server stopped");
        }
    }

    public boolean isRunning() {
        return server != null && !server.isDisposed();
    }

    public int getPort() {
        return port;
    }

    private Mono<Void> handleProxy(
            org.springframework.http.server.reactive.ServerHttpRequest request,
            org.springframework.http.server.reactive.ServerHttpResponse response
    ) {
        Optional<Account> accountOpt = accountService.getActiveAccount();

        if (accountOpt.isEmpty()) {
            response.setStatusCode(org.springframework.http.HttpStatus.SERVICE_UNAVAILABLE);
            return response.writeWith(Mono.just(
                    response.bufferFactory().wrap("No active account configured".getBytes())
            ));
        }

        Account account = accountOpt.get();
        String targetUrl = account.getBaseUrl() + request.getPath().value();

        log.debug("Proxying {} {} -> {}", request.getMethod(), request.getPath(), targetUrl);

        WebClient client = webClientBuilder
                .baseUrl(account.getBaseUrl())
                .defaultHeader("x-api-key", account.getApiKey())
                .defaultHeader("anthropic-version", "2023-06-01")
                .build();

        return client.method(request.getMethod())
                .uri(request.getPath().value())
                .headers(headers -> {
                    request.getHeaders().forEach((key, values) -> {
                        if (!key.equalsIgnoreCase("host") &&
                            !key.equalsIgnoreCase("x-api-key")) {
                            headers.addAll(key, values);
                        }
                    });
                })
                .body(request.getBody(), org.springframework.core.io.buffer.DataBuffer.class)
                .exchangeToMono(clientResponse -> {
                    response.setStatusCode(clientResponse.statusCode());
                    response.getHeaders().putAll(clientResponse.headers().asHttpHeaders());

                    return response.writeWith(clientResponse.bodyToFlux(org.springframework.core.io.buffer.DataBuffer.class));
                })
                .onErrorResume(error -> {
                    log.error("Proxy error: {}", error.getMessage());
                    response.setStatusCode(org.springframework.http.HttpStatus.BAD_GATEWAY);
                    return response.writeWith(Mono.just(
                            response.bufferFactory().wrap(("Proxy error: " + error.getMessage()).getBytes())
                    ));
                });
    }
}
