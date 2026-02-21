package com.practice.auth_service.auth_service.config;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Bean
    @LoadBalanced // 👈 THIS IS THE FIX. It tells Spring to look up 'USER-SERVICE' in Eureka.
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}