package org.example.orderservice.config;

import feign.RequestInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import jakarta.servlet.http.HttpServletRequest;

@Configuration
@RequiredArgsConstructor
public class FeignConfig {

    @Value("${user.service.api-key:internal-auth-key-12345}")
    private String userServiceApiKey;

    @Bean
    public RequestInterceptor requestInterceptor() {
        return template -> {

            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String authorizationHeader = request.getHeader("Authorization");

                if (authorizationHeader != null && !authorizationHeader.isEmpty()) {
                    template.header("Authorization", authorizationHeader);
                }
            }

            if (userServiceApiKey != null && !userServiceApiKey.isEmpty()) {
                template.header("X-API-Key", userServiceApiKey);
            }
        };
    }
}