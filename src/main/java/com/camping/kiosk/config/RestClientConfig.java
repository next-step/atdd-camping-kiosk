package com.camping.kiosk.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class RestClientConfig {

    private static final Logger log = LoggerFactory.getLogger(RestClientConfig.class);

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        ClientHttpRequestInterceptor requestIdInterceptor = (request, body, execution) -> {
            String requestId = MDC.get(RequestIdFilter.REQUEST_ID_HEADER);
            if (requestId != null) {
                request.getHeaders().add(RequestIdFilter.REQUEST_ID_HEADER, requestId);
            }
            return execution.execute(request, body);
        };

        ClientHttpRequestInterceptor loggingInterceptor = (request, body, execution) -> {
            long startMs = System.currentTimeMillis();
            try {
                var response = execution.execute(request, body);
                long took = System.currentTimeMillis() - startMs;
                log.info("[HTTP] {} {} -> {} ({}ms)", request.getMethod(), request.getURI(), response.getStatusCode(), took);
                return response;
            } catch (Exception e) {
                long took = System.currentTimeMillis() - startMs;
                log.error("[HTTP][ERROR] {} {} ({}ms): {}", request.getMethod(), request.getURI(), took, e.toString());
                throw e;
            }
        };

        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        interceptors.add(requestIdInterceptor);
        interceptors.add(loggingInterceptor);

        return builder
                .setConnectTimeout(Duration.ofSeconds(2))
                .setReadTimeout(Duration.ofSeconds(3))
                .additionalInterceptors(interceptors)
                .build();
    }
}


