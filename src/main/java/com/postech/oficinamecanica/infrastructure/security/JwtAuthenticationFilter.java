package com.postech.oficinamecanica.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.postech.oficinamecanica.application.auth.TokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

/**
 * Protege as rotas administrativas ("/api/*") exigindo um JWT válido no header
 * Authorization. Registrado via {@link JwtFilterConfig} com FilterRegistrationBean
 * (não como @Component direto na classe) de propósito: assim ele não é
 * auto-detectado pelos testes @WebMvcTest já existentes de outros integrantes
 * (Customer/Material/Service/Vehicle), que continuam passando sem qualquer
 * alteração.
 */
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final TokenProvider tokenProvider;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public JwtAuthenticationFilter(TokenProvider tokenProvider) {
        this.tokenProvider = tokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String header = request.getHeader(AUTH_HEADER);

        if (header == null || !header.startsWith(BEARER_PREFIX)) {
            reject(response);
            return;
        }

        String token = header.substring(BEARER_PREFIX.length()).trim();

        if (token.isEmpty() || !tokenProvider.isValid(token)) {
            reject(response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void reject(HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Map.of("message", "Token inválido ou ausente")));
    }
}
