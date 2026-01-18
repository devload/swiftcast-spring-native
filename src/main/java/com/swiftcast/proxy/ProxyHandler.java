package com.swiftcast.proxy;

import com.swiftcast.model.Account;
import com.swiftcast.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class ProxyHandler {

    private final AccountService accountService;
    private final WebClient.Builder webClientBuilder;

    public Mono<ServerResponse> handleProxy(ServerRequest request) {
        Optional<Account> accountOpt = accountService.getActiveAccount();

        if (accountOpt.isEmpty()) {
            log.warn("No active account configured");
            return ServerResponse.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .bodyValue("No active account configured");
        }

        Account account = accountOpt.get();
        String targetUrl = account.getBaseUrl() + request.path();

        log.debug("Proxying {} {} -> {}", request.method(), request.path(), targetUrl);

        WebClient client = webClientBuilder
                .baseUrl(account.getBaseUrl())
                .defaultHeader("x-api-key", account.getApiKey())
                .defaultHeader("anthropic-version", "2023-06-01")
                .build();

        return client.method(request.method())
                .uri(request.path())
                .headers(headers -> {
                    request.headers().asHttpHeaders().forEach((key, values) -> {
                        if (!key.equalsIgnoreCase("host") &&
                            !key.equalsIgnoreCase("x-api-key")) {
                            headers.addAll(key, values);
                        }
                    });
                })
                .body(request.bodyToMono(String.class), String.class)
                .exchangeToMono(clientResponse ->
                        ServerResponse.status(clientResponse.statusCode())
                                .headers(h -> h.addAll(clientResponse.headers().asHttpHeaders()))
                                .body(clientResponse.bodyToMono(String.class), String.class)
                )
                .onErrorResume(error -> {
                    log.error("Proxy error: {}", error.getMessage(), error);
                    return ServerResponse.status(HttpStatus.BAD_GATEWAY)
                            .bodyValue("Proxy error: " + error.getMessage());
                });
    }
}
