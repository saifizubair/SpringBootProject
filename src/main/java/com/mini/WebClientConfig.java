package com.mini;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;


@Configuration
public class WebClientConfig {

    private static final int TIMEOUT_MS_API1 = 2000;
    private static final int TIMEOUT_MS_API2 = 1000;
    private static final int TIMEOUT_MS_API3 = 1000;

    private WebClient.Builder createWebClient(int timeout, String baseUrl) {
        HttpClient httpClient = HttpClient.create()
                .responseTimeout(java.time.Duration.ofMillis(timeout));

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .exchangeStrategies(ExchangeStrategies.builder()
                        .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                        .build())
                .baseUrl(baseUrl)
                .defaultHeader("Content-Type", "application/json")
                .defaultHeader("Accept", "application/json");
    }

    @Bean(name = "api1WebClient")
    public WebClient api1WebClient() {
        return createWebClient(TIMEOUT_MS_API1, "https://randomuser.me/api/").build();
    }

    @Bean(name = "api2WebClient")
    public WebClient api2WebClient() {
        return createWebClient(TIMEOUT_MS_API2, "https://api.nationalize.io/").build();
    }

    @Bean(name = "api3WebClient")
    public WebClient api3WebClient() {
        return createWebClient(TIMEOUT_MS_API3, "https://api.genderize.io/").build();
    }
}
