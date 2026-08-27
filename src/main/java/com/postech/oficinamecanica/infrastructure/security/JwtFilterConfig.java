package com.postech.oficinamecanica.infrastructure.security;

import com.postech.oficinamecanica.application.auth.TokenProvider;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Registra o {@link JwtAuthenticationFilter} apenas para as rotas administrativas
 * ("/api/*"). "/auth/login", Swagger UI e o OpenAPI docs continuam públicos.
 */
@Configuration
public class JwtFilterConfig {

    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtAuthenticationFilter(TokenProvider tokenProvider) {
        FilterRegistrationBean<JwtAuthenticationFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new JwtAuthenticationFilter(tokenProvider));
        registration.addUrlPatterns("/api/*");
        registration.setOrder(1);
        return registration;
    }
}
