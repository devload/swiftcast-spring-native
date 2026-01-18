package com.swiftcast.config;

import com.swiftcast.proxy.ProxyHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.embedded.netty.NettyReactiveWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Configuration
@RequiredArgsConstructor
public class ProxyConfig {

    private final ProxyHandler proxyHandler;

    @Bean
    public RouterFunction<ServerResponse> proxyRoutes() {
        return RouterFunctions.route()
                .GET("/**", proxyHandler::handleProxy)
                .POST("/**", proxyHandler::handleProxy)
                .PUT("/**", proxyHandler::handleProxy)
                .DELETE("/**", proxyHandler::handleProxy)
                .PATCH("/**", proxyHandler::handleProxy)
                .build();
    }

    @Bean
    public WebServerFactoryCustomizer<NettyReactiveWebServerFactory> webServerFactoryCustomizer() {
        return factory -> factory.setPort(8080);
    }
}
